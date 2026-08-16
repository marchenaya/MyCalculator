# Edge case analysis — `DefaultExpressionEvaluator`

Scope: `app/src/main/java/com/plcoding/mycalculator/calculator/domain/DefaultExpressionEvaluator.kt`
against `app/src/test/java/com/plcoding/mycalculator/calculator/domain/DefaultExpressionEvaluatorTest.kt`.

Only functional defects are listed — inputs that make the evaluator produce a **wrong number**,
**crash**, or otherwise break the contract in `ExpressionEvaluator.kt`. Style, naming and
"would be nice to also assert" observations are deliberately excluded.

## Method

1. Mapped the attack surface: one entry point, no collaborators, so every break vector travels
   through the expression string or through `Double` semantics on the way out.
2. Walked every branch of the parser and confirmed the existing suite already reaches all of
   them — the gaps are semantic, not structural.
3. Verified each candidate by running it against the real evaluator (throwaway probe test,
   since removed).
4. Ran two fuzz passes to catch anything reasoning missed (results in
   [What was checked and is genuinely fine](#what-was-checked-and-is-genuinely-fine)).

## Summary

| # | Bug | Severity | Reachable from the keypad |
|---|-----|----------|---------------------------|
| 1 | A division by zero in a divisor is swallowed and yields a finite, wrong result | High | **Yes** — verified end-to-end through `CalculatorViewModel` |
| 2 | Unbounded recursion — `StackOverflowError` instead of a `Double?` | Medium | No (today), but the evaluator is a public API over arbitrary `String` |

---

## Bug 1 — a division by zero inside a divisor produces a finite, wrong result

**Severity: High. User-visible today.**

### What happens

`parse()` checks finiteness once, on the final value:

```kotlin
return value.takeIf { index == expression.length && it.isFinite() }
```

`±Infinity` is therefore a perfectly legal *intermediate*. And `finite ÷ ±Infinity` is `±0.0`,
which is finite — so the guard is defeated exactly when the undefined sub-result is consumed by
a division. The mathematically undefined term silently collapses to zero and the surrounding
expression returns an answer that looks completely ordinary.

| Expression | Actual | Contract says |
|---|---|---|
| `1÷(1÷0)` | `0.0` | `null` |
| `5÷(5÷0)` | `0.0` | `null` |
| `1÷(9÷(3−3))` | `0.0` | `null` |
| `(2+3)÷(1÷(4−4))` | `0.0` | `null` |
| `2÷(1÷0)+3` | `3.0` | `null` |
| `5+1÷(1÷0)` | `5.0` | `null` |
| `1÷(1÷0)−1` | `-1.0` | `null` |
| `1÷(1÷0)×5` | `0.0` | `null` |
| `−1÷(1÷0)` | `-0.0` | `null` |
| `1÷−(1÷0)` | `-0.0` | `null` |

The same hole swallows an **overflowed operand** used as a divisor, for the same reason:

| Expression | Actual | Contract says |
|---|---|---|
| `1÷` + `"9".repeat(400)` | `0.0` | `null` (the operand is `Infinity`) |

### Reachability

Fully reachable through the UI — `1÷(1÷0)` uses only keypad-producible characters and every
`ExpressionRules` guard permits it (the group is non-empty and ends on a digit, so
`canCloseGroup()` is true). Driving `CalculatorViewModel` with
`1`, `÷`, `(`, `1`, `÷`, `0`, `)` produces:

```
expression = 1÷(1÷0)
result     = 0
```

The user is shown **`0`** as the answer to a division by zero. Worse, `OnCalculateClick` commits
that `0` into the expression, so the wrong value becomes the base of every subsequent calculation.

### Why the current suite does not catch it

`NonFiniteResults` only exercises non-finiteness that *survives to the top*: `5÷0`, `0÷0`,
`5÷(3−3)`, `5÷(0×−1)`, and the 400-digit operand standing alone. The one test shaped like a
recovery — `an intermediate overflow is never recovered from` (`1e300×1e100÷1e100`) — picks the
case where recovery fails, because there the infinity sits in the **dividend**
(`Infinity ÷ finite == Infinity`), so it still ends up `null`.

The family also *looks* covered because every other way of consuming a non-finite intermediate
does produce `NaN` and therefore `null`:

```
0×(1÷0)      -> null   (Infinity × 0   == NaN)
(1÷0)−(1÷0)  -> null   (Infinity − Infinity == NaN)
(1÷0)÷(1÷0)  -> null   (Infinity ÷ Infinity == NaN)
```

Division **into** a non-finite value is the single escaping case, and no test covers it.

### Tests to add

```kotlin
@Test
fun `a division by zero inside a divisor has no result`() {
    assertThat(evaluator.evaluate("1÷(1÷0)")).isNull()
}

@Test
fun `a division by zero inside a divisor does not vanish from the surrounding sum`() {
    assertThat(evaluator.evaluate("2÷(1÷0)+3")).isNull()
}

@Test
fun `an operand too large for a Double has no result when it is a divisor`() {
    assertThat(evaluator.evaluate("1÷" + "9".repeat(400))).isNull()
}
```

### Fix direction

Check finiteness where the non-finite value is *created*, not only at the end — reject in
`applyTo` (or right after each `applyTo` / each operand parse) as soon as a step yields a
non-finite value, instead of relying on the top-level `isFinite()`.

---

## Bug 2 — unbounded recursion throws `StackOverflowError` instead of returning `Double?`

**Severity: Medium. Latent — not reachable from the keypad today.**

### What happens

`parseOperand` recurses once per `(` and once per unary `−`. Nothing bounds the depth, so past a
stack-size-dependent limit the call throws `StackOverflowError` — an `Error`, not a `null`.
Measured on the JVM test runner:

| Input | Result |
|---|---|
| `"(".repeat(3_000) + "1+1" + ")".repeat(3_000)` | `2.0` |
| `"(".repeat(3_500) + "1+1" + ")".repeat(3_500)` | **throws `StackOverflowError`** |
| `"(".repeat(2_000) + "1"` (unclosed, malformed) | `null` |
| `"(".repeat(5_000) + "1"` (unclosed, malformed) | **throws `StackOverflowError`** |
| `"−".repeat(10_000) + "5"` | `5.0` |
| `"−".repeat(15_000) + "5"` | **throws `StackOverflowError`** |

Note the third and fourth rows: a *malformed* expression crashes before it can reach the `null`
path, so the failure is not limited to inputs that would have been expensive to evaluate anyway.

The `fun interface ExpressionEvaluator` contract promises a `Double?` and its KDoc names exactly
three reasons for `null` — none of them is "throws". `CalculatorViewModel.previewResult()` calls
`evaluate` synchronously from `updateExpression`, i.e. on every keystroke and on state
restoration, with no `try`/`catch` anywhere in the chain.

### Reachability

Not reachable from the keypad as the app stands: `MAX_EXPRESSION_LENGTH = 40` caps `openGroup()`
at ~40 nested parentheses, and `enterOperator` collapses stacked operators
(`dropLastWhile { isOperator(it) }`), so a chain of unary minuses cannot be typed.

It is still a real bug, for three reasons:

- `evaluate(expression: String)` is a public domain API that accepts any `String`; the depth
  limit is undocumented and unenforced.
- The suite itself already feeds the evaluator non-keypad input, explicitly reasoning about
  "a paste, a deep link, a state written by another version" (see the
  `implicit multiplication against a group is not parsed` test).
- The expression restored from `SavedStateHandle` on process death is not re-validated before
  being handed to the evaluator.

The threshold is stack-size dependent and therefore device- and thread-dependent, so a test
pinning an exact depth would be flaky. The bug is the *absence of any bound*, and that is what a
test should target.

### Why the current suite does not catch it

`ScaleAndPurity` pins 200 nested groups, 100 unary minuses and a 2,000-term chain. The first two
sit an order of magnitude below the limit; the third exercises `parseSum`'s **iterative** `while`
loop, which is not the path at risk. The suite therefore demonstrates that scale is handled
without ever approaching the point where it is not.

### Tests to add

Assert the *contract* rather than a magic depth:

```kotlin
@ParameterizedTest
@ValueSource(ints = [1_000, 50_000])
fun `a deeply nested expression returns a result or null but never throws`(depth: Int) {
    assertThat(evaluator.evaluate("(".repeat(depth) + "1+1" + ")".repeat(depth))).isEqualTo(2.0)
}

@Test
fun `a long chain of unary minuses never throws`() {
    assertThat(evaluator.evaluate("−".repeat(50_000) + "5")).isEqualTo(5.0)
}

@Test
fun `an unclosed group nested far beyond the stack has no result`() {
    assertThat(evaluator.evaluate("(".repeat(50_000) + "1")).isNull()
}
```

### Fix direction

Either carry an explicit depth counter through `Parser` and return `null` past a documented
maximum, or make the group and unary-minus paths iterative so depth costs heap, not stack. A
documented depth cap is the smaller change and matches the existing "return `null` when it cannot
be turned into a number" contract.

---

## What was checked and is genuinely fine

Recorded so the next pass does not re-tread it.

**Parsing semantics — verified by differential fuzzing.** 200,000 randomly generated ASTs
(4 levels deep, all four operators, unary negation, integer and fractional literals) were
rendered flat using standard precedence and left-associativity rules, then parsed. **Zero
mismatches.** Precedence, left-associativity, grouping, unary-minus placement and the
`parseOperand`/`parseProduct`/`parseSum` layering are correct.

**Robustness on malformed input — verified by fuzzing.** 500,000 random strings over the full
alphabet (`0-9 . ( ) + − × ÷`, lengths 0–29): no exception, no hang (176 ms total), and every
non-null result was finite. No input drives the parser into an infinite loop — every path either
consumes at least one character or returns `null`.

**Branch coverage is complete.** Every branch of `parse`, `parseSum`, `parseProduct`,
`parseOperand`, `readOperator` and all four `applyTo` arms is reached by an existing test,
including the empty-operand, unclosed-group, leftover-input and missing-`)` paths.

**Already well covered by the existing suite**, and confirmed correct on re-check: non-ASCII
digits surviving `Char.isDigit()` but rejected by `toDoubleOrNull`; locale independence;
whitespace, control, zero-width and bidi characters; emoji and surrogate pairs (no half of a pair
is ever read as part of a number); ASCII operator look-alikes; alternative numeric notations
(`1e5`, `0x10`, `NaN`, `1,5`); leading and trailing decimal separators and double separators;
`−0.0` staying distinguishable from `0.0`; `Double` precision limits; statelessness across calls.

**Not bugs, though they look like candidates:**

- `DefaultExpressionEvaluator` is stateless (a fresh `Parser` per call), so concurrent use is
  safe; the "leaves nothing behind" test covers what there is to cover.
- Underflow to `0.0` for an operand below the smallest subnormal is already pinned by
  `an operand too small for a Double collapses to zero`.
- Accumulated floating-point error over a long chain (`0.1` added ten times gives
  `0.9999999999999999`) is the same root cause the `DoublePrecisionLimits` tests deliberately
  pin, and `CalculatorViewModel.format()` rounds it away at 8 decimals.
- `readOperator`'s `entries.first { … }` cannot throw: it is only called after `isAdditive()` or
  `isMultiplicative()` has matched one of the four symbols.

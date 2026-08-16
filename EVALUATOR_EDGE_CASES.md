# `DefaultExpressionEvaluator` — Edge Case Analysis

An analysis of which edge cases the current expression evaluator handles and which it does
not yet consider for a fully functioning calculator.

**Method:** the evaluator source was compiled into a standalone harness and run against ~45
probe expressions, together with the `format()` path from `CalculatorViewModel`. Everything
below is observed behaviour, not inferred.

---

## What it already handles correctly

Precedence, left-associativity, nested groups, unary minus (including `−−5` → `5` and
`5×−3` → `−15`, which the keypad cannot even produce), `.5` / `5.` operands, `1.2.3` → `null`,
stray characters and whitespace → `null`, trailing `)` → `null`, and every division-by-zero
variant probed (`1÷0`, `0÷0`, `1÷0×0`, `1÷0−1÷0`, `5÷(3−3)`) → `null`.

`DefaultExpressionEvaluatorTest` covers this ground well.

---

## Edge cases it does not consider

### 1. Double precision past 2^53 — wrong digits presented as exact

The most serious one, and reachable from the keypad.

`999999999999×999999999999` uses two 12-digit operands, both allowed by `MAX_NUMBER_LENGTH`.
The evaluator returns `9.99999999998E23`, which `format()` prints as:

```
displayed: 999999999998000000000000
exact:     999999999998000000000001
```

24 digits shown, all asserted as significant, the last one wrong. `Double` carries ~15–16
significant digits. Nothing in the evaluator caps significant digits or falls back to
`BigDecimal`, and `format()` (`CalculatorViewModel.kt:151`) uses `setScale(8)`, which only
bounds the *fractional* part — never the integer part. A real calculator either switches to
scientific notation past its digit budget or computes in decimal.

### 2. Committing the result destroys precision and bypasses the input limits

`calculate()` (`CalculatorViewModel.kt:128`) writes the *formatted* string back as the new
expression:

```
1÷3 =   ->  0.33333333
×3  =   ->  0.99999999      (expected: 1)
```

Hardware calculators keep full internal precision behind a truncated display. The same path
also bypasses `MAX_NUMBER_LENGTH` (12) and `MAX_EXPRESSION_LENGTH` (40) — `updateExpression`
enforces neither — so a committed 24-character result already exceeds both.

### 3. Results below the rounding threshold silently become `0`

The evaluator correctly returns `1.0E-9` for `1÷1000000000` and `-9.99999999999999E-10` for
`0.00000001−0.000000011`. Both display as `0`, the second one losing its sign as well.
Anything under `1e-8` reads as exactly zero.

### 4. `null` conflates three different meanings

`evaluate` returns `null` for *incomplete* (`5+`, `(5+3`), *empty*, and *mathematically
undefined* (`5÷0`). The UI cannot tell them apart, so dividing by zero shows a blank preview
identical to a half-typed expression. No "Cannot divide by zero" state is possible without
changing the return type to something like `Result<Double, CalculationError>`.

### 5. An undocumented contract between `isFinite()` and `format()`

`DefaultExpressionEvaluator.kt:28` is the only thing standing between the ViewModel and a
crash: `BigDecimal.valueOf(Double.POSITIVE_INFINITY)` and `valueOf(Double.NaN)` both throw
`NumberFormatException` (verified). The `isFinite` check reads as a UX decision in its
comment, but it is load-bearing for stability, and the KDoc on `ExpressionEvaluator` does not
state it.

### 6. Grammar gaps for a "full" calculator

The alphabet is fixed at digits, `.`, the four binary operators, and parentheses. Not
expressible at all: percent, sign toggle (±) on an existing operand, exponent/root,
scientific-notation entry, constants.

Implicit multiplication is also *not* parsed — `5(3)` and `(1+2)(3)` both return `null`. It
only works because the keypad materialises the `×` beforehand. That makes this a
keypad-specific parser rather than a general evaluator, which means the expression format is
not safe to feed from anywhere else (clipboard paste, deep link, a restored
`SavedStateHandle` written by another version).

### 7. No auto-close of open groups

`2×(3+4` yields no preview and a disabled equals key. Most calculators implicitly close
trailing parentheses on equals.

### 8. Repeated equals does nothing

`5+3 =` gives `8`; pressing `=` again is a no-op, because `previewResult()`
(`CalculatorViewModel.kt:144`) requires an operator in the expression. Conventional behaviour
is to repeat the last operation (`11`). Worth deciding deliberately rather than inheriting.

### 9. Recursion depth (robustness only)

Nesting overflows the stack at ~5000 `(`, unary minus at ~20000 `−`. Not reachable today —
`openGroup` / `enterOperator` cap the expression at 40 characters — but the parser itself has
no depth guard, so this becomes live the moment anything other than the keypad supplies an
expression (see #6).

---

## What to fix first

#1 and #2 are the only ones that produce *wrong numbers* rather than missing features. Both
point at the same root cause: `Double` as the calculation and hand-off type.

Moving the evaluator to `BigDecimal` with a `MathContext`, and keeping the unrounded value for
the equals hand-off, resolves both — and #3 largely falls out of it too.

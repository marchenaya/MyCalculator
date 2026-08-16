package com.plcoding.mycalculator.calculator.presentation

import com.plcoding.mycalculator.calculator.domain.CalculatorOperator

internal const val DECIMAL_SEPARATOR = '.'
internal const val LEFT_PARENTHESIS = '('
internal const val RIGHT_PARENTHESIS = ')'

/**
 * The digits and separator of the number that is currently being typed, i.e. everything
 * after the last operator or parenthesis. Empty when no number is being typed right now.
 */
internal fun String.currentNumber(): String =
    takeLastWhile { it.isDigit() || it == DECIMAL_SEPARATOR }

/**
 * Whether the expression ends on something that is already a finished value — a digit or a
 * closed group. Both an operator and a closing parenthesis may follow such an expression.
 */
internal fun String.endsOnCompleteOperand(): Boolean =
    lastOrNull()?.let { it.isDigit() || it == RIGHT_PARENTHESIS } == true

/** How many groups are still waiting to be closed. */
internal fun String.unclosedGroupCount(): Int =
    count { it == LEFT_PARENTHESIS } - count { it == RIGHT_PARENTHESIS }

/**
 * A group can only be closed when one is actually open and it already holds a complete
 * operand, which is what keeps empty groups (`()`) and dangling operators (`(5+)`) out of
 * the expression.
 */
internal fun String.canCloseGroup(): Boolean {
    val expression = trimEnd(DECIMAL_SEPARATOR)
    return expression.unclosedGroupCount() > 0 && expression.endsOnCompleteOperand()
}

/**
 * Writing a value directly against a closed group multiplies the two, so the multiplication
 * is materialised in the expression itself: typing `3` after `(1+2)` gives `(1+2)×3`.
 */
internal fun String.withImplicitMultiplication(): String =
    if (lastOrNull() == RIGHT_PARENTHESIS) this + CalculatorOperator.MULTIPLY.symbol else this

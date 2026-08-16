package com.plcoding.mycalculator.calculator.presentation

/**
 * @param expression the raw expression the user has entered so far, exactly as it is rendered.
 * @param result the live preview of the evaluated [expression], or `null` while the expression
 * cannot be evaluated (empty, still incomplete, or not an actual calculation yet).
 */
data class CalculatorState(val expression: String = "", val result: String? = null) {
    val canDelete: Boolean
        get() = expression.isNotEmpty()

    val canCalculate: Boolean
        get() = result != null

    val canCloseParenthesis: Boolean
        get() = expression.canCloseGroup()
}

package com.plcoding.mycalculator.calculator.domain

/**
 * The binary operators a calculator expression can contain.
 *
 * The [symbol] is what gets appended to the expression string and what is rendered
 * on screen — the expression the user sees and the expression the [ExpressionEvaluator]
 * receives are always the exact same string.
 */
enum class CalculatorOperator(val symbol: Char) {
    ADD('+'),
    SUBTRACT('−'),
    MULTIPLY('×'),
    DIVIDE('÷'),
    ;

    companion object {
        private val symbols = entries.map { it.symbol }.toSet()

        fun isOperator(char: Char): Boolean = char in symbols
    }
}

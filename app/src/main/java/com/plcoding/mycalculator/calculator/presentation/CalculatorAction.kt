package com.plcoding.mycalculator.calculator.presentation

import com.plcoding.mycalculator.calculator.domain.CalculatorOperator

sealed interface CalculatorAction {
    data class OnDigitClick(val digit: Int) : CalculatorAction
    data class OnOperatorClick(val operator: CalculatorOperator) : CalculatorAction
    data object OnDecimalClick : CalculatorAction
    data object OnLeftParenthesisClick : CalculatorAction
    data object OnRightParenthesisClick : CalculatorAction
    data object OnDeleteClick : CalculatorAction
    data object OnClearClick : CalculatorAction
    data object OnCalculateClick : CalculatorAction
}

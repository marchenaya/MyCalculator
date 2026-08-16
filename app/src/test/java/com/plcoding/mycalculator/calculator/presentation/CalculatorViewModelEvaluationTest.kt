package com.plcoding.mycalculator.calculator.presentation

import androidx.lifecycle.SavedStateHandle
import com.plcoding.mycalculator.calculator.domain.CalculatorOperator
import com.plcoding.mycalculator.calculator.domain.DefaultExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * The ViewModel driving the real evaluator: the live preview, how a result is formatted, and
 * what happens when the user keeps calculating with a committed result.
 */
class CalculatorViewModelEvaluationTest {

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        viewModel = CalculatorViewModel(
            savedStateHandle = SavedStateHandle(),
            expressionEvaluator = DefaultExpressionEvaluator()
        )
    }

    private fun digits(vararg digits: Int) =
        digits.forEach { viewModel.onAction(CalculatorAction.OnDigitClick(it)) }

    private fun operator(operator: CalculatorOperator) =
        viewModel.onAction(CalculatorAction.OnOperatorClick(operator))

    private val expression get() = viewModel.state.value.expression
    private val result get() = viewModel.state.value.result

    @Test
    fun `a whole result is previewed without decimals`() {
        digits(1)
        operator(CalculatorOperator.ADD)
        digits(2)
        assertEquals("3", result)
        assertTrue(viewModel.state.value.canCalculate)
    }

    @Test
    fun `a fractional result keeps its decimals without trailing zeros`() {
        digits(1)
        operator(CalculatorOperator.DIVIDE)
        digits(4)
        assertEquals("0.25", result)
    }

    @Test
    fun `a repeating result is rounded to eight decimals`() {
        digits(1)
        operator(CalculatorOperator.DIVIDE)
        digits(3)
        assertEquals("0.33333333", result)
    }

    @Test
    fun `a negative result is previewed with the calculator minus`() {
        digits(3)
        operator(CalculatorOperator.SUBTRACT)
        digits(5)
        assertEquals("${CalculatorOperator.SUBTRACT.symbol}2", result)
    }

    @Test
    fun `a lone number is not previewed as a result`() {
        digits(4, 2)
        assertNull(result)
    }

    @Test
    fun `an incomplete expression is not previewed as a result`() {
        digits(4)
        operator(CalculatorOperator.ADD)
        assertNull(result)
    }

    @Test
    fun `dividing by zero is not previewed as a result`() {
        digits(4)
        operator(CalculatorOperator.DIVIDE)
        digits(0)
        assertNull(result)
    }

    @Test
    fun `pressing equals commits the result as the new expression`() {
        digits(1, 2)
        operator(CalculatorOperator.MULTIPLY)
        digits(2)
        viewModel.onAction(CalculatorAction.OnCalculateClick)
        assertEquals("24", expression)
        assertNull(result)
    }

    @Test
    fun `a committed negative result can be calculated with further`() {
        digits(3)
        operator(CalculatorOperator.SUBTRACT)
        digits(5)
        viewModel.onAction(CalculatorAction.OnCalculateClick)
        operator(CalculatorOperator.ADD)
        digits(1, 0)
        assertEquals("8", result)
    }

    @Test
    fun `a group is previewed as soon as it is closed`() {
        digits(2)
        operator(CalculatorOperator.MULTIPLY)
        viewModel.onAction(CalculatorAction.OnLeftParenthesisClick)
        digits(3)
        operator(CalculatorOperator.ADD)
        digits(4)
        assertNull(result)
        viewModel.onAction(CalculatorAction.OnRightParenthesisClick)
        assertEquals("14", result)
    }
}

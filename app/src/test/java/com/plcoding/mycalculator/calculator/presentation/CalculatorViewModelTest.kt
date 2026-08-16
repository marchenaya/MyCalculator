package com.plcoding.mycalculator.calculator.presentation

import androidx.lifecycle.SavedStateHandle
import com.plcoding.mycalculator.calculator.domain.CalculatorOperator
import com.plcoding.mycalculator.calculator.domain.NoOpExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculatorViewModelTest {

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        viewModel = CalculatorViewModel(
            savedStateHandle = SavedStateHandle(),
            expressionEvaluator = NoOpExpressionEvaluator()
        )
    }

    private fun enter(vararg actions: CalculatorAction) = actions.forEach(viewModel::onAction)

    private fun digits(vararg digits: Int) =
        digits.forEach { viewModel.onAction(CalculatorAction.OnDigitClick(it)) }

    private fun operator(operator: CalculatorOperator) =
        viewModel.onAction(CalculatorAction.OnOperatorClick(operator))

    private val expression get() = viewModel.state.value.expression

    @Test
    fun `starts empty`() {
        assertEquals("", expression)
        assertFalse(viewModel.state.value.canDelete)
        assertFalse(viewModel.state.value.canCalculate)
    }

    @Test
    fun `digits are appended in order`() {
        digits(1, 2, 3)
        assertEquals("123", expression)
        assertTrue(viewModel.state.value.canDelete)
    }

    @Test
    fun `leading zero is replaced by the next digit`() {
        digits(0, 7)
        assertEquals("7", expression)
    }

    @Test
    fun `repeated zeros collapse into a single zero`() {
        digits(0, 0, 0)
        assertEquals("0", expression)
    }

    @Test
    fun `leading zero of an operand is replaced too`() {
        digits(5)
        operator(CalculatorOperator.ADD)
        digits(0, 9)
        assertEquals("5+9", expression)
    }

    @Test
    fun `zero followed by a decimal separator keeps the zero`() {
        digits(0)
        enter(CalculatorAction.OnDecimalClick)
        digits(5)
        assertEquals("0.5", expression)
    }

    @Test
    fun `decimal separator on an empty operand inserts a leading zero`() {
        enter(CalculatorAction.OnDecimalClick)
        digits(2, 5)
        assertEquals("0.25", expression)

        operator(CalculatorOperator.MULTIPLY)
        enter(CalculatorAction.OnDecimalClick)
        digits(5)
        assertEquals("0.25×0.5", expression)
    }

    @Test
    fun `a number can only have one decimal separator`() {
        digits(1)
        enter(CalculatorAction.OnDecimalClick, CalculatorAction.OnDecimalClick)
        digits(5)
        enter(CalculatorAction.OnDecimalClick)
        assertEquals("1.5", expression)
    }

    @Test
    fun `each operand may have its own decimal separator`() {
        digits(1)
        enter(CalculatorAction.OnDecimalClick)
        digits(5)
        operator(CalculatorOperator.ADD)
        digits(2)
        enter(CalculatorAction.OnDecimalClick)
        digits(5)
        assertEquals("1.5+2.5", expression)
    }

    @Test
    fun `a number length is capped`() {
        repeat(20) { viewModel.onAction(CalculatorAction.OnDigitClick(9)) }
        assertEquals(12, expression.length)
    }

    @Test
    fun `the cap applies per operand`() {
        repeat(20) { viewModel.onAction(CalculatorAction.OnDigitClick(9)) }
        operator(CalculatorOperator.ADD)
        digits(1)
        assertEquals("999999999999+1", expression)
    }

    @Test
    fun `an operator is appended between operands`() {
        digits(1, 2)
        operator(CalculatorOperator.MULTIPLY)
        digits(3)
        assertEquals("12×3", expression)
    }

    @Test
    fun `a second operator replaces the previous one`() {
        digits(1, 2)
        operator(CalculatorOperator.MULTIPLY)
        operator(CalculatorOperator.DIVIDE)
        operator(CalculatorOperator.ADD)
        assertEquals("12+", expression)
    }

    @Test
    fun `a trailing decimal separator is dropped when an operator follows`() {
        digits(5)
        enter(CalculatorAction.OnDecimalClick)
        operator(CalculatorOperator.ADD)
        assertEquals("5+", expression)
    }

    @Test
    fun `only a minus may start an expression`() {
        operator(CalculatorOperator.MULTIPLY)
        operator(CalculatorOperator.DIVIDE)
        operator(CalculatorOperator.ADD)
        assertEquals("", expression)

        operator(CalculatorOperator.SUBTRACT)
        digits(5)
        assertEquals("−5", expression)
    }

    @Test
    fun `a leading minus can be swapped for another operator only after an operand`() {
        operator(CalculatorOperator.SUBTRACT)
        operator(CalculatorOperator.ADD)
        assertEquals("−", expression)
    }

    @Test
    fun `delete removes the last character only`() {
        digits(1, 2)
        operator(CalculatorOperator.ADD)
        digits(3)
        enter(CalculatorAction.OnDeleteClick)
        assertEquals("12+", expression)
        enter(CalculatorAction.OnDeleteClick)
        assertEquals("12", expression)
    }

    @Test
    fun `delete on an empty expression is a no-op`() {
        enter(CalculatorAction.OnDeleteClick, CalculatorAction.OnDeleteClick)
        assertEquals("", expression)
    }

    @Test
    fun `clear wipes the whole expression`() {
        digits(1, 2)
        operator(CalculatorOperator.ADD)
        digits(3)
        enter(CalculatorAction.OnClearClick)
        assertEquals("", expression)
        assertFalse(viewModel.state.value.canDelete)
    }

    @Test
    fun `there is no result while the evaluator cannot evaluate`() {
        digits(1, 2)
        operator(CalculatorOperator.ADD)
        digits(3)
        assertEquals(null, viewModel.state.value.result)
        assertFalse(viewModel.state.value.canCalculate)
    }

    @Test
    fun `calculate is a no-op without a result`() {
        digits(1, 2)
        operator(CalculatorOperator.ADD)
        digits(3)
        enter(CalculatorAction.OnCalculateClick)
        assertEquals("12+3", expression)
    }

    @Test
    fun `the expression survives process death`() {
        val savedStateHandle = SavedStateHandle()
        val evaluator = NoOpExpressionEvaluator()
        viewModel = CalculatorViewModel(savedStateHandle, evaluator)
        digits(4, 2)
        operator(CalculatorOperator.DIVIDE)
        digits(7)

        val restored = CalculatorViewModel(savedStateHandle, evaluator)
        assertEquals("42÷7", restored.state.value.expression)
    }

    private fun openGroup() = viewModel.onAction(CalculatorAction.OnLeftParenthesisClick)

    private fun closeGroup() = viewModel.onAction(CalculatorAction.OnRightParenthesisClick)

    @Test
    fun `a group can be opened at the start of an expression`() {
        openGroup()
        digits(1)
        operator(CalculatorOperator.ADD)
        digits(2)
        closeGroup()
        assertEquals("(1+2)", expression)
    }

    @Test
    fun `a group can be opened after an operator`() {
        digits(5)
        operator(CalculatorOperator.MULTIPLY)
        openGroup()
        assertEquals("5×(", expression)
    }

    @Test
    fun `groups can be nested`() {
        openGroup()
        openGroup()
        digits(2)
        closeGroup()
        assertEquals("((2)", expression)
        closeGroup()
        assertEquals("((2))", expression)
    }

    @Test
    fun `a group opened against a number multiplies with it`() {
        digits(5)
        openGroup()
        assertEquals("5×(", expression)
    }

    @Test
    fun `a group opened against a closed group multiplies with it`() {
        openGroup()
        digits(2)
        closeGroup()
        openGroup()
        assertEquals("(2)×(", expression)
    }

    @Test
    fun `a trailing decimal separator is dropped when a group opens`() {
        digits(5)
        enter(CalculatorAction.OnDecimalClick)
        openGroup()
        assertEquals("5×(", expression)
    }

    @Test
    fun `a digit after a closed group multiplies with it`() {
        openGroup()
        digits(1)
        operator(CalculatorOperator.ADD)
        digits(2)
        closeGroup()
        digits(3)
        assertEquals("(1+2)×3", expression)
    }

    @Test
    fun `a decimal separator after a closed group multiplies with it`() {
        openGroup()
        digits(2)
        closeGroup()
        enter(CalculatorAction.OnDecimalClick)
        digits(5)
        assertEquals("(2)×0.5", expression)
    }

    @Test
    fun `a group cannot be closed when none is open`() {
        digits(5)
        closeGroup()
        assertEquals("5", expression)
        assertFalse(viewModel.state.value.canCloseParenthesis)
    }

    @Test
    fun `an empty group cannot be closed`() {
        openGroup()
        closeGroup()
        assertEquals("(", expression)
        assertFalse(viewModel.state.value.canCloseParenthesis)
    }

    @Test
    fun `a group ending on an operator cannot be closed`() {
        openGroup()
        digits(5)
        operator(CalculatorOperator.ADD)
        assertFalse(viewModel.state.value.canCloseParenthesis)
        closeGroup()
        assertEquals("(5+", expression)
    }

    @Test
    fun `a trailing decimal separator is dropped when a group closes`() {
        openGroup()
        digits(5)
        enter(CalculatorAction.OnDecimalClick)
        assertTrue(viewModel.state.value.canCloseParenthesis)
        closeGroup()
        assertEquals("(5)", expression)
    }

    @Test
    fun `an already closed group cannot be closed again`() {
        openGroup()
        digits(5)
        closeGroup()
        assertFalse(viewModel.state.value.canCloseParenthesis)
        closeGroup()
        assertEquals("(5)", expression)
    }

    @Test
    fun `only a minus may start a group`() {
        openGroup()
        operator(CalculatorOperator.MULTIPLY)
        operator(CalculatorOperator.ADD)
        assertEquals("(", expression)

        operator(CalculatorOperator.SUBTRACT)
        digits(5)
        assertEquals("(−5", expression)
    }

    @Test
    fun `an operator can follow a closed group`() {
        openGroup()
        digits(5)
        closeGroup()
        operator(CalculatorOperator.MULTIPLY)
        digits(2)
        assertEquals("(5)×2", expression)
    }

    @Test
    fun `delete removes a parenthesis like any other character`() {
        openGroup()
        digits(5)
        closeGroup()
        enter(CalculatorAction.OnDeleteClick)
        assertEquals("(5", expression)
        assertTrue(viewModel.state.value.canCloseParenthesis)
    }

    @Test
    fun `an implicit multiplication is deleted on its own`() {
        openGroup()
        digits(2)
        closeGroup()
        digits(3)
        assertEquals("(2)×3", expression)
        enter(CalculatorAction.OnDeleteClick)
        assertEquals("(2)×", expression)
        enter(CalculatorAction.OnDeleteClick)
        assertEquals("(2)", expression)
    }

    @Test
    fun `the number cap applies per group operand`() {
        openGroup()
        repeat(20) { viewModel.onAction(CalculatorAction.OnDigitClick(9)) }
        closeGroup()
        assertEquals("(999999999999)", expression)
    }
}

package com.plcoding.mycalculator.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefaultExpressionEvaluatorTest {

    private val evaluator = DefaultExpressionEvaluator()

    @Test
    fun `adds two numbers`() {
        assertEquals(3.0, evaluator.evaluate("1+2")!!, 0.0)
    }

    @Test
    fun `subtracts two numbers`() {
        assertEquals(5.0, evaluator.evaluate("9−4")!!, 0.0)
    }

    @Test
    fun `chains additions and subtractions from left to right`() {
        assertEquals(3.0, evaluator.evaluate("10−4−3")!!, 0.0)
    }

    @Test
    fun `multiplication binds tighter than addition`() {
        assertEquals(14.0, evaluator.evaluate("2+3×4")!!, 0.0)
    }

    @Test
    fun `division binds tighter than subtraction`() {
        assertEquals(8.0, evaluator.evaluate("10−4÷2")!!, 0.0)
    }

    @Test
    fun `chains multiplications and divisions from left to right`() {
        assertEquals(1.0, evaluator.evaluate("12÷4÷3")!!, 0.0)
    }

    @Test
    fun `a group is evaluated before the operator in front of it`() {
        assertEquals(14.0, evaluator.evaluate("2×(3+4)")!!, 0.0)
    }

    @Test
    fun `nested groups are evaluated from the inside out`() {
        assertEquals(10.0, evaluator.evaluate("((2+3)×2)")!!, 0.0)
    }

    @Test
    fun `a group can be followed by another operator`() {
        assertEquals(11.0, evaluator.evaluate("(3+4)+(2×2)")!!, 0.0)
    }

    @Test
    fun `a leading minus makes the first operand negative`() {
        assertEquals(-2.0, evaluator.evaluate("−5+3")!!, 0.0)
    }

    @Test
    fun `a minus right after an opening parenthesis makes the operand negative`() {
        assertEquals(-6.0, evaluator.evaluate("2×(−3)")!!, 0.0)
    }

    @Test
    fun `a negative first operand still respects precedence`() {
        assertEquals(-7.0, evaluator.evaluate("−1×7")!!, 0.0)
    }

    @Test
    fun `decimal operands are evaluated with their fractional part`() {
        assertEquals(0.75, evaluator.evaluate("0.5+0.25")!!, 0.0)
    }

    @Test
    fun `a decimal operand can be negative`() {
        assertEquals(-1.5, evaluator.evaluate("−2.5+1")!!, 0.0)
    }

    @Test
    fun `an empty expression has no result`() {
        assertNull(evaluator.evaluate(""))
    }

    @Test
    fun `an expression ending on an operator has no result`() {
        assertNull(evaluator.evaluate("5+"))
    }

    @Test
    fun `a lone minus has no result`() {
        assertNull(evaluator.evaluate("−"))
    }

    @Test
    fun `an unclosed group has no result`() {
        assertNull(evaluator.evaluate("(5+3"))
    }

    @Test
    fun `an operand ending on a decimal separator is still evaluated`() {
        assertEquals(7.0, evaluator.evaluate("5.+2")!!, 0.0)
    }

    @Test
    fun `dividing by zero has no result`() {
        assertNull(evaluator.evaluate("5÷0"))
    }

    @Test
    fun `dividing zero by zero has no result`() {
        assertNull(evaluator.evaluate("0÷0"))
    }

    @Test
    fun `dividing by a group that evaluates to zero has no result`() {
        assertNull(evaluator.evaluate("5÷(3−3)"))
    }

    @Test
    fun `an expression with a trailing closing parenthesis has no result`() {
        assertNull(evaluator.evaluate("5)"))
    }

    @Test
    fun `an expression that is only a closing parenthesis has no result`() {
        assertNull(evaluator.evaluate(")"))
    }
}

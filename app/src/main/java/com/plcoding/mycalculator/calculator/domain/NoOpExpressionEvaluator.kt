package com.plcoding.mycalculator.calculator.domain

/**
 * Placeholder evaluator: it never produces a result, so the UI simply never shows a
 * preview and the equals key stays disabled.
 *
 * TODO: replace with a real parser + evaluator implementation. Nothing outside of this
 *  class needs to change — the rest of the app only talks to [ExpressionEvaluator].
 */
class NoOpExpressionEvaluator : ExpressionEvaluator {
    override fun evaluate(expression: String): Double? = null
}

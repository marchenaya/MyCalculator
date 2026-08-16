package com.plcoding.mycalculator.calculator.presentation

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plcoding.mycalculator.calculator.presentation.components.CalculatorDisplay
import com.plcoding.mycalculator.calculator.presentation.components.CalculatorKeypad
import com.plcoding.mycalculator.ui.theme.MyCalculatorTheme

@Composable
fun CalculatorRoot(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = viewModel(factory = CalculatorViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CalculatorScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
fun CalculatorScreen(
    state: CalculatorState,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(SCREEN_PADDING),
    ) {
        CalculatorDisplay(
            expression = state.expression,
            result = state.result,
            modifier = Modifier
                .fillMaxWidth()
                .weight(DISPLAY_WEIGHT)
                .padding(bottom = SCREEN_PADDING),
        )
        CalculatorKeypad(
            onDigitClick = { onAction(CalculatorAction.OnDigitClick(it)) },
            onOperatorClick = { onAction(CalculatorAction.OnOperatorClick(it)) },
            onDecimalClick = { onAction(CalculatorAction.OnDecimalClick) },
            onLeftParenthesisClick = { onAction(CalculatorAction.OnLeftParenthesisClick) },
            onRightParenthesisClick = { onAction(CalculatorAction.OnRightParenthesisClick) },
            onDeleteClick = { onAction(CalculatorAction.OnDeleteClick) },
            onClearClick = { onAction(CalculatorAction.OnClearClick) },
            onCalculateClick = { onAction(CalculatorAction.OnCalculateClick) },
            isDeleteEnabled = state.canDelete,
            isCloseParenthesisEnabled = state.canCloseParenthesis,
            isCalculateEnabled = state.canCalculate,
            modifier = Modifier
                .fillMaxWidth()
                .weight(KEYPAD_WEIGHT),
        )
    }
}

private val SCREEN_PADDING = 16.dp
private const val DISPLAY_WEIGHT = 1f
private const val KEYPAD_WEIGHT = 2.4f

@Preview(name = "Empty")
@Preview(name = "Empty dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CalculatorScreenEmptyPreview() {
    MyCalculatorTheme {
        CalculatorScreen(
            state = CalculatorState(),
            onAction = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(name = "Typing a number")
@Composable
private fun CalculatorScreenTypingPreview() {
    MyCalculatorTheme {
        CalculatorScreen(
            state = CalculatorState(expression = "128"),
            onAction = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(name = "With result preview")
@Preview(name = "With result preview dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CalculatorScreenWithResultPreview() {
    MyCalculatorTheme {
        CalculatorScreen(
            state = CalculatorState(expression = "(128+4)×16", result = "2112"),
            onAction = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(name = "Long expression")
@Composable
private fun CalculatorScreenLongExpressionPreview() {
    MyCalculatorTheme {
        CalculatorScreen(
            state = CalculatorState(
                expression = "123456.789×(987654÷12+4444)−999",
                result = "10162034373.2",
            ),
            onAction = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(name = "Compact", widthDp = 320, heightDp = 560)
@Preview(name = "Expanded", widthDp = 840, heightDp = 720)
@Composable
private fun CalculatorScreenSizePreview() {
    MyCalculatorTheme {
        CalculatorScreen(
            state = CalculatorState(expression = "42÷7", result = "6"),
            onAction = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

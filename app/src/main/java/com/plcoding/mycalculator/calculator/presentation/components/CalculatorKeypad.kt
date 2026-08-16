package com.plcoding.mycalculator.calculator.presentation.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plcoding.mycalculator.R
import com.plcoding.mycalculator.calculator.domain.CalculatorOperator
import com.plcoding.mycalculator.ui.theme.MyCalculatorTheme

/**
 * The 4×5 key grid. Every key stretches to fill an equal share of the space the keypad is
 * given, so the grid adapts to whatever height the screen leaves for it.
 *
 * @param isCloseParenthesisEnabled whether there is an open group that can be closed right now.
 */
@Composable
internal fun CalculatorKeypad(
    onDigitClick: (Int) -> Unit,
    onOperatorClick: (CalculatorOperator) -> Unit,
    onDecimalClick: () -> Unit,
    onLeftParenthesisClick: () -> Unit,
    onRightParenthesisClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit,
    onCalculateClick: () -> Unit,
    isDeleteEnabled: Boolean,
    isCloseParenthesisEnabled: Boolean,
    isCalculateEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(KeySpacing),
    ) {
        KeyRow {
            CalculatorButton(
                symbol = stringResource(R.string.key_clear),
                onClick = onClearClick,
                style = CalculatorButtonStyle.DESTRUCTIVE,
                contentDescription = stringResource(R.string.cd_key_clear),
                modifier = keyModifier(),
            )
            CalculatorButton(
                symbol = stringResource(R.string.key_left_parenthesis),
                onClick = onLeftParenthesisClick,
                style = CalculatorButtonStyle.ACTION,
                contentDescription = stringResource(R.string.cd_key_left_parenthesis),
                modifier = keyModifier(),
            )
            CalculatorButton(
                symbol = stringResource(R.string.key_right_parenthesis),
                onClick = onRightParenthesisClick,
                style = CalculatorButtonStyle.ACTION,
                contentDescription = stringResource(R.string.cd_key_right_parenthesis),
                enabled = isCloseParenthesisEnabled,
                modifier = keyModifier(),
            )
            OperatorKey(CalculatorOperator.DIVIDE, onOperatorClick)
        }
        KeyRow {
            DigitKey(digit = 7, onDigitClick = onDigitClick)
            DigitKey(digit = 8, onDigitClick = onDigitClick)
            DigitKey(digit = 9, onDigitClick = onDigitClick)
            OperatorKey(CalculatorOperator.MULTIPLY, onOperatorClick)
        }
        KeyRow {
            DigitKey(digit = 4, onDigitClick = onDigitClick)
            DigitKey(digit = 5, onDigitClick = onDigitClick)
            DigitKey(digit = 6, onDigitClick = onDigitClick)
            OperatorKey(CalculatorOperator.SUBTRACT, onOperatorClick)
        }
        KeyRow {
            DigitKey(digit = 1, onDigitClick = onDigitClick)
            DigitKey(digit = 2, onDigitClick = onDigitClick)
            DigitKey(digit = 3, onDigitClick = onDigitClick)
            OperatorKey(CalculatorOperator.ADD, onOperatorClick)
        }
        KeyRow {
            CalculatorButton(
                symbol = stringResource(R.string.key_delete),
                onClick = onDeleteClick,
                style = CalculatorButtonStyle.ACTION,
                contentDescription = stringResource(R.string.cd_key_delete),
                enabled = isDeleteEnabled,
                modifier = keyModifier(),
            )
            DigitKey(digit = 0, onDigitClick = onDigitClick)
            CalculatorButton(
                symbol = stringResource(R.string.key_decimal_separator),
                onClick = onDecimalClick,
                contentDescription = stringResource(R.string.cd_key_decimal_separator),
                modifier = keyModifier(),
            )
            CalculatorButton(
                symbol = stringResource(R.string.key_equals),
                onClick = onCalculateClick,
                style = CalculatorButtonStyle.ACCENT,
                contentDescription = stringResource(R.string.cd_key_equals),
                enabled = isCalculateEnabled,
                modifier = keyModifier(),
            )
        }
    }
}

@Composable
private fun ColumnScope.KeyRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(KeySpacing),
        content = content,
    )
}

@Composable
private fun RowScope.DigitKey(
    digit: Int,
    onDigitClick: (Int) -> Unit,
) {
    CalculatorButton(
        symbol = digit.toString(),
        onClick = { onDigitClick(digit) },
        style = CalculatorButtonStyle.NUMBER,
        modifier = keyModifier(),
    )
}

@Composable
private fun RowScope.OperatorKey(
    operator: CalculatorOperator,
    onOperatorClick: (CalculatorOperator) -> Unit,
) {
    CalculatorButton(
        symbol = operator.symbol.toString(),
        onClick = { onOperatorClick(operator) },
        style = CalculatorButtonStyle.OPERATOR,
        contentDescription = operator.contentDescription(),
        modifier = keyModifier(),
    )
}

@Composable
private fun CalculatorOperator.contentDescription(): String = stringResource(
    when (this) {
        CalculatorOperator.ADD -> R.string.cd_key_add
        CalculatorOperator.SUBTRACT -> R.string.cd_key_subtract
        CalculatorOperator.MULTIPLY -> R.string.cd_key_multiply
        CalculatorOperator.DIVIDE -> R.string.cd_key_divide
    },
)

private fun RowScope.keyModifier(): Modifier = Modifier
    .weight(1f)
    .fillMaxHeight()

private val KeySpacing = 12.dp

@Preview(name = "Keypad", widthDp = 360, heightDp = 480)
@Preview(name = "Keypad dark", widthDp = 360, heightDp = 480, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CalculatorKeypadPreview() {
    MyCalculatorTheme {
        PreviewKeypad(
            isDeleteEnabled = true,
            isCloseParenthesisEnabled = true,
            isCalculateEnabled = true,
        )
    }
}

@Preview(name = "Nothing entered", widthDp = 360, heightDp = 480)
@Preview(name = "Nothing entered dark", widthDp = 360, heightDp = 480, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CalculatorKeypadEmptyStatePreview() {
    MyCalculatorTheme {
        PreviewKeypad(
            isDeleteEnabled = false,
            isCloseParenthesisEnabled = false,
            isCalculateEnabled = false,
        )
    }
}

@Preview(name = "Open group", widthDp = 360, heightDp = 480)
@Composable
private fun CalculatorKeypadOpenGroupPreview() {
    MyCalculatorTheme {
        PreviewKeypad(
            isDeleteEnabled = true,
            isCloseParenthesisEnabled = true,
            isCalculateEnabled = false,
        )
    }
}

@Preview(name = "Compact width", widthDp = 320, heightDp = 420)
@Preview(name = "Expanded width", widthDp = 840, heightDp = 480)
@Preview(name = "Large font scale", widthDp = 360, heightDp = 480, fontScale = 2f)
@Composable
private fun CalculatorKeypadSizePreview() {
    MyCalculatorTheme {
        PreviewKeypad(
            isDeleteEnabled = true,
            isCloseParenthesisEnabled = true,
            isCalculateEnabled = true,
        )
    }
}

@Composable
private fun PreviewKeypad(
    isDeleteEnabled: Boolean,
    isCloseParenthesisEnabled: Boolean,
    isCalculateEnabled: Boolean,
) {
    CalculatorKeypad(
        onDigitClick = {},
        onOperatorClick = {},
        onDecimalClick = {},
        onLeftParenthesisClick = {},
        onRightParenthesisClick = {},
        onDeleteClick = {},
        onClearClick = {},
        onCalculateClick = {},
        isDeleteEnabled = isDeleteEnabled,
        isCloseParenthesisEnabled = isCloseParenthesisEnabled,
        isCalculateEnabled = isCalculateEnabled,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    )
}

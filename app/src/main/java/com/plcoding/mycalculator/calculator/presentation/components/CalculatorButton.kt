package com.plcoding.mycalculator.calculator.presentation.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plcoding.mycalculator.ui.theme.MyCalculatorTheme

/**
 * The visual weight of a key, which is what tells the four kinds of keys apart at a glance.
 */
enum class CalculatorButtonStyle {
    NUMBER,
    OPERATOR,
    ACTION,
    DESTRUCTIVE,
    ACCENT,
}

/**
 * A single calculator key. It fills whatever space the keypad hands it, so the caller
 * decides the size via [modifier] weights.
 *
 * @param contentDescription spoken label for keys whose [symbol] is a glyph that does not
 * read well, e.g. `⌫`. Pass `null` when the symbol itself is a fine label.
 */
@Composable
internal fun CalculatorButton(
    symbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CalculatorButtonStyle = CalculatorButtonStyle.NUMBER,
    contentDescription: String? = null,
    enabled: Boolean = true,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(KeyCornerRadius),
        colors = style.buttonColors(),
        elevation = null,
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = if (contentDescription != null) {
                Modifier.clearAndSetSemantics { this.contentDescription = contentDescription }
            } else {
                Modifier
            },
        )
    }
}

@Composable
private fun CalculatorButtonStyle.buttonColors(): ButtonColors = when (this) {
    CalculatorButtonStyle.NUMBER -> ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
    CalculatorButtonStyle.OPERATOR -> ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
    CalculatorButtonStyle.ACTION -> ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    CalculatorButtonStyle.DESTRUCTIVE -> ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    )
    CalculatorButtonStyle.ACCENT -> ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    )
}

private val KeyCornerRadius = 24.dp

@Preview(name = "Styles", widthDp = 360)
@Preview(name = "Styles dark", widthDp = 360, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CalculatorButtonStylesPreview() {
    MyCalculatorTheme {
        Row(modifier = Modifier.height(80.dp)) {
            CalculatorButtonStyle.entries.forEach { style ->
                CalculatorButton(
                    symbol = "7",
                    onClick = {},
                    style = style,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(name = "Disabled", widthDp = 120, heightDp = 100)
@Composable
private fun CalculatorButtonDisabledPreview() {
    MyCalculatorTheme {
        CalculatorButton(
            symbol = "=",
            onClick = {},
            style = CalculatorButtonStyle.ACCENT,
            enabled = false,
            modifier = Modifier.height(80.dp),
        )
    }
}

@Preview(name = "Large font scale", widthDp = 120, heightDp = 100, fontScale = 2f)
@Composable
private fun CalculatorButtonLargeFontPreview() {
    MyCalculatorTheme {
        CalculatorButton(
            symbol = "÷",
            onClick = {},
            style = CalculatorButtonStyle.OPERATOR,
            modifier = Modifier.height(80.dp),
        )
    }
}

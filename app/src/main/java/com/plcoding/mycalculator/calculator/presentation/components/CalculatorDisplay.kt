package com.plcoding.mycalculator.calculator.presentation.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plcoding.mycalculator.R
import com.plcoding.mycalculator.ui.theme.MyCalculatorTheme

/**
 * The read-out above the keypad: the expression being typed, with the live result
 * preview underneath it.
 *
 * Both lines stay on a single line and scroll horizontally instead of wrapping, so a long
 * expression never pushes the keypad around.
 *
 * @param result the preview of what the expression evaluates to, or `null` when there is
 * nothing to preview yet.
 */
@Composable
internal fun CalculatorDisplay(
    expression: String,
    result: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = expression.ifEmpty { stringResource(R.string.display_placeholder) },
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Light,
            color = if (expression.isEmpty()) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.End,
            softWrap = false,
            maxLines = 1,
            // Reversed so the caret end of a long expression is what stays in view.
            modifier = Modifier.horizontalScroll(rememberScrollState(), reverseScrolling = true)
        )
        AnimatedVisibility(
            visible = result != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Text(
                text = result.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                softWrap = false,
                maxLines = 1,
                modifier = Modifier.horizontalScroll(rememberScrollState(), reverseScrolling = true)
            )
        }
    }
}

@Preview(name = "Empty", widthDp = 360, heightDp = 200)
@Preview(name = "Empty dark", widthDp = 360, heightDp = 200, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CalculatorDisplayEmptyPreview() {
    MyCalculatorTheme {
        CalculatorDisplay(
            expression = "",
            result = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Typing", widthDp = 360, heightDp = 200)
@Composable
private fun CalculatorDisplayTypingPreview() {
    MyCalculatorTheme {
        CalculatorDisplay(
            expression = "128×4",
            result = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "With result", widthDp = 360, heightDp = 200)
@Preview(name = "With result dark", widthDp = 360, heightDp = 200, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CalculatorDisplayWithResultPreview() {
    MyCalculatorTheme {
        CalculatorDisplay(
            expression = "128×4+16",
            result = "528",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Long expression", widthDp = 360, heightDp = 200)
@Composable
private fun CalculatorDisplayLongExpressionPreview() {
    MyCalculatorTheme {
        CalculatorDisplay(
            expression = "123456.789×987654÷12+4444−999",
            result = "9876543.21",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Expanded width", widthDp = 840, heightDp = 200)
@Composable
private fun CalculatorDisplayWidePreview() {
    MyCalculatorTheme {
        CalculatorDisplay(
            expression = "128×4+16",
            result = "528",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

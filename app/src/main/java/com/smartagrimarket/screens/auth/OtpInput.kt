package com.smartagrimarket.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagrimarket.ui.theme.AgriCardBg
import com.smartagrimarket.ui.theme.AgriTextPrimary
import com.smartagrimarket.ui.theme.AgriTextSecondary
import com.smartagrimarket.ui.theme.AgriYellow
import kotlinx.coroutines.delay

/**
 * Multi-digit OTP field with:
 *  - auto-advance as you type
 *  - auto-back on backspace
 *  - paste support (fills all boxes at once)
 *  - hides the keyboard automatically when complete
 *
 * A single invisible [BasicTextField] captures all input (including pastes
 * and IME autofill). The visible boxes are drawn behind it and update as
 * [value] changes. Tapping anywhere in the field refocuses the hidden input.
 */
@Composable
fun OtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    boxHeight: Dp = 56.dp,
    enabled: Boolean = true,
    autoFocus: Boolean = true,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var hasFocus by remember { mutableStateOf(false) }
    val display = value.padEnd(length, ' ').take(length)

    // Focus once so the user can start typing immediately.
    LaunchedEffect(autoFocus, enabled) {
        if (autoFocus && enabled) {
            delay(150)
            runCatching { focusRequester.requestFocus() }
        }
    }

    Box(modifier = modifier) {
        // ── Visible boxes ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(boxHeight)
        ) {
            repeat(length) { index ->
                val char = display[index]
                val isFilled = char != ' '
                // Highlight the next slot to fill while focused.
                val isActive = hasFocus && enabled && index == value.length
                OtpBox(
                    char = if (isFilled) char.toString() else "",
                    isActive = isActive,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        // ── Hidden input on top: captures typing, paste, autofill ──
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                val sanitized = raw.filter { it.isDigit() }.take(length)
                onValueChange(sanitized)
                if (sanitized.length == length) {
                    keyboard?.hide()
                }
            },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboard?.hide() }
            ),
            textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
                .matchParentSize()
                .focusRequester(focusRequester)
                .onFocusChanged { hasFocus = it.isFocused }
        )
    }
}

@Composable
private fun OtpBox(
    char: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AgriCardBg)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) AgriYellow
                else AgriTextSecondary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (char.isNotEmpty()) {
            Text(
                text = char,
                style = MaterialTheme.typography.titleLarge.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                ),
                color = AgriTextPrimary
            )
        }
    }
}
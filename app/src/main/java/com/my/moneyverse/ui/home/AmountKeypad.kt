package com.my.moneyverse.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

private const val BACKSPACE = "⌫"
private val keys = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf(".", "0", BACKSPACE),
)

/** Applies a keypad press to the current amount text (max 9 integer digits, 2 decimals). */
fun applyKey(current: String, key: String): String = when (key) {
    BACKSPACE -> current.dropLast(1)
    "." -> when {
        current.contains('.') -> current
        current.isEmpty() -> "0."
        else -> "$current."
    }
    else -> {
        val dot = current.indexOf('.')
        when {
            dot >= 0 && current.length - dot > 2 -> current
            dot < 0 && current.length >= 9 -> current
            current == "0" -> key
            else -> current + key
        }
    }
}

@Composable
fun AmountKeypad(onKey: (String) -> Unit, onClear: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    KeypadKey(key, Modifier.weight(1f), onKey, if (key == BACKSPACE) onClear else null)
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(key: String, modifier: Modifier, onKey: (String) -> Unit, onLongPress: (() -> Unit)?) {
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "key")
    Box(
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (pressed) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh,
            )
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onLongClick = onLongPress?.let { clear ->
                    {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        clear()
                    }
                },
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKey(key)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (key == BACKSPACE) {
            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete digit")
        } else {
            Text(key, style = MaterialTheme.typography.titleLarge)
        }
    }
}

package com.my.moneyverse.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.my.moneyverse.ui.theme.Amber
import com.my.moneyverse.ui.theme.Black
import com.my.moneyverse.ui.theme.Border
import com.my.moneyverse.ui.theme.ExpenseRed
import com.my.moneyverse.ui.theme.Green
import com.my.moneyverse.ui.theme.GreenGradient
import com.my.moneyverse.ui.theme.Lime
import com.my.moneyverse.ui.theme.PrimaryGradient
import com.my.moneyverse.ui.theme.SurfaceHigh
import com.my.moneyverse.util.formatMoney

fun parseHexColor(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Green)

val CardShape = RoundedCornerShape(16.dp)

/** Dark card with a hairline border, matching the website's `.pain-card` / `.sb-overall`. */
@Composable
fun MvCard(
    modifier: Modifier = Modifier,
    background: Brush = SolidColor(SurfaceHigh),
    borderColor: Color = Border,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(background)
            .border(1.dp, borderColor, CardShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content,
    )
}

/** Uppercase green label shown above section titles. */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = Green) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color, modifier = modifier)
}

@Composable
fun ScreenHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = horizontalPadding, end = horizontalPadding, top = 20.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Eyebrow(eyebrow)
            Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 2.dp))
        }
        trailing()
    }
}

/** Gradient "M" tile from the website logo. */
@Composable
fun LogoMark(size: Dp = 36.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.28f)).background(GreenGradient),
        contentAlignment = Alignment.Center,
    ) {
        Text("M", color = Black, fontWeight = FontWeight.Black, fontSize = (size.value * 0.45f).sp)
    }
}

/** Primary call-to-action styled like the website's `.btn--primary`. */
@Composable
fun GradientFab(text: String, onClick: () -> Unit, expanded: Boolean = true) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .shadow(16.dp, shape, ambientColor = Green, spotColor = Green)
            .clip(shape)
            .background(PrimaryGradient)
            .clickable(onClick = onClick)
            .animateContentSize()
            .padding(horizontal = if (expanded) 22.dp else 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Add, contentDescription = text, tint = Black)
        if (expanded) {
            Spacer(Modifier.width(8.dp))
            Text(text, color = Black, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun CategoryIcon(emoji: String, colorHex: String, size: Dp = 40.dp) {
    val color = parseHexColor(colorHex)
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = (size.value * 0.45f).sp)
    }
}

/** Counts up/down to [amountMinor] whenever it changes. */
@Composable
fun AnimatedMoney(
    amountMinor: Long,
    style: TextStyle,
    color: Color = MaterialTheme.colorScheme.onSurface,
    prefix: String = "",
    modifier: Modifier = Modifier,
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(amountMinor) {
        anim.animateTo(amountMinor.toFloat(), tween(durationMillis = 700))
    }
    val shown = if (anim.isRunning) anim.value.toLong() else amountMinor
    Text(prefix + formatMoney(shown), style = style, color = color, modifier = modifier)
}

private val WarnGradient = Brush.horizontalGradient(listOf(Amber, Color(0xFFFF9100)))
private val DangerGradient = Brush.horizontalGradient(listOf(ExpenseRed, Color(0xFFD50000)))
private val OkGradient = Brush.horizontalGradient(listOf(Green, Lime))

/** Progress bar that goes green → amber (80%) → red (100%), as on the website. */
@Composable
fun BudgetBar(fraction: Float, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val progress by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(1200), label = "budget")
    val fill = when {
        fraction >= 1f -> DangerGradient
        fraction >= 0.8f -> WarnGradient
        else -> OkGradient
    }
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.06f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress)
                .height(height)
                .clip(RoundedCornerShape(50))
                .background(fill),
        )
    }
}

fun budgetColor(fraction: Float): Color = when {
    fraction >= 1f -> ExpenseRed
    fraction >= 0.8f -> Amber
    else -> Green
}

@Composable
fun EmptyState(emoji: String, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 56.sp)
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

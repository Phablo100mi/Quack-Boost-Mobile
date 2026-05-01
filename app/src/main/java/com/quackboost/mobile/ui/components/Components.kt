package com.quackboost.mobile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quackboost.mobile.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ─── Info Card Container ────────────────────────────────────────────────────

@Composable
fun InfoCard(
    title: String,
    icon: String = "",
    accentColor: Color = Accent,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier  = modifier.fillMaxWidth(),
        color     = BgCard,
        shape     = RoundedCornerShape(16.dp),
        border    = BorderStroke(1.dp, BgCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    if (icon.isNotEmpty()) {
                        Text(icon, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text       = title,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = accentColor
                    )
                }
            }
            content()
        }
    }
}

// ─── Info Row ────────────────────────────────────────────────────────────────

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text     = label,
            fontSize = 12.sp,
            color    = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text       = value,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Medium,
            color      = valueColor,
            textAlign  = TextAlign.End,
            modifier   = Modifier.weight(1f)
        )
    }
    HorizontalDivider(color = BgCardBorder, thickness = 0.5.dp)
}

// ─── Arc Gauge ───────────────────────────────────────────────────────────────

@Composable
fun ArcGauge(
    value: Float,          // 0..100
    label: String,
    sublabel: String = "",
    size: Dp = 130.dp,
    color: Color = Accent,
    strokeWidth: Dp = 10.dp
) {
    val animValue by animateFloatAsState(
        targetValue  = value.coerceIn(0f, 100f),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "gauge"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val radius = (this.size.minDimension - stroke) / 2f
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val startAngle = 135f
            val sweepTotal = 270f

            // Background arc
            drawArc(
                color      = BgCardBorder,
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter  = false,
                style      = Stroke(stroke, cap = StrokeCap.Round),
                topLeft    = Offset(cx - radius, cy - radius),
                size       = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )

            // Filled arc
            val sweep = sweepTotal * (animValue / 100f)
            drawArc(
                brush      = Brush.sweepGradient(
                    colors = listOf(color.copy(alpha = 0.7f), color),
                    center = Offset(cx, cy)
                ),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter  = false,
                style      = Stroke(stroke, cap = StrokeCap.Round),
                topLeft    = Offset(cx - radius, cy - radius),
                size       = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "${animValue.toInt()}%",
                fontSize   = (size.value * 0.18f).sp,
                fontWeight = FontWeight.Bold,
                color      = color
            )
            Text(
                text     = label,
                fontSize = (size.value * 0.085f).sp,
                color    = TextSecondary
            )
            if (sublabel.isNotEmpty()) {
                Text(
                    text     = sublabel,
                    fontSize = (size.value * 0.075f).sp,
                    color    = TextMuted
                )
            }
        }
    }
}

// ─── Linear Progress Bar ─────────────────────────────────────────────────────

@Composable
fun LabeledProgressBar(
    label: String,
    value: Float,     // 0..100
    valueText: String,
    color: Color = Accent,
    modifier: Modifier = Modifier
) {
    val animValue by animateFloatAsState(
        targetValue  = (value / 100f).coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "progress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Text(valueText, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress       = { animValue },
            modifier       = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color          = color,
            trackColor     = BgCardBorder,
            strokeCap      = StrokeCap.Round
        )
    }
}

// ─── Temperature Badge ────────────────────────────────────────────────────────

@Composable
fun TempBadge(celsius: Float, modifier: Modifier = Modifier) {
    val color = when {
        celsius >= 80 -> ColorDanger
        celsius >= 60 -> ColorWarn
        else -> ColorGood
    }
    Surface(
        modifier = modifier,
        color    = color.copy(alpha = 0.15f),
        shape    = RoundedCornerShape(8.dp),
        border   = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text     = "%.1f°C".format(celsius),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color    = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ─── Section Header ──────────────────────────────────────────────────────────

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text       = text.uppercase(),
        fontSize   = 10.sp,
        fontWeight = FontWeight.Bold,
        color      = TextMuted,
        letterSpacing = 1.5.sp,
        modifier   = modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

// ─── Core Frequency Grid ──────────────────────────────────────────────────────

@Composable
fun CoreFrequencyGrid(frequencies: List<Long>, maxFreq: Long) {
    val cols = 2
    val rows = (frequencies.size + cols - 1) / cols

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(cols) { col ->
                    val idx = row * cols + col
                    if (idx < frequencies.size) {
                        val freq = frequencies[idx]
                        val pct  = if (maxFreq > 0) freq.toFloat() / maxFreq.toFloat() else 0f
                        CoreCell(
                            index = idx,
                            freqMhz = freq,
                            pct = pct,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CoreCell(
    index: Int,
    freqMhz: Long,
    pct: Float,
    modifier: Modifier = Modifier
) {
    val animPct by animateFloatAsState(
        targetValue  = pct.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "corefreq"
    )
    val barColor = Accent.copy(alpha = 0.5f + 0.5f * animPct)

    Surface(
        modifier = modifier,
        color    = BgBase,
        shape    = RoundedCornerShape(10.dp),
        border   = BorderStroke(1.dp, BgCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CPU${index}",
                fontSize = 10.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (freqMhz > 0) "${freqMhz}MHz" else "—",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = barColor
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress    = { animPct },
                modifier    = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color       = barColor,
                trackColor  = BgCardBorder
            )
        }
    }
}

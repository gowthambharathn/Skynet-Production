package skynetbee.developers.production

/**
 * Created by Gowtham Barath
 * Date: 23-06-2026
 */

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun BarGraph(
    title: String,
    dataList: List<Pair<String, Float>>,
) {
    val axisColor = Color(0xFF868686)

    val baseColors = listOf(
        Color(0xFF008EFF),
        Color(0xFF00FF0D),
        Color(0xFFFF9700),
        Color(0xFFD900FF)
    )

    // Limit to top 4 entries (as before)
    val filteredData = dataList.take(4)

    // ── ERROR HANDLING: NO DATA ──────────────────────────────────────
    // Empty list, or a list where every entry is null/blank, never
    // reaches the canvas — it shows a clean "No Data Available" card
    // instead of drawing empty axes.
    if (dataList.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .backgroundCard(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Gold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            Text("No Data Available", color = Color.Gray)
        }
        return
    }

    // ── ERROR HANDLING: BAD ENTRIES ──────────────────────────────────
    // Blank labels or NaN/Infinite values would otherwise silently draw
    // a broken bar (or crash Canvas' text layout). Flag it clearly
    // instead of rendering garbage.
    val invalidEntries = filteredData.filter { (label, value) ->
        label.isBlank() || value.isNaN() || value.isInfinite()
    }
    if (invalidEntries.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .backgroundCard(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Gold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "Invalid data detected\n${invalidEntries.size} entr${if (invalidEntries.size == 1) "y" else "ies"} could not be plotted",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    // ── SCALING ───────────────────────────────────────────────────────
    // Values are treated as percentages (0-100 axis, as the original
    // design intended), but negative values or values above 100 no
    // longer silently draw off-chart or upside down — the axis stretches
    // to fit whatever range is actually present, with a safe fallback
    // when every value is 0.
    val maxValueRaw = filteredData.maxOf { it.second }
    val minValueRaw = filteredData.minOf { it.second }
    val axisMax = max(100f, maxValueRaw)
    val axisMin = min(0f, minValueRaw)
    val axisRange = (axisMax - axisMin).let { if (it == 0f) 1f else it }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .backgroundCard(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Gold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (dataList.size > 4) {
                Text(
                    text = "Only top 4 subjects shown.",
                    color = Silver,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // ── RESPONSIVE CANVAS ───────────────────────────────────────
            // No fixed dp size anymore — fills the available width and
            // keeps a fixed aspect ratio, so it scales correctly on
            // phones, tablets, landscape, and foldables.
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.3f)
                    .padding(8.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Adaptive padding, proportional to canvas size
                val leftPadding = canvasWidth * 0.14f
                val bottomPadding = canvasHeight * 0.12f
                val topPadding = canvasHeight * 0.14f
                val rightPadding = canvasWidth * 0.04f

                val yAxisX = leftPadding
                val xAxisY = canvasHeight - bottomPadding
                val graphRight = canvasWidth - rightPadding
                val graphTop = topPadding

                val graphWidth = (graphRight - yAxisX).coerceAtLeast(0f)
                val graphHeight = (xAxisY - graphTop).coerceAtLeast(0f)

                // Adaptive text size so labels stay readable on any screen
                val baseTextSize = (canvasWidth / 32f).coerceIn(18f, 28f)

                // Draw axes
                drawLine(
                    color = axisColor,
                    start = Offset(yAxisX, graphTop),
                    end = Offset(yAxisX, xAxisY),
                    strokeWidth = 2f
                )
                drawLine(
                    color = axisColor,
                    start = Offset(yAxisX, xAxisY),
                    end = Offset(graphRight, xAxisY),
                    strokeWidth = 2f
                )

                // Y-axis grid & labels, scaled to the real axis range
                // instead of an assumed fixed 0..100
                val yStepCount = 5
                for (step in 0..yStepCount) {
                    val value = axisMin + axisRange * step / yStepCount
                    val y = xAxisY - ((value - axisMin) / axisRange) * graphHeight

                    drawContext.canvas.nativeCanvas.drawText(
                        "${value.toInt()}%",
                        yAxisX - 12f,
                        y + baseTextSize * 0.35f,
                        Paint().apply {
                            color = Silver.toArgb()
                            textSize = baseTextSize
                            textAlign = Paint.Align.RIGHT
                            isAntiAlias = true
                        }
                    )

                    drawLine(
                        color = axisColor.copy(alpha = 0.2f),
                        start = Offset(yAxisX, y),
                        end = Offset(graphRight, y),
                        strokeWidth = 1f
                    )
                }

                // Zero line drawn solid when the axis dips below 0, so
                // negative bars are still easy to read at a glance
                if (axisMin < 0f) {
                    val zeroY = xAxisY - ((0f - axisMin) / axisRange) * graphHeight
                    drawLine(
                        color = axisColor.copy(alpha = 0.6f),
                        start = Offset(yAxisX, zeroY),
                        end = Offset(graphRight, zeroY),
                        strokeWidth = 1.5f
                    )
                }

                // Compute bar widths and spacing per column
                val itemCount = filteredData.size.coerceAtLeast(1)
                val columnWidth = if (graphWidth > 0f) graphWidth / itemCount else 0f
                val barWidth = columnWidth * 0.6f
                val spacing = (columnWidth - barWidth) / 2f

                filteredData.forEachIndexed { index, (_, value) ->
                    val zeroY = xAxisY - ((0f - axisMin) / axisRange) * graphHeight
                    val valueY = xAxisY - ((value - axisMin) / axisRange) * graphHeight

                    val topY = min(zeroY, valueY)
                    val barHeight = (abs(zeroY - valueY)).coerceAtLeast(0f)

                    val leftX = yAxisX + index * columnWidth + spacing
                    val barColor = baseColors[index % baseColors.size]

                    // Draw main bar (vertical gradient)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                barColor.copy(alpha = 0.9f),
                                barColor.copy(alpha = 0.7f)
                            )
                        ),
                        topLeft = Offset(leftX, topY),
                        size = Size(width = barWidth, height = barHeight)
                    )

                    // Top bevel (simple 3D look)
                    val bevelOffset = 10f.coerceAtMost(barWidth / 2f)
                    val topShade = Path().apply {
                        moveTo(leftX, topY)
                        lineTo(leftX + barWidth, topY)
                        lineTo(leftX + barWidth - bevelOffset, topY - bevelOffset)
                        lineTo(leftX + bevelOffset, topY - bevelOffset)
                        close()
                    }
                    drawPath(path = topShade, color = barColor.copy(alpha = 0.3f))

                    // Draw value above (or below, for negative bars) the bar
                    val labelY = if (value >= 0f) topY - baseTextSize * 0.6f else topY + barHeight + baseTextSize
                    drawContext.canvas.nativeCanvas.drawText(
                        "${value.toInt()}%",
                        leftX + barWidth / 2f,
                        labelY,
                        Paint().apply {
                            color = Silver.toArgb()
                            textSize = baseTextSize
                            textAlign = Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend (horizontal scroll if needed)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .wrapContentWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                filteredData.forEachIndexed { index, (name, _) ->
                    if (index > 0) Spacer(Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(baseColors[index % baseColors.size], CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = name,
                            color = Silver,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BarGraph(
    title: String,
    dataMap: Map<String, Float>, // Key-Value Pair version
) {
    // Convert Map to List<Pair<String, Float>> (existing version expects this)
    val dataList = dataMap.map { it.key to it.value }
    BarGraph(title = title, dataList = dataList)
}
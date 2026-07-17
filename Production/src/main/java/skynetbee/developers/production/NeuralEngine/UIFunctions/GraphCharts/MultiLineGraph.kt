package skynetbee.developers.developerenvironment


import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import skynetbee.developers.production.Gold
import skynetbee.developers.production.Silver
import skynetbee.developers.production.backgroundCard
import kotlin.math.max
import kotlin.math.min

@Composable
fun MultiLineGraph(
    title: String,
    dataValues: List<Pair<String, List<Float>>>, // Dynamic dataset list
) {

    val axisColor = Color(0xFF868686)

    // Default Colors (Will repeat if there are more datasets)
    val baseColors = listOf(
        Color(0xFFE53935), Color(0xFFFFD600), Color(0xFF1E88E5),
        Color(0xFF43A047), Color(0xFF8E24AA), Color(0xFFFB8C00),
        Color(0xFF00ACC1), Color(0xFF8D6E63)
    )
    val lineColors = List(dataValues.size) { baseColors[it % baseColors.size] }
    val dotColors = List(dataValues.size) { baseColors[it % baseColors.size].copy(alpha = 0.6f) }

    // ── NO DATA STATE ────────────────────────────────────────────────
    val hasAnyPoints = dataValues.any { it.second.isNotEmpty() }
    if (dataValues.isEmpty() || !hasAnyPoints) {
        Column(
            modifier = Modifier
                .fillMaxWidth(.95f)
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

    // ── SCALE CALCULATION ───────────────────────────────────────────────
    // The old version hardcoded minValue = 0f / maxValue = 100f, so any
    // series with values outside 0-100 (or a tight range within it) was
    // drawn wrong. Now the scale is derived from the real min/max across
    // every series, with a small pad and a divide-by-zero guard.
    val allValues = dataValues.flatMap { it.second }
    val dataMin = allValues.min()
    val dataMax = allValues.max()

    var minValue = dataMin
    var maxValue = dataMax

    if (maxValue - minValue == 0f) {
        val pad = if (maxValue == 0f) 1f else kotlin.math.abs(maxValue) * 0.1f
        minValue -= pad
        maxValue += pad
    } else {
        val pad = (maxValue - minValue) * 0.08f
        minValue -= pad
        maxValue += pad
    }
    val range = maxValue - minValue

    // The longest series decides how many points sit along the X-axis;
    // every series is positioned against this same count so lines of
    // different lengths still line up correctly on shared X positions.
    val maxDataPoints = dataValues.maxOf { it.second.size }

    Column(
        modifier = Modifier
            .fillMaxWidth(.95f)
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
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // ── RESPONSIVE CANVAS ───────────────────────────────────────
            // No fixed dp size anymore — fills the available width and
            // keeps a fixed aspect ratio so it scales correctly across
            // phones, tablets, landscape, and foldables.
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.3f)
                    .padding(8.dp)
            ) {
                val width = size.width
                val height = size.height

                // Adaptive padding, proportional to canvas size
                val leftPadding = width * 0.12f
                val bottomPadding = height * 0.14f
                val topPadding = height * 0.08f
                val rightPadding = width * 0.04f

                val yAxisX = leftPadding
                val xAxisY = height - bottomPadding
                val graphRight = width - rightPadding
                val graphTop = topPadding

                val graphWidth = graphRight - yAxisX
                val graphHeight = xAxisY - graphTop

                // Adaptive text size so labels stay readable on any screen
                val baseTextSize = (width / 32f).coerceIn(20f, 30f)

                // Draw Axes
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

                // Draw Y-axis labels using the real data range (5 evenly
                // spaced ticks) instead of a hardcoded 0-100 scale
                val ySteps = 5
                for (step in 0..ySteps) {
                    val value = minValue + range * (step / ySteps.toFloat())
                    val yPosition = xAxisY - ((value - minValue) / range) * graphHeight

                    drawContext.canvas.nativeCanvas.drawText(
                        formatLabel(value),
                        yAxisX - 12f,
                        yPosition + baseTextSize * 0.35f,
                        Paint().apply {
                            color = Color.Gray.toArgb()
                            textSize = baseTextSize
                            textAlign = Paint.Align.RIGHT
                            isAntiAlias = true
                        }
                    )

                    drawLine(
                        color = axisColor.copy(alpha = 0.2f),
                        start = Offset(yAxisX, yPosition),
                        end = Offset(graphRight, yPosition),
                        strokeWidth = 1f
                    )
                }

                // Draw X-axis labels — skip some when there isn't room so
                // labels never overlap on narrow screens
                if (maxDataPoints > 0) {
                    val maxLabels = (graphWidth / (baseTextSize * 4f)).toInt().coerceAtLeast(2)
                    val labelStep = (maxDataPoints / maxLabels).coerceAtLeast(1)

                    for (index in 0 until maxDataPoints) {
                        if (index % labelStep != 0 && index != maxDataPoints - 1) continue

                        val xPosition =
                            yAxisX + (index.toFloat() / (maxDataPoints - 1).coerceAtLeast(1)) * graphWidth

                        drawContext.canvas.nativeCanvas.drawText(
                            "${index + 1}",
                            xPosition,
                            xAxisY + baseTextSize * 1.3f,
                            Paint().apply {
                                color = Silver.toArgb()
                                textSize = baseTextSize * 0.9f
                                textAlign = Paint.Align.CENTER
                                isAntiAlias = true
                            }
                        )
                    }
                }

                // Draw every line, all positioned against maxDataPoints so
                // series of different lengths still share the same X scale
                dataValues.forEachIndexed { dataIndex, (_, values) ->
                    if (values.isEmpty()) return@forEachIndexed

                    val points = values.mapIndexed { index, value ->
                        val x =
                            yAxisX + (index.toFloat() / (maxDataPoints - 1).coerceAtLeast(1)) * graphWidth
                        val y =
                            xAxisY - ((value - minValue) / range) * graphHeight
                        Offset(x, y)
                    }

                    // Draw Line
                    if (points.size >= 2) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path,
                            color = lineColors[dataIndex],
                            style = Stroke(width = 4f, cap = StrokeCap.Round)
                        )
                    }

                    // Draw Dots
                    points.forEach { point ->
                        drawCircle(color = dotColors[dataIndex], radius = 8f, center = point)
                        drawCircle(color = lineColors[dataIndex], radius = 4f, center = point)
                    }
                }
            }

            // Legend
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.8f)
                    .horizontalScroll(rememberScrollState())
            ) {
                dataValues.forEachIndexed { index, (name, _) ->
                    if (index > 0) Spacer(Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(lineColors[index], CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            name,
                            color = Color(0xFFBFBFBF),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MultiLineGraph(
    title: String,
    dataMap: Map<String, List<Float>> // KVP: Key = Dataset Name, Value = Data Points
) {
    // Convert Map to List<Pair<String, List<Float>>> for reuse
    val convertedData = dataMap.map { it.key to it.value }
    MultiLineGraph(
        title = title,
        dataValues = convertedData
    )
}

/** Formats Y-axis values without unnecessary decimal noise. */
private fun formatLabel(value: Float): String {
    return if (value == value.toInt().toFloat()) {
        value.toInt().toString()
    } else {
        String.format("%.2f", value)
    }
}
package skynetbee.developers.production

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min

@Composable
fun OneLineGraph(
    title: String,
    dataValues: Map<String, List<Float>>,
    xAxisValues: List<String>,
    yAxisValues: List<Float>
) {

    val entry = dataValues.entries.firstOrNull()

    if (entry == null) {
        Column(
            modifier = Modifier
                .fillMaxWidth(.95f)
                .backgroundCard(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = title,
                color = Color(0xFFFFD277),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            Text("No Data Available", color = Color.Gray)
        }
        return
    }

    val shareName = entry.key
    val values = entry.value

    // ── VALIDATION ────────────────────────────────────────────────────
    // Only the X-axis labels must line up with the data points.
    // The Y-axis label count is completely independent — you can pass
    // 5 Y labels for 10 data points (or 20 for 3) and the graph will
    // still calculate the correct position for every point.
    if (values.isEmpty() || values.size != xAxisValues.size) {

        Column(
            modifier = Modifier
                .fillMaxWidth(.95f)
                .backgroundCard(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = title,
                color = Color(0xFFFFD277),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = """
                    Data mismatch detected
                    Data values: ${values.size}
                    X-axis values: ${xAxisValues.size}
                    
                    """.trimIndent(),
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }

        return
    }

    val axisColor = Color(0xFF868686)
    val lineColor = Color(0xFF6200EE)
    val dotColor = Color(0xFF03DAC6)

    // ── SCALE CALCULATION ───────────────────────────────────────────────
    // The old version scaled points against maxY only, which broke as
    // soon as values didn't start at 0 or went negative, and it required
    // yAxisValues.size == values.size to even get this far. Now the scale
    // is derived from the real min/max of the data (and stretched further
    // if the supplied Y labels fall outside that range), so it works for
    // negative values and for any number of Y labels.
    val dataMin = values.min()
    val dataMax = values.max()
    val labelMin = yAxisValues.minOrNull() ?: dataMin
    val labelMax = yAxisValues.maxOrNull() ?: dataMax

    var minY = min(dataMin, labelMin)
    var maxY = max(dataMax, labelMax)

    // Avoid a divide-by-zero when every value (and every label) is identical
    if (maxY - minY == 0f) {
        val pad = if (maxY == 0f) 1f else kotlin.math.abs(maxY) * 0.1f
        minY -= pad
        maxY += pad
    }

    // Y labels to actually draw: use whatever was passed in, or fall back
    // to 5 evenly spaced ticks across the calculated range
    val yLabelsToDraw = if (yAxisValues.isNotEmpty()) {
        yAxisValues
    } else {
        (0..4).map { i -> minY + (maxY - minY) * (i / 4f) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(.95f)
            .backgroundCard(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = title,
            color = Color(0xFFFFD277),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(12.dp)
        )

        // ── RESPONSIVE CANVAS ───────────────────────────────────────────
        // No fixed dp size anymore. The canvas fills the available width
        // and keeps a fixed aspect ratio, so it scales correctly on
        // phones, tablets, landscape orientation, and foldables.
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.3f)
                .padding(8.dp)
        ) {

            val width = size.width
            val height = size.height

            // Adaptive padding, proportional to the canvas size instead
            // of a fixed pixel value
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

            val textPaint = Paint().apply {
                color = Color.Gray.toArgb()
                textSize = baseTextSize
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            // AXES
            drawLine(axisColor, Offset(yAxisX, graphTop), Offset(yAxisX, xAxisY), 2f)
            drawLine(axisColor, Offset(yAxisX, xAxisY), Offset(graphRight, xAxisY), 2f)

            // Y GRID + LABELS — positioned using the shared min/max scale,
            // regardless of how many Y labels were supplied
            yLabelsToDraw.forEach { value ->
                val clamped = value.coerceIn(minY, maxY)
                val y = xAxisY - ((clamped - minY) / (maxY - minY)) * graphHeight

                drawLine(
                    axisColor.copy(alpha = 0.2f),
                    Offset(yAxisX, y),
                    Offset(graphRight, y),
                    1.5f
                )

                drawContext.canvas.nativeCanvas.drawText(
                    formatLabel(value),
                    yAxisX - 12f,
                    y + baseTextSize * 0.35f,
                    textPaint.apply { textAlign = Paint.Align.RIGHT }
                )
            }

            // POINTS — positioned using the same min/max scale as the labels
            val points = values.mapIndexed { index, value ->

                val x =
                    yAxisX + (index.toFloat() / (values.size - 1).coerceAtLeast(1)) * graphWidth

                val y =
                    xAxisY - ((value - minY) / (maxY - minY)) * graphHeight

                Offset(x, y)
            }

            // LINE
            if (points.size > 1) {

                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(4f, cap = StrokeCap.Round)
                )
            }

            // DOTS
            points.forEach {
                drawCircle(dotColor, 8f, it)
                drawCircle(lineColor, 4f, it)
            }

            // X AXIS LABELS — skip some if there isn't room, so labels
            // never overlap on narrow screens
            val maxLabels = (graphWidth / (baseTextSize * 4f)).toInt().coerceAtLeast(2)
            val labelStep = (xAxisValues.size / maxLabels).coerceAtLeast(1)

            xAxisValues.forEachIndexed { i, label ->

                if (i % labelStep != 0 && i != xAxisValues.lastIndex) return@forEachIndexed

                val x =
                    yAxisX + (i.toFloat() / (values.size - 1).coerceAtLeast(1)) * graphWidth

                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x,
                    xAxisY + baseTextSize * 1.3f,
                    textPaint.apply { textAlign = Paint.Align.CENTER }
                )
            }
        }

        // LEGEND
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(lineColor, CircleShape)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = shareName,
                color = Color(0xFFBFBFBF)
            )
        }
    }
}

/** Formats Y-axis values without unnecessary decimal noise. */
private fun formatLabel(value: Float): String {
    return if (value == value.toInt().toFloat()) {
        value.toInt().toString()
    } else {
        String.format("%.2f", value)
    }
}
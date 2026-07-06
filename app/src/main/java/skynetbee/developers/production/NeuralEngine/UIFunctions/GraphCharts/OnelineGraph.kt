//package skynetbee.developers.production
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.*
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.nativeCanvas
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.graphics.toArgb
//
//@Composable
//fun OneLineGraph(
//    title: String,
//    dataValues: Map<String, List<Float>>,
//    xAxisValues: List<String>,
//    yAxisValues: List<Float>
//) {
//
//    val entry = dataValues.entries.firstOrNull()
//
//    if (entry == null) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth(.95f)
//                .backgroundCard(),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//
//            Text(
//                text = title,
//                color = Color(0xFFFFD277),
//                style = MaterialTheme.typography.titleLarge,
//                modifier = Modifier.padding(16.dp)
//            )
//
//            Text("No Data Available", color = Color.Gray)
//        }
//        return
//    }
//
//    val shareName = entry.key
//    val values = entry.value
//
//    // STRICT DATA VALIDATION
//    if (values.size != xAxisValues.size || values.size != yAxisValues.size) {
//
//        Column(
//            modifier = Modifier
//                .fillMaxWidth(.95f)
//                .backgroundCard(),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//
//            Text(
//                text = title,
//                color = Color(0xFFFFD277),
//                style = MaterialTheme.typography.titleLarge,
//                modifier = Modifier.padding(16.dp)
//            )
//
//            Text(
//                text = """
//Data mismatch detected
//
//Data values: ${values.size}
//X-axis values: ${xAxisValues.size}
//Y-axis values: ${yAxisValues.size}
//
//""".trimIndent(),
//                color = Color.Red,
//                modifier = Modifier.padding(16.dp)
//            )
//        }
//
//        return
//    }
//
//    val axisColor = Color(0xFF868686)
//    val lineColor = Color(0xFF6200EE)
//    val dotColor = Color(0xFF03DAC6)
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth(.95f)
//            .height(415.dp)
//            .backgroundCard(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        Text(
//            text = title,
//            color = Color(0xFFFFD277),
//            style = MaterialTheme.typography.titleLarge,
//            modifier = Modifier.padding(12.dp)
//        )
//
//        Canvas(modifier = Modifier.size(300.dp)) {
//
//            val padding = 50f
//            val width = size.width
//            val height = size.height
//
//            val graphWidth = width - padding
//            val graphHeight = height - padding * 2
//
//            val xAxisY = height - padding
//            val yAxisX = padding
//
//            val textPaint = Paint().apply {
//                color = Color.Gray.toArgb()
//                textSize = 26f
//                textAlign = Paint.Align.CENTER
//            }
//
//            // AXES
//            drawLine(axisColor, Offset(yAxisX, padding), Offset(yAxisX, xAxisY), 2f)
//            drawLine(axisColor, Offset(yAxisX, xAxisY), Offset(width, xAxisY), 2f)
//
//            val maxY = yAxisValues.maxOrNull() ?: 100f
//
//            // Y GRID + LABELS
//            yAxisValues.forEach { value ->
//
//                val y = xAxisY - (value / maxY) * graphHeight
//
//                drawLine(
//                    axisColor.copy(alpha = 0.2f),
//                    Offset(yAxisX, y),
//                    Offset(width, y),
//                    1.5f
//                )
//
//                drawContext.canvas.nativeCanvas.drawText(
//                    value.toInt().toString(),
//                    yAxisX - 25f,
//                    y + 8f,
//                    textPaint.apply { textAlign = Paint.Align.RIGHT }
//                )
//            }
//
//            // POINTS
//            val points = values.mapIndexed { index, value ->
//
//                val x =
//                    yAxisX + (index.toFloat() / (values.size - 1).coerceAtLeast(1)) * graphWidth
//
//                val y =
//                    xAxisY - (value / maxY) * graphHeight
//
//                Offset(x, y)
//            }
//
//            // LINE
//            if (points.size > 1) {
//
//                val path = Path().apply {
//                    moveTo(points.first().x, points.first().y)
//                    points.drop(1).forEach { lineTo(it.x, it.y) }
//                }
//
//                drawPath(
//                    path = path,
//                    color = lineColor,
//                    style = Stroke(4f, cap = StrokeCap.Round)
//                )
//            }
//
//            // DOTS
//            points.forEach {
//                drawCircle(dotColor, 8f, it)
//                drawCircle(lineColor, 4f, it)
//            }
//
//            // X AXIS LABELS
//            xAxisValues.forEachIndexed { i, label ->
//
//                val x =
//                    yAxisX + (i.toFloat() / (values.size - 1).coerceAtLeast(1)) * graphWidth
//
//                drawContext.canvas.nativeCanvas.drawText(
//                    label,
//                    x,
//                    xAxisY + 35f,
//                    textPaint.apply { textAlign = Paint.Align.CENTER }
//                )
//            }
//        }
//
//        // LEGEND
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.padding(top = 8.dp)
//        ) {
//
//            Box(
//                modifier = Modifier
//                    .size(14.dp)
//                    .background(lineColor, CircleShape)
//            )
//
//            Spacer(Modifier.width(8.dp))
//
//            Text(
//                text = shareName,
//                color = Color(0xFFBFBFBF)
//            )
//        }
//    }
//}
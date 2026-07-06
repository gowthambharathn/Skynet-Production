//package skynetbee.developers.production.NeuralEngine.UIFunctions.Utils.background
//
//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.size
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.unit.dp
//import kotlin.math.cos
//import kotlin.math.sin
//
//
///**
// * Created by Gowtham Barath
// * Date: 25-06-2026
// */
//@Composable
//fun Background() {
//    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
//    val time by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 2f * Math.PI.toFloat(),
//        animationSpec = infiniteRepeatable(
//            animation = tween(10000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ),
//        label = "time"
//    )
//
//    val configuration = LocalConfiguration.current
//    val screenWidth = configuration.screenWidthDp.dp
//    val screenHeight = configuration.screenHeightDp.dp
//    val isPad = configuration.screenWidthDp >= 600
//    val circleSize = if (isPad) 600.dp else 400.dp
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(28, 16, 62)) // Dark navy base
//    ) {
//        val colors = listOf(
//            Color(80, 120, 255), // Blue
//            Color(255, 0, 255)   // Magenta
//        )
//
//        colors.forEachIndexed { index, color ->
//            // Calculate movement offsets
//            val offsetX = (sin(time + index * 2) * (screenWidth.value * 0.3f)).dp
//            val offsetY = (cos(time + index * 2) * (screenHeight.value * 0.2f)).dp
//
//            Box(
//                modifier = Modifier
//                    .size(circleSize)
//                    .offset(x = offsetX, y = offsetY)
//                    .align(Alignment.Center)
//                    .background(
//                        Brush.radialGradient(
//                            0.0f to color.copy(alpha = 0.6f),
//                            1.0f to Color.Transparent
//                        )
//                    )
//            )
//        }
//
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    Brush.verticalGradient(
//                        listOf(Color.Black.copy(0.8f), Color.Black.copy(0.3f), Color.Transparent)
//                    )
//                )
//        )
//    }
//}
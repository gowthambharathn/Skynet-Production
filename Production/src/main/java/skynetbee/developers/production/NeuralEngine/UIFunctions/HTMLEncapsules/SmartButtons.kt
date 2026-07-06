//package skynetbee.developers.production
//
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.defaultMinSize
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.LocalContentColor
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.CompositionLocalProvider
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Shape
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun SmartButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    type: String = "black",
//    shape: Shape = RoundedCornerShape(10.dp),
//    content: @Composable () -> Unit
//) {
//    val gradientOffset = remember { Animatable(0f) }
//    var isAnimating by remember { mutableStateOf(false) }
//
//
//    val backgroundColor = remember(type) {
//        when (type.lowercase()) {
//            "red" -> Color(0xAA6F0404)
//            "green" -> Color(0xAA0B3D0B)
//            else -> Color(0xD6000000)
//        }
//    }
//
//    LaunchedEffect(isAnimating) {
//        if (isAnimating) {
//            gradientOffset.snapTo(-1f)
//            gradientOffset.animateTo(
//                targetValue = 0f,
//                animationSpec = tween(durationMillis = 800, easing = LinearEasing)
//            )
//            isAnimating = false
//        }
//    }
//
//    val shift = gradientOffset.value
//    val goldColors = listOf(Color(0xFF77530A), Color(0xFFFFD277), Color(0xFF77530A), Color(0xFFFFD277), Color(0xFF77530A))
//
//    val animatedBrush = Brush.linearGradient(
//        colors = goldColors,
//        start = Offset(shift * 800f, shift * 800f),
//        end = Offset(shift * 800f + 800f, shift * 800f + 800f)
//    )
//
//    val borderBrush = remember {
//        Brush.horizontalGradient(
//            listOf(Color(0xFFB8860B), Color(0xFFFFD700), Color(0xFFB8860B))
//        )
//    }
//
//    Box(
//        modifier = modifier
//            .defaultMinSize(minHeight = 50.dp, minWidth = 160.dp)
//            .clip(shape)
//            .background(animatedBrush)
//            .border(2.dp, borderBrush, shape)
//            .clickable(
//                indication = null,
//                interactionSource = remember { MutableInteractionSource() }
//            ) {
//                if (!isAnimating) {
//                    isAnimating = true
//                    onClick()
//                }
//            }
//    ) {
//        Box(
//            modifier = Modifier
//                .matchParentSize()
//                .background(backgroundColor),
//            contentAlignment = Alignment.Center
//        ) {
//            CompositionLocalProvider(LocalContentColor provides Color(0xFFFFD700)) {
//                content()
//            }
//        }
//    }
//}
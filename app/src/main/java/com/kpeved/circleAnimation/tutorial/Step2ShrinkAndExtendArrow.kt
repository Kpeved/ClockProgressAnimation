package com.kpeved.circleAnimation.tutorial

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kpeved.circleAnimation.ui.theme.CircleAnimationTheme


@Preview(showBackground = true)
@Composable
fun Step2ShrinkAndExtendArrowPreview() {
    CircleAnimationTheme {
        val size = 300.dp
        Box(
            modifier = Modifier
                .width(size)
                .height(size)
                .background(Color.Black)
        ) {
            Step2ShrinkAndExtendArrow(duration = 6000)
        }
    }
}

@Composable
fun Step2ShrinkAndExtendArrow(duration: Int) {
    val infiniteTransition = rememberInfiniteTransition()
    // Create an infinite animation
    val animationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 720f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val currentHour by remember(animationAngle) {
        derivedStateOf { animationAngle.toInt() / 30 }
    }
    var strokeWidth by remember { mutableStateOf(0f) }

    Spacer(modifier = Modifier
        .fillMaxSize()
        // Set strokeWidth based on the size of the viewport
        .onGloballyPositioned {
            strokeWidth = (it.size.width / 24).toFloat()
        }
        .drawBehind {
            val stepHeight = size.height / 24

            val center = Offset(size.width / 2, size.height / 2)
            val endOffset = Offset(
                size.width / 2,
                size.height / 2 -
                        calculateClockHandLength(stepHeight, currentHour)
            )
            // Rotate for 0 to 720 degrees the line around the pivot point, which is the
            // center of the screen
            rotate(animationAngle, pivot = center) {
                drawLine(
                    color = Color.White,
                    start = center,
                    end = endOffset,
                    strokeWidth = strokeWidth,
                )
            }
        }
    )
}

private fun calculateClockHandLength(stepHeight: Float, currentHour: Int): Float {
    // Height decreases first 360 deg, then increases again
    return stepHeight * if (currentHour < 12) {
        12 - 1 - currentHour
    } else {
        currentHour - 12
    }
}

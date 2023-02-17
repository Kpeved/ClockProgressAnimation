package com.kpeved.circleAnimation.tutorial

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.runtime.LaunchedEffect
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
fun Step4AssembleAnimationPreview() {
    CircleAnimationTheme {
        val size = 300.dp
        Box(
            modifier = Modifier
                .width(size)
                .height(size)
                .background(Color.Black)
        ) {
            Step4AssembleAnimation(duration = 6000)
        }
    }
}

@Composable
fun Step4AssembleAnimation(duration: Int) {
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

    val hours = remember { List(12) { it } }
    val dotsVisibility = remember(currentHour) {
        hours.map { index ->
            when {
                index > currentHour -> false
                index > currentHour - 12 -> true
                else -> false
            }
        }
    }

    val assembleAnim = remember { Animatable(-1f) }

    LaunchedEffect(currentHour) {
        assembleAnim.snapTo(0f)
        assembleAnim.animateTo(
            1f,
            tween(50 * (24 - currentHour), easing = LinearOutSlowInEasing)
        )
    }

    var strokeWidth by remember { mutableStateOf(0f) }

    Spacer(modifier = Modifier
        .fillMaxSize()
        // Set strokeWidth based on the size of the viewport
        .onGloballyPositioned {
            strokeWidth = (it.size.width / 24).toFloat()
        }
        .drawBehind {
            val halfStroke: Float = strokeWidth / 2
            val stepHeight = size.height / 24

            val center = Offset(size.width / 2, size.height / 2)
            val endOffset = Offset(
                size.width / 2,
                size.height / 2 -
                        calculateArrowHeight(stepHeight, currentHour)
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

                if (assembleAnim.value != -1f) {
                    val positionY = halfStroke +
                            calculateAssembleDistance(stepHeight, currentHour) *
                            assembleAnim.value

                    val start = Offset(size.width / 2, positionY - halfStroke)
                    val end = Offset(size.width / 2, positionY + halfStroke)
                    drawLine(
                        color = Color.White,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth
                    )
                }

            }

            hours.forEach {
                if (!dotsVisibility[it]) return@forEach
                val degree = it * 30f
                rotate(degree) {
                    val start = Offset(size.width / 2, 0f)
                    val end = Offset(size.width / 2, strokeWidth)
                    drawLine(
                        color = Color.White,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth,
                    )
                }
            }
        }
    )
}

private fun calculateArrowHeight(stepHeight: Float, currentHour: Int): Float {
    // Height decreases first 360 deg, then increases again
    return stepHeight * if (currentHour < 12) {
        12 - 1 - currentHour
    } else {
        currentHour - 12
    }
}

private fun calculateAssembleDistance(stepHeight: Float, currentHour: Int): Float {
    val fixedHour = 24 - currentHour - 1
    return stepHeight * fixedHour
}

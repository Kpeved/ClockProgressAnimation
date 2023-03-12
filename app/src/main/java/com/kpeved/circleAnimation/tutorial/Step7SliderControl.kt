package com.kpeved.circleAnimation.tutorial

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
fun Step7SliderControlPreview() {
    CircleAnimationTheme {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val size = 300.dp
            var progress by remember { mutableStateOf(0f) }
            var animationAngle by remember { mutableStateOf(0f) }
            Box(
                modifier = Modifier
                    .size(size)
                    .background(Color.Black)
            ) {
                Step7SliderControl(animationAngle)
            }
            Text("Control animation with a slider!")
            Slider(
                modifier = Modifier.padding(16.dp),
                value = progress,
                onValueChange = {
                    progress = it
                    animationAngle = it * 720f
                }
            )
        }
    }
}

@Composable
fun Step7SliderControl(animationAngle: Float) {

    var currentHour by remember { mutableStateOf(0) }

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

    val assembleValue = remember(animationAngle) {
        if (animationAngle >= 360) {
            (animationAngle % 30) / 30
        } else -1f
    }
    val dotsPositions = remember(animationAngle) {
        List(12) { currentDot ->
            angleToFraction(
                angle = animationAngle,
                startAngle = currentDot * 30f,
                degreeLimit = 60f,
                easing = LinearOutSlowInEasing
            )
        }
    }

    LaunchedEffect(animationAngle) {
        val newCurrentHour = animationAngle.toInt() / 30
        if (newCurrentHour != currentHour) {
            currentHour = newCurrentHour
        }
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

                if (assembleValue != -1f) {
                    val positionY = halfStroke +
                            calculateAssembleDistance(stepHeight, currentHour) *
                            assembleValue

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
                    val positionY = halfStroke +
                            stepHeight * it *
                            (1 - dotsPositions[it])

                    val start = Offset(size.width / 2, positionY - halfStroke)
                    val end = Offset(size.width / 2, positionY + halfStroke)
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

private fun calculateClockHandLength(stepHeight: Float, currentHour: Int): Float {
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

// Returns fraction from 0 to 1
@Suppress("SameParameterValue")
private fun angleToFraction(
    angle: Float,
    startAngle: Float,
    degreeLimit: Float,
    easing: Easing
): Float {
    val currentDeg: Float = (angle - startAngle).coerceIn(0f, degreeLimit)
    // Progress from 0 to 1
    val progressFraction: Float = currentDeg / degreeLimit
    return easing.transform(progressFraction)
}

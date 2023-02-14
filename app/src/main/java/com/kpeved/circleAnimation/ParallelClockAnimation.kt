package com.kpeved.circleAnimation

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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun ParallelClockPreview() {
    CircleAnimationTheme {
        val size = 300.dp
        Box(
            modifier = Modifier
                .width(size)
                .height(size)
                .background(Color.Black)
        ) {
            ClockAnimation(duration = 20000)
        }
    }
}

@Composable
fun ClockAnimation(duration: Int) {
    // Step 1. Set infinite animation
    val infiniteTransition = rememberInfiniteTransition()
    var strokeWidth by remember { mutableStateOf(0f) }

    // Creates a child animation of float type as a part of the [InfiniteTransition].
    val clockAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 720f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    var currentHour by remember { mutableStateOf(0) }
    // Our clock is 24h. First 12h arrow decreases, next 12h arrow increases
    val hoursPositions = remember { List(12) { it } }

    val assembleAnim = remember { Animatable(-1f) }

    val disassembleAnimations =
        remember { hoursPositions.map { Animatable(-1f) } }

    val currentHourChannel = remember { Channel<Int>(capacity = Channel.CONFLATED) }
    val currentHourFlow = remember(currentHourChannel) { currentHourChannel.receiveAsFlow() }
    LaunchedEffect(clockAnimation) {
        val newCurrentHour = clockAnimation.toInt() / 30
        // WHY we have this - because LaunchedEffect is triggered after the hour is changed.
        // That triggers the redraw of assemble position faster than the new animation started,
        // which results in a position jump
        // So with changing hour we have to instantly reset animation
        if (newCurrentHour != currentHour) {
            currentHour = newCurrentHour
            currentHourChannel.trySend(currentHour)
            assembleAnim.snapTo(-1f)
        }
    }
    LaunchedEffect(currentHourFlow) {
        currentHourFlow.collectLatest {
            launch {
                if (currentHour < 12) {
                    assembleAnim.snapTo(-1f)
                    disassembleAnimations[currentHour].snapTo(0f)
                    disassembleAnimations[currentHour].animateTo(
                        1f,
                        tween(50 * currentHour / 2, easing = LinearOutSlowInEasing)
                    )
                } else {
                    disassembleAnimations[currentHour - 12].snapTo(-1f)
                    assembleAnim.snapTo(0.1f)
                    assembleAnim.animateTo(
                        1f,
                        tween(50 * (24 - currentHour), easing = LinearOutSlowInEasing)
                    )
                }
            }
        }
    }

    //Step 2 -  create a spacer for animation
    Spacer(modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned {
            strokeWidth = (it.size.width / 24).toFloat()
        }
        .drawBehind {
            val halfStroke = strokeWidth / 2

            // Step 3 - set center
            val center = Offset(size.width / 2, size.height / 2)
            val endOffset = Offset(
                size.width / 2,
                size.height / 2 - calculateHeight(size.height / 2, currentHour)
            )
            rotate(clockAnimation, pivot = center) {
                drawLine(
                    color = Color.White,
                    start = center,
                    end = endOffset,
                    strokeWidth = strokeWidth,
                )
                if (assembleAnim.value != -1f) {
                    val positionY = halfStroke +
                            calculateAssembleDistance(size.height / 2, currentHour) *
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

            hoursPositions.forEach {
                if (disassembleAnimations[it].value == -1f) return@forEach

                val degree = it * 30f
                rotate(degree) {
                    val positionY = halfStroke +
                            calculateDisassembleDistance(size.height / 2, currentHour) *
                            (1 - disassembleAnimations[it].value)

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

// Step 4 try to decrease length each 30 deg by 1 item
private fun calculateHeight(maxHeight: Float, currentHour: Int): Float {
    val stepHeight = maxHeight / 12
    // Height decreases first 360 deg, then increases again

    return stepHeight * if (currentHour < 12) {
        12 - 1 - currentHour
    } else {
        currentHour - 12
    }
}

private fun calculateAssembleDistance(maxHeight: Float, currentHour: Int): Float {
    val stepHeight = maxHeight / 12
    val fixedHour = 24 - currentHour - 1
    return stepHeight * fixedHour
}

private fun calculateDisassembleDistance(maxHeight: Float, currentHour: Int): Float {
    val stepHeight = maxHeight / 12
    return stepHeight * currentHour
}

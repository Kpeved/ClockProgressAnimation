package com.kpeved.circleAnimation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kpeved.circleAnimation.ui.theme.CircleAnimationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CircleAnimationTheme {
                // A surface container using the 'background' color from the theme
                var showParallel by remember { mutableStateOf(true) }
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (showParallel) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.Black)
                            ) {
                                ClockAnimation(5000)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.Black)
                            ) {
                                SingleClockAnimation(duration = 5000)
                            }

                            var progress by remember { mutableStateOf(0f) }
                            var animationAngle by remember { mutableStateOf(0f) }
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .background(Color.Black)
                            ) {
                                SingleClockAnimationProgress(animationAngle)
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

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showParallel = !showParallel },
                        ) {
                            Text(if (showParallel) "Show parallel animations" else "Show single animation")
                        }
                    }
                }
            }
        }
    }
}

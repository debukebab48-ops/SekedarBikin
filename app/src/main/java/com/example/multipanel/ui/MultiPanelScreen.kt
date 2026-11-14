package com.example.multipanel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun MultiPanelScreen() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()

    // Bobot ukuran panel
    var leftWeight by rememberSaveable { mutableFloatStateOf(0.4f) }
    var topRightWeight by rememberSaveable { mutableFloatStateOf(0.6f) }
    var bottomRightTopWeight by rememberSaveable { mutableFloatStateOf(0.7f) }

    // Warna Divider & Background
    val dividerColor = Color(0xFF333333)
    val bgColor1 = Color(0xFF1E1E1E) // Gelap
    val bgColor2 = Color(0xFF252526) // Agak terang

    // Root Layout pakai fillMaxSize & background biar full satu layar
    Row(Modifier.fillMaxSize().background(bgColor1)) {
        // 1. AI Panel (Kiri)
        Box(Modifier.weight(leftWeight).fillMaxHeight().background(bgColor2)) {
            Text("1. AI Panel", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }

        // Garis Geser Vertikal (Kiri - Kanan) - Lebih tipis (4.dp)
        Box(
            Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(dividerColor)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = dragAmount.x / screenWidth
                        leftWeight = (leftWeight + delta).coerceIn(0.15f, 0.85f)
                    }
                }
        )

        // Kolom Kanan
        Column(Modifier.weight(1f - leftWeight).fillMaxHeight()) {
            // 3. Editor Panel (Kanan Atas)
            Box(Modifier.weight(topRightWeight).fillMaxWidth().background(bgColor1)) {
                Text("3. Editor Panel", color = Color.White, modifier = Modifier.align(Alignment.Center))
            }

            // Garis Geser Horizontal 1 - Lebih tipis (4.dp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(dividerColor)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val delta = dragAmount.y / screenHeight
                            topRightWeight = (topRightWeight + delta).coerceIn(0.2f, 0.8f)
                        }
                    }
            )

            // Kolom Kanan Bawah
            Column(Modifier.weight(1f - topRightWeight)) {
                // 2. File Manager (Kanan Tengah)
                Box(Modifier.weight(bottomRightTopWeight).fillMaxWidth().background(bgColor2)) {
                    Text("2. File Manager", color = Color.White, modifier = Modifier.align(Alignment.Center))
                }

                // Garis Geser Horizontal 2 - Lebih tipis (4.dp)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(dividerColor)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val delta = dragAmount.y / (screenHeight * (1f - topRightWeight))
                                bottomRightTopWeight = (bottomRightTopWeight + delta).coerceIn(0.1f, 0.9f)
                            }
                        }
                )

                // 4. Log Panel (Kanan Bawah)
                Box(Modifier.weight(1f - bottomRightTopWeight).fillMaxWidth().background(bgColor1)) {
                    Text("4. Log Panel", color = Color.White, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

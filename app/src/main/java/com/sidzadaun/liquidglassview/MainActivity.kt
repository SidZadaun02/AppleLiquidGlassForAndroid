package com.sidzadaun.liquidglassview

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sidzadaun.liquidglass.LiquidGlassView
import com.sidzadaun.liquidglassview.ui.theme.LiquidGlassViewTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiquidGlassViewTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LiquidGlassDemo(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LiquidGlassDemo(modifier: Modifier = Modifier) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
        // Rich Background Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Liquid Glass Demo",
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                color = Color.Black
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.size(80.dp).background(Color.Red, RoundedCornerShape(12.dp)))
                Box(modifier = Modifier.size(80.dp).background(Color.Blue, RoundedCornerShape(12.dp)))
                Box(modifier = Modifier.size(80.dp).background(Color.Green, RoundedCornerShape(12.dp)))
            }

            Text(
                text = "Drag the glass card over these items to see the blur effect.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray
            )

            repeat(5) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(Color.Magenta, CircleShape))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("List Item $it", color = Color.Black)
                }
            }
            
            // Large image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF6200EE), Color(0xFF03DAC5))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Text("Gradient Card", color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
        }

        // Draggable Liquid Glass View
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            AndroidView(
                factory = { context ->
                    LiquidGlassView(context).apply {
                        setupWithActivityRoot()
                        setRadius(15f)
                        setZoom(1.2f)
                        setGlassColor(android.graphics.Color.parseColor("#20FFFFFF")) // More transparent
                        
                        val textView = TextView(context).apply {
                            text = "Drag Me"
                            setTextColor(android.graphics.Color.BLACK)
                            textSize = 20f
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = android.view.Gravity.CENTER
                            }
                        }
                        addView(textView)
                    }
                },
                update = { view ->
                    // Force redraw when position changes
                    // Reading offsetX/offsetY here ensures this block runs on state change
                    val x = offsetX
                    val y = offsetY
                    view.invalidate()
                },
                modifier = Modifier.size(250.dp, 150.dp)
            )
        }
    }
}
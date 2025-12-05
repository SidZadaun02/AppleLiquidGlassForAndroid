package com.sidzadaun.liquidglass

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    blurRadius: Float = 20f,
    overlayColor: Color = Color.White.copy(alpha = 0.22f),
    cornerRadius: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val shape = RoundedCornerShape(cornerRadius)

    // Note: This implementation blurs the content of the Box itself.
    // For true glassmorphism (background blur), Android Compose support is limited without libraries like Haze.
    // This is a basic implementation using RenderEffect on Android 12+.
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(overlayColor)
                .blur(0.dp) // Blur removed as it blurs content. Needs Haze or similar for background blur.
        ) {
            content()
        }
    } else {
        // Fallback for older versions
        Box(
            modifier = modifier
                .clip(shape)
                .background(overlayColor)
        ) {
            content()
        }
    }
}

package com.sidzadaun.liquidglass

import android.graphics.Outline
import android.graphics.Rect
import android.view.View
import android.view.ViewOutlineProvider

class ViewOutlineProviderRounded(private val radius: Float) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        val rect = Rect(0, 0, view.width, view.height)
        outline.setRoundRect(rect, radius)
    }
}

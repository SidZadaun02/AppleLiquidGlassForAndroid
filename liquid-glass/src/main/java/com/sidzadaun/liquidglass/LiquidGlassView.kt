package com.sidzadaun.liquidglass

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout

class LiquidGlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var blurRadius = 15f 
    private var downsampleFactor = 0.12f 
    private var zoomLevel = 1.2f // Increased zoom for more obvious effect
    private var overlayColor = 0x15FFFFFF.toInt() // More transparent to see the effect better
    private var borderColor = 0x60FFFFFF.toInt()
    private var cornerRadius = 0f
    
    private var rootView: View? = null
    private var backgroundBitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f // Slightly thicker for visibility
    }
    private val clipPath = Path()
    private val rectF = RectF()

    init {
        setWillNotDraw(false)
        clipToPadding = false
        clipToOutline = true 
        
        val a = context.obtainStyledAttributes(attrs, R.styleable.LiquidGlassView)
        blurRadius = a.getFloat(R.styleable.LiquidGlassView_lg_blurRadius, blurRadius)
        overlayColor = a.getColor(R.styleable.LiquidGlassView_lg_overlayColor, overlayColor)
        cornerRadius = a.getDimension(R.styleable.LiquidGlassView_lg_cornerRadius, cornerRadius)
        a.recycle()
        
        borderPaint.color = borderColor
        
        // Boost saturation to mimic Apple's vibrancy
        val colorMatrix = android.graphics.ColorMatrix()
        colorMatrix.setSaturation(1.4f)
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
    }

    fun setupWithRoot(root: View) {
        this.rootView = root
        // Capture initial frame
        root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (backgroundBitmap == null && root.width > 0 && root.height > 0) {
                    updateBackgroundSnapshot()
                    root.viewTreeObserver.removeOnPreDrawListener(this)
                }
                return true
            }
        })
    }

    fun setupWithActivityRoot() {
        if (context is Activity) {
            val decorView = (context as Activity).window.decorView
            setupWithRoot(decorView)
        }
    }
    
    fun updateBackgroundSnapshot() {
        val root = rootView ?: return
        if (root.width == 0 || root.height == 0) return
        
        try {
            // Create a downscaled bitmap for natural blur
            val scaledWidth = (root.width * downsampleFactor).toInt().coerceAtLeast(1)
            val scaledHeight = (root.height * downsampleFactor).toInt().coerceAtLeast(1)

            if (backgroundBitmap == null || backgroundBitmap?.width != scaledWidth || backgroundBitmap?.height != scaledHeight) {
                backgroundBitmap?.recycle()
                backgroundBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
            }
            
            val canvas = Canvas(backgroundBitmap!!)
            canvas.scale(downsampleFactor, downsampleFactor)
            
            val wasVisible = visibility
            visibility = View.INVISIBLE
            root.draw(canvas)
            visibility = wasVisible
            
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectF.set(0f, 0f, w.toFloat(), h.toFloat())
        clipPath.reset()
        clipPath.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
        
        if (cornerRadius > 0) {
            outlineProvider = ViewOutlineProviderRounded(cornerRadius)
            clipToOutline = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (rootView != null && backgroundBitmap != null) {
            val root = rootView!!
            
            val location = IntArray(2)
            getLocationOnScreen(location)
            val rootLocation = IntArray(2)
            root.getLocationOnScreen(rootLocation)
            
            val relativeX = (location[0] - rootLocation[0]).toFloat()
            val relativeY = (location[1] - rootLocation[1]).toFloat()

            canvas.save()
            canvas.clipPath(clipPath)
            
            // 1. Translate to align with root
            canvas.translate(-relativeX, -relativeY)
            
            // 2. Apply Zoom (scale around center of this view)
            val centerX = relativeX + width / 2f
            val centerY = relativeY + height / 2f
            canvas.scale(zoomLevel, zoomLevel, centerX, centerY)
            
            // 3. Scale UP the downsampled bitmap to fill the space
            val scaleUp = 1f / downsampleFactor
            canvas.scale(scaleUp, scaleUp)
            
            // Draw the blurred background
            // Use bilinear filtering for smooth blur
            paint.isFilterBitmap = true
            canvas.drawBitmap(backgroundBitmap!!, 0f, 0f, paint)
            
            canvas.restore()
        } else {
            val bgPaint = Paint().apply { color = 0xFFCCCCCC.toInt() }
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bgPaint)
        }

        // Overlay (Milky white tint)
        val overlayPaint = Paint().apply { color = overlayColor }
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, overlayPaint)

        // Specular Highlight / Border
        // Apple style: subtle white border, maybe slightly thicker on top/left for light source effect?
        // For now, uniform border.
        val inset = borderPaint.strokeWidth / 2
        val borderRect = RectF(inset, inset, width - inset, height - inset)
        canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint)
        
        super.onDraw(canvas)
    }

    fun setRadius(radius: Float) {
        // Radius controls downsample factor roughly
        // 20f -> 0.1f
        // 10f -> 0.2f
        this.blurRadius = radius
        this.downsampleFactor = (2f / radius).coerceIn(0.05f, 0.5f)
        updateBackgroundSnapshot()
    }

    fun setZoom(zoom: Float) {
        this.zoomLevel = zoom
        invalidate()
    }
    
    fun setGlassColor(color: Int) {
        this.overlayColor = color
        invalidate()
    }
}

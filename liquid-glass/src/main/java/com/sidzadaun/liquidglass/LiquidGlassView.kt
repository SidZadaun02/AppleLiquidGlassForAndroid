package com.sidzadaun.liquidglass

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.core.graphics.createBitmap
import androidx.core.content.withStyledAttributes

class LiquidGlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var blurRadius = 15f 
    private var downsampleFactor = 0.08f // Lower for smoother/stronger blur
    private var zoomLevel = 1.1f // Reduced zoom for less distortion, more 'frosted' look
    private var overlayColor = 0x24FFFFFF.toInt() // Slightly stronger milky overlay
    private var borderColor = 0x80FFFFFF.toInt() // Brighter, more visible border
    private var cornerRadius = 0f
    
    private var rootView: View? = null
    private var backgroundBitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f // Thinner, crisper border
    }
    private val clipPath = Path()
    private val rectF = RectF()

    init {
        setWillNotDraw(false)
        clipToPadding = false
        clipToOutline = true

        context.withStyledAttributes(attrs, R.styleable.LiquidGlassView) {
            blurRadius = getFloat(R.styleable.LiquidGlassView_lg_blurRadius, blurRadius)
            overlayColor = getColor(R.styleable.LiquidGlassView_lg_overlayColor, overlayColor)
            cornerRadius = getDimension(R.styleable.LiquidGlassView_lg_cornerRadius, cornerRadius)
        }
        
        borderPaint.color = borderColor
        
        // Boost saturation significantly to mimic the vibrant glass look
        val colorMatrix = android.graphics.ColorMatrix()
        colorMatrix.setSaturation(1.6f)
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
        
        // Add default elevation for 3D card effect
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
             elevation = 8f * context.resources.displayMetrics.density
        }
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
                backgroundBitmap = createBitmap(scaledWidth, scaledHeight)
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

        // Glass Effect Rendering (Frosted / Apple-like)
        
        // 1. Top Specular Highligh / Sheen (Light source from top)
        // Soft gradient providing that 'curved glass' look at the top
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        highlightPaint.shader = LinearGradient(
            0f, 0f, 0f, height * 0.4f,
            0x60FFFFFF.toInt(), 0x00FFFFFF.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, highlightPaint)

        // 2. Bottom Reflection / Caustic (Light exiting the glass)
        // Subtle glow at the bottom edge
        val bottomGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bottomGlowPaint.shader = LinearGradient(
            0f, height * 0.6f, 0f, height.toFloat(),
            0x00FFFFFF.toInt(), 0x30FFFFFF.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bottomGlowPaint)

        // 3. Crisp Rim Border
        // A nice, bright, thin border defines the shape
        val inset = borderPaint.strokeWidth / 2
        val borderRect = RectF(inset, inset, width - inset, height - inset)
        canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint)
        
        // 4. Inner "Elevation" Bevel (3D Ridge)
        // A gradient stroke from Top-Left (White) to Bottom-Right (Black)
        // This creates the illusion of thickness/elevation inside the card
        val bevelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f * resources.displayMetrics.density // thicker stroke for visible depth
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xA0FFFFFF.toInt(), 0x10FFFFFF.toInt(), 0x00000000.toInt(), 0x40000000.toInt()),
                floatArrayOf(0.0f, 0.2f, 0.8f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        
        // Inset slightly so it renders fully inside
        val bevelInset = bevelPaint.strokeWidth / 2f
        val bevelRect = RectF(bevelInset, bevelInset, width - bevelInset, height - bevelInset)
        canvas.drawRoundRect(bevelRect, cornerRadius, cornerRadius, bevelPaint)
        
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
    fun setCornerRadius(radius: Float) {
        this.cornerRadius = radius
        if (cornerRadius > 0) {
            outlineProvider = ViewOutlineProviderRounded(cornerRadius)
            clipToOutline = true
        } else {
            outlineProvider = null
            clipToOutline = false
        }
        invalidate()
    }
}

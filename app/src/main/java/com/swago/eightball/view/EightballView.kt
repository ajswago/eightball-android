package com.swago.eightball.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.swago.eightball.R
import android.text.StaticLayout
import android.os.Build
import android.text.Layout
import android.util.Log
import android.view.animation.LinearInterpolator
import java.lang.String.format


/**
 * TODO: document your custom view class.
 */
class EightballView : View {

    private var _text: String? = null // TODO: use a default from R.string...
    private var _textColor: Int = Color.RED // TODO: use a default from R.color...
    private var _textSize: Float = 0f // TODO: use a default from R.dimen...

    private var textPaint: TextPaint? = null
    private var textWidth: Float = 0f
    private var textHeight: Float = 0f
    private var staticLayout: StaticLayout? = null
    private var staticTextWidth: Float = 0f
    private var staticTextHeight: Float = 0f

    private var trianglePath: Path? = null
    private var trianglePaint: Paint? = null

    private var triangleTopLeftX: Float = 0f
    private var triangleTopLeftY: Float = 0f
    private var triangleTopRightX: Float = 0f
    private var triangleTopRightY: Float = 0f
    private var triangleBottomX: Float = 0f
    private var triangleBottomY: Float = 0f

    private var triangleFillColor: Int = Color.parseColor("#000771")
    private var triangleStrokeColor: Int = Color.parseColor("#213f94")
    private var trianglePct = 0.25f
    private var triangleAlpha = 1.0f

    /**
     * The text to draw
     */
    var text: String?
        get() = _text
        set(value) {
            _text = value
            invalidateTextPaintAndMeasurements()
            staticTextWidth = width * trianglePct * 0.5f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                staticLayout =
                    StaticLayout.Builder.obtain(
                        text,
                        0,
                        text?.length ?: 0,
                        textPaint,
                        staticTextWidth.toInt())
                        .setAlignment(Layout.Alignment.ALIGN_CENTER)
                        .setLineSpacing(0.0f, 1.0f)
                        .setIncludePad (false).build()
            } else {
                staticLayout = StaticLayout(
                    text,
                    textPaint,
                    staticTextWidth.toInt(),
                    Layout.Alignment.ALIGN_CENTER,
                    1.0f,
                    0.0f,
                    false
                )
            }
            staticTextHeight = staticLayout!!.height.toFloat()
            invalidate()
        }

    fun setText(value: String, animated: Boolean) {
        if (animated) {
            val oldText = text
            var newText = oldText
            val valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
            valueAnimator.addUpdateListener {
                val animatedValue = it.animatedValue as Float
                if (animatedValue < 0.3f) {
                    triangleAlpha = 1.0f - (animatedValue / 0.3f)
                } else if (animatedValue > 0.7f) {
                    triangleAlpha = 1.0f - ((1.0f - animatedValue) / 0.3f)
                    newText = value
                } else {
                    triangleAlpha = 0.0f
                }
                Log.d("ANIMATION", format("Value: %s, Alpha: %s", animatedValue, triangleAlpha))
                text = newText
            }
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.duration = 600
            valueAnimator.start()
        } else {
            triangleAlpha = 1.0f
            text = value
        }
    }

    /**
     * The font color
     */
    var textColor: Int
        get() = _textColor
        set(value) {
            _textColor = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * In the example view, this dimension is the font size.
     */
    var textSize: Float
        get() = _textSize
        set(value) {
            _textSize = value
            invalidateTextPaintAndMeasurements()
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.EightballView, defStyle, 0
        )

        _text = a.getString(
            R.styleable.EightballView_text
        )
        _textColor = a.getColor(
            R.styleable.EightballView_textColor,
            textColor
        )
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        _textSize = a.getDimension(
            R.styleable.EightballView_textSize,
            textSize
        )

        a.recycle()

        trianglePath = Path()

        trianglePaint = Paint().apply {
            strokeWidth = 8.0f
        }

        // Set up a default TextPaint object
        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
        }

        setBackgroundResource(R.drawable.magic_eight_ball)

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()
    }

    private fun invalidateTextPaintAndMeasurements() {
        textPaint?.let {
            it.textSize = textSize
            it.color = textColor
            it.alpha = (triangleAlpha * 255).toInt()
            textWidth = it.measureText(text)
            textHeight = it.fontMetrics.bottom
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val triangleSide = (width * trianglePct).toInt()
        val triangleAltitude = triangleSide * (Math.sqrt(3.0).toFloat() / 2.0f)

        text?.let {

            triangleTopLeftX = (width / 2.0f) - (triangleSide / 2.0f)
            triangleTopLeftY = (height / 2.0f) - (triangleAltitude / 3.0f)
            triangleTopRightX = (width / 2.0f) + (triangleSide / 2.0f)
            triangleTopRightY = (height / 2.0f) - (triangleAltitude / 3.0f)
            triangleBottomX = (width / 2.0f)
            triangleBottomY = (height / 2.0f) + (triangleAltitude * 2.0f / 3.0f)
            trianglePath?.moveTo(triangleTopLeftX, triangleTopLeftY)
            trianglePath?.lineTo(triangleTopRightX, triangleTopRightY)
            trianglePath?.lineTo(triangleBottomX, triangleBottomY)
            trianglePath?.lineTo(triangleTopLeftX, triangleTopLeftY)
            trianglePath?.close()
            trianglePaint?.color = triangleFillColor
            trianglePaint?.alpha = (triangleAlpha * 255).toInt()
            trianglePaint?.style = Paint.Style.FILL
            canvas.drawPath(trianglePath!!, trianglePaint!!)
            trianglePaint?.color = triangleStrokeColor
            trianglePaint?.alpha = (triangleAlpha * 255).toInt()
            trianglePaint?.style = Paint.Style.STROKE
            trianglePaint?.strokeJoin = Paint.Join.ROUND
            canvas.drawPath(trianglePath!!, trianglePaint!!)

            canvas.save();
            canvas.translate((width / 2) - (staticTextWidth / 2.0f), (height / 2) - (staticTextHeight / 2))
            staticLayout?.draw(canvas);
            canvas.restore();

        }
    }
}

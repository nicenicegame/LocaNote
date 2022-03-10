package com.tatpol.locationnoteapp.presentation.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout

class InfoWindowView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val cornerRadius = 100f
    private val offset = 30f
    private val square = 30f

    private val shadowPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        maskFilter = BlurMaskFilter(offset, BlurMaskFilter.Blur.OUTER)
        alpha = 50
    }

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    init {
        setWillNotDraw(false)
    }

    private val path = Path()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        path.apply {
            reset()
            moveTo(offset, cornerRadius + offset)
            arcTo(
                offset,
                offset,
                cornerRadius + offset,
                cornerRadius + offset,
                180f,
                90f,
                false
            )
            arcTo(
                width - offset - cornerRadius,
                offset,
                width - offset,
                offset + cornerRadius,
                270f,
                90f,
                false
            )
            lineTo(width - offset, height - offset)
            lineTo(width / 2f + square, height - offset)
            lineTo(width / 2f, height - offset + square)
            lineTo(width / 2f - square, height - offset)
            lineTo(offset, height - offset)
            close()
        }
        canvas?.let {
            it.drawPath(path, shadowPaint)
            it.drawPath(path, backgroundPaint)
        }
    }
}
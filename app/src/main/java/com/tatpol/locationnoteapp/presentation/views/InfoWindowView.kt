package com.tatpol.locationnoteapp.presentation.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.tatpol.locationnoteapp.databinding.CustomInfoWindowBinding

class InfoWindowView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var binding: CustomInfoWindowBinding

    private val cornerRadius = 50f
    private val offset = 40f
    private val square = 30f

    private val shadowPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        maskFilter = BlurMaskFilter(offset / 2, BlurMaskFilter.Blur.OUTER)
        alpha = 75
    }

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val path = Path()

    init {
        setWillNotDraw(false)
        binding = CustomInfoWindowBinding
            .inflate(
                LayoutInflater.from(context),
                this,
                true
            )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setupPath()
        canvas?.let {
            it.drawPath(path, shadowPaint)
            it.drawPath(path, backgroundPaint)
        }
    }

    private fun setupPath() {
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
    }

    fun setInfoWindowContent(title: String?, snippet: String?) {
        binding.tvTitle.text = title
        binding.tvSnippet.text = snippet
    }
}
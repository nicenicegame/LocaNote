package com.tatpol.locationnoteapp.presentation.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.setPadding
import com.tatpol.locationnoteapp.databinding.CustomInfoWindowBinding

class InfoWindowView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var binding: CustomInfoWindowBinding

    private val cornerRadius = 16f
    private val offset = 32f
    private val square = 30f
    private val defaultPadding = 8f

    private val shadowPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(offset / 4, BlurMaskFilter.Blur.OUTER)
        alpha = 64
    }

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val path = Path()

    init {
        setWillNotDraw(false)
        setPadding((offset + defaultPadding).toInt(),)
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
                2 * cornerRadius + offset,
                2 * cornerRadius + offset,
                180f,
                90f,
                false
            )
            arcTo(
                width - (offset + 2 * cornerRadius),
                offset,
                width - offset,
                offset + 2 * cornerRadius,
                270f,
                90f,
                false
            )
            arcTo(
                width - (offset + 2 * cornerRadius),
                height - (offset + 2 * cornerRadius),
                width - offset,
                height - offset,
                0f,
                90f,
                false
            )
            lineTo(width / 2f + square, height - offset)
            lineTo(width / 2f, height - offset + square)
            lineTo(width / 2f - square, height - offset)
            arcTo(
                offset,
                height - (offset + 2 * cornerRadius),
                offset + 2 * cornerRadius,
                height - offset,
                90f,
                90f,
                false
            )
            close()
        }
    }

    fun setInfoWindowContent(title: String?, snippet: String?) {
        binding.tvTitle.text = title
        binding.tvSnippet.text = snippet
    }
}
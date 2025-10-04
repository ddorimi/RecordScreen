package com.plcoding.recordscreen

import android.content.Context
import android.graphics.*
import android.view.View

class OverlayView(context: Context) : View(context) {
    private val paintBox = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val paintText = Paint().apply {
        color = Color.YELLOW
        textSize = 36f
        style = Paint.Style.FILL
    }

    private var results: List<DetectionResult> = emptyList()
    private var viewWidth: Int = 1
    private var viewHeight: Int = 1

    fun updateResults(newResults: List<DetectionResult>, w: Int, h: Int) {
        results = newResults
        viewWidth = w
        viewHeight = h
        postInvalidate() // trigger redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (result in results) {
            val box = result.box
            // SSD boxes are normalized: [ymin, xmin, ymax, xmax]
            val left = box[1] * viewWidth
            val top = box[0] * viewHeight
            val right = box[3] * viewWidth
            val bottom = box[2] * viewHeight

            canvas.drawRect(left, top, right, bottom, paintBox)
            canvas.drawText(
                "ID:${result.classId} ${(result.score * 100).toInt()}%",
                left,
                top - 10,
                paintText
            )
        }
    }
}
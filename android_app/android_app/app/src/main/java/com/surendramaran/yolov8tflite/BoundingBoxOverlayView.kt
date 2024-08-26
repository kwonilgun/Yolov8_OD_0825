package com.surendramaran.yolov8tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class BoundingBoxOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val transparentPaint = Paint().apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
    }

    // Red Paint for the bounding box border
    private val borderPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f // Adjust the thickness of the border
    }

    private val boxPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 10f // Adjust the thickness of the border
    }

    private var boundingBoxes: List<BoundingBox> = listOf()
    private var bitmap:Bitmap? = null

    fun setBoundingBoxes(boundingBoxes: List<BoundingBox>, bitmap: Bitmap) {
        this.boundingBoxes = boundingBoxes
        this.bitmap = bitmap
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bitmap?.let { bmp ->
            boundingBoxes.forEach { box ->
                // First, fill the entire canvas with white color
                canvas.drawColor(Color.WHITE)

                // Scale the bitmap to fit the canvas size
                val scaledBitmap = Bitmap.createScaledBitmap(bmp, canvas.width, canvas.height, true)


                val left = (box.x1 * width).toInt()
                val top = (box.y1 * height).toInt()
                val right = (box.x2 * width).toInt()
                val bottom = (box.y2 * height).toInt()

                // BoundingBox 영역의 이미지를 잘라냄
                val croppedBitmap = Bitmap.createBitmap(scaledBitmap, left, top, right - left  , bottom - top )

                // Calculate the top-left corner coordinates to center the croppedBitmap on the canvas
                val xOffset = (canvas.width - croppedBitmap.width) / 2f
                val yOffset = (canvas.height - croppedBitmap.height) / 2f

                canvas.save()

                // 잘라낸 부분을 이전 위치에서 이동된 중앙 위치에 그림
                canvas.drawBitmap(croppedBitmap, xOffset, yOffset, null)

                // Draw the red border around the bounding box
                canvas.drawRect(
                    xOffset,
                    yOffset,
                    xOffset + croppedBitmap.width,
                    yOffset + croppedBitmap.height,
                    borderPaint
                )

                // Draw the red border around the bounding box
                canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat() ,
                    height.toFloat() ,
                    borderPaint
                )
                canvas.restore()
            }
        }

    }
}



/*
        // Draw the bounding boxes
        boundingBoxes.forEach{
            val left = it.x1 * width
            val top = it.y1 * height
            val right = it.x2 * width
            val bottom = it.y2 * height
//            canvas.drawRect(left,top,right,bottom,transparentPaint)
            // Draw the top rectangle (above the bounding box)
            canvas.drawRect(0f, 0f, width.toFloat(), top, paint)
            // Draw the bottom rectangle (below the bounding box)
            canvas.drawRect(0f, bottom, width.toFloat(), height.toFloat(), paint)
            // Draw the left rectangle (left of the bounding box)
            canvas.drawRect(0f, top, left, bottom, paint)
            // Draw the right rectangle (right of the bounding box)
            canvas.drawRect(right, top, width.toFloat(), bottom, paint)
        }

 */
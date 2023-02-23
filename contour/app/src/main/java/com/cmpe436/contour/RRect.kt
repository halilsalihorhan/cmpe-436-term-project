package com.cmpe436.contour

import android.graphics.Paint
import android.graphics.Rect

data class RRect(
    var rect: Rect,
    var rotation: Float,
    var id: Int = -1,
    var isAcquired: Boolean = false,
    var paint: Paint = Paint().apply {
        color = 0x7F000000
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 10f
    }

)

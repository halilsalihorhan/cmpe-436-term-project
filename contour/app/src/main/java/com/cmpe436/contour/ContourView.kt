package com.cmpe436.contour

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.almeros.android.multitouch.RotateGestureDetector
import kotlin.math.absoluteValue


class ContourView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val gestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val rotateGestureDetector = RotateGestureDetector(context, RotateListener())

    lateinit var viewModel: ContourViewModel

    var isDrawing = false

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        viewModel.shapes.value?.forEach { entry ->
            viewModel.selectedShape.value?.let { selectedShape ->
               if(entry.value.id == selectedShape.id) {
                   return@forEach
               }
            }
            entry.value.let {
                canvas?.save()
                canvas?.rotate(it.rotation,it.rect.centerX().toFloat(),it.rect.centerY().toFloat())
                canvas?.drawRect(it.rect,it.paint)
                canvas?.restore()
            }
        }
        viewModel.selectedShape.value?.let {
            canvas?.save()
            canvas?.rotate(it.rotation,it.rect.centerX().toFloat(),it.rect.centerY().toFloat())
            canvas?.drawRect(it.rect,it.paint.apply { color = 0x7FF00000 })
            canvas?.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.pointerCount == 2) {
            gestureDetector.onTouchEvent(event)
            rotateGestureDetector.onTouchEvent(event)
            return false
        }

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // on shape click
                viewModel.shapes.value?.forEach { entry ->
                    entry.value.let {
                        var matrix = Matrix()
                        matrix.setRotate(-it.rotation,it.rect.centerX().toFloat(),it.rect.centerY().toFloat())
                        var rotatedPoint = floatArrayOf(0f, 0f)
                        matrix.mapPoints(rotatedPoint, floatArrayOf(event..x, event.y))

                        if (it.rect.contains(rotatedPoint[0].toInt(), rotatedPoint[1].toInt())) {
                            viewModel.acquireShape(it.id)
                            return true
                        }
                        var inverse = Rect(
                            it.rect.right,
                            it.rect.bottom,
                            it.rect.left,
                            it.rect.top
                        )
                        if (inverse.contains(rotatedPoint[0].toInt(), rotatedPoint[1].toInt())) {
                            viewModel.acquireShape(it.id)
                            return true
                        }
                        var mirror = Rect(
                            it.rect.right,
                            it.rect.top,
                            it.rect.left,
                            it.rect.bottom
                        )
                        if (mirror.contains(rotatedPoint[0].toInt(), rotatedPoint[1].toInt())) {
                            viewModel.acquireShape(it.id)
                            return true
                        }
                        var vertical = Rect(
                            it.rect.left,
                            it.rect.bottom,
                            it.rect.right,
                            it.rect.top
                        )
                        if (vertical.contains(rotatedPoint[0].toInt(), rotatedPoint[1].toInt())) {
                            viewModel.acquireShape(it.id)
                            return true
                        }


                    }
                }
                // create new shape

                isDrawing = true
                viewModel.selectedShape.postValue(RRect(Rect(event.x.toInt(), event.y.toInt(), event.x.toInt(), event.y.toInt()), 0f).apply {
                    rect.apply {
                        left = event.x.toInt()
                        top = event.y.toInt()
                    }
                })
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if(isDrawing){
                    viewModel.selectedShape.postValue(viewModel.selectedShape.value?.apply {
                        rect.apply {
                            right = event.x.toInt()
                            bottom = event.y.toInt()
                        }
                    })
                    invalidate()
                }
                else{
                    // translate temp.rect by event.x - temp.rect.centerX() and event.y - temp.rect.centerY()
                    viewModel.selectedShape.postValue(viewModel.selectedShape.value?.apply {
                        var dx = event.x - rect.centerX()
                        var dy = event.y - rect.centerY()
                        rect.apply {
                            left += dx.toInt()
                            right += dx.toInt()
                            top += dy.toInt()
                            bottom += dy.toInt()
                        }
                    })

                    invalidate()
                    return false
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if(isDrawing) {
                    isDrawing = false

                    viewModel.selectedShape.value?.let {
                       val width = it.rect.width().absoluteValue
                        val height = it.rect.height().absoluteValue
                        if(width > 60 && height > 60) {
                            viewModel.createShape()
                        } else {
                            viewModel.selectedShape.postValue(null)
                        }
                    }

                    invalidate()
                }
                viewModel.modifyShape()
                viewModel.releaseShape()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    inner class RotateListener : RotateGestureDetector.OnRotateGestureListener {
        override fun onRotate(detector: RotateGestureDetector?): Boolean {
            viewModel.selectedShape.postValue(viewModel.selectedShape.value?.apply {
                this.rotation -= detector?.rotationDegreesDelta ?: 0f
            })
            invalidate()
            return true
        }

        override fun onRotateBegin(detector: RotateGestureDetector?): Boolean {

            return true
        }

        override fun onRotateEnd(detector: RotateGestureDetector?) {

        }

    }
    inner class ScaleListener : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScale(p0: ScaleGestureDetector?): Boolean {
            var scale = p0?.scaleFactor
            scale = 0.1f.coerceAtLeast((scale!!).coerceAtMost(10.0f));
            viewModel.selectedShape.postValue(viewModel.selectedShape.value?.apply {
                var centerX = rect.centerX()
                var centerY = rect.centerY()
                rect.apply {
                    left = (centerX - (centerX - left) * scale).toInt()
                    right = (centerX + (right - centerX) * scale).toInt()
                    top = (centerY - (centerY - top) * scale).toInt()
                    bottom = (centerY + (bottom - centerY) * scale).toInt()
                }
            })
            invalidate()
            return true
        }

        override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
            return true
        }

        override fun onScaleEnd(p0: ScaleGestureDetector?) {

        }

    }
    fun listenToViewModel() {
        viewModel.shapes.observeForever {
            invalidate()
        }
    }
}
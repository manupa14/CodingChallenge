package com.esaurio.codingchallenge.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.sqrt

class ScaleImageView : AppCompatImageView, OnTouchListener {
    private var mContext: Context
    private val mMatrix: Matrix by lazy { Matrix() }
    private val mMatrixValues = FloatArray(9)

    // display width height.
    private var mWidth = 0
    private var mHeight = 0
    private var mIntrinsicWidth = 0
    private var mIntrinsicHeight = 0
    private var mScale = 0f
    private var mMinScale = 0f
    private var mPrevDistance = 0f
    private var isScaling = false
    private var mPrevMoveX = 0
    private var mPrevMoveY = 0
    private val mDetector: GestureDetector

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
        mContext = context
        mDetector = initializeGestureDetector()
        initialize()
    }

    constructor(context: Context) : super(context) {
        mContext = context
        mDetector = initializeGestureDetector()
        initialize()
    }

    private fun initializeGestureDetector(): GestureDetector {
        return GestureDetector(mContext, object : SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                maxZoomTo(e.x.toInt(), e.y.toInt())
                cutting()
                return super.onDoubleTap(e)
            }
        })
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        initialize()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        initialize()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        initialize()
    }

    private fun initialize() {
        this.scaleType = ScaleType.MATRIX
        val d = drawable
        if (d != null) {
            mIntrinsicWidth = d.intrinsicWidth
            mIntrinsicHeight = d.intrinsicHeight
            setOnTouchListener(this)
        }
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        mWidth = r - l
        mHeight = b - t
        mMatrix.reset()
        val r_norm = r - l
        mScale = r_norm.toFloat() / mIntrinsicWidth.toFloat()
        var paddingHeight = 0
        var paddingWidth = 0
        // scaling vertical
        if (mScale * mIntrinsicHeight > mHeight) {
            mScale = mHeight.toFloat() / mIntrinsicHeight.toFloat()
            mMatrix.postScale(mScale, mScale)
            paddingWidth = (r - mWidth) / 2
            paddingHeight = 0
            // scaling horizontal
        } else {
            mMatrix.postScale(mScale, mScale)
            paddingHeight = (b - mHeight) / 2
            paddingWidth = 0
        }
        mMatrix.postTranslate(paddingWidth.toFloat(), paddingHeight.toFloat())
        imageMatrix = mMatrix
        mMinScale = mScale
        cutting()
        return super.setFrame(l, t, r, b)
    }

    private fun getValue(matrix: Matrix, whichValue: Int): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[whichValue]
    }

    val scale: Float
        get() = getValue(mMatrix, Matrix.MSCALE_X)

    val translateX: Float
        get() = getValue(mMatrix, Matrix.MTRANS_X)

    val translateY: Float
        get() = getValue(mMatrix, Matrix.MTRANS_Y)

    private fun maxZoomTo(x: Int, y: Int) {
        if (mMinScale != scale && scale - mMinScale > 0.1f) {
            // threshold 0.1f
            val scale = mMinScale / scale
            zoomTo(scale, x, y)
        } else {
            val scale = Companion.MAX_SCALE / scale
            zoomTo(scale, x, y)
        }
    }

    fun zoomTo(scale: Float, x: Int, y: Int) {
        if (this.scale * scale < mMinScale) {
            return
        }
        if (scale >= 1 && this.scale * scale > Companion.MAX_SCALE) {
            return
        }
        mMatrix.postScale(scale, scale)
        // move to center
        mMatrix.postTranslate(-(mWidth * scale - mWidth) / 2, -(mHeight * scale - mHeight) / 2)

        // move x and y distance
        mMatrix.postTranslate(-(x - mWidth / 2) * scale, 0f)
        mMatrix.postTranslate(0f, -(y - mHeight / 2) * scale)
        imageMatrix = mMatrix
    }

    fun cutting() {
        val width = (mIntrinsicWidth * scale).toInt()
        val height = (mIntrinsicHeight * scale).toInt()
        if (translateX < -(width - mWidth)) {
            mMatrix.postTranslate(-(translateX + width - mWidth), 0f)
        }
        if (translateX > 0) {
            mMatrix.postTranslate(-translateX, 0f)
        }
        if (translateY < -(height - mHeight)) {
            mMatrix.postTranslate(0f, -(translateY + height - mHeight))
        }
        if (translateY > 0) {
            mMatrix.postTranslate(0f, -translateY)
        }
        if (width < mWidth) {
            mMatrix.postTranslate((mWidth - width) / 2.toFloat(), 0f)
        }
        if (height < mHeight) {
            mMatrix.postTranslate(0f, (mHeight - height) / 2.toFloat())
        }
        imageMatrix = mMatrix
    }

    private fun distance(x0: Float, x1: Float, y0: Float, y1: Float): Float {
        val x = x0 - x1
        val y = y0 - y1
        return sqrt(x * x + y * y.toDouble()).toFloat()
    }

    private fun dispDistance(): Float {
        return sqrt(mWidth * mWidth + mHeight * mHeight.toDouble()).toFloat()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mDetector.onTouchEvent(event)) {
            return true
        }
        val touchCount = event.pointerCount
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_1_DOWN, MotionEvent.ACTION_POINTER_2_DOWN -> {
                if (touchCount >= 2) {
                    val distance = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1))
                    mPrevDistance = distance
                    isScaling = true
                } else {
                    mPrevMoveX = event.x.toInt()
                    mPrevMoveY = event.y.toInt()
                }
                if (touchCount >= 2 && isScaling) {
                    val dist = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1))
                    var scale = (dist - mPrevDistance) / dispDistance()
                    mPrevDistance = dist
                    scale += 1f
                    scale *= scale
                    zoomTo(scale, mWidth / 2, mHeight / 2)
                    cutting()
                } else if (!isScaling) {
                    val distanceX = mPrevMoveX - event.x.toInt()
                    val distanceY = mPrevMoveY - event.y.toInt()
                    mPrevMoveX = event.x.toInt()
                    mPrevMoveY = event.y.toInt()
                    mMatrix.postTranslate(-distanceX.toFloat(), -distanceY.toFloat())
                    cutting()
                }
            }
            MotionEvent.ACTION_MOVE -> if (touchCount >= 2 && isScaling) {
                val dist = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1))
                var scale = (dist - mPrevDistance) / dispDistance()
                mPrevDistance = dist
                scale += 1f
                scale *= scale
                zoomTo(scale, mWidth / 2, mHeight / 2)
                cutting()
            } else if (!isScaling) {
                val distanceX = mPrevMoveX - event.x.toInt()
                val distanceY = mPrevMoveY - event.y.toInt()
                mPrevMoveX = event.x.toInt()
                mPrevMoveY = event.y.toInt()
                mMatrix.postTranslate(-distanceX.toFloat(), -distanceY.toFloat())
                cutting()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_POINTER_2_UP -> if (event.pointerCount <= 1) {
                isScaling = false
            }
        }
        return true
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    companion object {
        private const val MAX_SCALE = 4f
        const val TAG = "ScaleImageView"
    }
}
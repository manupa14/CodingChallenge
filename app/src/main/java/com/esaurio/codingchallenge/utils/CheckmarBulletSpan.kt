package com.esaurio.codingchallenge.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.BulletSpan

class CheckmarBulletSpan(
        gapWidth : Int,
        private val bulletColor : Int
) : BulletSpan(gapWidth, bulletColor) {

    override fun drawLeadingMargin(canvas: Canvas, paint: Paint, x: Int, dir: Int, top: Int, baseline: Int, bottom: Int, text: CharSequence, start: Int, end: Int, first: Boolean, layout: Layout?) {
        if ((text as Spanned).getSpanStart(this) == start) {
            val style = paint.style
            val oldColor = paint.color
            val oldTextSize = paint.textSize
            //paint.textSize = oldTextSize * 1.3f
            paint.color = bulletColor
            paint.style = Paint.Style.FILL
            canvas.drawText("✓",x.toFloat(), baseline.toFloat(), paint)
            paint.color = oldColor
            //paint.textSize = oldTextSize
            paint.style = style
        }
    }
}
package com.xyoye.comictools

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.Button
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ConvertUtils

/**
 * Created by xyoye on 2020/1/17.
 */

class ProgressButton : Button {
    private var progress: Float = 0f
    private var isLocked = false

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas?) {
        if (canvas != null) {
            val leftX2 = when (progress) {
                1f -> measuredWidth
                0f -> 0
                else -> (measuredWidth * progress).toInt()
            }

            val extraPX = ConvertUtils.dp2px(4f)
            val rightX1 =
                if (measuredWidth - leftX2 < extraPX) 0 else leftX2 - extraPX

            //right
            val drawableRight =
                ContextCompat.getDrawable(context, R.drawable.background_button_gray)
            drawableRight?.setBounds(rightX1, 0, measuredWidth, measuredHeight)
            drawableRight?.draw(canvas)

            //left
            val drawableLeft = background
            drawableLeft?.setBounds(0, 0, leftX2, measuredHeight)
            drawableLeft?.draw(canvas)
        }
        super.onDraw(canvas)
    }

    public fun progress(progress: Float) {
        this@ProgressButton.progress = progress
        postInvalidate()
    }

    public fun isLocked(): Boolean{
        synchronized(isLocked){
            return isLocked
        }
    }

    public fun setLock(lock: Boolean){
        synchronized(isLocked){
            isLocked = lock
        }
    }
}
package me.erwa.sourceset.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import java.util.*
import kotlin.properties.Delegates

/**
 * @author: drawf
 * @date: 2019/4/13
 * @see: <a href=""></a>
 * @description: 文字时钟View
 */
class TextClockView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * 全局画笔
     */
    private val mPaint = createPaint()
    private val mHelperPaint = createPaint(color = Color.RED)
    private var mWidth: Float by Delegates.notNull()
    private var mHeight: Float by Delegates.notNull()

    private var mHourR: Float by Delegates.notNull()
    private var mMinuteR: Float by Delegates.notNull()
    private var mSecondR: Float by Delegates.notNull()

    private var mHourDeg: Float by Delegates.notNull()
    private var mMinuteDeg: Float by Delegates.notNull()
    private var mSecondDeg: Float by Delegates.notNull()

    private var mAnimator: ValueAnimator by Delegates.notNull()

    init {
        //处理动画
        mAnimator = ValueAnimator.ofFloat(6f, 0f)
        mAnimator.duration = 150
        mAnimator.interpolator = LinearInterpolator()
        doInvalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWidth = (measuredWidth - paddingLeft - paddingRight).toFloat()
        mHeight = (measuredHeight - paddingTop - paddingBottom).toFloat()

        mHourR = mWidth * 0.143f
        mMinuteR = mWidth * 0.35f
        mSecondR = mWidth * 0.35f
    }

    /**
     * 开始计时
     */
    fun doInvalidate() {
        Calendar.getInstance().run {
            val hour = get(Calendar.HOUR)
            val minute = get(Calendar.MINUTE)
            val second = get(Calendar.SECOND)

            mHourDeg = -360 / 12f * (hour - 1)
            mMinuteDeg = -360 / 60f * (minute - 1)
            mSecondDeg = -360 / 60f * (second - 1)

            //记录当前角度，用于计算动画
            val hd = mHourDeg
            val md = mMinuteDeg
            val sd = mSecondDeg

            //处理动画
            mAnimator.removeAllUpdateListeners()
            mAnimator.addUpdateListener {
                val av = (it.animatedValue as Float)

                if (minute == 0 && second == 0) {
                    mHourDeg = hd + av * 5
                }

                if (second == 0) {
                    mMinuteDeg = md + av
                }

                mSecondDeg = sd + av

                invalidate()
            }
            mAnimator.start()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        canvas.drawColor(Color.BLACK)
        canvas.save()
        canvas.translate(mWidth / 2, mHeight / 2)

        drawHour(canvas, mHourDeg)
        drawMinute(canvas, mMinuteDeg)
        drawSecond(canvas, mSecondDeg)

        //辅助线
        //canvas.drawLine(0f, 0f, mHourR + 500, 0f, mHelperPaint)

        canvas.restore()
    }

    /**
     * 绘制小时
     */
    private fun drawHour(canvas: Canvas, degrees: Float) {
        mPaint.textSize = mHourR * 0.16f

        //处理整体旋转
        canvas.save()
        canvas.rotate(degrees)

        for (i in 0 until 12) {
            canvas.save()

            val iDeg = 360 / 12f * i
            canvas.rotate(iDeg)

            mPaint.alpha = if (iDeg + degrees == 0f) 255 else (0.6f * 255).toInt()
            mPaint.textAlign = Paint.Align.LEFT

            canvas.drawText("${(i + 1).toText()}点", mHourR, mPaint.getCenteredY(), mPaint)
            canvas.restore()
        }

        canvas.restore()
    }

    /**
     * 绘制分钟
     */
    private fun drawMinute(canvas: Canvas, degrees: Float) {
        mPaint.textSize = mHourR * 0.16f

        //处理整体旋转
        canvas.save()
        canvas.rotate(degrees)

        for (i in 0 until 60) {
            canvas.save()

            val iDeg = 360 / 60f * i
            canvas.rotate(iDeg)

            mPaint.alpha = if (iDeg + degrees == 0f) 255 else (0.6f * 255).toInt()
            mPaint.textAlign = Paint.Align.RIGHT

            if (i < 59) {
                canvas.drawText("${(i + 1).toText()}分", mMinuteR, mPaint.getCenteredY(), mPaint)
            }
            canvas.restore()
        }

        canvas.restore()
    }

    /**
     * 绘制秒
     */
    private fun drawSecond(canvas: Canvas, degrees: Float) {
        mPaint.textSize = mHourR * 0.16f

        //处理整体旋转
        canvas.save()
        canvas.rotate(degrees)

        for (i in 0 until 60) {
            canvas.save()

            val iDeg = 360 / 60f * i
            canvas.rotate(iDeg)

            mPaint.alpha = if (iDeg + degrees == 0f) 255 else (0.6f * 255).toInt()
            mPaint.textAlign = Paint.Align.LEFT

            if (i < 59) {
                canvas.drawText("${(i + 1).toText()}秒", mSecondR, mPaint.getCenteredY(), mPaint)
            }
            canvas.restore()
        }

        canvas.restore()
    }


    /**
     * 数字转换文字
     */
    private fun Int.toText(): String {
        var result = ""
        val iArr = "$this".toCharArray().map { it.toString().toInt() }

        //处理 10，11，12.. 20，21，22.. 等情况
        if (iArr.size > 1) {
            if (iArr[0] != 1) {
                result += NUMBER_TEXT_LIST[iArr[0]]
            }
            result += "十"
            if (iArr[1] > 0) {
                result += NUMBER_TEXT_LIST[iArr[1]]
            }
        } else {
            result = NUMBER_TEXT_LIST[iArr[0]]
        }

        return result
    }

    /**
     * 创建画笔
     */
    private fun createPaint(colorString: String? = null, color: Int = Color.WHITE): Paint {
        return Paint().apply {
            this.color = if (colorString != null) Color.parseColor(colorString) else color
            this.isAntiAlias = true
            this.style = Paint.Style.FILL
        }
    }

    /**
     * 扩展获取绘制文字时垂直居中y坐标
     */
    private fun Paint.getCenteredY(): Float {
        return this.fontSpacing / 2 - this.fontMetrics.bottom
    }

    private fun dp2px(dpValue: Float): Float {
        val scale = resources.displayMetrics.density
        return dpValue * scale + 0.5f
    }

    companion object {
        val NUMBER_TEXT_LIST = listOf(
            "零",
            "一",
            "二",
            "三",
            "四",
            "五",
            "六",
            "七",
            "八",
            "九",
            "十"
        )
    }

}
package me.erwa.sourceset.view.layout

import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import me.erwa.sourceset.R
import me.erwa.sourceset.view.*
import kotlin.math.absoluteValue
import kotlin.properties.Delegates

/**
 * @author: drawf
 * @date: 2019/3/21
 * @see: <a href=""></a>
 * @description: 可设置阴影的布局
 *
 * NOTE: ShadowLayout实际宽度=内容区域宽度+（mShadowRadius + Math.abs(mDx)）*2
 *       ShadowLayout实际高度=内容区域高度+（mShadowRadius + Math.abs(mDy)）*2
 * 当只设置一边显示阴影时，阴影部分占用的大小是（mShadowRadius + Math.abs(mDx、mDy)）
 */
class ShadowLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * 阴影颜色
     */
    @ColorInt
    private var mShadowColor: Int = 0
    /**
     * 阴影发散距离 blur
     */
    private var mShadowRadius: Float = 0f
    /**
     * x轴发散距离
     */
    private var mDx: Float = 0f
    /**
     * y轴发散距离
     */
    private var mDy: Float = 0f
    /**
     * 圆角半径
     */
    private var mCornerRadius: Float = 0f
    /**
     * 边框颜色
     */
    @ColorInt
    private var mBorderColor: Int = 0
    /**
     * 边框宽度
     */
    private var mBorderWidth: Float = 0f
    /**
     * 控制四边是否显示阴影
     */
    private var mShadowSides: Int = default_shadowSides

    private var mPaint: Paint = createPaint(color = Color.WHITE)
    private var mHelpPaint: Paint = createPaint(color = Color.RED)
    private var mContentRF: RectF by Delegates.notNull()

    init {
        initAttributes(context, attrs)
        processPadding()
        //设置软件渲染类型
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    private fun initAttributes(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ShadowLayout)
        try {
            a?.run {
                mShadowColor = getColor(R.styleable.ShadowLayout_sl_shadowColor, default_shadowColor)
                mShadowRadius =
                    getDimension(R.styleable.ShadowLayout_sl_shadowRadius, context.dpf2pxf(default_shadowRadius))
                mDx = getDimension(R.styleable.ShadowLayout_sl_dx, default_dx)
                mDy = getDimension(R.styleable.ShadowLayout_sl_dy, default_dy)

                mCornerRadius =
                    getDimension(R.styleable.ShadowLayout_sl_cornerRadius, context.dpf2pxf(default_cornerRadius))
                mBorderColor = getColor(R.styleable.ShadowLayout_sl_borderColor, default_borderColor)
                mBorderWidth =
                    getDimension(R.styleable.ShadowLayout_sl_borderWidth, context.dpf2pxf(default_borderWidth))

                mShadowSides = getInt(R.styleable.ShadowLayout_sl_shadowSides, default_shadowSides)
            }
        } finally {
            a?.recycle()
        }
    }

    private fun processPadding() {
        val xPadding = (mShadowRadius + mDx.absoluteValue).toInt()
        val yPadding = (mShadowRadius + mDy.absoluteValue).toInt()

        setPadding(
            if (mShadowSides.containsFlag(FLAG_SIDES_LEFT)) xPadding else 0,
            if (mShadowSides.containsFlag(FLAG_SIDES_TOP)) yPadding else 0,
            if (mShadowSides.containsFlag(FLAG_SIDES_RIGHT)) xPadding else 0,
            if (mShadowSides.containsFlag(FLAG_SIDES_BOTTOM)) yPadding else 0
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mContentRF = RectF(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (w - paddingRight).toFloat(),
            (h - paddingBottom).toFloat()
        )
    }

    override fun dispatchDraw(canvas: Canvas?) {
        if (canvas == null) return

        canvas.helpGreenCurtain(debug)

        //绘制阴影
        drawShadow(canvas)

        //绘制子View
        drawChild(canvas) {
            super.dispatchDraw(it)
        }

        //绘制边框
        drawBorder(canvas)
    }

    private fun drawShadow(canvas: Canvas) {
        canvas.save()

        mPaint.setShadowLayer(mShadowRadius, mDx, mDy, mShadowColor)
        canvas.drawRoundRect(mContentRF, mCornerRadius, mCornerRadius, mPaint)
        mPaint.utilReset()

        canvas.restore()
    }

    private fun drawChild(canvas: Canvas, block: (Canvas) -> Unit) {
        canvas.saveLayer(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), mPaint, Canvas.ALL_SAVE_FLAG)

        //先绘制子控件
        block.invoke(canvas)

        //使用path绘制圆角矩形
        val path = Path().apply {
            addRect(
                mContentRF,
                Path.Direction.CW
            )
            addRoundRect(
                mContentRF,
                mCornerRadius,
                mCornerRadius,
                Path.Direction.CW
            )
            fillType = Path.FillType.EVEN_ODD
        }

        //使用xfermode在图层上进行合成，处理圆角
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        canvas.drawPath(path, mPaint)
        mPaint.utilReset()

        canvas.restore()
    }

    private fun drawBorder(canvas: Canvas) {
        val bw = mBorderWidth / 3
        if (bw > 0) {
            canvas.save()

            val borderRF = RectF(
                mContentRF.left + bw,
                mContentRF.top + bw,
                mContentRF.right - bw,
                mContentRF.bottom - bw
            )
            mPaint.strokeWidth = mBorderWidth
            mPaint.style = Paint.Style.STROKE
            mPaint.color = mBorderColor
            canvas.drawRoundRect(borderRF, mCornerRadius, mCornerRadius, mPaint)
            mPaint.utilReset()

            canvas.restore()
        }
    }

    companion object {
        const val debug = false

        private const val FLAG_SIDES_TOP = 1
        private const val FLAG_SIDES_RIGHT = 2
        private const val FLAG_SIDES_BOTTOM = 4
        private const val FLAG_SIDES_LEFT = 8
        private const val FLAG_SIDES_ALL = 15

        const val default_shadowColor = Color.BLACK
        const val default_shadowRadius = 0f
        const val default_dx = 0f
        const val default_dy = 0f
        const val default_cornerRadius = 0f
        const val default_borderColor = Color.RED
        const val default_borderWidth = 0f
        const val default_shadowSides = FLAG_SIDES_ALL

    }

}

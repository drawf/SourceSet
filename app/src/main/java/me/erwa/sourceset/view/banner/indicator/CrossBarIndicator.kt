package me.erwa.sourceset.view.banner.indicator

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import me.erwa.sourceset.R
import me.erwa.sourceset.view.banner.IIndicator
import me.erwa.sourceset.view.banner.IIndicatorInstance
import me.erwa.sourceset.view.createPaint
import me.erwa.sourceset.view.dpf2pxf
import me.erwa.sourceset.view.utilReset
import kotlin.properties.Delegates

/**
 * @author: drawf
 * @date: 2019/3/19
 * @see: <a href=""></a>
 * @description: banner 横杆指示器
 */
class CrossBarIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), IIndicatorInstance {

    //********************************
    //* 自定义属性部分
    //********************************
    /**
     * 指示器宽度
     */
    private var mItemWidth: Float = 0f

    /**
     * 指示器高度
     */
    private var mItemHeight: Float = 0f

    /**
     * 指示器间隔
     */
    private var mItemSpace: Float = 0f

    /**
     * 指示器背景颜色
     */
    @ColorInt
    private var mItemBackgroundColor: Int = 0

    /**
     * 指示器前景颜色
     */
    @ColorInt
    private var mItemForegroundColor: Int = 0

    //********************************
    //* 绘制使用的属性部分
    //********************************
    /**
     * 全局画笔
     */
    private val mPaint = createPaint(color = Color.WHITE)

    //********************************
    //* 计算属性部分
    //********************************
    /**
     * 该View的宽度
     */
    private var mWidth: Float by Delegates.notNull()

    /**
     * 该View的高度
     */
    private var mHeight: Float by Delegates.notNull()

    /**
     * 指示器圆角半径
     */
    private var mItemRadius: Float = 0f

    /**
     * 首个指示器的坐标
     */
    private var mFirstItemRectF: RectF by Delegates.notNull()

    /**
     * 指示器坐标列表
     */
    private val mItemRectFList: MutableList<RectF> = mutableListOf()

    //********************************
    //* 设置数据属性部分
    //********************************
    private lateinit var mIndicatorImpl: IIndicator

    override fun setIndicator(impl: IIndicator) {
        this.mIndicatorImpl = impl
        initView()
        this.invalidate()
    }

    override fun doRequestLayout() {
        initView()
        requestLayout()
    }

    override fun doInvalidate() {
        invalidate()
    }

    init {
        initAttributes(context, attrs)
        mItemRadius = mItemHeight / 2

        if (isInEditMode) {
            mIndicatorImpl = object : IIndicator {
                override fun getCount(): Int {
                    return 3
                }

                override fun getCurrentIndex(): Int {
                    return 1
                }
            }
            initView()
        }
    }

    /**
     * 初始化View
     */
    private fun initView() {
        initItemRectF()
    }

    /**
     * 初始化指示器坐标
     */
    private fun initItemRectF() {
        val count = mIndicatorImpl.getCount()
        if (count <= 0) return

        mFirstItemRectF = RectF(0f, 0f, mItemWidth, mItemHeight)

        //指示器坐标集合
        mItemRectFList.clear()
        (0 until count).forEach { i ->
            if (i == 0) {
                mItemRectFList.add(mFirstItemRectF)
            } else {
                val prev = mItemRectFList[i - 1]
                mItemRectFList.add(
                    RectF(
                        prev.right + mItemSpace,
                        prev.top,
                        prev.right + mItemSpace + prev.width(),
                        prev.bottom
                    )
                )
            }
        }
    }

    /**
     * 初始化自定义属性
     */
    private fun initAttributes(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CrossBarIndicator)
        try {
            a?.run {
                mItemWidth =
                    a.getDimension(
                        R.styleable.CrossBarIndicator_cbi_itemWidth,
                        context.dpf2pxf(default_itemWidth)
                    )
                mItemHeight =
                    a.getDimension(
                        R.styleable.CrossBarIndicator_cbi_itemHeight,
                        context.dpf2pxf(default_itemHeight)
                    )
                mItemSpace =
                    a.getDimension(
                        R.styleable.CrossBarIndicator_cbi_itemSpace,
                        context.dpf2pxf(default_itemSpace)
                    )
                mItemBackgroundColor =
                    a.getColor(
                        R.styleable.CrossBarIndicator_cbi_itemBackgroundColor,
                        default_itemBackgroundColor
                    )
                mItemForegroundColor =
                    a.getColor(
                        R.styleable.CrossBarIndicator_cbi_itemForegroundColor,
                        default_itemForegroundColor
                    )
            }
        } finally {
            a?.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (::mIndicatorImpl.isInitialized) {
            val count = mIndicatorImpl.getCount()
            if (count == 0) return

            var width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
            val calcWidth = mFirstItemRectF.width() * count + mItemSpace * (count - 1)

            if (calcWidth < width) {
                width = calcWidth
            }

            //设置当前view的W，H
            setMeasuredDimension(width.toInt(), mFirstItemRectF.height().toInt())
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = (w - paddingLeft - paddingRight).toFloat()
        mHeight = (h - paddingTop - paddingBottom).toFloat()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null || !::mIndicatorImpl.isInitialized) return

        //绘制背景
        mPaint.color = mItemBackgroundColor
        mItemRectFList.forEach {
            canvas.drawRoundRect(it, mItemRadius, mItemRadius, mPaint)
        }
        mPaint.utilReset()

        //绘制前景
        mPaint.color = mItemForegroundColor
        val selected = mItemRectFList[mIndicatorImpl.getCurrentIndex()]
        canvas.drawRoundRect(selected, mItemRadius, mItemRadius, mPaint)
        mPaint.utilReset()
    }

    companion object {
        private const val default_itemWidth = 24f
        private const val default_itemHeight = 2f
        private const val default_itemSpace = 4f
        private const val default_itemBackgroundColor = Color.GREEN
        private const val default_itemForegroundColor = Color.RED
    }

}
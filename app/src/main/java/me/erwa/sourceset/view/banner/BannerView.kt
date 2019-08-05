package me.erwa.sourceset.view.banner

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import me.erwa.sourceset.R
import me.erwa.sourceset.view.banner.factory.PagerViewFactory
import me.erwa.sourceset.view.createPaint
import me.erwa.sourceset.view.dpf2pxf
import kotlin.properties.Delegates


/**
 * @author: drawf
 * @date: 2019/3/19
 * @see: <a href=""></a>
 * @description: banner
 */
class BannerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), IBannerViewInstance, ViewTreeObserver.OnGlobalLayoutListener {

    //********************************
    //* 自定义属性部分
    //********************************

    /**
     * 视图的高度
     * 小于等于0时为 Match Parent
     */
    private var mViewHeight: Float = 0f

    /**
     * 视图圆角半径
     */
    private var mViewCornerRadius: Float = 0f

    /**
     * 根据百分百设置item的宽度
     */
    private var mItemViewWidthRatio: Float = 0f

    /**
     * 设置item的间距
     */
    private var mItemViewMargin: Float = 0f

    /**
     * 轮换时间
     */
    private var mIntervalInMillis: Int = 0

    /**
     * 页面停留时长，只在SMOOTH模式下生效
     */
    private var mPageHoldInMillis: Int = default_pageHoldInMillis

    /**
     * 滚动模式
     */
    private var mScrollMode: Int = default_scrollMode

    /**
     * itemView对齐方式
     */
    private var mItemViewAlign: Int = default_itemViewAlign

    //********************************
    //* 绘制使用的属性部分
    //********************************

    /**
     * 全局画笔
     */
    private val mPaint = createPaint(color = Color.WHITE)

    /**
     * 全局Path
     */
    private val mPath = Path()

    /**
     * PagerView实例
     */
    private lateinit var mPagerViewInstance: IPagerViewInstance

    //********************************
    //* 计算属性部分
    //********************************
    /**
     * 该View的宽度
     */
    private var mWidth: Float = -1f

    /**
     * 该View的高度
     */
    private var mHeight: Float by Delegates.notNull()

    /**
     * 视图区域的坐标
     */
    private var mViewRectF: RectF = RectF()

    //********************************
    //* 设置数据属性部分
    //********************************
    /**
     * BannerView的接口实现
     */
    private var mBannerViewImpl: IBannerView? = null

    fun setBannerViewImpl(impl: IBannerView) {
        this.mBannerViewImpl = impl
        initView()
    }

    /**
     * 开始自动滚动
     */
    fun startAutoScroll() {
        if (::mPagerViewInstance.isInitialized && !mFlagAutoScroll && mBannerViewImpl != null) {
            mFlagAutoScroll = if (mBannerViewImpl!!.getCount() > 1) {
                mPagerViewInstance.startAutoScroll(mIntervalInMillis)
                true
            } else {
                mPagerViewInstance.stopAutoScroll()
                false
            }
        }
    }

    /**
     * 停止自动滚动
     */
    fun stopAutoScroll() {
        if (::mPagerViewInstance.isInitialized && mFlagAutoScroll) {
            mPagerViewInstance.stopAutoScroll()
            mFlagAutoScroll = false
        }
    }

    /**
     * 数据刷新后，主动重建banner
     */
    fun doRecreate() {
        initView()
        processSingleView()
    }

    /**
     * 自动滚动标识位
     */
    private var mFlagAutoScroll: Boolean = false
    private var mIndicator: IIndicatorInstance? = null

    init {
        initAttributes(context, attrs)
        viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        viewTreeObserver.removeOnGlobalLayoutListener(this)
        processSingleView()
        processInitView()
    }

    /**
     * 处理单张图情况
     */
    private fun processSingleView() {
        if (mBannerViewImpl != null) {
            val bvImpl = mBannerViewImpl!!
            val count = bvImpl.getCount()
            if (count <= 1) {
                val view = if (count < 1) {
                    bvImpl.getDefaultView(context)
                } else {
                    bvImpl.getItemView(context).apply {
                        bvImpl.onBindView(this, 0)
                        bvImpl.onPageSelected(0)
                    }
                } ?: View(context).apply { setBackgroundColor(Color.WHITE) }

                val lp = LayoutParams(getItemViewWidth(), mViewHeight.toInt()).apply {
                    addRule(getItemViewAlign())
                }
                addView(view, lp)
            }
        }
    }

    /**
     * 布局完毕后，初始化view
     */
    private fun processInitView() {
        if (mBannerViewImpl != null && mBannerViewImpl!!.getCount() > 1) {
            initView()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        findIndicator()
    }

    /**
     * 在子View中找到指示器
     */
    private fun findIndicator() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is IIndicatorInstance) {
                mIndicator = child
                return
            }
        }
    }

    /**
     * 初始化自定义属性
     */
    private fun initAttributes(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BannerView)
        try {
            a?.run {
                mViewHeight = a.getDimension(R.styleable.BannerView_bv_viewHeight, context.dpf2pxf(default_viewHeight))
                //默认为MATCH_PARENT
                if (mViewHeight <= 0) mViewHeight = (LayoutParams.MATCH_PARENT).toFloat()
                mViewCornerRadius =
                    a.getDimension(
                        R.styleable.BannerView_bv_viewCornerRadius,
                        context.dpf2pxf(default_viewCornerRadius)
                    )
                mItemViewWidthRatio =
                    a.getFloat(R.styleable.BannerView_bv_itemViewWidthRatio, default_itemViewWidthRatio)
                mItemViewMargin =
                    a.getDimension(
                        R.styleable.BannerView_bv_itemViewMargin,
                        context.dpf2pxf(default_itemViewMargin)
                    )
                mIntervalInMillis =
                    a.getInteger(R.styleable.BannerView_bv_intervalInMillis, default_intervalInMillis)
                mPageHoldInMillis =
                    a.getInteger(R.styleable.BannerView_bv_pageHoldInMillis, default_pageHoldInMillis)
                mScrollMode =
                    a.getInteger(R.styleable.BannerView_bv_scrollMode, default_scrollMode)
                mItemViewAlign =
                    a.getInteger(R.styleable.BannerView_bv_itemViewAlign, default_itemViewAlign)
            }
        } finally {
            a?.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = (w - paddingLeft - paddingRight).toFloat()
        mHeight = (h - paddingTop - paddingBottom).toFloat()

        mViewRectF.set(0f, 0f, mWidth, if (mViewHeight <= 0) mHeight else mViewHeight)
    }

    /**
     * 初始化view
     */
    private fun initView() {
        if (mBannerViewImpl != null && mWidth > 0) {
            val bvImpl = mBannerViewImpl!!

            removeAllViews()

            //当数据量为0、1时，设置单个view
            if (bvImpl.getCount() <= 1) {
                return
            }

            //处理pager实例逻辑
            mPagerViewInstance = PagerViewFactory(this).getPagerView()
            mPagerViewInstance.setPageHoldInMillis(
                if (isSmoothMode()) {
                    mPageHoldInMillis
                } else {
                    mIntervalInMillis
                }
            )
            mPagerViewInstance.setOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    mIndicator?.doInvalidate()
                    val realPos = position % bvImpl.getCount()
                    bvImpl.onPageSelected(realPos)
                }
            })

            //初始化自动滚动设置
            mFlagAutoScroll = if (bvImpl.isDefaultAutoScroll()) {
                mPagerViewInstance.startAutoScroll(mIntervalInMillis)
                true
            } else {
                mPagerViewInstance.stopAutoScroll()
                false
            }

            //添加view
            addView(mPagerViewInstance as View, LayoutParams(LayoutParams.MATCH_PARENT, mViewHeight.toInt()))

            //初始化指示器
            if (mIndicator != null) {
                mIndicator?.setIndicator(object : IIndicator {

                    override fun getCount(): Int {
                        return bvImpl.getCount()
                    }

                    override fun getCurrentIndex(): Int {
                        return mPagerViewInstance.getRealCurrentPosition(bvImpl.getCount())
                    }

                })
                //添加指示器
                addView(mIndicator as View)
            }
        }

    }

    override fun dispatchDraw(canvas: Canvas?) {
        if (canvas == null) return
        if (mViewCornerRadius <= 0) {
            super.dispatchDraw(canvas)
            return
        }

        //1. 开图层
        canvas.saveLayer(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), null, Canvas.ALL_SAVE_FLAG)

        //2. 绘制子View
        super.dispatchDraw(canvas)

        //3. 裁剪合成
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        // 构建四个圆角
        val path = mPath.apply {
            addRoundRect(
                mViewRectF,
                mViewCornerRadius,
                mViewCornerRadius,
                Path.Direction.CW
            )
            addRect(
                mViewRectF,
                Path.Direction.CW
            )
            fillType = Path.FillType.EVEN_ODD
        }
        canvas.drawPath(path, mPaint)
        mPath.reset()
        canvas.restore()
    }

    override fun getCount(): Int {
        checkDataState()
        return mBannerViewImpl!!.getCount()
    }

    override fun getItemView(context: Context): View {
        checkDataState()
        return mBannerViewImpl!!.getItemView(context)
    }

    override fun onBindView(itemView: View, position: Int) {
        checkDataState()
        return mBannerViewImpl!!.onBindView(itemView, position)
    }

    override fun getItemViewWidth(): Int {
        if (mWidth <= 0 || mItemViewWidthRatio <= 0) throw IllegalStateException("数据状态异常")
        return (mWidth * mItemViewWidthRatio).toInt()
    }

    override fun getItemViewMargin(): Int {
        return mItemViewMargin.toInt()
    }

    override fun getItemViewAlign(): Int {
        return when (mItemViewAlign) {
            202 -> {
                ALIGN_PARENT_LEFT
            }
            203 -> {
                ALIGN_PARENT_RIGHT
            }
            else -> {
                CENTER_HORIZONTAL
            }
        }
    }

    override fun isSmoothMode(): Boolean {
        return mScrollMode == SCROLL_MODE_SMOOTH
    }

    private fun checkDataState() {
        if (mBannerViewImpl == null) {
            throw IllegalStateException("数据状态异常")
        }
    }

    companion object {
        private const val SCROLL_MODE_INTERVAL = 101
        private const val SCROLL_MODE_SMOOTH = 102

        private const val ALIGN_CENTER_HORIZONTAL = 201
        private const val ALIGN_ALIGN_PARENT_LEFT = 202
        private const val ALIGN_ALIGN_PARENT_RIGHT = 203

        private const val default_viewHeight = 0f
        private const val default_viewCornerRadius = 0f
        private const val default_itemViewWidthRatio = 1f
        private const val default_itemViewMargin = 0f
        private const val default_intervalInMillis = 2000
        private const val default_pageHoldInMillis = 0
        private const val default_scrollMode = SCROLL_MODE_INTERVAL
        private const val default_itemViewAlign = ALIGN_CENTER_HORIZONTAL
    }

}
package me.erwa.sourceset.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import me.erwa.sourceset.R
import kotlin.math.absoluteValue
import kotlin.properties.Delegates

/**
 * @author: drawf
 * @date: 2019/5/7
 * @see: <a href=""></a>
 * @description: 雷达图
 * NOTE: View的宽高由属性[mWebRadius]根据UI稿的比例计算得出
 */
class RadarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //********************************
    //* 自定义属性部分
    //********************************

    /**
     * 雷达网图半径
     */
    private var mWebRadius: Float = 0f

    /**
     * 雷达网图半径对应的最大进度
     */
    private var mWebMaxProgress: Int = 0

    /**
     * 雷达网线颜色
     */
    @ColorInt
    private var mWebLineColor: Int = 0

    /**
     * 雷达网线宽度
     */
    private var mWebLineWidth: Float = 0f

    /**
     * 雷达图各定点文字颜色
     */
    @ColorInt
    private var mTextArrayedColor: Int = 0

    /**
     * 雷达图文字数组字体路径
     */
    private var mTextArrayedFontPath: String? = null

    /**
     * 雷达图中心连接区域颜色
     */
    @ColorInt
    private var mAreaColor: Int = 0

    /**
     * 雷达图中心连接区域边框颜色
     */
    @ColorInt
    private var mAreaBorderColor: Int = 0

    /**
     * 雷达图中心文字名称
     */
    private var mTextCenteredName: String = default_textCenteredName

    /**
     * 雷达图中心文字颜色
     */
    @ColorInt
    private var mTextCenteredColor: Int = 0

    /**
     * 雷达图中心文字字体路径
     */
    private var mTextCenteredFontPath: String? = null

    /**
     * 文字数组，且以该数组长度确定雷达图是几边形
     */
    private var mTextArray: Array<String> by Delegates.notNull()

    /**
     * 进度数组，与TextArray一一对应
     */
    private var mProgressArray: Array<Int> by Delegates.notNull()

    /**
     * 执行动画前的进度数组，与TextArray一一对应
     */
    private var mOldProgressArray: Array<Int> by Delegates.notNull()

    /**
     * 动画时间，为0代表没有动画
     * NOTE: 如果是速度一定模式下，代表从雷达中心执行动画到顶点的时间
     */
    private var mAnimateTime: Long = 0L

    /**
     * 动画模式，默认为时间一定模式
     */
    private var mAnimateMode: Int = default_animateMode

    //********************************
    //* 计算属性部分
    //********************************

    /**
     * 垂直文本距离雷达主图的宽度
     */
    private var mVerticalSpaceWidth: Float by Delegates.notNull()

    /**
     * 水平文本距离雷达主图的宽度
     */
    private var mHorizontalSpaceWidth: Float by Delegates.notNull()

    /**
     * 文字数组中的字体大小
     */
    private var mTextArrayedSize: Float by Delegates.notNull()

    /**
     * 文字数组设置字体大小后的文字宽度，取字数最多的
     */
    private var mTextArrayedWidth: Float by Delegates.notNull()

    /**
     * 文字数组设置字体大小后的文字高度
     */
    private var mTextArrayedHeight: Float by Delegates.notNull()

    /**
     * 该View的宽度
     */
    private var mWidth: Float by Delegates.notNull()

    /**
     * 该View的高度
     */
    private var mHeight: Float by Delegates.notNull()

    //********************************
    //* 绘制使用的属性部分
    //********************************

    /**
     * 全局画笔
     */
    private val mPaint = createPaint()
    private val mHelperPaint = createPaint()

    /**
     * 全局路径
     */
    private val mPath = Path()

    /**
     * 雷达网虚线效果
     */
    private var mDashPathEffect: DashPathEffect by Delegates.notNull()

    /**
     * 雷达主图各顶点的坐标数组
     */
    private var mPointArray: Array<PointF> by Delegates.notNull()

    /**
     * 文字数组各文字的坐标数组
     */
    private var mTextArrayedPointArray: Array<PointF> by Delegates.notNull()

    /**
     * 文字数组各进度的坐标数组
     */
    private var mProgressPointArray: Array<PointF> by Delegates.notNull()

    /**
     * 作转换使用的临时变量
     */
    private var mTempPointF: PointF = PointF()

    /**
     * 雷达图文字数组字体
     */
    private var mTextArrayedTypeface: Typeface? = null

    /**
     * 雷达图中心文字字体
     */
    private var mTextCenteredTypeface: Typeface? = null

    /**
     * 动画处理器数组
     */
    private var mAnimatorArray: Array<ValueAnimator?> by Delegates.notNull()

    /**
     * 各雷达属性动画的时间数组
     */
    private var mAnimatorTimeArray: Array<Long> by Delegates.notNull()

    //********************************
    //* 设置数据属性部分
    //********************************

    fun setTextArray(textList: List<String>) {
        this.mTextArray = textList.toTypedArray()
        this.mProgressArray = Array(mTextArray.size) { 0 }
        this.mOldProgressArray = Array(mTextArray.size) { 0 }
        initView()
    }

    fun setProgressList(progressList: List<Int>) {
        this.mProgressArray = progressList.toTypedArray()
        initView()
    }

    fun setOldProgressList(oldProgressList: List<Int>) {
        this.mOldProgressArray = oldProgressList.toTypedArray()
        initView()
    }

    init {
        initAttributes(context, attrs)
        initView()
        //设置软件渲染类型，解决DashPathEffect不生效问题
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    private fun initView() {
        initCalculateAttributes()
        initDrawAttributes()
        initAnimator()
    }

    /**
     * 初始化自定义属性
     */
    private fun initAttributes(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RadarView)
        try {
            a?.run {
                mWebRadius = a.getDimension(R.styleable.RadarView_rv_webRadius, context.dpf2pxf(default_webRadius))
                mWebMaxProgress = a.getInt(R.styleable.RadarView_rv_webMaxProgress, default_webMaxProgress)
                mWebLineColor = a.getColor(R.styleable.RadarView_rv_webLineColor, default_webLineColor)
                mWebLineWidth =
                    a.getDimension(R.styleable.RadarView_rv_webLineWidth, context.dpf2pxf(default_webLineWidth))

                mTextArrayedColor = a.getColor(R.styleable.RadarView_rv_textArrayedColor, default_textArrayedColor)
                mTextArrayedFontPath = a.getString(R.styleable.RadarView_rv_textArrayedFontPath)

                mAreaColor = a.getColor(R.styleable.RadarView_rv_areaColor, default_areaColor)
                mAreaBorderColor = a.getColor(R.styleable.RadarView_rv_areaBorderColor, default_areaBorderColor)

                mTextCenteredName = a.getString(R.styleable.RadarView_rv_textCenteredName) ?: default_textCenteredName
                mTextCenteredColor = a.getColor(R.styleable.RadarView_rv_textCenteredColor, default_textCenteredColor)
                mTextCenteredFontPath = a.getString(R.styleable.RadarView_rv_textCenteredFontPath)

                mAnimateTime = a.getInt(R.styleable.RadarView_rv_animateTime, default_animateTime).toLong()
                mAnimateMode = a.getInt(R.styleable.RadarView_rv_animateMode, default_animateMode)

                mTextArray = default_textArray
                mProgressArray = default_progressArray
                mOldProgressArray = default_oldProgressArray
            }
        } finally {
            a?.recycle()
        }
    }

    /**
     * 初始化计算属性，基本的宽高、字体大小、间距等数据
     * NOTE：以UI稿比例为准，根据[mWebRadius]来计算
     */
    private fun initCalculateAttributes() {
        //根据比例计算相应属性
        (mWebRadius / 100).let {
            mVerticalSpaceWidth = it * 8
            mHorizontalSpaceWidth = it * 10

            mTextArrayedSize = it * 12
        }

        //设置字体大小后，计算文字所占宽高
        mPaint.textSize = mTextArrayedSize
        mTextArray.maxBy { it.length }?.apply {
            mTextArrayedWidth = mPaint.measureText(this)
            mTextArrayedHeight = mPaint.fontSpacing
        }
        mPaint.utilReset()

        //动态计算出view的实际宽高
        mWidth = (mTextArrayedWidth + mHorizontalSpaceWidth + mWebRadius) * 2.1f
        mHeight = (mTextArrayedHeight + mVerticalSpaceWidth + mWebRadius) * 2.1f
    }

    /**
     * 初始化绘制相关的属性
     */
    private fun initDrawAttributes() {
        context.dpf2pxf(2f).run {
            mDashPathEffect = DashPathEffect(floatArrayOf(this, this), this)
        }

        mPointArray = Array(mTextArray.size) { PointF(0f, 0f) }
        mTextArrayedPointArray = Array(mTextArray.size) { PointF(0f, 0f) }
        mProgressPointArray = Array(mTextArray.size) { PointF(0f, 0f) }
        if (mTextArrayedFontPath != null) {
            mTextArrayedTypeface = Typeface.createFromAsset(context.assets, mTextArrayedFontPath)
        }
        if (mTextCenteredFontPath != null) {
            mTextCenteredTypeface = Typeface.createFromAsset(context.assets, mTextCenteredFontPath)
        }
    }

    /**
     * 初始化动画处理器
     */
    private fun initAnimator() {
        try {
            mAnimatorArray = Array(mTextArray.size) { null }
            mAnimatorTimeArray = Array(mTextArray.size) { 0L }
            mAnimatorArray.forEachIndexed { index, _ ->
                val sv = mOldProgressArray[index].toFloat()
                val ev = mProgressArray[index].toFloat()
                mAnimatorArray[index] = if (sv == ev) null else ValueAnimator.ofFloat(sv, ev)

                if (mAnimateMode == ANIMATE_MODE_TIME) {
                    mAnimatorTimeArray[index] = mAnimateTime
                } else {
                    //根据最大进度和动画时间算出恒定速度
                    val v = mWebMaxProgress.toFloat() / mAnimateTime
                    mAnimatorTimeArray[index] = if (sv == ev) 0L else ((ev - sv) / v).toLong()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mWidth.toInt(), mHeight.toInt())
    }

    /**
     * 开始动画
     */
    fun doInvalidate() {
        mAnimatorArray.forEachIndexed { index, valueAnimator ->
            val at = mAnimatorTimeArray[index]
            if (valueAnimator != null && at > 0) {
                valueAnimator.duration = at
                valueAnimator.removeAllUpdateListeners()
                valueAnimator.addUpdateListener {
                    val av = (it.animatedValue as Float)
                    mProgressArray[index] = av.toInt()

                    invalidate()
                }
                valueAnimator.start()
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return
        canvas.helpGreenCurtain(debug)

        canvas.save()
        canvas.translate(mWidth / 2, mHeight / 2)

        if (checkIllegalData(canvas)) {
            //绘制网状图形
            drawWeb(canvas)

            //绘制文字数组
            drawTextArray(canvas)

            //绘制连接区域
            drawConnectionArea(canvas)

            //绘制中心的文字
            drawCenterText(canvas)
        }

        canvas.restore()
    }

    /**
     * 检查数据是否合法
     */
    private fun checkIllegalData(canvas: Canvas): Boolean {
        var errorMsg: String? = null

        if (mTextArray.size < 3 && errorMsg == null) {
            errorMsg = "TextArray长度不能小于3"
        }

        if (mTextArray.size != mProgressArray.size && errorMsg == null) {
            errorMsg = "TextArray长度与ProgressArray长度不相等"
        }

        if (mTextArray.size != mOldProgressArray.size && errorMsg == null) {
            errorMsg = "TextArray长度与OldProgressArray长度不相等"
        }

        if (errorMsg != null) {
            mPaint.textSize = mTextArrayedSize
            mPaint.color = Color.RED
            mPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(errorMsg, 0f, mPaint.getCenteredY(), mPaint)
            mPaint.utilReset()
            return false
        }
        return true
    }

    /**
     * 绘制网状图形
     */
    private fun drawWeb(canvas: Canvas) {
        canvas.save()

        val rDeg = 360f / mTextArray.size

        mTextArray.forEachIndexed { index, _ ->
            //绘制虚线，每次都将坐标系逆时针旋转(rDeg * index)度
            canvas.save()
            canvas.rotate(-rDeg * index)

            mPaint.pathEffect = mDashPathEffect
            mPaint.color = mWebLineColor
            mPaint.strokeWidth = mWebLineWidth
            canvas.drawLine(0f, 0f, 0f, -mWebRadius, mPaint)
            mPaint.utilReset()

            //用三角函数计算出最长的网的边
            val lineW = mWebRadius * (rDeg / 2).degreeSin() * 2
            for (i in 1..4) {
                //绘制网的边，每次将坐标系向上移动(mWebRadius / 4f)*i，
                //且顺时针旋转(rDeg / 2)度，然后绘制长度为(lineW / 4f * i)的实线
                canvas.save()
                canvas.translate(0f, -mWebRadius / 4f * i)
                canvas.rotate(rDeg / 2)

                mPaint.color = mWebLineColor
                mPaint.strokeWidth = mWebLineWidth
                canvas.drawLine(0f, 0f, lineW / 4f * i, 0f, mPaint)
                mPaint.utilReset()

                canvas.restore()
            }

            canvas.restore()
        }

        canvas.restore()
    }

    /**
     * 绘制文字数组
     */
    private fun drawTextArray(canvas: Canvas) {
        canvas.save()

        val rDeg = 360f / mTextArray.size

        //先计算出雷达图各个顶点的坐标
        mPointArray.mapIndexed { index, pointF ->
            if (index == 0) {
                pointF.x = 0f
                pointF.y = -mWebRadius
            } else {
                mPointArray[index - 1].degreePointF(pointF, rDeg)
            }

            //绘制辅助圆点
            if (debug) {
                mHelperPaint.color = Color.RED
                canvas.drawCircle(pointF.x, pointF.y, 5f, mHelperPaint)
                mHelperPaint.utilReset()
            }

            return@mapIndexed pointF
        }

        //基于各顶点坐标，计算出文字坐标并绘制文字
        mTextArrayedPointArray.mapIndexed { index, pointF ->
            pointF.x = mPointArray[index].x
            pointF.y = mPointArray[index].y
            return@mapIndexed pointF
        }.forEachIndexed { index, pointF ->
            mPaint.color = mTextArrayedColor
            mPaint.textSize = mTextArrayedSize
            if (mTextArrayedTypeface != null) {
                mPaint.typeface = mTextArrayedTypeface
            }

            when {
                index == 0 -> {
                    //微调修正文字y坐标
                    pointF.y += mPaint.getBottomedY()

                    pointF.y = -(pointF.y.absoluteValue + mVerticalSpaceWidth)
                    mPaint.textAlign = Paint.Align.CENTER
                }
                mTextArray.size / 2f == index.toFloat() -> {
                    //微调修正文字y坐标
                    pointF.y += mPaint.getToppedY()

                    pointF.y = (pointF.y.absoluteValue + mVerticalSpaceWidth)
                    mPaint.textAlign = Paint.Align.CENTER
                }
                index < mTextArray.size / 2f -> {
                    //微调修正文字y坐标
                    if (pointF.y < 0) {
                        pointF.y += mPaint.getBottomedY()
                    } else {
                        pointF.y += mPaint.getToppedY()
                    }

                    pointF.x = (pointF.x.absoluteValue + mHorizontalSpaceWidth)
                    mPaint.textAlign = Paint.Align.LEFT
                }
                index > mTextArray.size / 2f -> {
                    //微调修正文字y坐标
                    if (pointF.y < 0) {
                        pointF.y += mPaint.getBottomedY()
                    } else {
                        pointF.y += mPaint.getToppedY()
                    }

                    pointF.x = -(pointF.x.absoluteValue + mHorizontalSpaceWidth)
                    mPaint.textAlign = Paint.Align.RIGHT
                }
            }

            canvas.drawText(mTextArray[index], pointF.x, pointF.y, mPaint)
            mPaint.utilReset()
        }

        canvas.restore()
    }

    /**
     * 绘制雷达连接区域
     */
    private fun drawConnectionArea(canvas: Canvas) {
        canvas.save()

        val rDeg = 360f / mTextArray.size

        //根据雷达图第一个坐标最为基坐标进行相应计算，算出各个进度坐标
        val bPoint = mPointArray.first()
        mProgressPointArray.mapIndexed { index, pointF ->
            val progress = mProgressArray[index] / mWebMaxProgress.toFloat()
            pointF.x = bPoint.x * progress
            pointF.y = bPoint.y * progress
            pointF.degreePointF(mTempPointF, rDeg * index)

            pointF.x = mTempPointF.x
            pointF.y = mTempPointF.y

            //绘制辅助圆点
            if (debug) {
                mHelperPaint.color = Color.BLACK
                canvas.drawCircle(pointF.x, pointF.y, 5f, mHelperPaint)
                mHelperPaint.utilReset()
            }

            //使用路径连接各个点
            if (index == 0) {
                mPath.moveTo(pointF.x, pointF.y)
            } else {
                mPath.lineTo(pointF.x, pointF.y)
            }
            if (index == mProgressPointArray.lastIndex) {
                mPath.close()
            }
        }
        //绘制区域路径
        mPaint.color = mAreaColor
        canvas.drawPath(mPath, mPaint)
        mPaint.utilReset()

        //绘制区域路径的边框
        mPaint.color = mAreaBorderColor
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mWebLineWidth
        mPaint.strokeJoin = Paint.Join.ROUND
        canvas.drawPath(mPath, mPaint)
        mPath.reset()
        mPaint.utilReset()

        canvas.restore()
    }

    /**
     * 绘制中心文字
     */
    private fun drawCenterText(canvas: Canvas) {
        canvas.save()

        //绘制数字
        mPaint.color = mTextCenteredColor
        mPaint.textSize = mTextArrayedSize / 12 * 20
        mPaint.textAlign = Paint.Align.CENTER
        if (mTextCenteredTypeface != null) {
            mPaint.typeface = mTextCenteredTypeface
        }
        //将坐标系向下微调移动
        canvas.translate(0f, mPaint.fontMetrics.bottom)
        var sum = mProgressArray.sum().toString()
        //添加辅助文本
        if (debug) {
            sum += "ajk你好"
        }
        canvas.drawText(sum, 0f, mPaint.getBottomedY(), mPaint)
        mPaint.utilReset()

        //绘制名字
        mPaint.color = mTextCenteredColor
        mPaint.textSize = mTextArrayedSize / 12 * 10
        mPaint.textAlign = Paint.Align.CENTER
        if (mTextArrayedTypeface != null) {
            mPaint.typeface = mTextArrayedTypeface
        }
        canvas.drawText(mTextCenteredName, 0f, mPaint.getToppedY(), mPaint)
        mPaint.utilReset()

        //绘制辅助线
        if (debug) {
            mHelperPaint.color = Color.RED
            mHelperPaint.strokeWidth = context.dpf2pxf(1f)
            canvas.drawLine(-mWidth, 0f, mWidth, 0f, mHelperPaint)
            mHelperPaint.utilReset()
        }

        canvas.restore()
    }

    companion object {
        var debug = false

        private const val ANIMATE_MODE_TIME = 101
        private const val ANIMATE_MODE_SPEED = 102

        private const val default_webRadius = 100f
        private const val default_webMaxProgress = 100
        private const val default_webLineColor = Color.BLUE
        private const val default_webLineWidth = 1f
        private const val default_textArrayedColor = Color.RED
        private var default_areaColor = Color.parseColor("#CCFFFF00")
        private const val default_areaBorderColor = Color.BLACK
        private const val default_textCenteredName = "雷达值"
        private const val default_textCenteredColor = Color.BLACK
        private const val default_animateTime = 0
        private const val default_animateMode = ANIMATE_MODE_TIME

        private val default_textArray = arrayOf("雷达属性1", "雷达属性2", "雷达属性3", "雷达属性4", "雷达属性5", "雷达属性6")
        private val default_progressArray = if (debug) {
            arrayOf(20, 30, 40, 50, 75, 100)
        } else {
            Array(default_textArray.size) { 0 }
        }
        private val default_oldProgressArray = Array(default_textArray.size) { 0 }
    }
}
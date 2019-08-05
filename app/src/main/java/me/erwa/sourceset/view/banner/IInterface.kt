package me.erwa.sourceset.view.banner

import android.content.Context
import android.view.View

/**
 * @author: drawf
 * @date: 2019/3/19
 * @see: <a href=""></a>
 * @description: 定义 banner 涉及到的接口
 */

/**
 * BannerView依赖的外部实现
 */
interface IBannerView : OnPageChangeListener, IBannerViewBase {

    /**
     * 当count为0时的默认view
     */
    fun getDefaultView(context: Context): View? {
        return null
    }

    /**
     * 默认关闭自动滚动
     */
    fun isDefaultAutoScroll(): Boolean {
        return false
    }

    override fun onPageSelected(position: Int) {}

}

/**
 * 定义页面切换回调
 */
interface OnPageChangeListener {
    fun onPageSelected(position: Int)
}

interface IBannerViewBase {
    fun getCount(): Int

    fun getItemView(context: Context): View

    fun onBindView(itemView: View, position: Int)
}

/**
 * 定义BannerView实例接口
 */
interface IBannerViewInstance : IBannerViewBase {

    fun getContext(): Context

    fun isSmoothMode(): Boolean

    fun getItemViewWidth(): Int

    fun getItemViewMargin(): Int

    fun getItemViewAlign(): Int
}

/**
 * PagerView功能实例需实现的接口
 */
interface IPagerViewInstance {

    /**
     * 设置自动滚动
     * @param intervalInMillis: Int 在INTERVAL模式下为页面切换间隔 在SMOOTH模式下为滚动一页所需时间
     */
    fun startAutoScroll(intervalInMillis: Int)

    fun stopAutoScroll()

    fun getCurrentPosition(): Int

    fun getRealCurrentPosition(realCount: Int): Int

    fun setSmoothMode(enabled: Boolean)

    fun setPageHoldInMillis(pageHoldInMillis: Int)

    fun setOnPageChangeListener(listener: OnPageChangeListener)

    fun notifyDataSetChanged()
}


/**
 * 指示器实例需实现的接口
 */
interface IIndicatorInstance {

    /**
     * 设置外部实现
     */
    fun setIndicator(impl: IIndicator)

    /**
     * 重新布局
     */
    fun doRequestLayout()

    /**
     * 重新绘制
     */
    fun doInvalidate()

}

/**
 * 指示器依赖的外部实现
 */
interface IIndicator {

    /**
     * 获取adapter总数目
     */
    fun getCount(): Int

    /**
     * 获取当前选中页面的索引
     */
    fun getCurrentIndex(): Int

}

/**
 * 定义PagerView工厂接口
 */
interface IPagerViewFactory {
    fun getPagerView(): IPagerViewInstance
}
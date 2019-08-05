package me.erwa.sourceset.view.banner.factory

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import me.erwa.sourceset.R
import me.erwa.sourceset.view.banner.IBannerViewInstance
import me.erwa.sourceset.view.banner.IPagerViewFactory
import me.erwa.sourceset.view.banner.IPagerViewInstance
import me.erwa.sourceset.view.banner.pager.PagerRecyclerView

/**
 * @author: drawf
 * @date: 2019-08-04
 * @see: <a href=""></a>
 * @description: 生成PagerView实例的工厂
 */
internal class PagerViewFactory(
    private val bannerView: IBannerViewInstance,
    private val intervalUseViewPager: Boolean = false
) : IPagerViewFactory {

    /**
     * 工厂根据参数创建对应PagerView实例
     */
    override fun getPagerView(): IPagerViewInstance {
        return if (bannerView.isSmoothMode()) {
            casePagerRecycler(true)
        } else {
            if (intervalUseViewPager) {
                //这里可以根据需要用ViewPager做底层实现
                throw IllegalStateException("这里未使用ViewPager做底层实现")
            } else {
                casePagerRecycler(false)
            }
        }
    }

    /**
     * 处理PagerRecyclerView
     */
    private fun casePagerRecycler(isSmoothMode: Boolean): IPagerViewInstance {
        val recyclerView = PagerRecyclerView(bannerView.getContext())
        recyclerView.layoutManager = LinearLayoutManager(bannerView.getContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemCount(): Int {
                return Int.MAX_VALUE
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val realPos = position % bannerView.getCount()
                bannerView.onBindView(holder.itemView.findViewById(R.id.id_real_item_view), realPos)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val itemWrapper = LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_banner_item_wrapper,
                    parent,
                    false
                ) as RelativeLayout

                //处理ItemViewWrapper的宽
                itemWrapper.layoutParams.width = bannerView.getItemViewWidth() + bannerView.getItemViewMargin()

                //外部实际的ItemView
                val itemView = bannerView.getItemView(parent.context)
                itemView.id = R.id.id_real_item_view
                val ivParams = RelativeLayout.LayoutParams(
                    bannerView.getItemViewWidth(),
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                ivParams.addRule(bannerView.getItemViewAlign())

                //添加ItemView到Wrapper
                itemWrapper.addView(itemView, ivParams)
                return object : RecyclerView.ViewHolder(itemWrapper) {}
            }
        }

        //初始化位置
        recyclerView.scrollToPosition(bannerView.getCount() * 100)
        recyclerView.setSmoothMode(isSmoothMode)

        return recyclerView
    }

}

package me.erwa.sourceset

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_stage_banner_view.*
import kotlinx.android.synthetic.main.activity_stage_radar_view.*
import kotlinx.android.synthetic.main.activity_stage_text_clock.*
import me.erwa.sourceset.view.banner.IBannerView
import java.io.Serializable
import java.util.*
import kotlin.concurrent.timer

/**
 * @author: drawf
 * @date: 2019/4/16
 * @see: <a href=""></a>
 * @description: 用于展示demo的舞台
 */
class StageActivity : AppCompatActivity() {

    private lateinit var mArgParams: ActivityParams
    private lateinit var mTypeMap: MutableMap<String, () -> Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mTypeMap = mutableMapOf(
            TYPE_TEXT_CLOCK to { caseTextClock() },
            TYPE_SHADOW_LAYOUT to { caseShadowLayout() },
            TYPE_RADAR_VIEW to { caseRadarView() },
            TYPE_BANNER_VIEW to { caseBannerView() }
        )

        //处理参数数据
        mArgParams = if (savedInstanceState != null) {
            savedInstanceState.getSerializable(ARG_PARAMS) as ActivityParams
        } else {
            intent.getSerializableExtra(ARG_PARAMS) as ActivityParams
        }

        mTypeMap[mArgParams.showType]?.invoke()
    }

    //********************************
    //* 文字时钟
    //********************************
    private var mTimer: Timer? = null

    private fun caseTextClock() {
        setContentView(R.layout.activity_stage_text_clock)

        mTimer = timer(period = 1000) {
            runOnUiThread {
                stage_textClock.doInvalidate()
            }
        }

    }

    //********************************
    //* 阴影布局
    //********************************
    private fun caseShadowLayout() {
        setContentView(R.layout.activity_stage_shadow_layout)
    }

    //********************************
    //* 雷达图
    //********************************
    val oldProgressList = listOf(20, 20, 20, 20, 20, 20)
    //    val progressList = listOf(100, 20, 30, 40, 50, 60)
    val progressList = mutableListOf(20, 20, 20, 20, 20, 20)
    val tempProgressList = listOf(100, 20, 30, 40, 50, 60)
    private fun caseRadarView() {
        setContentView(R.layout.activity_stage_radar_view)
        val textList = listOf(
            "数学抽象",
            "逻辑推理",
            "数据分析",
            "数学建模",
            "直观想象",
            "数学运算"
        )
        stage_radarView.setTextArray(textList)
        //demo：各属性动画一起执行
//        stage_radarView.setOldProgressList(oldProgressList)
        stage_radarView.setProgressList(progressList)
//        stage_radarView.doInvalidate()
//        stage_radarView.setOnClickListener {
//            stage_radarView.doInvalidate()
//        }

        //demo：各属性动画依次执行
        doAnimSuccessive(0)
        stage_radarView.setOnClickListener {
            progressList.forEachIndexed { index, _ ->
                progressList[index] = oldProgressList[index]
            }
            doAnimSuccessive(0)
        }
    }

    private fun doAnimSuccessive(index: Int) {
        if (progressList.size == tempProgressList.size && index >= 0 && index < progressList.size) {
            progressList[index] = tempProgressList[index]
            stage_radarView.setOldProgressList(oldProgressList)
            stage_radarView.setProgressList(progressList)

            stage_radarView.doInvalidate(index) {
                doAnimSuccessive((it + 1))
            }
        }
    }


    //********************************
    //* Banner图
    //********************************
    private fun caseBannerView() {
        setContentView(R.layout.activity_stage_banner_view)
        val imageList = mutableListOf(
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565003123891&di=6b99987620571a5600e681f1ed9a7e56&imgtype=0&src=http%3A%2F%2Fimg0.ph.126.net%2FqpYuMBtI9tONDBEBXrp6Cg%3D%3D%2F6631251384142500810.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565003197464&di=7de9e4ce6a18c31469492d743472a1b1&imgtype=0&src=http%3A%2F%2Fi0.hdslb.com%2Fbfs%2Farticle%2F5c5a4a0f4f967198c9dd9ccb46174efc61a4707b.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565003219156&di=5061bb93e67f62b54d0d20b23e1bf425&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201611%2F22%2F20161122082357_sjyKQ.jpeg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1565003180679&di=e567595cdfdbffd601297374aac6e2f5&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201701%2F13%2F20170113092725_AucYf.jpeg"
        )

        stage_bannerViewInterval.setBannerViewImpl(object : IBannerView {
            override fun getCount(): Int {
                return imageList.size
            }

            override fun getItemView(context: Context): View {
                return ImageView(context)
            }

            override fun onBindView(itemView: View, position: Int) {
                if (itemView is ImageView) {
                    itemView.scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(itemView.context)
                        .load(imageList[position])
                        .into(itemView)

                    itemView.setOnClickListener {
                        Log.d("BannerView", "itemView onClick >>> $position")
                    }
                }
            }

            override fun getDefaultView(context: Context): View? {
                return View(context).apply {
                    setBackgroundColor(Color.BLUE)
                }
            }

            override fun isDefaultAutoScroll(): Boolean {
                return true
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("BannerView", "onPageSelected interval >>> $position")
            }

        })

        stage_bannerViewSmooth.setBannerViewImpl(object : IBannerView {
            override fun getCount(): Int {
                return imageList.size
            }

            override fun getItemView(context: Context): View {
                return ImageView(context)
            }

            override fun onBindView(itemView: View, position: Int) {
                if (itemView is ImageView) {
                    itemView.scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(itemView.context)
                        .load(imageList[position])
                        .into(itemView)

                    itemView.setOnClickListener {
                        Log.d("BannerView", "itemView onClick >>> $position")
                    }
                }
            }

            override fun getDefaultView(context: Context): View? {
                return View(context).apply {
                    setBackgroundColor(Color.BLUE)
                }
            }

            override fun isDefaultAutoScroll(): Boolean {
                return true
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("BannerView", "onPageSelected smooth >>> $position")
            }

        })

        stage_clearSmooth.setOnClickListener {
            imageList.clear()
            stage_bannerViewInterval.doRecreate()
            stage_bannerViewSmooth.doRecreate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimer?.cancel()
    }

    /**
     * 保存参数数据
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (::mArgParams.isInitialized) {
            outState?.putSerializable(ARG_PARAMS, mArgParams)
        }
    }

    companion object {
        private const val ARG_PARAMS = "ARG_PARAMS"

        const val TYPE_TEXT_CLOCK = "TYPE_TEXT_CLOCK"
        const val TYPE_SHADOW_LAYOUT = "TYPE_SHADOW_LAYOUT"
        const val TYPE_RADAR_VIEW = "TYPE_RADAR_VIEW"
        const val TYPE_BANNER_VIEW = "TYPE_BANNER_VIEW"

        @JvmStatic
        fun navigate(context: Context, params: ActivityParams) {
            val intent = Intent(context, StageActivity::class.java).apply {
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            intent.putExtra(ARG_PARAMS, params)
            context.startActivity(intent)
        }
    }

    data class ActivityParams(
        val showType: String
    ) : Serializable


}
package me.erwa.sourceset

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_stage_text_clock.*
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
            TYPE_SHADOW_LAYOUT to { caseShadowLayout() }
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
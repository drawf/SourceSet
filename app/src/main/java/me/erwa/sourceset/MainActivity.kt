package me.erwa.sourceset

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author: drawf
 * @date: 2019/4/16
 * @see: <a href=""></a>
 * @description:
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_action_textClock.setOnClickListener {
            StageActivity.navigate(
                this,
                StageActivity.ActivityParams(StageActivity.TYPE_TEXT_CLOCK)
            )
        }

        main_action_shadowLayout.setOnClickListener {
            StageActivity.navigate(
                this,
                StageActivity.ActivityParams(StageActivity.TYPE_SHADOW_LAYOUT)
            )
        }

        main_action_radarView.setOnClickListener {
            StageActivity.navigate(
                this,
                StageActivity.ActivityParams(StageActivity.TYPE_RADAR_VIEW)
            )
        }

        main_action_bannerView.setOnClickListener {
            StageActivity.navigate(
                this,
                StageActivity.ActivityParams(StageActivity.TYPE_BANNER_VIEW)
            )
        }
    }

}
package me.erwa.sourceset.view

import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import java.util.*
import kotlin.concurrent.timer

/**
 * @author: drawf
 * @date: 2019-08-12
 * @see: <a href=""></a>
 * @description: 文字时钟动态壁纸服务
 */
class TextClockWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return MyEngine()
    }

    inner class MyEngine : Engine() {
        private val mClockView = TextClockView(this@TextClockWallpaperService.baseContext)
        private val mHandler = Handler()
        private var mTimer: Timer? = null

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            Log.d("clock", "onCreate")
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            Log.d("clock", "onSurfaceCreated")
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            Log.d("clock", "onSurfaceChanged")
        }

        /**
         * Called to inform you of the wallpaper becoming visible or
         * hidden.  <em>It is very important that a wallpaper only use
         * CPU while it is visible.</em>.
         *
         * 当壁纸显示或隐藏是会回调该方法。
         * 很重要的一点是，要只在壁纸显示的时候做绘制操作（占用CPU）。
         */
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            Log.d("clock", "onVisibilityChanged >>> $visible")
            if (visible) {
                startClock()
            } else {
                stopClock()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            Log.d("clock", "onSurfaceDestroyed")
            stopClock()
        }

        override fun onDestroy() {
            super.onDestroy()
            Log.d("clock", "onDestroy")
        }

        /**
         * 开始绘制
         */
        private fun startClock() {
            if (mTimer != null) return

            mTimer = timer(period = 1000) {
                mHandler.post {
                    mClockView.doInvalidate {
                        if (mTimer != null && surfaceHolder != null) {
                            surfaceHolder.lockCanvas()?.let { canvas ->
                                mClockView.initWidthHeight(canvas.width.toFloat(), canvas.height.toFloat())
                                mClockView.draw(canvas)
                                surfaceHolder.unlockCanvasAndPost(canvas)
//                            Log.d("clock", "doInvalidate >>> 触发绘制")
                            }
                        }
                    }
                }
            }
        }

        /**
         * 停止绘制
         */
        private fun stopClock() {
            mTimer?.cancel()
            mTimer = null
            mClockView.stopInvalidate()
        }
    }

}
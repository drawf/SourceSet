@file:JvmName("DrawUtil")

package me.erwa.sourceset.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.ColorInt
import android.view.View

/**
 * @author: drawf
 * @date: 2019/4/30
 * @see: <a href=""></a>
 * @description: 绘制View工具类
 */

//********************************
//* 绘制开发工具
//********************************

/**
 * 创建画笔
 */
@JvmOverloads
fun <T : View> T.createPaint(colorString: String? = null, @ColorInt color: Int? = null): Paint {
    return Paint().apply {
        this.utilReset(colorString, color)
    }
}

/**
 * 自定义画笔重置方法
 */
@JvmOverloads
fun Paint.utilReset(colorString: String? = null, @ColorInt color: Int? = null) {
    this.reset()
    //这里默认值使用白色，可处理掉系统渲染抗锯齿时，人眼可观察到像素颜色
    this.color = color ?: Color.parseColor(colorString ?: "#FFFFFF")
    this.isAntiAlias = true
    this.style = Paint.Style.FILL
    this.strokeWidth = 0f
}

/**
 * dp转px
 */
fun Context.dpf2pxf(dpValue: Float): Float {
    if (dpValue == 0f) return 0f
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f)
}

/**
 * 取RectF的宽
 */
val RectF.width: Float
    get() = right - left

/**
 * 取RectF的高
 */
val RectF.height: Float
    get() = bottom - top

//********************************
//* 绘制辅助工具
//********************************

/**
 * 辅助绿幕背景
 */
fun Canvas.helpGreenCurtain(debug: Boolean) {
    if (debug) {
        this.drawColor(Color.GREEN)
    }
}

/**
 * 辅助红色画笔
 */
fun Paint.helpRedColor(debug: Boolean) {
    helpColor(debug, Color.RED)
}

/**
 * 辅助蓝色画笔
 */
fun Paint.helpBlueColor(debug: Boolean) {
    helpColor(debug, Color.BLUE)
}

/**
 * 辅助画笔颜色
 */
fun Paint.helpColor(debug: Boolean, @ColorInt color: Int) {
    if (debug) {
        this.color = color
    }
}

//********************************
//* 属性计算工具
//********************************

/**
 * Flags基本操作 FlagSet是否包含Flag
 */
fun Int.containsFlag(flag: Int): Boolean {
    return this or flag == this
}

/**
 * Flags基本操作 向FlagSet添加Flag
 */
fun Int.addFlag(flag: Int): Int {
    return this or flag
}

/**
 * Flags基本操作 FlagSet移除Flag
 */
fun Int.removeFlag(flag: Int): Int {
    return this and (flag.inv())
}

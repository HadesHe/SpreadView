package com.example.heyalun.spreadview.view

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.example.heyalun.spreadview.R
import java.util.ArrayList

class SpreadView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private var paintStyle = 0
    private val maxCircleCount: Int//开启绘制新圆的数量
    private var showBitmap: Boolean=true//是否显示中心图片
    private val centerPaint: Paint //中心圆paint
    private var radius = 100 //中心圆半径
    private val spreadPaint: Paint //扩散圆paint
    private var centerX: Float = 0.toFloat()//圆心x
    private var centerY: Float = 0.toFloat()//圆心y
    private var distance = 5 //每次圆递增间距
    private var maxRadius = 80 //最大圆半径
    private val delayMilliseconds = 33//扩散延迟间隔，越大扩散越慢
    private var maxAlpha = 230//初始最大透明度
    private var angle = 0
    private val spreadRadius = ArrayList<Int>()//扩散圆层级数，元素为扩散的距离
    private val alphas = ArrayList<Int>()//对应每层圆的透明度
    private var bitmap: Bitmap? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SpreadView, defStyleAttr, 0)
        radius = a.getInt(R.styleable.SpreadView_spread_radius, radius)
        maxRadius = a.getInt(R.styleable.SpreadView_spread_max_radius, maxRadius)
        val centerColor =
            a.getColor(R.styleable.SpreadView_spread_center_color, ContextCompat.getColor(context, android.R.color.holo_blue_bright))
        val spreadColor =
            a.getColor(R.styleable.SpreadView_spread_spread_color, ContextCompat.getColor(context, android.R.color.holo_blue_bright))
        showBitmap= a.getBoolean(R.styleable.SpreadView_spread_show_image, true)
        distance = a.getInt(R.styleable.SpreadView_spread_distance, distance)
        maxAlpha = a.getInt(R.styleable.SpreadView_spread_max_alpha, 230)
        //透明度范围约束
        if (maxAlpha > 255) {
            maxAlpha = 255
        } else if (maxAlpha < 0) {
            maxAlpha = 0
        }
        maxCircleCount = a.getInt(R.styleable.SpreadView_spread_max_circle_count, 8)
        paintStyle = a.getInt(R.styleable.SpreadView_spread_paint_style, 0)
        //设置圆圈填充类型
        if (paintStyle > -1 && paintStyle < 3) {

        } else {
            paintStyle = 0
        }

        a.recycle()

        if (showBitmap) {
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.circle_loading)
        }

        centerPaint = Paint()
        centerPaint.alpha = maxAlpha
        centerPaint.color = centerColor
        centerPaint.isAntiAlias = true
        //最开始不透明且扩散距离为0
        alphas.add(maxAlpha)
        spreadRadius.add(0)
        spreadPaint = Paint()
        spreadPaint.isAntiAlias = true
        spreadPaint.alpha = maxAlpha

        if (paintStyle == 1) {
            spreadPaint.style = Paint.Style.STROKE
        } else if (paintStyle == 2) {
            spreadPaint.style = Paint.Style.FILL_AND_STROKE
        } else {
            spreadPaint.style = Paint.Style.FILL
        }
        spreadPaint.color = spreadColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //圆心位置
        centerX = (w / 2).toFloat()
        centerY = (h / 2).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in spreadRadius.indices) {
            var alpha = alphas[i]
            spreadPaint.alpha = alpha
            val width = spreadRadius[i]
            //绘制扩散的圆

            if (width < this.width / 2) {
                canvas.drawCircle(centerX, centerY, (radius + width).toFloat(), spreadPaint)
            }

            //每次扩散圆半径递增，圆透明度递减
            if (alpha > 0 && width < 300) {
                alpha = if (alpha - distance > 0) alpha - distance else 0
                alphas[i] = alpha
                spreadRadius[i] = width + distance
            }
        }

        //当最外层扩散圆半径达到最大半径时添加新扩散圆
        if (spreadRadius[spreadRadius.size - 1] > maxRadius) {
            spreadRadius.add(0)
            alphas.add(maxAlpha)
        }
        //超过8个扩散圆，删除最先绘制的圆，即最外层的圆
        if (spreadRadius.size >= maxCircleCount) {
            alphas.removeAt(0)
            spreadRadius.removeAt(0)
        }
        //中间的圆
        canvas.drawCircle(centerX, centerY, radius.toFloat(), centerPaint)
        //TODO 可以在中间圆绘制文字或者图片


        if (showBitmap) {
            drawRotateBitmap(
                canvas,
                null,
                bitmap!!,
                angle.toFloat(),
                centerX - bitmap!!.width / 2,
                centerY - bitmap!!.height / 2
            )
            if (angle > 360) {
                angle -= 360
            } else {
                angle += 10
            }
        }

        //延迟更新，达到扩散视觉差效果
        postInvalidateDelayed(delayMilliseconds.toLong())
    }

    /**
     * 绘制自旋转位图
     *
     * @param canvas
     * @param paint
     * @param bitmap
     * 位图对象
     * @param rotation
     * 旋转度数
     * @param posX
     * 在canvas的位置坐标
     * @param posY
     */
    private fun drawRotateBitmap(
        canvas: Canvas, paint: Paint?, bitmap: Bitmap,
        rotation: Float, posX: Float, posY: Float
    ) {
        val matrix = Matrix()
        val offsetX = bitmap.width / 2
        val offsetY = bitmap.height / 2
        matrix.postTranslate((-offsetX).toFloat(), (-offsetY).toFloat())
        matrix.postRotate(rotation)
        matrix.postTranslate(posX + offsetX, posY + offsetY)
        canvas.drawBitmap(bitmap, matrix, paint)
    }
}
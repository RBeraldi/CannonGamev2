package com.labmacc.cannongame

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withMatrix
import java.lang.Math.abs
import java.lang.Math.pow
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class CannonView(context: Context?) : View(context), View.OnTouchListener {


    init {
        setOnTouchListener(this)
        State.landscape= BitmapFactory.
        decodeStream(getContext().
        assets.open("landscape.jpg"))

        State.basket = BitmapFactory.
        decodeStream(getContext().
        assets.open("basket.png"))
        //State.basket.reconfigure(20,20, Bitmap.Config.ARGB_8888)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        with (State) {
            worldToScreen.setScale(1f, -1f)
            worldToScreen.postTranslate(0f, h.toFloat())
            aspectRatio = w.toFloat() / h.toFloat()
            speed=sqrt((w.toFloat()- 1.05f*cannonWidth)* gravity)
            maxRange= speed* speed/ gravity

            landscape = Bitmap.createScaledBitmap(landscape, w, h, false)
            basket = Bitmap.createScaledBitmap(basket, 150, 150, false)
            basketMatrix.setTranslate(w* basketX,h-200f)
            Log.i("RANGE",""+ maxRange)
        }
    }
    override fun onDraw(cv: Canvas) {
        super.onDraw(cv)

        with (cv){
        with (State){
            drawBitmap(State.landscape,0f,0f,State.ballPaint)
            drawBitmap(basket,State.basketMatrix,State.ballPaint)
            drawText(message,0,State.message.length,100f,100f,State.textPaint)
            withMatrix(worldToScreen) {
                drawBitmap(barrel, cannonMatrix, null)
            }
            if (firing) {
                val now=System.currentTimeMillis()
                val dt = (now-currentFireTime)*mpp
                currentFireTime=now
                ballx+=vx*dt/1000f
                bally+=vy*dt/1000f
                vy-=gravity*dt/1000f
                drawLine(maxRange+ cannonWidth,0f, maxRange+ cannonWidth,0f+cv.height, ballPaint)
                withMatrix(worldToScreen){
                    drawCircle(ballx,bally,ballradius,ballPaint)
                }
                if ((ballx>width) or (bally<0) ) {
                    firing=false
                    message="TRY AGAIN..."
                }

                Log.i("GOAL",""+ballx+"  "+basketX*width)
                if ((abs(ballx-150-basketX*width)<40f) and (bally<90f)) {
                    message="HIT!"
                    firing=false
                }
                Log.i("RANGE", "onDraw: "+ballx+" "+bally)
        }
        } //with state
        } //with canvas
        invalidate()
        return
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                with (State){
                if (!firing) {
                    message="PREPARE"
                    val sen = height.toDouble() - event.y
                    val mod = sqrt(pow(sen, 2.0) + pow(event.x.toDouble(), 2.0))
                    a = asin(sen / mod)

                    cannonMatrix.setRotate(a.toFloat() * 180 / Math.PI.toFloat(), 0f, 0f)

                    vx = speed * cos(a.toFloat())
                    vy = speed * sin(a.toFloat())

                }
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                with (State) {
                    if (firing) return true
                    firing = true
                    message="FIRING..."
                    val initialPosition =
                        floatArrayOf(cannonWidth+0f,
                            cannonHeight/2f)

                    val startingPosition = floatArrayOf(0f,0f)
                    Matrix().apply {
                        setRotate(a.toFloat()* 180 / Math.PI.toFloat())
                        mapVectors(startingPosition,0,initialPosition,0,1)
                    }
                    ballx = startingPosition[0]
                    bally = startingPosition[1]

                    Log.i("INITIAL","INITIAL ANGLE:"+
                            a.toFloat()* 180 / Math.PI.toFloat()+
                            "REST POSITION"+initialPosition[0]+" "+initialPosition[1]+" "+
                    startingPosition[0]+" "+startingPosition[1])
                    currentFireTime = System.currentTimeMillis()

                }

                invalidate()
            }

        }
        return true
    }

}

object State{

    var aspectRatio = 1f
    val ballradius = 30f

    var firing = false

    var ballx=0f
    var bally=0f

    val basketX = 0.9f //Where the put the basket

    var vx =0f
    var vy= 0f

    var message="PREPARE"

    lateinit var landscape : Bitmap
    lateinit var basket : Bitmap
    val mpp = 15f //simulation-time/real-time
    var speed = 490*0.277778f //[km]/[s]
    var gravity = 9.8f //[m]^2/[s]
    var maxRange = speed*speed/ gravity

    var currentFireTime=0L

    val cannonWidth = 200
    val cannonHeight = 100
    val cannonMouth = 20f

    var worldToScreen = Matrix()
    var cannonMatrix = Matrix()
    val basketMatrix = Matrix()

    var a = 0.0

    var barrel : Bitmap = createBarrel()

    val ballPaint = Paint().apply {
        color = Color.parseColor("#AAFF0000")
        strokeWidth = 1f
        textSize=30f
        setShader(RadialGradient(
            0f,0f,
            ballradius*0.5f,
            Color.RED,
            Color.BLUE,
            Shader.TileMode.MIRROR))
        blendMode=BlendMode.MULTIPLY
        //xfermode=PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
       // setColorFilter(LightingColorFilter(Color.WHITE,0) )
    }
    val textPaint = Paint().also {
        it.color = Color.parseColor("#AAAA0000")
        it.strokeWidth = 10f
        it.textSize=120f
    }

    fun createBarrel():Bitmap {
        val bitmap = Bitmap.createBitmap(
            cannonWidth,
            cannonHeight,
            Bitmap.Config.ARGB_8888)
        val paint = Paint().apply {
            //it.color = Color.parseColor("#AAFF0000")
            strokeWidth = 1f
            strokeCap=Paint.Cap.ROUND
            style=Paint.Style.FILL_AND_STROKE
            color=Color.RED
            setShader(
                LinearGradient(0f, 0f, 100f,
                    100f,
                    Color.BLACK,
                    Color.WHITE,
                    Shader.TileMode.MIRROR)
            )
        }
        val path = Path().apply {
            moveTo(0f,0f)
            lineTo(cannonWidth+0f,cannonMouth)
            lineTo(0f+ cannonWidth,0f+ cannonHeight-cannonMouth)
            lineTo(0f,0f+ cannonHeight)
        }
        Canvas(bitmap).apply {
            //drawLines(lines,paint)
            drawPath(path,paint)
        }

        return bitmap
    }
}
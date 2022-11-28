package com.labmacc.cannongame

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withMatrix
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
        State.basket = Bitmap.createScaledBitmap(State.basket, 150, 150, false)
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

            Log.i("RANGE",""+ maxRange)
        }
    }
    override fun onDraw(cv: Canvas) {
        super.onDraw(cv)

        cv.drawBitmap(State.landscape,0f,0f,State.ballPaint)

        cv.drawBitmap(State.basket,Matrix().apply { setTranslate(width/1.5f,height-200f) },State.ballPaint)

        cv.withMatrix(State.worldToScreen){
            //drawBitmap(State.barrel, State.cannonMatrix.also{it.postConcat(State.cannonSetup)} ,null)
            drawBitmap(State.barrel,
                State.cannonMatrix ,null)
            //drawLine(0f,0f,400f,800f, State.ballPaint)
        }

        with (State){
        if (firing) {
            val now=System.currentTimeMillis()
            val dt = (now-currentFireTime)*mpp
            currentFireTime=now
            ballx+=vx*dt/1000f
            bally+=vy*dt/1000f
            vy-=gravity*dt/1000f

            cv.drawLine(maxRange+ cannonWidth,0f, maxRange+ cannonWidth,0f+cv.height, ballPaint)

            cv.withMatrix(worldToScreen){
                drawCircle(ballx,bally,ballradius,ballPaint)
                }

            invalidate()
            if ((ballx>width) or (bally<0) ){
                firing=false
            }
            Log.i("RANGE", "onDraw: "+ballx+" "+bally)
        }
        }
        return
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                with (State){
                if (!firing) {
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

    var vx =0f
    var vy= 0f

    lateinit var landscape : Bitmap
    lateinit var basket : Bitmap

    val ballPaint = Paint().apply {
        color = Color.parseColor("#AAFF0000")
        strokeWidth = 1f
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


    val mpp = 15f //simulation-time/real-time

    //Kinematics parameters
    var speed = 490*0.277778f //[km]/[s]
    var gravity = 9.8f //[m]^2/[s]
    var maxRange = speed*speed/ gravity

    var currentFireTime=0L

    val cannonWidth = 200
    val cannonHeight = 100
    val cannonMouth = 20f

    var worldToScreen = Matrix()
    var cannonMatrix = Matrix()


    var a = 0.0


    var barrel : Bitmap = createBarrel()

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
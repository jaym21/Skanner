package dev.jaym21.skanner.utils

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import dev.jaym21.skanner.R
import kotlin.math.hypot

class ImageViewerActivity : AppCompatActivity() {

    private var xCoordinate: Float = 0.0f
    private var yCoordinate: Float = 0.0f
    private var screenCenterX: Double = 0.0
    private var screenCenterY: Double = 0.0
    private var alpha: Int = 0
    private lateinit var imageView: ImageView
    private lateinit var back: ImageView
    private lateinit var view: View

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        val imageUrl = intent.getStringExtra("clickedImage")
        val imageBitmap = BitmapFactory.decodeFile(imageUrl)

        imageView = findViewById(R.id.imageView)
        back = findViewById(R.id.ivExitImageViewer)
        view = findViewById(R.id.layout)
        view.background.alpha = 255

        Glide.with(this).load(imageBitmap).into(imageView)

        back.setOnClickListener {
            finish()
        }

        //calculating max hypo value and center of screen
        val display = resources.displayMetrics;
        screenCenterX = (display.widthPixels / 2).toDouble();
        screenCenterY = ((display.heightPixels - getStatusBarHeight()) / 2).toDouble();
        val maxHypo = hypot(screenCenterX, screenCenterY);

        imageView.setOnTouchListener( object: View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                //calculating hypo value of current imageview position according to center
                val centerYPos = imageView.y + (imageView.height / 2);
                val centerXPos = imageView.x + (imageView.width / 2);
                val a = screenCenterX - centerXPos;
                val b = screenCenterY - centerYPos;
                val hypo = hypot(a, b);


                //changing alpha of background of layout
                alpha = ((hypo * 255) / maxHypo).toInt()
                if (alpha < 255)
                    view.background.alpha = 255 - alpha

                when (event!!.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        xCoordinate = imageView.x - event.rawX
                        yCoordinate = imageView.y - event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        imageView.animate().x(event.rawX + xCoordinate)
                            .y(event.rawY + yCoordinate).setDuration(0).start()
                    }
                    MotionEvent.ACTION_UP -> {
                        if (alpha > 70) {
                            supportFinishAfterTransition()
                            return false
                        } else {
                            imageView.animate().x(0F)
                                .y((screenCenterY -imageView.height / 2).toFloat())
                                .setDuration(100).start()
                            view.background.alpha = 255
                        }
                    }
                    else ->
                        return false
                }
                return true
            }
        })
    }


    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}
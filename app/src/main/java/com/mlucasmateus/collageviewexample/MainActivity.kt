package com.mlucasmateus.collageviewexample

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.mlucasmateus.collageview.VideoCollageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val constraintLayout: ConstraintLayout = findViewById(R.id.constraintLayout)
        val collageView = VideoCollageView(this)
        collageView.setBackgroundColor(Color.CYAN)
        val constraintLayoutParams = ConstraintLayout.LayoutParams(600,600)
        collageView.layoutParams = constraintLayoutParams
        constraintLayout.addView(collageView)
    }
}
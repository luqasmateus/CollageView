package com.mlucasmateus.collageviewexample

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.mlucasmateus.collageview.CollageView
import com.mlucasmateus.collageview.VideoCollageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val constraintLayout: ConstraintLayout = findViewById(R.id.constraintLayout)
        val constraintLayoutParams = ConstraintLayout.LayoutParams(600,600)
        constraintLayoutParams.topToBottom = R.id.button
        constraintLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        constraintLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        constraintLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        //var collageView = VideoCollageView(this)
        val collageViewBuilder = VideoCollageView(this).Builder()
        collageViewBuilder.apply {
            setRowCount(2)
            setColumnCount(2)
            addSlots(CollageView.Slot(),
                CollageView.Slot(0, 1),
                CollageView.Slot(1,0),
                CollageView.Slot(1,1))
        }
        val collageView = collageViewBuilder.build(600, 600) as VideoCollageView

        collageView.layoutParams = constraintLayoutParams
        collageView.setBackgroundColor(Color.CYAN)
        constraintLayout.addView(collageView)
    }
}
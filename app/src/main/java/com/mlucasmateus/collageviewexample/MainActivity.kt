package com.mlucasmateus.collageviewexample

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.mlucasmateus.collageview.CollageView

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

        val collageView = CollageView(this)
        collageView.layoutParams = constraintLayoutParams
        collageView.buildGrid(CollageView.GridAttributes().apply {
            setRowCount(1)
            setColumnCount(3)
            addSlots(CollageView.Slot(),
                CollageView.Slot(columnPosition = 1),
                CollageView.Slot(columnPosition = 2))
        })
        collageView.setBackgroundColor(Color.CYAN)
        constraintLayout.addView(collageView)

        collageView.setGridBorderSize(10)
    }
}

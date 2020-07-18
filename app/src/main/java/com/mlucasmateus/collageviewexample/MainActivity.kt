package com.mlucasmateus.collageviewexample

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.widget.Button
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

        collageView.setBorderSize(10)

        val path = getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath
        collageView.addImage("$path/TestImage.jpg", 0)
        collageView.addVideo("$path/TestVideo.mp4", 1, MediaPlayer.OnPreparedListener {
            it.start()
            it.pause()
        })
        collageView.addItem(Button(this), 2)
    }
}

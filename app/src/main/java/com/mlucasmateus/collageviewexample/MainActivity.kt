package com.mlucasmateus.collageviewexample

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.mlucasmateus.collageview.CollageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val constraintLayout: ConstraintLayout = findViewById(R.id.constraintLayout)
        val constraintLayoutParams = ConstraintLayout.LayoutParams(600,600)
        constraintLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        constraintLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        constraintLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        constraintLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

        val collageView = CollageView(this)
        collageView.layoutParams = constraintLayoutParams
        collageView.buildGrid(CollageView.GridAttributes().apply {
            setRowCount(2)
            setColumnCount(3)
            addSlots(CollageView.Slot(),
                CollageView.Slot(columnPosition = 1),
                CollageView.Slot(columnPosition = 2),
                CollageView.Slot(rowPosition = 1))
        })
        collageView.setBackgroundColor(Color.CYAN)
        constraintLayout.addView(collageView)

        val path = getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath
        collageView.setBorderSize(10)
        collageView.fillWithButtons(R.drawable.flying_cat, View.OnClickListener {
            collageView.fillWithImages("$path/TestImage.jpg")
        })

        collageView.addImage("$path/TestImage.jpg", 0)
        collageView.addVideo("$path/TestVideo.mp4", 2, MediaPlayer.OnPreparedListener {
            it.start()
            it.pause()
        })

        collageView.getButton(1).setOnClickListener {
            Toast.makeText(this, "Testing Toast", Toast.LENGTH_LONG).show()
            collageView.getImage(0).alpha = 0.2f
            collageView.buildGrid(collageView.getGridAttributes()
                .addSlots(CollageView.Slot(rowPosition = 1, columnPosition = 1, columnSpan = 2)))
            collageView.setBorderSize(15)
        }
    }
}

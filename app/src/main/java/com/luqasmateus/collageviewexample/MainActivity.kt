package com.luqasmateus.collageviewexample

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.luqasmateus.collageview.CollageView
import com.luqasmateus.collageview.GridAttributes
import com.luqasmateus.collageview.Slot

class MainActivity : AppCompatActivity() {
    private lateinit var collageView: CollageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val constraintLayout: ConstraintLayout = findViewById(R.id.constraintLayout)
        val constraintLayoutParams = ConstraintLayout.LayoutParams(600,600)
        constraintLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        constraintLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        constraintLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        constraintLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

        collageView = CollageView(this)
        collageView.layoutParams = constraintLayoutParams
        collageView.buildGrid(GridAttributes().apply {
            setRowCount(2)
            setColumnCount(3)
            addSlots(
                Slot(),
                Slot(columnPosition = 1),
                Slot(columnPosition = 2),
                Slot(rowPosition = 1))
        })
        collageView.setBackgroundColor(Color.CYAN)
        constraintLayout.addView(collageView)

        val path = getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath
        collageView.setBorderSize(10)
        collageView.fill(CollageView.Button(0,null, R.drawable.flying_cat_xml),0,
            object: CollageView.OnItemClickListener {
            override fun onItemClick(item: View, index: Int) {
                Toast.makeText(this@MainActivity, "A FLYING CAT!!!", Toast.LENGTH_LONG).show()
            }
        })

        collageView.add(CollageView.Image(0,null, "$path/TestImage.jpg"),
            CollageView.Image(1,null, "$path/TestImage.jpg"))
        collageView.add(CollageView.Video(2, null, "$path/TestVideo.mp4", MediaPlayer.OnPreparedListener {
            it.start()
            it.isLooping = true
        }))

        collageView.getButton(3)?.setOnClickListener {
            Toast.makeText(this, "Testing Toast", Toast.LENGTH_LONG).show()
            collageView.getImage(0)?.alpha = 0.2f
            collageView.removeItem(1)
            collageView.rebuildGrid(collageView.getGridAttributes()
                .addSlots(Slot(rowPosition = 1, columnPosition = 1, columnSpan = 2)))
            collageView.add(CollageView.Button(4, View.OnClickListener {
                Toast.makeText(this, "Rebuild works", Toast.LENGTH_LONG).show()
                collageView.setBorderSize(25)
                collageView.fillEmptySlots(CollageView.Button(-1, null, R.drawable.flying_cat_xml), 0,
                    object: CollageView.OnItemClickListener{
                        override fun onItemClick(item: View, index: Int) {
                            Toast.makeText(this@MainActivity, "$index", Toast.LENGTH_SHORT).show()
                        }
                    })
            }, R.drawable.flying_cat_xml))
        }
    }

    override fun onStop() {
        super.onStop()
        collageView.release()
    }
}

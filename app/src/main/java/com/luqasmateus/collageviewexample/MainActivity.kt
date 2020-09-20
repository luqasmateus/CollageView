package com.luqasmateus.collageviewexample

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.luqasmateus.collageview.CollageView
import com.luqasmateus.collageview.GridAttributes
import com.luqasmateus.collageview.MediaGenerator
import com.luqasmateus.collageview.Slot

class MainActivity : AppCompatActivity() {
    private lateinit var collageView: CollageView
    private lateinit var mediaGenerator: MediaGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val constraintLayout: ConstraintLayout = findViewById(R.id.constraintLayout)
        val constraintLayoutParams = ConstraintLayout.LayoutParams(600,600)
        constraintLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        constraintLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        constraintLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        constraintLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

        val path = getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath
        collageView = CollageView(this)
        collageView.layoutParams = constraintLayoutParams
        constraintLayout.addView(collageView)

        collageView.buildGrid(GridAttributes().apply {
            setRowCount(2)
            setColumnCount(3)
            addSlots(
                Slot(),
                Slot(columnPosition = 1),
                Slot(columnPosition = 2),
                Slot(rowPosition = 1))
        })
        collageView.setBorderSize(15)
        collageView.setBackgroundColor(Color.CYAN)
        mediaGenerator = path?.let { MediaGenerator(it) }!!

        collageView.fill(CollageView.Button(0,null, R.drawable.flying_cat_xml),0,
            object: CollageView.OnItemClickListener {
            override fun onItemClick(item: View, index: Int) {
                Toast.makeText(this@MainActivity, "A FLYING CAT!!!", Toast.LENGTH_LONG).show()
            }
        })

        collageView.add(CollageView.Image(0,null, "$path/TestImage.jpg"),
            CollageView.Image(1,null, "$path/TestImage.jpg"))
        collageView.fill(CollageView.Video(2, null, "$path/under3.mp4", {
            it.start()
            it.pause()
        }))

        /*collageView.getButton(3)?.setOnClickListener {
            Toast.makeText(this, "Testing Toast", Toast.LENGTH_LONG).show()
            collageView.getImage(0)?.alpha = 0.2f
            collageView.removeItem(1)
            collageView.rebuildGrid(collageView.getGridAttributes()
                .addSlots(Slot(rowPosition = 1, columnPosition = 1, columnSpan = 2)))
            collageView.add(CollageView.Button(4, {
                Toast.makeText(this, "Rebuild works", Toast.LENGTH_LONG).show()
                collageView.setBorderSize(25)
                collageView.fillEmptySlots(CollageView.Button(-1, null, R.drawable.flying_cat_xml), 0,
                    object: CollageView.OnItemClickListener{
                        override fun onItemClick(item: View, index: Int) {
                            Toast.makeText(this@MainActivity, "$index", Toast.LENGTH_SHORT).show()
                        }
                    })
                collageView.getButton(1)?.setOnClickListener {
                    collageView.rebuildGrid(GridAttributes().addSlots(Slot()))
                    collageView.add(CollageView.Video(0, null, "$path/TestVideo.mp4") {
                        it.start()
                        it.pause()
                    })
                }
            }, R.drawable.flying_cat_xml))
        }*/
    }

    override fun onStop() {
        super.onStop()
        collageView.release()
    }

    fun startGeneratingVideo(view: View) {
        mediaGenerator.generate(collageView, MediaGenerator.VIDEO) {
            findViewById<ProgressBar>(R.id.progress).incrementProgressBy(it)
        }
    }

    fun startGeneratingImage(view: View) {
        mediaGenerator.generate(collageView, MediaGenerator.IMAGE) {
            findViewById<ProgressBar>(R.id.progress).incrementProgressBy(it)
        }
    }
}

package com.mlucasmateus.collageview

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.Px
import com.yqritc.scalablevideoview.ScalableType
import com.yqritc.scalablevideoview.ScalableVideoView
import kotlin.math.floor

class CollageView(context: Context): GridLayout(context) {
    private var gridBuilt = false
    private var cellWidth = 0
    private var cellHeight = 0
    private var borderSize = 0
    private val items: ArrayList<Item> = arrayListOf()
    private val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT)
    private lateinit var gridAttributes: GridAttributes

    interface OnItemClickListener {
        fun onItemClick(item: View, index: Int)
    }

    private open class Item(val index: Int, val listener: OnClickListener?)
    private class Image(index: Int, listener: OnClickListener?, val path: String): Item(index, listener)
    private class Video(index: Int, listener: OnClickListener?, val path: String,
                val onPreparedListener: MediaPlayer.OnPreparedListener): Item(index, listener)
    private class Button(index: Int, listener: OnClickListener?, val resId: Int): Item(index, listener)

    private fun addItem(item: View, index: Int, listener: OnClickListener? = null) {
        if (index >= 0 && index < gridAttributes.getSlotCount()) {
            val childView = getChildAt(index) as LinearLayout
            childView.removeAllViews()
            item.layoutParams = linearLayoutParams
            listener ?: item.setOnClickListener(listener)
            childView.addView(item)
        }else
            throw NoSuchFieldException("This CollageView doesn't have a slot at index $index. " +
                    "Available slot indexes: [0,${gridAttributes.getSlotCount() - 1}]")
    }

    fun getItem(index: Int): View? = (getChildAt(index) as LinearLayout?)?.getChildAt(0)

    fun removeItem(index: Int) {
        (getChildAt(index) as LinearLayout?)?.removeAllViews()
        items.remove(items[index])
    }

    fun addVideo(path: String, index: Int, onPreparedListener: MediaPlayer.OnPreparedListener, listener: OnClickListener? = null) {
        val video = Video(index, listener, path, onPreparedListener)
        addVideo(video)
        items.add(video)
    }

    private fun addVideo(video: Video) {
        val videoView = ScalableVideoView(context)
        videoView.setDataSource(video.path)
        videoView.setScalableType(ScalableType.CENTER_CROP)
        videoView.prepare(video.onPreparedListener)
        addItem(videoView, video.index, video.listener)
    }

    fun fillWithVideos(path: String, onPreparedListener: MediaPlayer.OnPreparedListener, listener: OnItemClickListener? = null) {
        for (i in 0 until gridAttributes.getSlotCount())
            addVideo(path, i, onPreparedListener, OnClickListener {
                listener?.onItemClick(it, i)
            })
    }

    fun getVideo(index: Int) = getItem(index) as ScalableVideoView?

    fun addImage(path: String, index: Int, listener: OnClickListener? = null) {
        val image = Image(index, listener, path)
        addImage(image)
        items.add(image)
    }

    private fun addImage(image: Image) {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageURI(Uri.parse(image.path))
        addItem(imageView, image.index, image.listener)
    }

    fun fillWithImages(path: String, listener: OnItemClickListener? = null) {
        for (i in 0 until gridAttributes.getSlotCount())
            addImage(path, i, OnClickListener {
                listener?.onItemClick(it, i)
            })
    }

    fun getImage(index: Int) = getItem(index) as ImageView?

    fun addButton(resId: Int, index: Int, listener: OnClickListener? = null) {
        val button = Button(index, listener, resId)
        addButton(button)
        items.add(button)
    }

    private fun addButton(button: Button) {
        val imageButton = ImageButton(context)
        imageButton.setBackgroundColor(Color.BLACK)
        imageButton.setImageResource(button.resId)
        imageButton.scaleType = ImageView.ScaleType.FIT_CENTER
        imageButton.adjustViewBounds = true
        addItem(imageButton, button.index, button.listener)
    }

    fun fillWithButtons(resId: Int, listener: OnItemClickListener? = null) {
        for (i in 0 until gridAttributes.getSlotCount())
            addButton(resId, i, OnClickListener {
                listener?.onItemClick(it, i)
            })
    }

    fun getButton(index: Int) = getItem(index) as ImageButton?

    fun setBorderSize(@Px borderSize: Int) {
        val floorBorderSize = floor(borderSize/2f).toInt()
        val ceilingBorderSize = floor(borderSize/2f).toInt()

        gridAttributes.getSlotList().forEachIndexed { i: Int, slot: Slot ->
            getChildAt(i).setPadding(
                if (slot.columnPosition == 0) borderSize else ceilingBorderSize,
                if (slot.rowPosition == 0) borderSize else ceilingBorderSize,
                if (slot.columnPosition + slot.columnSpan == gridAttributes.getColumnCount())
                    borderSize else floorBorderSize,
                if (slot.rowPosition + slot.rowSpan == gridAttributes.getRowCount())
                    borderSize else floorBorderSize
            )
        }
        this.borderSize = borderSize
    }

    fun releaseAt(index: Int) {
        getVideo(index)?.apply {
            stop()
            reset()
            release()
        }
    }

    fun release() {
        items.forEach{
            if (it is Video)
                releaseAt(it.index)
        }
    }

    fun getGridAttributes() = gridAttributes

    fun buildGrid(gridAttributes: GridAttributes = GridAttributes()) {
        if (layoutParams == null)
            throw IllegalStateException("LayoutParams wasn't set yet. This CollageView doesn't have a height or a width")

        removeAllViews()
        this.gridAttributes = gridAttributes

        rowCount = gridAttributes.getRowCount()
        columnCount = gridAttributes.getColumnCount()

        cellWidth = layoutParams.width / columnCount
        cellHeight = layoutParams.height / rowCount

        gridAttributes.getSlotList().forEach {
            addView(getItemPlaceholder(it))
        }
        gridBuilt = true
    }

    fun rebuildGrid(gridAttributes: GridAttributes = GridAttributes()) {
        if (!gridBuilt) {
            buildGrid(gridAttributes)
            return
        }

        release()
        buildGrid(gridAttributes)
        setBorderSize(borderSize)

        items.forEach {
            when (it) {
                is Image -> addImage(it)
                is Video -> addVideo(it)
                is Button -> addButton(it)
            }
        }
    }

    private fun getItemPlaceholder(slot: Slot): LinearLayout {
        val itemPlaceholder = LinearLayout(context)

        val itemPlaceholderParams = LayoutParams(slot.rowSpec, slot.columnSpec)
        itemPlaceholderParams.width = cellWidth * slot.columnSpan
        itemPlaceholderParams.height = cellHeight * slot.rowSpan

        itemPlaceholder.layoutParams = itemPlaceholderParams
        itemPlaceholder.addView(getBasicView())
        return itemPlaceholder
    }

    private fun getBasicView(): View {
        val basicView = View(context)
        basicView.setBackgroundColor(Color.BLACK)
        basicView.layoutParams = linearLayoutParams
        return basicView
    }
}
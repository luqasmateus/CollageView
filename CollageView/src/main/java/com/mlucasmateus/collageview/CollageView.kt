package com.mlucasmateus.collageview

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.Px
import com.yqritc.scalablevideoview.ScalableType
import com.yqritc.scalablevideoview.ScalableVideoView
import kotlin.math.floor

class CollageView(context: Context): GridLayout(context) {
    private var gridBuilt = false
    private var cellWidth = 0
    private var cellHeight = 0
    private var borderSize = 0
    private var items: ArrayList<Item?> = arrayListOf()
    private val frameLayoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER }
    private lateinit var gridAttributes: GridAttributes

    interface OnItemClickListener {
        fun onItemClick(item: View, index: Int)
    }

    abstract class Item(val index: Int, val listener: OnClickListener?)
    class Image(index: Int, listener: OnClickListener?, val path: String): Item(index, listener)
    class Video(index: Int, listener: OnClickListener?, val path: String,
                val onPreparedListener: MediaPlayer.OnPreparedListener): Item(index, listener)
    class Button(index: Int, listener: OnClickListener?, val resId: Int): Item(index, listener)

    private fun addItem(item: View, index: Int, listener: OnClickListener? = null) {
        if (index >= 0 && index < gridAttributes.getSlotCount()) {
            val childView = getChildAt(index) as FrameLayout
            removeItem(index)
            if (listener != null) item.setOnClickListener(listener)
            childView.addView(item)
        }else
            throw NoSuchFieldException("This CollageView doesn't have a slot at index $index. " +
                    "Available slot indexes: [0,${gridAttributes.getSlotCount() - 1}]")
    }

    fun getItem(index: Int): View? = (getChildAt(index) as FrameLayout?)?.getChildAt(1)

    fun removeItem(index: Int) {
        val linearLayout = getChildAt(index) as FrameLayout
        if (linearLayout.childCount > 1) {
            linearLayout.removeViewAt(1)
            items.removeAt(index)
        }
    }

    fun addVideo(path: String, index: Int, onPreparedListener: MediaPlayer.OnPreparedListener, listener: OnClickListener? = null) {
        val video = Video(index, listener, path, onPreparedListener)
        addVideo(video)
        items.add(index, video)
    }

    private fun addVideo(video: Video) {
        val videoView = ScalableVideoView(context)
        videoView.setDataSource(video.path)
        videoView.setScalableType(ScalableType.CENTER_CROP)
        videoView.prepare(video.onPreparedListener)
        addItem(videoView, video.index, video.listener)
    }

    @Deprecated("Please use fill() or fillEmptySpaces() instead." +
            " This method will no longer be available in version 1.0.0")
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
        items.add(index, image)
    }

    private fun addImage(image: Image) {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageURI(Uri.parse(image.path))
        addItem(imageView, image.index, image.listener)
    }

    @Deprecated("Please use fill() or fillEmptySpaces() instead." +
            " This method will no longer be available in version 1.0.0")
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
        items.add(index, button)
    }

    private fun addButton(button: Button) {
        val imageButton = ImageButton(context)
        imageButton.layoutParams = frameLayoutParams
        imageButton.setBackgroundColor(Color.BLACK)
        imageButton.setImageResource(button.resId)
        imageButton.scaleType = ImageView.ScaleType.FIT_CENTER
        imageButton.adjustViewBounds = true
        addItem(imageButton, button.index, button.listener)
    }

    @Deprecated("Please use fill() or fillEmptySpaces() instead." +
            " This method will no longer be available in version 1.0.0")
    fun fillWithButtons(resId: Int, listener: OnItemClickListener? = null) {
        for (i in 0 until gridAttributes.getSlotCount())
            addButton(resId, i, OnClickListener {
                listener?.onItemClick(it, i)
            })
    }

    fun getButton(index: Int) = getItem(index) as ImageButton?

    fun fill(startIndex: Int = 0, listener: OnItemClickListener? = null, vararg items: Item) {
        TODO()
    }

    fun fillEmptySpaces(startIndex: Int = 0, listener: OnItemClickListener? = null, vararg items: Item) {
        TODO()
    }

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

        items.ensureCapacity(gridAttributes.getSlotCount())
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

    private fun getItemPlaceholder(slot: Slot): FrameLayout {
        val itemPlaceholder = FrameLayout(context)

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
        return basicView
    }
}
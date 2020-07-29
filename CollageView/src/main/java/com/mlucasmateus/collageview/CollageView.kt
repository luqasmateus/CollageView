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
    private var items: Array<Item?> = arrayOf()
    private val frameLayoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER }
    private lateinit var gridAttributes: GridAttributes

    interface OnItemClickListener {
        fun onItemClick(item: View, index: Int)
    }

    abstract class Item(var index: Int, var listener: OnClickListener?)
    class Image(index: Int, listener: OnClickListener?, val path: String): Item(index, listener)
    class Video(index: Int, listener: OnClickListener?, val path: String, val onPreparedListener: MediaPlayer.OnPreparedListener): Item(index, listener)
    class Button(index: Int, listener: OnClickListener?, val resId: Int): Item(index, listener)

    fun add(vararg items: Item) {
        items.forEach {
            when (it) {
                is Image -> addImage(it)
                is Video -> addVideo(it)
                is Button -> addButton(it)
                else -> throw IllegalArgumentException("The argument needs to be an Item type.")
            }
            this.items[it.index] = it
        }
    }

    private fun addImage(image: Image) {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageURI(Uri.parse(image.path))
        append(imageView, image.index, image.listener)
    }

    private fun addVideo(video: Video) {
        val videoView = ScalableVideoView(context)
        videoView.setDataSource(video.path)
        videoView.setScalableType(ScalableType.CENTER_CROP)
        videoView.prepare(video.onPreparedListener)
        append(videoView, video.index, video.listener)
    }

    private fun addButton(button: Button) {
        val imageButton = ImageButton(context)
        imageButton.layoutParams = frameLayoutParams
        imageButton.setBackgroundColor(Color.BLACK)
        imageButton.setImageResource(button.resId)
        imageButton.scaleType = ImageView.ScaleType.FIT_CENTER
        imageButton.adjustViewBounds = true
        append(imageButton, button.index, button.listener)
    }

    fun fill(item: Item, startIndex: Int = 0, listener: OnItemClickListener? = null) {
       for (i in startIndex until items.size){
           val clickListener = OnClickListener {
               listener?.onItemClick(it, i)
           }
           add(when (item) {
               is Image -> Image(i, clickListener, item.path)
               is Video -> Video(i, clickListener, item.path, item.onPreparedListener)
               is Button -> Button(i, clickListener, item.resId)
               else -> throw IllegalArgumentException("The argument needs to be an Item type.")
           }
           )
       }
    }

    fun fillEmptySlots(item: Item, startIndex: Int = 0, listener: OnItemClickListener? = null) {
        for (i in startIndex until items.size) {
            if (items[i] == null) {
                val clickListener = OnClickListener {
                    listener?.onItemClick(it, i)
                }
                add(when (item) {
                        is Image -> Image(i, clickListener, item.path)
                        is Video -> Video(i, clickListener, item.path, item.onPreparedListener)
                        is Button -> Button(i, clickListener, item.resId)
                        else -> throw IllegalArgumentException("The argument needs to be an Item type.")
                    }
                )
            }
        }
    }

    private fun append(item: View, index: Int, listener: OnClickListener? = null) {
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
            items[index] = null
        }
    }

    fun getVideo(index: Int) = getItem(index) as ScalableVideoView?

    fun getImage(index: Int) = getItem(index) as ImageView?

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
        items.forEachIndexed { index, item ->
            if (item is Video)
                releaseAt(index)
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

        val itemsAux = items
        items = Array(gridAttributes.getSlotCount()) {
            if (it >= itemsAux.size)
                return@Array null
            itemsAux[it]
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
            if (it != null) {
                add(it)
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

    @Deprecated("Please use add() instead. This method will no longer be available in version 1.0.0")
    fun addVideo(path: String, index: Int, onPreparedListener: MediaPlayer.OnPreparedListener, listener: OnClickListener? = null) {
        val video = Video(index,listener, path, onPreparedListener)
        addVideo(video)
        items[index] = video
    }

    @Deprecated("Please use add() instead. This method will no longer be available in version 1.0.0")
    fun addImage(path: String, index: Int, listener: OnClickListener? = null) {
        val image = Image(index, listener, path)
        addImage(image)
        items[index] = image
    }

    @Deprecated("Please use add() instead. This method will no longer be available in version 1.0.0")
    fun addButton(resId: Int, index: Int, listener: OnClickListener? = null) {
        val button = Button(index, listener, resId)
        addButton(button)
        items[index] = button
    }

    @Deprecated("Please use fill() or fillEmptySpaces() instead." +
            " This method will no longer be available in version 1.0.0")
    fun fillWithVideos(path: String, onPreparedListener: MediaPlayer.OnPreparedListener, listener: OnItemClickListener? = null) {
        for (i in 0 until gridAttributes.getSlotCount())
            addVideo(path, i, onPreparedListener, OnClickListener {
                listener?.onItemClick(it, i)
            })
    }

    @Deprecated("Please use fill() or fillEmptySpaces() instead." +
            " This method will no longer be available in version 1.0.0")
    fun fillWithImages(path: String, listener: OnItemClickListener? = null) {
        for (i in 0 until gridAttributes.getSlotCount())
            addImage(path, i, OnClickListener {
                listener?.onItemClick(it, i)
            })
    }

    @Deprecated("Please use fill() or fillEmptySpaces() instead." +
            " This method will no longer be available in version 1.0.0")
    fun fillWithButtons(resId: Int, listener: OnItemClickListener? = null) {
        for (i in 0 until gridAttributes.getSlotCount())
            addButton(resId, i, OnClickListener {
                listener?.onItemClick(it, i)
            })
    }
}
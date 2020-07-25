package com.mlucasmateus.collageview

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
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
    private var cellWidth = 0
    private var cellHeight = 0
    private val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT)
    private lateinit var gridAttributes: GridAttributes

    /**Class Slot is used to define the position and the span of an item inside
     * CollageView.*/
    class Slot(val rowPosition: Int = 0,
                    val columnPosition: Int = 0,
                    val rowSpan: Int = 1,
                    val columnSpan: Int = 1) {
        val rowSpec: Spec = spec(rowPosition, rowSpan)
        val columnSpec: Spec = spec(columnPosition, columnSpan)
    }

    class GridAttributes {
        private var rowCount = 1
        private var columnCount = 1
        private var slotList: Array<Slot> = arrayOf()

        fun setRowCount(rows: Int): GridAttributes {
            rowCount = rows
            return this
        }

        fun getRowCount() = rowCount

        fun setColumnCount(cols: Int): GridAttributes {
            columnCount = cols
            return this
        }

        fun getColumnCount() = columnCount

        fun addSlots(vararg slotList: Slot): GridAttributes {
            this.slotList = this.slotList.plus(slotList)
            return this
        }

        fun getSlotList() = slotList

        fun getSlotCount() = slotList.size
    }

    fun addItem(item: View, index: Int) {
        if (index >= 0 && index < gridAttributes.getSlotCount()) {
            val childView = getChildAt(index) as LinearLayout
            childView.removeAllViews()
            item.layoutParams = linearLayoutParams
            childView.addView(item)
        }else
            throw NoSuchFieldException("This CollageView doesn't have a slot at index $index. " +
                    "Available slot indexes: [0,${gridAttributes.getSlotCount() - 1}]")
    }

    fun fillWithItems(item: View) {
        for (i in 0 until gridAttributes.getSlotCount())
            addItem(item, i)
    }

    fun getItem(index: Int): View = (getChildAt(index) as LinearLayout).getChildAt(0)

    fun addVideo(path: String, index: Int, onPreparedListener: MediaPlayer.OnPreparedListener) {
        val videoView = ScalableVideoView(context)
        videoView.setDataSource(path)
        videoView.setScalableType(ScalableType.CENTER_CROP)
        videoView.prepare(onPreparedListener)
        addItem(videoView, index)
    }

    fun fillWithVideos(path: String, onPreparedListener: MediaPlayer.OnPreparedListener) {
        for (i in 0 until gridAttributes.getSlotCount())
            addVideo(path, i, onPreparedListener)
    }

    fun getVideo(index: Int): ScalableVideoView = getItem(index) as ScalableVideoView

    fun addImage(path: String, index: Int) {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageURI(Uri.parse(path))
        addItem(imageView, index)
    }

    fun fillWithImages(path: String) {
        for (i in 0 until gridAttributes.getSlotCount())
            addImage(path, i)
    }

    fun getImage(index: Int) = getItem(index) as ImageView

    fun addButton(resId: Int, index: Int, listener: OnClickListener? = null) {
        val imageButton = ImageButton(context)
        imageButton.setBackgroundColor(Color.BLACK)
        imageButton.setImageResource(resId)
        imageButton.scaleType = ImageView.ScaleType.FIT_CENTER
        imageButton.adjustViewBounds = true
        if (listener != null)
            imageButton.setOnClickListener(listener)
        addItem(imageButton, index)
    }

    fun fillWithButtons(resId: Int, listener: OnClickListener? = null) {
        for (i in 0 until gridAttributes.getSlotCount())
            addButton(resId, i, listener)
    }

    fun getButton(index: Int) = getItem(index) as ImageButton

    fun getGridAttributes() = gridAttributes

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
    }

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
    }

    private fun getItemPlaceholder(slot: Slot): LinearLayout {
        val videoPlaceholder = LinearLayout(context)

        val videoPlaceholderParams = LayoutParams(slot.rowSpec, slot.columnSpec)
        videoPlaceholderParams.width = cellWidth * slot.columnSpan
        videoPlaceholderParams.height = cellHeight * slot.rowSpan

        videoPlaceholder.layoutParams = videoPlaceholderParams
        videoPlaceholder.addView(getBasicView())
        return videoPlaceholder
    }

    private fun getBasicView(): View {
        val basicView = View(context)
        basicView.setBackgroundColor(Color.BLACK)
        basicView.layoutParams = linearLayoutParams
        return basicView
    }
}
package com.mlucasmateus.collageview

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.Px
import com.yqritc.scalablevideoview.ScalableVideoView
import kotlin.math.floor

class CollageView(context: Context): GridLayout(context) {
    private var cellWidth = 0
    private var cellHeight = 0
    private val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT)
    private lateinit var gridAttributes: GridAttributes

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
        private var slotList: Array<out Slot> = arrayOf(Slot())

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
            this.slotList = slotList
            return this
        }

        fun getSlotList() = slotList
    }

    fun setGridBorderSize(@Px borderSize: Int) {
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

    private fun getImageView(): ImageView {
        val imageView = ImageView(context)
        imageView.layoutParams = linearLayoutParams
        return imageView
    }

    private fun getVideoView(): ScalableVideoView {
        val videoView = ScalableVideoView(context)
        videoView.layoutParams = linearLayoutParams
        return videoView
    }

    private fun getImageButton(): ImageButton {
        val imageButton = ImageButton(context)
        imageButton.layoutParams = linearLayoutParams
        imageButton.setBackgroundColor(Color.BLACK)
        return imageButton
    }
}
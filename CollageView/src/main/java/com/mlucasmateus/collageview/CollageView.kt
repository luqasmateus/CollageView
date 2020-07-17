package com.mlucasmateus.collageview

import android.content.Context
import android.graphics.Color
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ImageView

abstract class CollageView(context: Context): FrameLayout(context) {
    protected var cellWidth = 0
    protected var cellHeight = 0
    protected val grid: GridLayout = GridLayout(context)
    protected val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT)
    protected lateinit var childRowSpecs: Array<CustomSpec>
    protected lateinit var childColumnSpecs: Array<CustomSpec>

    open class Slot(val rowPosition: Int = 0,
                    val columnPosition: Int = 0,
                    val rowSpan: Int = 1,
                    val columnSpan: Int = 1)

    open inner class Builder {
        private var rowCount = 1
        private var columnCount = 1
        private var slotCount = 1

        fun setRowCount(rows: Int): Builder {
            rowCount = rows
            return this
        }

        fun setColumnCount(cols: Int): Builder {
            columnCount = cols
            return this
        }

        fun addSlots(vararg slotList: Slot): Builder {
            slotCount = slotList.size
            childRowSpecs = Array(slotCount) {
                CustomSpec.spec(slotList[it].rowPosition, slotList[it].rowSpan)
            }
            childColumnSpecs = Array(slotCount) {
                CustomSpec.spec(slotList[it].columnPosition, slotList[it].columnSpan)
            }
            return this
        }

        fun build(width: Int, height: Int): CollageView {
            grid.rowCount = rowCount
            grid.columnCount = columnCount
            /*cellWidth = layoutParams.width / columnCount
            cellHeight = layoutParams.height / rowCount*/
            cellWidth = width / columnCount
            cellHeight = height / rowCount

            for (i in 0 until slotCount) {
                grid.addView(getItemPlaceholder(childRowSpecs[i], childColumnSpecs[i]))
            }
            return this@CollageView
        }
    }

    data class CustomSpec(var start: Int, var size: Int){
        val spec: GridLayout.Spec = GridLayout.spec(start, size)
        companion object{
            fun spec(start: Int, size: Int) = CustomSpec(start, size)
        }
    }

    abstract fun getItemPlaceholder(rowSpec: CustomSpec, columnSpec: CustomSpec): LinearLayout

    protected fun getImageView(): ImageView {
        val imageView = ImageView(context)
        imageView.layoutParams = linearLayoutParams
        imageView.visibility = GONE
        return imageView
    }

    protected fun getButton(): ImageButton {
        val imageButton = ImageButton(context)
        imageButton.layoutParams = linearLayoutParams
        imageButton.setBackgroundColor(Color.BLACK)
        return imageButton
    }
}
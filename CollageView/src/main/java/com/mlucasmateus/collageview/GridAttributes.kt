package com.mlucasmateus.collageview

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
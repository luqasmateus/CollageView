package com.luqasmateus.collageview

import android.widget.GridLayout

class Slot(val rowPosition: Int = 0,
           val columnPosition: Int = 0,
           val rowSpan: Int = 1,
           val columnSpan: Int = 1) {
    val rowSpec: GridLayout.Spec = GridLayout.spec(rowPosition, rowSpan)
    val columnSpec: GridLayout.Spec = GridLayout.spec(columnPosition, columnSpan)
}
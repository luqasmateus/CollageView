package com.mlucasmateus.collageview

import android.content.Context
import android.widget.FrameLayout
import android.widget.GridLayout

abstract class CollageView(context: Context, var attributes : Attributes): FrameLayout(context) {

    open class Attributes
    open class Builder
    open class CustomSpec(start: Int, size: Int){
        val spec: GridLayout.Spec = GridLayout.spec(start, size)
        companion object{
            fun spec(start: Int, size: Int) = CustomSpec(start, size)
        }
    }
}
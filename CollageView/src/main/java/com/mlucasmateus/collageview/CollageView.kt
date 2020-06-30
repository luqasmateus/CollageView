package com.mlucasmateus.collageview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

abstract class CollageView(context: Context, var attributes : Attributes) : FrameLayout(context) {

    open class Attributes
    open class Builder
    open class CustomSpec
}
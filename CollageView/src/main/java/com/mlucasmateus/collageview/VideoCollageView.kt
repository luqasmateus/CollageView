package com.mlucasmateus.collageview

import android.content.Context
import android.graphics.Color
import android.widget.GridLayout
import android.widget.LinearLayout
import com.yqritc.scalablevideoview.ScalableVideoView

class VideoCollageView(context: Context): CollageView(context) {
    override fun getItemPlaceholder(slot: Slot): LinearLayout {
        val videoPlaceholder = LinearLayout(context)

        val videoPlaceholderParams = GridLayout.LayoutParams(slot.rowSpec, slot.columnSpec)
        videoPlaceholderParams.width = cellWidth * slot.columnSpan
        videoPlaceholderParams.height = cellHeight * slot.rowSpan

        videoPlaceholder.layoutParams = videoPlaceholderParams
        videoPlaceholder.addView(getImageButton())
        videoPlaceholder.addView(getVideoView())
        videoPlaceholder.addView(getImageView())
        return videoPlaceholder
    }

    private fun getVideoView(): ScalableVideoView {
        val videoView = ScalableVideoView(context)
        videoView.layoutParams = linearLayoutParams
        videoView.visibility = GONE
        return videoView
    }
}
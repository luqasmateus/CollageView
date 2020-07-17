package com.mlucasmateus.collageview

import android.content.Context
import android.graphics.Color
import android.widget.GridLayout
import android.widget.LinearLayout
import com.yqritc.scalablevideoview.ScalableVideoView

class VideoCollageView(context: Context): CollageView(context) {
    override fun getItemPlaceholder(rowSpec: CustomSpec, columnSpec: CustomSpec): LinearLayout {
        val videoPlaceholder = LinearLayout(context)

        //TODO: Remove testing code below
        videoPlaceholder.setPadding(10,10,10,10)
        videoPlaceholder.setBackgroundColor(Color.BLUE)

        val videoPlaceholderParams = GridLayout.LayoutParams(rowSpec.spec, columnSpec.spec)
        videoPlaceholderParams.width = cellWidth * columnSpec.size
        videoPlaceholderParams.height = cellHeight * rowSpec.size

        videoPlaceholder.layoutParams = videoPlaceholderParams
        videoPlaceholder.addView(getButton())
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
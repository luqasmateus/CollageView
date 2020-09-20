package com.luqasmateus.collageview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.system.measureTimeMillis

class MediaGenerator(var savePath: String) {
    private lateinit var collageView: CollageView
    private val logTag = "MediaGenerator"
    private val width = 1080
    private val height = 1080
    private var cellWidth: Int = 0
    private var cellHeight = 0
    private var borderSize = 0
    private var leftDiff: Int = 0
    private var topDiff: Int = 0
    private var rightDiff: Int = 0
    private var bottomDiff: Int = 0
    private var audioTrackCount = 0
    companion object {
        const val IMAGE = 0
        const val VIDEO = 1
    }
    private var mOutputType = 0
    private var currentlyGenerating = false

    fun generate(collageView: CollageView, outputType: Int, progressCallback: (Int) -> (Unit)) {
        this.collageView = collageView
        mOutputType = outputType

        cellWidth = width / collageView.getGridAttributes().getColumnCount()
        cellHeight = height / collageView.getGridAttributes().getRowCount()
        borderSize = (width * collageView.getBorderSize()) / collageView.layoutParams.width

        if (currentlyGenerating) throw IllegalStateException("This instance of MediaGenerator is currently generating media.")
        GlobalScope.launch(Dispatchers.IO) {
            val outputStream = FileOutputStream("$savePath/background.jpg")
            val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(background)
            canvas.drawColor(collageView.getBorderColor())
            background.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            when (mOutputType) {
                VIDEO -> FFmpeg.execute("-y -i $savePath/background.jpg $savePath/output.mp4")
                IMAGE -> FFmpeg.execute("-y -i $savePath/background.jpg $savePath/output.jpg")
                else -> throw IllegalArgumentException("Argument outputType must be either IMAGE or VIDEO.")
            }

            val itemsList = collageView.getItemsList()
            val progress = 100 / (itemsList.size + 1)
            progressCallback.invoke(progress)
            val slotList = collageView.getGridAttributes().getSlotList()
            for (i in itemsList.indices) {
                itemsList[i]?.let { encodeItem(it, slotList[i]) }
                progressCallback.invoke(progress)
            }

            if(mOutputType == VIDEO) {
                var audioTracks = ""
                val rc: Int
                val time = measureTimeMillis {
                    for (i in 1..audioTrackCount)
                        audioTracks += "-i $savePath/track$i.aac "
                    rc = FFmpeg.execute("-y $audioTracks -filter_complex " +
                            "amix=inputs=$audioTrackCount:duration=longest $savePath/audio.aac") as Int
                    FFmpeg.execute("-y -i $savePath/output.mp4 -i $savePath/audio.aac -c:v copy -c:a aac " +
                            "-map 0:v:0 -map 1:a:0 $savePath/collage.mp4")
                }

                when (rc) {
                    Config.RETURN_CODE_SUCCESS -> Log.i(logTag, "Command execution completed successfully. Total time: $time")
                    Config.RETURN_CODE_CANCEL -> Log.i(logTag, "Command execution cancelled by user. Total time: $time")
                    else -> {
                        Log.i(logTag, String.format("Command execution failed with rc=%d and the output below.", rc))
                        Config.printLastCommandOutput(Log.INFO)
                    }
                }

                for (i in 1..audioTrackCount)
                    File("$savePath/track$i.aac").delete()
                File("$savePath/audio.aac").delete()
                File("$savePath/output_tmp.mp4").delete()
                File("$savePath/output.mp4").delete()
                File("$savePath/background.mp4").delete()
            } else {
                File("$savePath/output_tmp.jpg").delete()
            }
            File("$savePath/background.jpg").delete()
            progressCallback.invoke(progress)
        }
    }

    private fun encodeItem(item: CollageView.Item, slot: Slot) {
        val input = when (item) {
            is CollageView.Image -> item.path
            is CollageView.Video -> item.path
            else -> return
        }

        leftDiff = if (slot.columnPosition == 0) borderSize else borderSize / 2
        topDiff = if (slot.rowPosition == 0) borderSize else borderSize  / 2
        rightDiff = if (slot.columnPosition + slot.columnSpan == collageView.getGridAttributes().getColumnCount())
            borderSize else borderSize / 2
        bottomDiff = if (slot.rowPosition + slot.rowSpan == collageView.getGridAttributes().getRowCount())
            borderSize else borderSize / 2

        val itemWidth = cellWidth * slot.columnSpan
        val itemHeight = cellHeight * slot.rowSpan

        val scaleCmd = when {
            itemWidth > itemHeight -> "max(iw*(${itemHeight - bottomDiff - topDiff}/ih)\\,${itemWidth - rightDiff - leftDiff}):" +
                    "max(ih*(${itemWidth - rightDiff - leftDiff}/iw)\\,${itemHeight - bottomDiff - topDiff})"
            itemHeight > itemWidth -> "max(iw*(${itemHeight - bottomDiff - topDiff}/ih)\\,${itemWidth - rightDiff - leftDiff}):" +
                    "max(ih*(${itemWidth - rightDiff - leftDiff}/iw)\\,${itemHeight - bottomDiff - topDiff})"
            else -> "${itemWidth - rightDiff - leftDiff}:${itemHeight - bottomDiff - topDiff}"
        }

        val itemPosX = cellWidth * slot.columnPosition + leftDiff
        val itemPosY = cellHeight * slot.rowPosition + topDiff

        var rc: Int
        val time = measureTimeMillis {
            when (mOutputType) {
                VIDEO -> {
                    rc = FFmpeg.execute("-y -i $savePath/output.mp4 -i $input -filter_complex " +
                            "[1:v]scale=$scaleCmd," +
                            "crop=min(${itemWidth - rightDiff - leftDiff}\\,iw):" +
                            "min(${itemHeight - bottomDiff - topDiff}\\,ih)[s1];" +
                            "[0:v][s1]overlay=$itemPosX:$itemPosY " +
                            "-b:v 6000k -vcodec mpeg4 $savePath/output_tmp.mp4")

                    FFmpeg.execute("-y -i $savePath/output_tmp.mp4 -c copy $savePath/output.mp4")
                    if (item is CollageView.Video) {
                        audioTrackCount++
                        FFmpeg.execute("-y -i $input -vn -acodec copy $savePath/track$audioTrackCount.aac")
                    }
                }
                else -> {
                    rc = FFmpeg.execute("-y -i $savePath/output.jpg -i $input -frames:v 1 -filter_complex " +
                            "[1:v]scale=$scaleCmd," +
                            "crop=min(${itemWidth - rightDiff - leftDiff}\\,iw):" +
                            "min(${itemHeight - bottomDiff - topDiff}\\,ih)[s1];" +
                            "[0:v][s1]overlay=$itemPosX:$itemPosY " +
                            "$savePath/output_tmp.jpg")

                    FFmpeg.execute("-y -i $savePath/output_tmp.jpg -c copy $savePath/output.jpg")
                }
            }
        }

        when (rc) {
            Config.RETURN_CODE_SUCCESS -> Log.i(logTag, "Command execution completed successfully. Total time: $time")
            Config.RETURN_CODE_CANCEL -> Log.i(logTag, "Command execution cancelled by user. Total time: $time")
            else -> {
                Log.i(logTag, String.format("Command execution failed with rc=%d and the output below.", rc))
                Config.printLastCommandOutput(Log.INFO)
            }
        }
    }
}

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
import java.util.Calendar
import kotlin.math.ceil
import kotlin.system.measureTimeMillis

class MediaGenerator {
    private val logTag = "MediaGenerator"
    private lateinit var mediaAttributes: MediaAttributes
    private var leftDiff: Int = 0
    private var topDiff: Int = 0
    private var rightDiff: Int = 0
    private var bottomDiff: Int = 0
    companion object {
        const val IMAGE = 0
        const val VIDEO = 1
    }
    private var currentlyGenerating = false

    class MediaAttributes(val collageView: CollageView,
                          var savePath: String,
                          var tmpPath: String = savePath,
                          val height: Int = 1080,
                          val width: Int = 1080,
                          val outputType: Int = IMAGE,
                          var fps: Int = 15) {
        val output = savePath + "/" + Calendar.getInstance().timeInMillis.toString() +
                when (outputType) {
                    IMAGE -> ".jpg"
                    VIDEO -> ".mp4"
                    else -> throw IllegalArgumentException("Argument outputType must be either " +
                            "MediaGenerator.IMAGE or MediaGenerator.VIDEO.")
                }
        val cellWidth = width / collageView.getGridAttributes().getColumnCount()
        val cellHeight = height / collageView.getGridAttributes().getRowCount()
        val borderSize = (width * collageView.getBorderSize()) / collageView.layoutParams.width

        var audioTracks = ""
        var audioTrackCount = 0
        fun addExtraAudioTrack(path: String, offset: Int, duration: Int) {
            audioTrackCount++
            audioTracks += "-ss ${offset}ms -t ${duration}ms -i $path "
        }
    }

    fun generate(attr: MediaAttributes, progressCallback: (Int) -> (Unit)) {
        this.mediaAttributes = attr

        if (currentlyGenerating) throw IllegalStateException("This instance of MediaGenerator is currently generating media.")
        currentlyGenerating = true
        GlobalScope.launch(Dispatchers.IO) {
            val outputStream = FileOutputStream("${mediaAttributes.tmpPath}/background.jpg")
            val background = Bitmap.createBitmap(mediaAttributes.width, mediaAttributes.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(background)
            canvas.drawColor(mediaAttributes.collageView.getBorderColor())
            background.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            FFmpeg.execute("-y -i ${mediaAttributes.tmpPath}/background.jpg " +
                    when(mediaAttributes.outputType){
                        IMAGE -> mediaAttributes.output
                        else -> "${mediaAttributes.tmpPath}/output.mp4"
                    })

            val itemsList = mediaAttributes.collageView.getItemsList()
            val progress = ceil(100f / (itemsList.size + 2)).toInt()
            progressCallback.invoke(progress)
            val slotList = mediaAttributes.collageView.getGridAttributes().getSlotList()
            for (i in itemsList.indices) {
                itemsList[i]?.let { encodeItem(it, slotList[i]) }
                progressCallback.invoke(progress)
            }

            if(mediaAttributes.outputType == VIDEO) {
                val rc: Int
                val time = measureTimeMillis {
                    rc = FFmpeg.execute("-y ${mediaAttributes.audioTracks} -filter_complex " +
                            "amix=inputs=${mediaAttributes.audioTrackCount}:duration=longest ${mediaAttributes.tmpPath}/audio.aac")
                    FFmpeg.execute("-y -i ${mediaAttributes.tmpPath}/output.mp4 -i ${mediaAttributes.tmpPath}/audio.aac " +
                            "-c:v copy -c:a aac -map 0:v:0 -map 1:a:0 -shortest ${mediaAttributes.output}")
                }

                when (rc) {
                    Config.RETURN_CODE_SUCCESS -> Log.i(logTag, "Command execution completed successfully. Total time: $time")
                    Config.RETURN_CODE_CANCEL -> Log.i(logTag, "Command execution cancelled by user. Total time: $time")
                    else -> {
                        Log.i(logTag, String.format("Command execution failed with rc=%d and the output below.", rc))
                        Config.printLastCommandOutput(Log.INFO)
                    }
                }

                for (i in 1..mediaAttributes.audioTrackCount)
                    File("${mediaAttributes.tmpPath}/track$i.aac").delete()
                File("${mediaAttributes.tmpPath}/audio.aac").delete()
                File("${mediaAttributes.tmpPath}/output_tmp.mp4").delete()
                File("${mediaAttributes.tmpPath}/output.mp4").delete()
            } else {
                File("${mediaAttributes.tmpPath}/output.jpg").delete()
                File("${mediaAttributes.tmpPath}/output_tmp.jpg").delete()
            }
            File("${mediaAttributes.tmpPath}/background.jpg").delete()
            progressCallback.invoke(progress)
            currentlyGenerating = false
        }
    }

    private fun encodeItem(item: CollageView.Item, slot: Slot) {
        val input = when (item) {
            is CollageView.Image -> item.path
            is CollageView.Video -> item.path
            else -> return
        }

        leftDiff = if (slot.columnPosition == 0) mediaAttributes.borderSize else mediaAttributes.borderSize / 2
        topDiff = if (slot.rowPosition == 0) mediaAttributes.borderSize else mediaAttributes.borderSize  / 2
        rightDiff = if (slot.columnPosition + slot.columnSpan == mediaAttributes.collageView.getGridAttributes().getColumnCount())
            mediaAttributes.borderSize else mediaAttributes.borderSize / 2
        bottomDiff = if (slot.rowPosition + slot.rowSpan == mediaAttributes.collageView.getGridAttributes().getRowCount())
            mediaAttributes.borderSize else mediaAttributes.borderSize / 2

        val itemWidth = mediaAttributes.cellWidth * slot.columnSpan
        val itemHeight = mediaAttributes.cellHeight * slot.rowSpan

        val scaleCmd = when {
            itemWidth > itemHeight -> "max(iw*(${itemHeight - bottomDiff - topDiff}/ih)\\,${itemWidth - rightDiff - leftDiff}):" +
                    "max(ih*(${itemWidth - rightDiff - leftDiff}/iw)\\,${itemHeight - bottomDiff - topDiff})"
            itemHeight > itemWidth -> "max(iw*(${itemHeight - bottomDiff - topDiff}/ih)\\,${itemWidth - rightDiff - leftDiff}):" +
                    "max(ih*(${itemWidth - rightDiff - leftDiff}/iw)\\,${itemHeight - bottomDiff - topDiff})"
            else -> "${itemWidth - rightDiff - leftDiff}:${itemHeight - bottomDiff - topDiff}"
        }

        val itemPosX = mediaAttributes.cellWidth * slot.columnPosition + leftDiff
        val itemPosY = mediaAttributes.cellHeight * slot.rowPosition + topDiff

        var rc: Int
        val time = measureTimeMillis {
            when (mediaAttributes.outputType) {
                VIDEO -> {
                    rc = FFmpeg.execute("-y -i ${mediaAttributes.tmpPath}/output.mp4 -i $input -filter_complex " +
                            "[1:v]scale=$scaleCmd," +
                            "crop=min(${itemWidth - rightDiff - leftDiff}\\,iw):" +
                            "min(${itemHeight - bottomDiff - topDiff}\\,ih)[s1];" +
                            "[0:v][s1]overlay=$itemPosX:$itemPosY " +
                            "-r ${mediaAttributes.fps} -b:v 6000k -vcodec mpeg4 ${mediaAttributes.tmpPath}/output_tmp.mp4")

                    FFmpeg.execute("-y -i ${mediaAttributes.tmpPath}/output_tmp.mp4 -c copy ${mediaAttributes.tmpPath}/output.mp4")
                    if (item is CollageView.Video) {
                        if (FFmpeg.execute("-y -i $input -vn -acodec copy " + "${mediaAttributes.tmpPath}/" +
                                    "track${mediaAttributes.audioTrackCount + 1}.aac") == Config.RETURN_CODE_SUCCESS){
                            mediaAttributes.audioTrackCount++
                            mediaAttributes.audioTracks += "-i ${mediaAttributes.tmpPath}/track${mediaAttributes.audioTrackCount}.aac "
                        }
                    }
                }
                else -> {
                    rc = FFmpeg.execute("-y -i ${mediaAttributes.output} -i $input -frames:v 1 -filter_complex " +
                            "[1:v]scale=$scaleCmd," +
                            "crop=min(${itemWidth - rightDiff - leftDiff}\\,iw):" +
                            "min(${itemHeight - bottomDiff - topDiff}\\,ih)[s1];" +
                            "[0:v][s1]overlay=$itemPosX:$itemPosY " +
                            "${mediaAttributes.tmpPath}/output_tmp.jpg")

                    FFmpeg.execute("-y -i ${mediaAttributes.tmpPath}/output_tmp.jpg -c copy ${mediaAttributes.output}")
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

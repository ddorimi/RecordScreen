package com.plcoding.recordscreen

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class DetectionResult(
    val classId: Int,
    val score: Float,
    val box: FloatArray // [ymin, xmin, ymax, xmax]
)

class ObjectDetectorHelper(context: Context) {
    private val interpreter: Interpreter

    init {
        val model = FileUtil.loadMappedFile(context, "ssd_mobilenet_v1_0.75_depth_quantized_300x300_coco14_sync_2018_07_18.tflite")
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        interpreter = Interpreter(model, options)
    }

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        // Resize to SSD input size
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)

        // Convert bitmap → ByteBuffer
        val byteBuffer = bitmapToByteBuffer(scaledBitmap, 300, 300)

        // Wrap in TensorBuffer
        val inputBuffer = TensorBuffer.createFixedSize(
            intArrayOf(1, 300, 300, 3),
            org.tensorflow.lite.DataType.UINT8
        )
        inputBuffer.loadBuffer(byteBuffer)

        // SSD MobileNet outputs
        val locations = Array(1) { Array(10) { FloatArray(4) } }
        val classes = Array(1) { FloatArray(10) }
        val scores = Array(1) { FloatArray(10) }
        val numDetections = FloatArray(1)

        val outputMap = mapOf(
            0 to locations,
            1 to classes,
            2 to scores,
            3 to numDetections
        )

        interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer.buffer), outputMap)

        val results = mutableListOf<DetectionResult>()
        for (i in 0 until numDetections[0].toInt()) {
            val score = scores[0][i]
            if (score > 0.5f) { // confidence threshold
                results.add(
                    DetectionResult(
                        classId = classes[0][i].toInt(),
                        score = score,
                        box = locations[0][i]
                    )
                )
            }
        }
        return results
    }

    /**
     * Helper function: Convert Bitmap → ByteBuffer for TFLite input
     */
    private fun bitmapToByteBuffer(bitmap: Bitmap, inputWidth: Int, inputHeight: Int): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(1 * inputWidth * inputHeight * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixelIndex = 0
        for (y in 0 until inputHeight) {
            for (x in 0 until inputWidth) {
                val pixelValue = intValues[pixelIndex++]

                val r = (pixelValue shr 16 and 0xFF)
                val g = (pixelValue shr 8 and 0xFF)
                val b = (pixelValue and 0xFF)

                byteBuffer.put(r.toByte())
                byteBuffer.put(g.toByte())
                byteBuffer.put(b.toByte())
            }
        }
        byteBuffer.rewind()
        return byteBuffer
    }
}
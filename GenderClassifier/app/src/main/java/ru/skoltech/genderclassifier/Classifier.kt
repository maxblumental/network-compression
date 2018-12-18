package ru.skoltech.genderclassifier

import android.app.Activity
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class Classifier(activity: Activity) {
  private val options = Interpreter.Options()
  private lateinit var interpreter: Interpreter
  private lateinit var imgData: ByteBuffer
  private val labelProbArray: FloatArray = floatArrayOf(0f)
  private val imageWidth = 178
  private val imageHeight = 218
  private val intValues = IntArray(imageWidth * imageHeight)

  init {
    createInterpreter(activity)
    allocateMemoryForImage()
  }

  fun classify(bitmap: Bitmap): Pair<Long, Float> {
    convertBitmapToByteBuffer(bitmap)
    val time = measureExecutionTime { interpreter.run(imgData, labelProbArray) }
    return time to labelProbArray[0]
  }

  private fun allocateMemoryForImage() {
    imgData = ByteBuffer.allocateDirect(1 * imageWidth * imageHeight * 3 * 4)
    imgData.order(ByteOrder.nativeOrder())
  }

  private fun createInterpreter(activity: Activity) {
    val model = loadModelFile(activity)
    interpreter = Interpreter(model, options)
  }

  private fun loadModelFile(activity: Activity): MappedByteBuffer {
    val fileDescriptor = activity.assets.openFd("model.tflite")
    val fileChannel = FileInputStream(fileDescriptor.fileDescriptor).channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
  }

  private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
    imgData.rewind()
    bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    var pixel = 0
    for (i in 0 until imageWidth) {
      for (j in 0 until imageHeight) {
        val value = intValues[pixel++]
        addPixelValue(value)
      }
    }
  }

  private fun addPixelValue(pixelValue: Int) {
    imgData.putFloat((pixelValue shr 16 and 0xFF) / 255f)
    imgData.putFloat((pixelValue shr 8 and 0xFF) / 255f)
    imgData.putFloat((pixelValue and 0xFF) / 255f)
  }
}

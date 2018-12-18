package ru.skoltech.genderclassifier

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import android.widget.AdapterView
import android.widget.Toast
import android.widget.AdapterView.OnItemSelectedListener


class MainActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private lateinit var options: Interpreter.Options
    private lateinit var imgData: ByteBuffer
    private val labelProbArray: FloatArray = floatArrayOf(0f)
    private val intValues = IntArray(178 * 218)
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prepareSpinner()
        createInterpreter()
        allocateMemoryForImage()
        val bitmap = BitmapFactory.decodeStream(assets.open("000001.jpg"))
        inferGender(bitmap)
    }

    private fun prepareSpinner() {
        spinner = findViewById(R.id.filenames)
        val filenames = (1 until 31).map { "%06d.jpg".format(it) }
        spinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, filenames)
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val bitmap = BitmapFactory.decodeStream(assets.open(filenames[position]))
                inferGender(bitmap)
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
    }

    private fun inferGender(bitmap: Bitmap) {
        findViewById<ImageView>(R.id.image).setImageBitmap(bitmap)
        convertBitmapToByteBuffer(bitmap)
        val start = System.currentTimeMillis()
        interpreter.run(imgData, labelProbArray)
        val time = System.currentTimeMillis() - start
        findViewById<TextView>(R.id.prediction).text = "male: ${labelProbArray[0]}"
        findViewById<TextView>(R.id.inference_time).text = "inference time: ${time} ms"
    }

    private fun allocateMemoryForImage() {
        imgData = ByteBuffer.allocateDirect(1 * 178 * 218 * 3 * 4)
        imgData.order(ByteOrder.nativeOrder())
    }

    private fun createInterpreter() {
        val tfliteModel = loadModelFile(this)
        options = Interpreter.Options()
        interpreter = Interpreter(tfliteModel, options)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        imgData.rewind()
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        val startTime = SystemClock.uptimeMillis()
        for (i in 0 until 178) {
            for (j in 0 until 218) {
                val `val` = intValues[pixel++]
                addPixelValue(`val`)
            }
        }
        val endTime = SystemClock.uptimeMillis()
        Log.d(TAG, "Timecost to put values into ByteBuffer: " + java.lang.Long.toString(endTime - startTime))
    }

    protected fun addPixelValue(pixelValue: Int) {
        imgData.putFloat((pixelValue shr 16 and 0xFF) / 255f)
        imgData.putFloat((pixelValue shr 8 and 0xFF) / 255f)
        imgData.putFloat((pixelValue and 0xFF) / 255f)
    }

    private fun loadModelFile(activity: Activity): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}

private const val TAG = "HEY"

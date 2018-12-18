package ru.skoltech.genderclassifier

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener


class MainActivity : AppCompatActivity() {

  lateinit var classifier: Classifier
  private lateinit var filenameList: Spinner

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    prepareSpinner()
    classifier = Classifier(this)
    BitmapFactory.decodeStream(assets.open("pic.jpg"))
        .let(::inferGender)
  }

  private fun prepareSpinner() {
    filenameList = findViewById(R.id.filenames)
    val filenames = listOf("pic.jpg", "zemfira.jpg")
    filenameList.adapter =
        ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, filenames)
    filenameList.onItemSelectedListener = object : OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        BitmapFactory.decodeStream(assets.open(filenames[position]))
            .let(::inferGender)
      }

      override fun onNothingSelected(arg0: AdapterView<*>) = Unit
    }
  }

  private fun inferGender(bitmap: Bitmap) {
    findViewById<ImageView>(R.id.image).setImageBitmap(bitmap)
    val (time, maleScore) = classifier.classify(bitmap)
    findViewById<TextView>(R.id.prediction).text = "male: $maleScore"
    findViewById<TextView>(R.id.inference_time).text = "inference time: $time ms"
  }
}

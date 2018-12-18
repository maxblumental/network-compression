package ru.skoltech.genderclassifier.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.skoltech.genderclassifier.GenderClassifierApplication
import ru.skoltech.genderclassifier.R
import ru.skoltech.genderclassifier.classifier.Classifier
import javax.inject.Inject


class MainActivity : AppCompatActivity(), MvpView {

  @Inject
  lateinit var presenter: MainPresenter

  private lateinit var filenameList: Spinner

  val context: Context = this

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    prepareSpinner()
    GenderClassifierApplication.component.inject(this)
    presenter.attachView(this)
  }

  override fun onDestroy() {
    presenter.detachView()
    super.onDestroy()
  }

  private fun prepareSpinner() {
    filenameList = findViewById(R.id.filenames)
    filenameList.onItemSelectedListener = FilenameSelectionListener()
  }

  fun setFilenames(filenames: List<String>) {
    filenameList.adapter =
        ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, filenames)
  }

  fun showImage(bitmap: Bitmap) = findViewById<ImageView>(R.id.image).setImageBitmap(bitmap)

  fun showInferenceResults(scrore: Float, time: Long) {
    findViewById<TextView>(R.id.prediction).text = "male: $scrore"
    findViewById<TextView>(R.id.inference_time).text = "inference time: $time ms"
  }

  inner class FilenameSelectionListener : OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
      val filename = parent.getItemAtPosition(position)
      presenter.onFileSelected(filename as String)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
  }
}

class MainPresenter
@Inject constructor(private val classifier: Classifier) : MvpPresenter<MainActivity>() {

  override fun attachView(view: MainActivity) {
    super.attachView(view)
    view.context.assets.list("")
        .filter { it.endsWith(".jpg") }
        .sorted()
        .let(view::setFilenames)
  }

  fun onFileSelected(filename: String) =
      view?.context?.let {
        GlobalScope.launch(Dispatchers.IO) {
          val bitmap = BitmapFactory.decodeStream(it.assets.open(filename))
          val (time, score) = classifier.classify(bitmap)
          launch(Dispatchers.Main) {
            view?.showImage(bitmap)
            view?.showInferenceResults(score, time)
          }
        }
      }
}

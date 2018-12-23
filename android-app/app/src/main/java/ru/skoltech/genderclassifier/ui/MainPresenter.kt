package ru.skoltech.genderclassifier.ui

import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.skoltech.genderclassifier.classifier.Classifier
import java.io.OutputStreamWriter
import java.net.URL
import javax.inject.Inject

class MainPresenter
@Inject constructor(private val classifier: Classifier) : MvpPresenter<MainActivity>() {

  // range of images in test partition of Celeba dataset
  private val imageIdRange = 182638 to 202599

  fun onStart() {
    if (view?.isWritePermissionGranted != true) {
      view?.requestWritePermission()
      return
    }

    val (from, to) = imageIdRange
    GlobalScope.launch(Dispatchers.IO) {
      view?.createFileOutput(name = "celeba_test.csv")
          ?.let { OutputStreamWriter(it) }
          ?.use { stream ->
            stream.write("image_id,time,male\n")
            (from..to).forEach { number ->
              classifyImage(number, stream)
              launch(Dispatchers.Main) { showProgress(number) }
            }
          }
    }
  }

  private fun classifyImage(number: Int, output: OutputStreamWriter) {
    val imageName = "%06d.jpg".format(number)
    val url = URL("http://localhost:8000/img_align_celeba/$imageName") // reversed by adb to PC
    val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
    val (time, score) = classifier.classify(bitmap)
    output.write("$imageName,$time,$score\n")
  }

  private fun showProgress(number: Int) {
    val (from, to) = imageIdRange
    val current = number - from + 1
    val total = to - from + 1
    view?.updateProgress(current, total)
  }
}

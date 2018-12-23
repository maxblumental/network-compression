package ru.skoltech.genderclassifier.ui

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import ru.skoltech.genderclassifier.GenderClassifierApplication
import ru.skoltech.genderclassifier.R
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


class MainActivity : AppCompatActivity(), MvpView {

  @Inject
  lateinit var presenter: MainPresenter

  private lateinit var progressBar: ProgressBar
  private lateinit var progressText: TextView

  val isWritePermissionGranted: Boolean
    get() = checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    GenderClassifierApplication.component.inject(this)
    bindViews()
    presenter.attachView(this)
  }

  private fun bindViews() {
    progressBar = findViewById(R.id.progress_line)
    progressText = findViewById(R.id.progress_text)
    findViewById<Button>(R.id.start_button).setOnClickListener { presenter.onStart() }
  }

  override fun onDestroy() {
    presenter.detachView()
    super.onDestroy()
  }

  @SuppressLint("SetTextI18n")
  fun updateProgress(current: Int, total: Int) {
    progressBar.progress = current * 100 / total
    progressText.text = "%.3f".format(current * 100f / total) + " %"
  }


  fun requestWritePermission() {
    ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),
        WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST)
  }

  fun createFileOutput(name: String): FileOutputStream? {
    val documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val file = File(documents, name)
    if (file.exists()) {
      file.delete()
    }
    file.createNewFile()
    return FileOutputStream(file)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                          grantResults: IntArray) {
    if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST) {
      if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        presenter.onStart()
      } else {
        val message = "Permission request was rejected. Experiment was aborted."
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
      }
    }
  }
}

private const val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST = 0

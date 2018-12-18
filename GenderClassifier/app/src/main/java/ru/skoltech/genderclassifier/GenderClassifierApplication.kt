package ru.skoltech.genderclassifier

import android.app.Application
import ru.skoltech.genderclassifier.di.ContextModule
import ru.skoltech.genderclassifier.di.DaggerGenderClassifierComponent
import ru.skoltech.genderclassifier.di.GenderClassifierComponent

class GenderClassifierApplication : Application() {
  companion object {
    lateinit var component: GenderClassifierComponent
  }

  override fun onCreate() {
    super.onCreate()
    component = DaggerGenderClassifierComponent.builder().contextModule(ContextModule(this)).build()
  }
}
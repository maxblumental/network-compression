package ru.skoltech.genderclassifier.di

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ContextModule(val context: Context) {

  @Provides
  fun provide() = context
}
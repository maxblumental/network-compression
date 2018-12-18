package ru.skoltech.genderclassifier.di

import dagger.Component
import ru.skoltech.genderclassifier.ui.MainActivity


@Component(modules = [(ClassifierModule::class), (ContextModule::class)])
interface GenderClassifierComponent {
  fun inject(activity: MainActivity)
}
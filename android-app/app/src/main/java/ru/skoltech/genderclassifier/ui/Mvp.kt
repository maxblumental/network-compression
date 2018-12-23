package ru.skoltech.genderclassifier.ui

interface MvpView

abstract class MvpPresenter<T : MvpView> {

  protected var view: T? = null

  open fun attachView(view: T) {
    this.view = view
  }

  open fun detachView() {
    this.view = null
  }
}

package ru.skoltech.genderclassifier.classifier

fun measureExecutionTime(work: () -> Unit): Long {
  val start = System.currentTimeMillis()
  work()
  return System.currentTimeMillis() - start
}
package ru.skoltech.genderclassifier

fun measureExecutionTime(work: () -> Unit): Long {
  val start = System.currentTimeMillis()
  work()
  return System.currentTimeMillis() - start
}
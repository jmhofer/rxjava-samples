package de.johoop.rxjava.samples.swing

import rx.util.functions.Func1

object Predef {
  implicit def toFunc1[T, R](f: T => R): Func1[T, R] = new Func1[T, R] {
    override def call(t: T): R = f(t)
  }
}
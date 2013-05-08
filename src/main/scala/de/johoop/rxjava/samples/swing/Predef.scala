package de.johoop.rxjava.samples.swing

import rx.util.functions.Func1
import rx.util.functions.Func2

object Predef {
  implicit def func1[T, R](f: T => R): Func1[T, R] = new Func1[T, R] {
    override def call(t: T): R = f(t)
  }

  implicit def func2[S, T, R](f: (S, T) => R): Func2[S, T, R] = new Func2[S, T, R] {
    override def call(s: S, t: T): R = f(s, t)
  }
}
package de.johoop.rxjava.samples.swing.pong

import scala.swing.Swing._
import scala.swing.MainFrame
import scala.swing.SimpleSwingApplication
import scala.swing.Panel
import java.awt.Color
import rx.observables.SwingObservable
import de.johoop.rxjava.samples.swing.Predef._
import java.awt.Dimension
import rx.Observable
import rx.operators.OperationCombineLatest._
import java.util.{ Set => JSet }
import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit
import rx.concurrency.SwingScheduler

object Main extends SimpleSwingApplication {
  lazy val top = new MainFrame {
    title = "Rx Pong"
    contents = canvas
  }
  
  lazy val canvas = new Panel {
    preferredSize = (800, 600)
    background = Color.white
    focusable = true
  }
  
  val size = SwingObservable fromResizing canvas.peer
  val keys = SwingObservable fromPressedKeys canvas.peer
  
  val rawInputs = Observable create combineLatest(size, keys, func2 { (size: Dimension, keys: JSet[Integer]) =>
    ((size.getWidth, size.getHeight), keys.asScala)
  })

  val sampled = rawInputs.sample(1L, TimeUnit.SECONDS, SwingScheduler.getInstance)
  
  val subSampled = sampled subscribe func1(println)
}
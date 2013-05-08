package de.johoop.rxjava.samples.swing.pong

import scala.swing.Swing._
import scala.swing.MainFrame
import scala.swing.SimpleSwingApplication
import scala.swing.Panel
import java.awt.Color
import rx.observables.SwingObservable
import de.johoop.rxjava.samples.swing.Predef._
import java.awt.Dimension

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
  
  val subSize = size subscribe toFunc1({ dim: Dimension =>
    println(dim)
  })
}
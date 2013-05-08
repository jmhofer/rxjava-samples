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
import java.awt.Graphics2D
import java.awt.event.KeyEvent
import scala.collection.breakOut
import rx.util.functions.Func1

case class Inputs(width: Double, height: Double, direction: Option[Direction])

sealed trait Direction
case object Up extends Direction
case object Down extends Direction

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
  
  val direction = SwingObservable fromPressedKeys canvas.peer map keySetToDirection
  
  val keySetToDirection = func1 { (keys: JSet[Integer]) =>
    import KeyEvent._
    
    val result: Set[Direction] = (for {
      key <- keys.asScala if key == VK_UP || key == VK_DOWN
      direction = if (key == VK_UP) Up else Down
    } yield direction)(breakOut)
    
    if (result.size == 1) Some(result.head) else None
  }
  
  val inputs = Observable create combineLatest(size, direction, func2 { (size: Dimension, dir: Option[Direction]) =>
    Inputs(size.getWidth, size.getHeight, dir)
  })

  val sampled = inputs.sample(1L, TimeUnit.SECONDS, SwingScheduler.getInstance)

  val subSampled = sampled subscribe func1({ inputs: Inputs =>
    val graphics = canvas.peer.getGraphics.asInstanceOf[Graphics2D]
    
    graphics.clearRect(0, 0, inputs.width.toInt, inputs.height.toInt)
    graphics setColor Color.black
    val str = inputs.direction map { dir => if (dir == Up) "up" else "down" } getOrElse "none"
    graphics.drawString(str, 100, 100)
    // TODO canvas.paint(graphics)
  })
}
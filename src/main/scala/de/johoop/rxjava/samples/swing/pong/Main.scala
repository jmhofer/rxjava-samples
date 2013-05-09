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

case class Inputs(direction: Option[Direction])

sealed trait Direction
case object Up extends Direction
case object Down extends Direction

object Main extends SimpleSwingApplication {
  private var state: Inputs = Inputs(None) // TODO replace by observable
  
  override def top = new MainFrame {
    title = "Rx Pong"
    contents = canvas
  }
  
  lazy val canvas = new Panel {
    preferredSize = (800, 600)
    background = Color.white
    focusable = true
    
    override def paintComponent(graphics: Graphics2D): Unit = {
      val clip = graphics.getClipBounds
      val (x, y, width, height) = (clip.getX.toInt, clip.getY.toInt, clip.getWidth.toInt, clip.getHeight.toInt)
      graphics clearRect (x, y, width, height)

      graphics setColor Color.black
      val str = state.direction map { dir => if (dir == Up) "up" else "down" } getOrElse "none"
      graphics.drawString(str, 100, 100)
    }
  }

  val keys = SwingObservable fromPressedKeys canvas.peer
  
  val direction = keys map func1 { (keys: JSet[Integer]) =>
    import KeyEvent._
    
    val set: Set[Direction] = (for {
      key <- keys.asScala if key == VK_UP || key == VK_DOWN
      direction = if (key == VK_UP) Up else Down
    } yield direction)(breakOut)
    
    val result: Option[Direction] = if (set.size == 1) Some(set.head) else None
    
    result
  }

  val inputs = direction map func1 { (dir: Option[Direction]) =>
    Inputs(dir)
  }

  val sampled = inputs sample (40L, TimeUnit.MILLISECONDS, SwingScheduler.getInstance)

  sampled subscribe func1({ inputs: Inputs =>
    state = inputs
    canvas.repaint
  })
}

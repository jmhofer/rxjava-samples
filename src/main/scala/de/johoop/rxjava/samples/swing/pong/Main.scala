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
import java.awt.event.MouseEvent
import rx.operators.OperationCombineLatest

case class Inputs(player1: Option[Direction], player2: Option[Direction])

sealed trait Direction
case object Up extends Direction
case object Down extends Direction

object Main extends SimpleSwingApplication {
  private var state: Inputs = Inputs(None, None) // TODO replace by observable
  
  override def top = new MainFrame {
    title = "Rx Pong"
    contents = canvas
  }
  
  lazy val canvas = new Panel {
    preferredSize = (800, 600)
    focusable = true
    
    override def paintComponent(graphics: Graphics2D): Unit = {
      val clip = graphics.getClipBounds
      val (x, y, width, height) = (clip.getX.toInt, clip.getY.toInt, clip.getWidth.toInt, clip.getHeight.toInt)
      
      graphics setBackground Color.white
      graphics clearRect (x, y, width, height)

      graphics setColor Color.black
      val str1 = state.player1 map { dir => if (dir == Up) "up" else "down" } getOrElse "none"
      graphics.drawString(str1, 100, 100)
      
      val str2 = state.player2 map { dir => if (dir == Up) "up" else "down" } getOrElse "none"
      graphics.drawString(str2, 200, 100)
    }
  }

  val keys = SwingObservable fromPressedKeys canvas.peer
  val mouse = SwingObservable fromMouseMotionEvents canvas.peer
  
  val player2Direction = keys map func1 { (keys: JSet[Integer]) =>
    import KeyEvent._
    
    val set: Set[Direction] = (for {
      key <- keys.asScala if key == VK_UP || key == VK_DOWN
      direction = if (key == VK_UP) Up else Down
    } yield direction)(breakOut)
    
    if (set.size == 1) Some(set.head) else None
  }

  val player1Direction = mouse 
      .map (func1((_: MouseEvent).getY))
      .scan (func2((old: Int, current: Int) => current - old)) // FIXME too simple...
      .map (func1[Int, Option[Direction]]((dy: Int) => if (dy < 0) Some(Up) else if (dy > 0) Some(Down) else None))
  
  val inputs = Observable create OperationCombineLatest.combineLatest(player1Direction, player2Direction, func2 { Inputs(_: Option[Direction], _: Option[Direction]) })

  val sampled = inputs sample (40L, TimeUnit.MILLISECONDS, SwingScheduler.getInstance)

  sampled subscribe func1({ inputs: Inputs =>
    state = inputs
    canvas.repaint
  })
}

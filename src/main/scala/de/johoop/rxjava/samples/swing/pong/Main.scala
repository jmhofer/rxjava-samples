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
import java.awt.Point
import rx.util.Timestamped

case class Inputs(player1: Timestamped[Direction], player2: Direction)
case class State(player1: Direction, player2: Direction)

sealed trait Direction
case object Up extends Direction
case object Down extends Direction
case object Resting extends Direction

object Main extends SimpleSwingApplication {
  private var state: State = State(Resting, Resting) // TODO add real Pong stuff to state
  
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
      val str1 = state.player1.toString
      graphics.drawString(str1, 100, 100)
      
      val str2 = state.player2.toString
      graphics.drawString(str2, 200, 100)
    }
  }

  val keys = SwingObservable fromPressedKeys canvas.peer
  val mouse = SwingObservable fromRelativeMouseMotion canvas.peer
  
  val player2Direction = keys map func1 { (keys: JSet[Integer]) =>
    import KeyEvent._
    
    val set: Set[Direction] = (for {
      key <- keys.asScala if key == VK_UP || key == VK_DOWN
      direction = if (key == VK_UP) Up else Down
    } yield direction)(breakOut)
    
    if (set.size == 1) set.head else Resting
  }

  val player1Direction = mouse
      .map (func1((_: Point).getY.toInt))
      .map (func1[Int, Direction]((dy: Int) => if (dy < 0) Up else if (dy > 0) Down else Resting))
      .timestamp
  
  val inputs = Observable create OperationCombineLatest.combineLatest(player1Direction, player2Direction, func2 { Inputs((_: Timestamped[Direction]), (_: Direction))})

  val sampled = inputs 
      .sample (40L, TimeUnit.MILLISECONDS, SwingScheduler.getInstance)
      .map (func1[Inputs, State] { (inputs: Inputs) =>
        State(player1 = if (inputs.player1.getTimestampMillis < System.currentTimeMillis - 40L) Resting else inputs.player1.getValue,
              player2 = inputs.player2)
      })

  sampled subscribe func1({ newState: State =>
    state = newState
    canvas.repaint
  })
}

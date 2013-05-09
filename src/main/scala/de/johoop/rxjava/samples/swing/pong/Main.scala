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

case class State(player1: Paddle, player2: Paddle)

case class Inputs(player1: Timestamped[Direction], player2: Direction)
case class Paddle(position: Double = 0.5)
case class Ball(position: (Double, Double), velocity: (Double, Double))

sealed trait Direction
case object Up extends Direction
case object Down extends Direction
case object Resting extends Direction

object Main extends SimpleSwingApplication {
  val frameRateMillis = 40L
  
  private var state: Option[State] = None
  
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
      
      graphics setBackground Color.black
      graphics clearRect (x, y, width, height)

      state foreach { state => 
        graphics setColor Color.white
        
        for ((paddle, x) <- Seq((state.player1, x), (state.player2, x + width - 10))) {
          graphics.fillRect(x, y + (paddle.position * height).toInt - 20, 10, 40)
        } 
      }
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
      .sample (frameRateMillis, TimeUnit.MILLISECONDS, SwingScheduler.getInstance)
      .scan (State(Paddle(), Paddle()), func2 { (oldState: State, inputs: Inputs) =>
        val inputPlayer1 = if (inputs.player1.getTimestampMillis < System.currentTimeMillis - 40L) Resting else inputs.player1.getValue
        State(
            stepPaddle(frameRateMillis, oldState.player1, inputPlayer1),
            stepPaddle(frameRateMillis, oldState.player2, inputs.player2))
      })

  sampled subscribe func1({ newState: State =>
    state = Some(newState)
    canvas.repaint
  })
  
  private def stepPaddle(stepMillis: Long, paddle: Paddle, direction: Direction): Paddle = {
    val step = stepMillis.toDouble / 1000
    direction match {
      case Resting => paddle
      case Up => Paddle(math.max(0.0, paddle.position - step))
      case Down => Paddle(math.min(1.0, paddle.position + step))
    }
  }
}

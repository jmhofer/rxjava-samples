package de.johoop.rxjava.samples.swing.pong

import scala.swing.Swing._
import scala.swing.MainFrame
import scala.swing.SimpleSwingApplication
import scala.swing.Panel
import java.awt.Color
import rx.observables.SwingObservable
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
import rx.lang.scala.RxImplicits._

object Main extends SimpleSwingApplication {
  val frameRateMillis = 10L
  
  private var state: Option[State] = None // only required for painting
  
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
        
        val paddleWidth = (Game.paddleWidth * width).toInt
        val paddleHeight = (Game.paddleHeight * height).toInt
        
        for ((paddle, x) <- Seq((state.player1, x), (state.player2, x + width - paddleWidth))) {
          graphics.fillRect(x, y + (paddle.position * height).toInt - paddleHeight / 2, paddleWidth, paddleHeight)
        } 
      }
    }
  }

  val keys = SwingObservable fromPressedKeys canvas.peer publish
  
  val player1Direction = keys map { keysToDirection(_: JSet[Integer], KeyEvent.VK_W, KeyEvent.VK_S) }
  val player2Direction = keys map { keysToDirection(_: JSet[Integer], KeyEvent.VK_UP, KeyEvent.VK_DOWN) }

  val inputs = Observable combineLatest (player1Direction, player2Direction, Inputs)

  val sampled = inputs 
      .sample (frameRateMillis, TimeUnit.MILLISECONDS, SwingScheduler.getInstance)
      .scan (State(), (Game step frameRateMillis) _)

  sampled subscribe { newState: State =>
    state = Some(newState)
    canvas.repaint
  }
  
  keys.connect

  private def keysToDirection(keys: JSet[Integer], upKey: Int, downKey: Int): Direction = {
    val set: Set[Direction] = (for {
      key <- keys.asScala if key == upKey || key == downKey
      direction = if (key == upKey) Up else Down
    } yield direction)(breakOut)
    
    if (set.size == 1) set.head else Resting
  }
}

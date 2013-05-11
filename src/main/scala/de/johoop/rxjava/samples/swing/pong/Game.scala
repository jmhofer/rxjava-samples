package de.johoop.rxjava.samples.swing.pong

case class State(player1: Paddle, player2: Paddle)

case class Inputs(player1: Direction, player2: Direction)
case class Paddle(position: Double = 0.5)
case class Ball(position: (Double, Double), velocity: (Double, Double))

sealed trait Direction
case object Up extends Direction
case object Down extends Direction
case object Resting extends Direction

object Game {
  val paddleHeight = 0.1;
  val paddleWidth = 0.02;
  
  def step(stepMillis: Long, oldState: State, inputs: Inputs): State = State(
      Game stepPaddle (stepMillis, oldState.player1, inputs.player1),
      Game stepPaddle (stepMillis, oldState.player2, inputs.player2))
  
  def stepPaddle(stepMillis: Long, paddle: Paddle, direction: Direction): Paddle = {
    val step = stepMillis.toDouble / 1000
    direction match {
      case Resting => paddle
      case Up => Paddle(math.max(paddleHeight / 2, paddle.position - step))
      case Down => Paddle(math.min(1.0 - paddleHeight / 2, paddle.position + step))
    }
  }
}
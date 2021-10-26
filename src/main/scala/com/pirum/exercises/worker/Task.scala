package com.pirum.exercises.worker

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

// A task that either succeeds after n seconds, fails after n seconds, or never terminates
sealed trait Task {
  val name: String
  val delay: FiniteDuration
  def execute: Future[Unit]
}

case class SuccessfulTask(name: String, delay: FiniteDuration) extends Task {
  override def execute: Future[Unit] = Future.successful()
}

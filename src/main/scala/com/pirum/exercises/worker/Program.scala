package com.pirum.exercises.worker

import scala.concurrent.duration.FiniteDuration

trait Program {
  def program(tasks: List[Task], timeout: FiniteDuration, workers: Int): Unit
}

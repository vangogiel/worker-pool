package com.pirum.exercises.worker.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object SummaryActor {
  sealed trait Command
  case class PrintResults(
      succeeded: SucceededTasks,
      failed: FailedTasks,
      timedOut: TimedOutTasks
  ) extends Command

  def apply(): Behavior[Command] = {
    Behaviors.receiveMessage { case PrintResults(succeeded, failed, timedOut) =>
      printResults(succeeded, failed, timedOut)
      Behaviors.stopped
    }
  }

  def printResults(
      succeeded: SucceededTasks,
      failed: FailedTasks,
      timedOut: TimedOutTasks
  ): Unit = {
    println(
      s"result.successful = [${succeeded.events.map(taskName => s"$taskName").mkString(", ")}]"
    )
    println(
      s"result.failed = [${failed.events.map(taskName => s"$taskName").mkString(", ")}]"
    )
    println(
      s"result.timedOut = [${timedOut.events.map(taskName => s"$taskName").mkString(", ")}]"
    )
  }
}

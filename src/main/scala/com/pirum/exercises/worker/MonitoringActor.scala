package com.pirum.exercises.worker

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object MonitoringActor {

  sealed trait Command
  final case class AttemptResultSummary(
      succeeded: SucceededTasks,
      failed: FailedTasks,
      timedOut: TimedOutTasks
  ) extends Command
  final case class CompleteResultSummaryNow(
      succeeded: SucceededTasks,
      failed: FailedTasks,
      timedOut: TimedOutTasks
  ) extends Command

  def summariseResultsActor(tasks: List[Task]): Behavior[Command] =
    Behaviors.receive { (_, message) =>
      {
        message match {
          case AttemptResultSummary(succeeded, failed, timedOut) =>
            if (
              succeeded.events.length + failed.events.length + timedOut.events.length == tasks.length
            ) {
              printResults(succeeded, failed, timedOut)
              Behaviors.stopped
            } else {
              Behaviors.same
            }
          case CompleteResultSummaryNow(succeeded, failed, timedOut) =>
            val tasksNotCompleted: List[String] = tasks
              .filter(task => !succeeded.events.contains(task.name))
              .filter(task => !failed.events.contains(task.name))
              .filter(task => !timedOut.events.contains(task.name))
              .map(task => task.name)
              .appendedAll(timedOut.events)
            printResults(succeeded, failed, TimedOutTasks(tasksNotCompleted))
            Behaviors.stopped
        }
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

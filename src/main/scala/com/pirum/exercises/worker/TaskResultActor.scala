package com.pirum.exercises.worker

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

final case class SucceededTasks(events: List[String])
final case class FailedTasks(events: List[String])
final case class TimedOutTasks(events: List[String])

sealed trait Action
final case class Succeeded(task: String) extends Action
final case class Failed(task: String) extends Action
final case class TimedOut(task: String) extends Action
final case class CheckAndAttemptResult(tasks: List[Task]) extends Action
final case class AttemptResultNow(tasks: List[Task]) extends Action

object TaskResultActor {

  def processCompletedTaskActor(
      succeededTasks: SucceededTasks,
      failedTasks: FailedTasks,
      timedOutTasks: TimedOutTasks
  ): Behavior[Action] = Behaviors.receive { (_, message) =>
    {
      message match {
        case Succeeded(taskName) =>
          processCompletedTaskActor(
            SucceededTasks(succeededTasks.events :+ taskName),
            failedTasks,
            timedOutTasks
          )
        case Failed(taskName) =>
          processCompletedTaskActor(
            succeededTasks,
            FailedTasks(failedTasks.events :+ taskName),
            timedOutTasks
          )
        case TimedOut(taskName) =>
          processCompletedTaskActor(
            succeededTasks,
            failedTasks,
            TimedOutTasks(timedOutTasks.events :+ taskName)
          )
        case CheckAndAttemptResult(tasks) =>
          if (
            succeededTasks.events.length + failedTasks.events.length +
              timedOutTasks.events.length == tasks.length
          ) {
            printResults(succeededTasks, failedTasks, timedOutTasks)
            Behaviors.stopped
          } else {
            Behaviors.same
          }
        case AttemptResultNow(tasks) =>
          val tasksNotCompleted: List[String] = tasks
            .filter(task => !succeededTasks.events.contains(task.name))
            .filter(task => !failedTasks.events.contains(task.name))
            .filter(task => !timedOutTasks.events.contains(task.name))
            .map(t => t.name)
            .appendedAll(timedOutTasks.events)
          printResults(
            succeededTasks,
            failedTasks,
            TimedOutTasks(tasksNotCompleted)
          )
          Behaviors.stopped
      }
    }
  }

  def printResults(
      succeededTasks: SucceededTasks,
      failedTasks: FailedTasks,
      timedOutTasks: TimedOutTasks
  ): Unit = {
    println(
      s"result.successful = [${succeededTasks.events.map(taskName => s"$taskName").mkString(", ")}]"
    )
    println(
      s"result.failed = [${failedTasks.events.map(taskName => s"$taskName").mkString(", ")}]"
    )
    println(
      s"result.timedOut = [${timedOutTasks.events.map(taskName => s"$taskName").mkString(", ")}]"
    )
  }
}

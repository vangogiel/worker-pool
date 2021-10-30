package com.pirum.exercises.worker

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.pirum.exercises.worker.MonitoringActor.{
  CompleteResultSummaryNow,
  AttemptResultSummary
}

case class SucceededTasks(events: List[String])
case class FailedTasks(events: List[String])
case class TimedOutTasks(events: List[String])

object TaskResultActor {
  sealed trait Command
  case class Succeeded(task: String) extends Command
  case class Failed(task: String) extends Command
  case class TimedOut(task: String) extends Command
  case class CheckState(replyTo: ActorRef[MonitoringActor.Command])
      extends Command
  case class CheckStateAndFinalise(
      replyTo: ActorRef[MonitoringActor.Command]
  ) extends Command

  def processCompletedTaskActor(
      succeeded: SucceededTasks,
      failed: FailedTasks,
      timedOut: TimedOutTasks
  ): Behavior[Command] = Behaviors.receive { (_, message) =>
    {
      message match {
        case Succeeded(taskName) =>
          processCompletedTaskActor(
            SucceededTasks(succeeded.events :+ taskName),
            failed,
            timedOut
          )
        case Failed(taskName) =>
          processCompletedTaskActor(
            succeeded,
            FailedTasks(failed.events :+ taskName),
            timedOut
          )
        case TimedOut(taskName) =>
          processCompletedTaskActor(
            succeeded,
            failed,
            TimedOutTasks(timedOut.events :+ taskName)
          )
        case CheckState(replyTo) =>
          replyTo ! AttemptResultSummary(succeeded, failed, timedOut)
          Behaviors.same
        case CheckStateAndFinalise(replyTo) =>
          replyTo ! CompleteResultSummaryNow(succeeded, failed, timedOut)
          Behaviors.stopped
      }
    }
  }
}

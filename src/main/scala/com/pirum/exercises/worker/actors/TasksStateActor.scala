package com.pirum.exercises.worker.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.pirum.exercises.worker.actors.MonitoringActor.{
  AttemptResultSummary,
  CompleteResultSummaryNow
}

case class SucceededTasks(events: List[String])
case class FailedTasks(events: List[String])
case class TimedOutTasks(events: List[String])

object TasksStateActor {
  sealed trait Command
  case class Succeeded(task: String) extends Command
  case class Failed(task: String) extends Command
  case class TimedOut(task: String) extends Command
  case class AttemptToFinalise(
      monitoringActor: ActorRef[MonitoringActor.Command],
      summaryActor: ActorRef[SummaryActor.Command]
  ) extends Command
  case class ForceFinalise(
      monitoringActor: ActorRef[MonitoringActor.Command],
      summaryActor: ActorRef[SummaryActor.Command]
  ) extends Command

  def apply(
      succeeded: SucceededTasks,
      failed: FailedTasks,
      timedOut: TimedOutTasks
  ): Behavior[Command] = Behaviors.receive { (_, message) =>
    {
      message match {
        case Succeeded(taskName) =>
          apply(
            SucceededTasks(succeeded.events :+ taskName),
            failed,
            timedOut
          )
        case Failed(taskName) =>
          apply(
            succeeded,
            FailedTasks(failed.events :+ taskName),
            timedOut
          )
        case TimedOut(taskName) =>
          apply(
            succeeded,
            failed,
            TimedOutTasks(timedOut.events :+ taskName)
          )
        case AttemptToFinalise(monitoringActor, summaryActor) =>
          monitoringActor ! AttemptResultSummary(
            succeeded,
            failed,
            timedOut,
            summaryActor
          )
          Behaviors.same
        case ForceFinalise(monitoringActor, summaryActor) =>
          monitoringActor ! CompleteResultSummaryNow(
            succeeded,
            failed,
            summaryActor
          )
          Behaviors.stopped
      }
    }
  }
}

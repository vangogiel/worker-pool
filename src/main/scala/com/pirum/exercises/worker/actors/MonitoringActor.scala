package com.pirum.exercises.worker.actors

import akka.actor.PoisonPill
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.pirum.exercises.worker.Task
import com.pirum.exercises.worker.actors.ProgramActor.SubmitTask
import com.pirum.exercises.worker.actors.SummaryActor.PrintResults

object MonitoringActor {
  sealed trait Command
  final case class AttemptResultSummary(
      succeeded: SucceededTasks,
      failed: FailedTasks,
      timedOut: TimedOutTasks,
      replyTo: ActorRef[SummaryActor.Command]
  ) extends Command
  final case class CompleteResultSummaryNow(
      succeeded: SucceededTasks,
      failed: FailedTasks,
      replyTo: ActorRef[SummaryActor.Command]
  ) extends Command

  def apply(
      tasks: List[Task]
  ): Behavior[Command] = {
    Behaviors.receiveMessage {
      case AttemptResultSummary(succeeded, failed, timedOut, replyTo) =>
        if (succeeded.events.length + failed.events.length == tasks.length) {
          replyTo ! PrintResults(succeeded, failed, timedOut)
          Behaviors.stopped
        } else {
          Behaviors.same
        }
      case CompleteResultSummaryNow(succeeded, failed, replyTo) =>
        val tasksNotCompleted: List[String] = tasks
          .filter(task => !succeeded.events.contains(task.name))
          .filter(task => !failed.events.contains(task.name))
          .map(task => task.name)
        replyTo ! PrintResults(
          succeeded,
          failed,
          TimedOutTasks(tasksNotCompleted)
        )
        Behaviors.stopped
    }
  }
}

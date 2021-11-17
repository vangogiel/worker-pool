package com.pirum.exercises.worker.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.pirum.exercises.worker.Task
import com.pirum.exercises.worker.actors.TasksStateActor.{Failed, Succeeded}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object ProgramActor {
  sealed trait Command
  final case class SubmitTask(
      task: Task,
      replyTo: ActorRef[TasksStateActor.Command]
  ) extends Command

  def apply()(implicit context: ExecutionContext): Behavior[Command] = {
    Behaviors.receiveMessage { case SubmitTask(task, replyTo) =>
      Future {
        task.runnable.run()
      }.onComplete {
        case Success(_) => replyTo ! Succeeded(task.name)
        case Failure(e) =>
          e match {
            case _: Exception => replyTo ! Failed(task.name)
          }
      }
      Behaviors.same
    }
  }
}

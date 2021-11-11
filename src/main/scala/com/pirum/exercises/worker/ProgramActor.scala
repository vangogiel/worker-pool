package com.pirum.exercises.worker

import akka.actor.typed.ActorRef
import akka.actor.{ActorSystem, Timers}
import akka.pattern.after
import com.pirum.exercises.worker.TaskResultActor.{Failed, Succeeded, TimedOut}

import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import scala.util.{Failure, Success}

case class ProcessTask(task: Task, replyTo: ActorRef[TaskResultActor.Command])

class ProgramActor(implicit
    actorSystem: ActorSystem,
    executionContext: ExecutionContext
) extends Timers {
  override def receive: Receive = { case ProcessTask(task, replyTo) =>
    Future {
      task.runnable.run()
    }.onComplete {
      case Success(_) => replyTo ! Succeeded(task.name)
      case Failure(e) =>
        e match {
          case _: Exception => replyTo ! Failed(task.name)
        }
    }
  }
}

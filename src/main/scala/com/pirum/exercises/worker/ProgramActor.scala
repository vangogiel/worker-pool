package com.pirum.exercises.worker

import akka.actor.typed.ActorRef
import akka.actor.{ActorSystem, Timers}
import akka.pattern.after

import scala.concurrent.{ExecutionContext, TimeoutException}
import scala.util.{Failure, Success}

case class ProcessTask(task: Task, replyTo: ActorRef[Action])
case class TaskResponse(name: String, state: Action)

class ProgramActor(implicit
    actorSystem: ActorSystem,
    executionContext: ExecutionContext
) extends Timers {
  override def receive: Receive = { case ProcessTask(task, replyTo) =>
    after(task.delay)(task.execute)
      .onComplete {
        case Success(_) => replyTo ! Succeeded(task.name)
        case Failure(e) =>
          e match {
            case _: TimeoutException              => replyTo ! TimedOut(task.name)
            case _: UnsupportedOperationException => replyTo ! Failed(task.name)
          }
      }
  }
}

package com.pirum.exercises.worker

import akka.actor.{Actor, ActorSystem}
import akka.pattern.after

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, TimeoutException}
import scala.util.Success

sealed trait Command
case class ProcessTasks() extends Command

class ProgramActor(
    succeededTasksList: mutable.Buffer[String],
    failedTasksList: mutable.Buffer[String],
    timedOutTasksList: mutable.Buffer[String],
    tasks: List[Task]
)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext)
    extends Actor {
  override def receive: Receive = { case ProcessTasks =>
    tasks.foreach(task => {
      after(task.delay)(task.execute)
        .recover {
          case _: UnsupportedOperationException => failedTasksList += task.name
          case _: TimeoutException              => timedOutTasksList += task.name
        }
        .onComplete { case Success(_) =>
          succeededTasksList += task.name
        }
    })
  }
}

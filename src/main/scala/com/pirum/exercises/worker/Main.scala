package com.pirum.exercises.worker

import akka.actor.{ActorSystem, Props}

import java.util.concurrent.{Executors, TimeUnit}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object Main extends App with Program {
  implicit val system: ActorSystem = ActorSystem("worker-pool")

  val succeededTasksList: mutable.Buffer[String] = ListBuffer[String]()

  def program(
      tasks: List[Task],
      timeout: FiniteDuration,
      workers: Int
  ): Unit = {
    val executionContext: ExecutionContext =
      ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(workers)
      )
    val rootActor =
      system.actorOf(
        Props(
          new ProgramActor(
            succeededTasksList,
            tasks,
            timeout
          )(system, executionContext)
        )
      )
    rootActor ! ProcessTasks
  }

  val tasks = List(SuccessfulTask("Task1", FiniteDuration(3, TimeUnit.SECONDS)))
  program(tasks, FiniteDuration(4, TimeUnit.SECONDS), tasks.size)
}

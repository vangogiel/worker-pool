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
  val failedTasksList: mutable.Buffer[String] = ListBuffer[String]()
  val timedOutTasksList: mutable.Buffer[String] = ListBuffer[String]()

  def program(
      tasks: List[Task],
      timeout: FiniteDuration,
      workers: Int
  ): Unit = {
    val monitoringEC: ExecutionContext =
      ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
    val tasksEC: ExecutionContext =
      ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(workers)
      )
    val monitoringActor =
      system.actorOf(
        Props(
          new MonitoringActor(
            succeededTasksList,
            failedTasksList,
            timedOutTasksList,
            tasks,
            timeout
          )(system, monitoringEC)
        )
      )
    val programActor =
      system.actorOf(
        Props(
          new ProgramActor(
            succeededTasksList,
            failedTasksList,
            timedOutTasksList,
            tasks
          )(system, tasksEC)
        )
      )
    programActor ! ProcessTasks
    monitoringActor ! DisplayResults
  }

  val tasks = List(
    SuccessfulTask("Task1", FiniteDuration(1, TimeUnit.SECONDS)),
    SuccessfulTask("Task2", FiniteDuration(1, TimeUnit.SECONDS)),
    ThrowingTask("Task3", FiniteDuration(1, TimeUnit.SECONDS)),
    TimeoutTask("Task4", FiniteDuration(1, TimeUnit.SECONDS)),
    SuccessfulTask("Task5", FiniteDuration(3, TimeUnit.SECONDS)),
    SuccessfulTask("Task6", FiniteDuration(4, TimeUnit.SECONDS)),
    ThrowingTask("Task7", FiniteDuration(2, TimeUnit.SECONDS)),
    SuccessfulTask("Task8", FiniteDuration(5, TimeUnit.SECONDS)),
    SuccessfulTask("Task9", FiniteDuration(6, TimeUnit.SECONDS)),
    SuccessfulTask("Task10", FiniteDuration(6, TimeUnit.SECONDS)),
    SuccessfulTask("Task11", FiniteDuration(7, TimeUnit.SECONDS)),
    ThrowingTask("Task12", FiniteDuration(3, TimeUnit.SECONDS)),
    TimeoutTask("Task13", FiniteDuration(2, TimeUnit.SECONDS)),
    SuccessfulTask("Task14", FiniteDuration(8, TimeUnit.SECONDS)),
    SuccessfulTask("Task15", FiniteDuration(8, TimeUnit.SECONDS)),
    ThrowingTask("Task16", FiniteDuration(4, TimeUnit.SECONDS))
  )
  program(tasks, FiniteDuration(14, TimeUnit.SECONDS), tasks.size)
}

package com.pirum.exercises.worker.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.pirum.exercises.worker.Task
import com.pirum.exercises.worker.actors.ProgramActor.SubmitTask
import com.pirum.exercises.worker.actors.TasksStateActor.{
  AttemptToFinalise,
  ForceFinalise
}
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object RootActor {
  sealed trait Command
  final case class StartProgram(
      tasks: List[Task],
      replyTo: ActorRef[ProgramActor.Command],
      forwarder: ActorRef[TasksStateActor.Command]
  ) extends Command
  final case class StartMonitoring(
      replyTo: ActorRef[TasksStateActor.Command],
      forwarder: ActorRef[MonitoringActor.Command]
  ) extends Command
  final case class FinaliseNow(
      replyTo: ActorRef[TasksStateActor.Command],
      forwarder: ActorRef[MonitoringActor.Command]
  ) extends Command
  final case class StartAll() extends Command

  def apply(
      tasks: List[Task],
      timeout: FiniteDuration
  ): Behavior[Command] = {
    Behaviors.setup { ctx =>
      val taskExecContext = ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(tasks.length)
      )
      val programActor = ctx.spawn(
        ProgramActor()(taskExecContext),
        "program-actor"
      )
      val tasksStateActor = ctx.spawn(
        TasksStateActor(
          SucceededTasks(List.empty),
          FailedTasks(List.empty),
          TimedOutTasks(List.empty)
        ),
        "tasks-state-actor"
      )
      val monitoringActor = ctx.spawn(
        MonitoringActor(tasks),
        "monitoring-actor"
      )
      val summaryActor = ctx.spawn(
        SummaryActor(),
        "summary-actor"
      )
      getTimedBehavior(
        tasks,
        programActor,
        tasksStateActor,
        monitoringActor,
        summaryActor,
        timeout
      )
    }
  }

  def getTimedBehavior(
      tasks: List[Task],
      programActor: ActorRef[ProgramActor.Command],
      taskResultActor: ActorRef[TasksStateActor.Command],
      monitoringActor: ActorRef[MonitoringActor.Command],
      summaryActor: ActorRef[SummaryActor.Command],
      timeout: FiniteDuration
  ): Behaviors.Receive[Command] = {
    Behaviors.receivePartial { _ =>
      Behaviors.withTimers[Command] { timers =>
        timers.startSingleTimer(
          StartProgram(tasks, programActor, taskResultActor),
          0.seconds
        )
        timers.startTimerAtFixedRate(
          StartMonitoring(taskResultActor, monitoringActor),
          0.milliseconds,
          1.millisecond
        )
        timers.startSingleTimer(
          FinaliseNow(taskResultActor, monitoringActor),
          timeout
        )
        Behaviors.receiveMessagePartial {
          case StartProgram(tasks: List[Task], programActor, tasksStateActor) =>
            tasks.foreach { task =>
              programActor ! SubmitTask(task, tasksStateActor)
            }
            Behaviors.same
          case StartMonitoring(tasksStateActor, monitoringActor) =>
            tasksStateActor ! AttemptToFinalise(monitoringActor, summaryActor)
            Behaviors.same
          case FinaliseNow(tasksStateActor, monitoringActor) =>
            tasksStateActor ! ForceFinalise(monitoringActor, summaryActor)
            Behaviors.stopped
        }
      }
    }
  }
}

package lart.chakka.pool

import akka.actor.typed.*
import akka.actor.typed.scaladsl.Behaviors

/** Исполнитель должен:
 *    - принимать комманды из своего протокола и комманды протокола Command.
 *    - сообщать пулу об окончании выполнения работы сигналом Complete
 *    - сообщать пулу о завершении работы по ошибке(таймауту) сигналом Failed
 *
 *  Исполнитель задается в следующем виде:
 *  {{{
 *  def workerFactory(
 *    chief: ActorRef[PoolSignal],
 *    completeSignal: PoolSignal,
 *    failedSignal: PoolSignal
 *  ): Behavior[Int | Command]
 *  }}}
 */
object DynamicPool:

  sealed trait PoolSignal
  case class WorkComplete(workerId: Int) extends PoolSignal
  case class WorkFailed(workerId: Int) extends PoolSignal
  case class TimeToDown(workerId: Int) extends PoolSignal

  sealed trait Command
  case object Stop extends Command

  def apply[In](
      workerFactory: (
          ActorRef[PoolSignal],
          PoolSignal,
          PoolSignal
      ) => Behavior[In | Command],
      poolBusy: In => Unit,
      minPoolSize: Int = 2,
      maxPoolSize: Int = 8,
      stopWorkerOnFailure: Boolean = false
  ): Behavior[In | PoolSignal] =

    require(minPoolSize > 0, "minPoolSize must be > 0")
    require(maxPoolSize > minPoolSize, "maxPoolSize must be > minPoolSize")

    Behaviors.setup { ctx =>

      val workerPool: Array[ActorRef[In | Command]] = new Array(maxPoolSize)

      val workerIds = 0 to (maxPoolSize - 1)

      workerIds.foreach { id =>
        workerPool(id) = ctx.spawnAnonymous(
          workerFactory(ctx.self.narrow, WorkComplete(id), WorkFailed(id))
        )
      }

      Behaviors.receiveMessage {
        case WorkComplete(workerId) =>
          Behaviors.same
        case WorkFailed(workerId) =>
          if stopWorkerOnFailure then workerPool(workerId) ! Stop
          Behaviors.same
        case TimeToDown(workerId) =>
          Behaviors.same
        case msg: In @unchecked =>
          workerPool(0) ! msg
          Behaviors.same
      }
    }

  end apply

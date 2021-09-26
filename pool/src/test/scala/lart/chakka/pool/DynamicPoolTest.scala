package lart.chakka.pool

import akka.actor.testkit.typed.scaladsl.{
  ActorTestKit,
  LoggingTestKit,
  ScalaTestWithActorTestKit
}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import lart.chakka.pool.DynamicPool.{Command, PoolSignal}
import lart.chakka.testKit.ChakkaTestKit
import org.scalatest.*
import org.scalatest.freespec.*
import org.scalatest.matchers.*
import org.scalatest.matchers.should.Matchers

import scala.math.Fractional.Implicits.infixFractionalOps
import scala.math.Integral.Implicits.infixIntegralOps
import scala.math.Numeric.Implicits.infixNumericOps
import scala.util.{Failure, Success, Try}

class DynamicPoolTest extends ChakkaTestKit {

  val probe = testKit.createTestProbe[Int]()

  def dynamicPoolWorkerFactory(
      chief: ActorRef[PoolSignal],
      completeSignal: PoolSignal,
      failedSignal: PoolSignal
  ): Behavior[Int | Command] =
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case i: Int =>
          Try(100 / i) match
            case Failure(exception) =>
              ctx.log.error("Work failed by cause", exception)
              chief ! failedSignal
            case Success(value) =>
              probe ! value
              chief ! completeSignal
          Behaviors.same
        case DynamicPool.Stop =>
          println("Stooped")
          Behaviors.stopped
      }
    }

  def poolBusy(i: Int): Unit = {}

  "Dynamic pool" - {
    "should" in {

      assert(1 == 1)
      val pool =
        testKit.spawn(
          DynamicPool(dynamicPoolWorkerFactory, poolBusy),
          "pool"
        )
      pool ! 1
      probe.expectMessage(100)
      pool ! 2
      probe.expectMessage(50)
      pool ! 3
      probe.expectMessage(33)
      LoggingTestKit.error("Work failed by cause").expect {
        pool ! 0
      }
      probe.expectNoMessage()
    }
  }

}

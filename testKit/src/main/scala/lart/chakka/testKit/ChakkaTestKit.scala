package lart.chakka.testKit

import akka.actor.testkit.typed.scaladsl.*
import akka.actor.typed.ActorSystem
import org.scalatest.*
import org.scalatest.freespec.*
import org.scalatest.matchers.should.*

abstract class ChakkaTestKit
    extends AnyFreeSpec
    with BeforeAndAfterAll
    with Matchers:

  val testKit = ActorTestKit()

  given ActorSystem[Nothing] = testKit.system

  override def afterAll(): Unit = testKit.shutdownTestKit()

end ChakkaTestKit

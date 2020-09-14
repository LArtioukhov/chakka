package lart.chakka.testKit

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers

abstract class ChakkaTestKit extends ScalaTestWithActorTestKit with AnyFreeSpecLike with Matchers
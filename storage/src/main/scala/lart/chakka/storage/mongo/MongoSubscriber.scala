package lart.chakka.storage.mongo

import akka.actor.typed.ActorRef
import org.reactivestreams.{ Publisher, Subscriber, Subscription }

object MongoSubscriber {

  sealed trait Response[+T]

  final case class DataGram[T](data: T) extends Response[T]

  final case class Error(throwable: Throwable) extends Response[Nothing]

  case object Complete extends Response[Nothing]

  // TODO: Remove all println
  private class SubscriberImpl[T](replyTo: ActorRef[Response[T]], batchSize: Int = 1) extends Subscriber[T] {
    private var subscription: Subscription = _
    private var requests: Long             = 0

    override def onSubscribe(s: Subscription): Unit = {
      println(s"onSubscribe. Current thread id : ${Thread.currentThread().getName}")
      subscription = s
      subscription.request(batchSize)
      requests += batchSize
    }

    override def onNext(t: T): Unit = {
      println(s"onNext. Current thread id : ${Thread.currentThread().getName}")
      replyTo ! DataGram(t)
      requests -= 1
      if (requests <= 0) {
        subscription.request(batchSize)
        requests += batchSize
      }
    }

    override def onError(t: Throwable): Unit = {
      println(s"onError. Current thread id : ${Thread.currentThread().getName}")
      replyTo ! Error(t)
    }

    override def onComplete(): Unit = {
      println(s"onComplete. Current thread id : ${Thread.currentThread().getName}")
      replyTo ! Complete
    }
  }

  def subscribe[T](publisher: Publisher[T], replyTo: ActorRef[Response[T]], batchSize: Int = 1): Unit =
    publisher.subscribe(new SubscriberImpl[T](replyTo, batchSize))
}

package lart.chakka.storage.mongo

import lart.chakka.storage.mongo.MongoSubscriber.DataGram
import lart.chakka.testKit.ChakkaTestKit
import org.bson.codecs.configuration.CodecRegistries.{ fromProviders, fromRegistries }
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model._
import org.mongodb.scala.result.{ DeleteResult, InsertManyResult, InsertOneResult, UpdateResult }

import scala.jdk.CollectionConverters._

class MongoSubscriberSpec extends ChakkaTestKit {

  //region PreSets

  case class Person(_id: ObjectId, firstName: String, lastName: String)

  object Person {
    def apply(firstName: String, lastName: String): Person =
      Person(new ObjectId(), firstName, lastName)
  }

  val codecRegistry: CodecRegistry = fromRegistries(fromProviders(classOf[Person]), DEFAULT_CODEC_REGISTRY)

  val collection: MongoCollection[Person] =
    MongoClient()
      .getDatabase("chakkaTest")
      .withCodecRegistry(codecRegistry)
      .getCollection("MongoSubscriberSpec")

  val personList = Seq(
    Person("Ada", "Lovelace"),
    Person("Saanvi", "Campos"),
    Person("Chase", "Bone"),
    Person("Menachem", "Kaye"),
    Person("Donell", "Fitzpatrick"),
    Person("Jensen", "Pham"),
    Person("Harun", "Jordan"),
    Person("Tyla", "Whitehead"),
    Person("Olaf", "Quinn"),
    Person("Dominique", "Keller"),
    Person("Kayden", "Rojas"),
    Person("Bushra", "Liu"),
    Person("Albert", "Mccann"),
    Person("Vickie", "Vaughn"),
    Person("Zeeshan", "Mccormack"),
    Person("Inez", "Mcphee"),
    Person("Isaac", "Green"),
    Person("Kajol", "Cortez"),
    Person("Keira", "Blackwell"),
    Person("Isla-Mae", "Wyatt"))
  //endregion

  "MongoSubscriber should" - {

    "process single" - {

      "create requests" in {

        val receiver = testKit.createTestProbe[MongoSubscriber.Response[InsertOneResult]]()

        personList.foreach { person =>
          MongoSubscriber.subscribe(collection.insertOne(person), receiver.ref)

          receiver
            .expectMessageType[MongoSubscriber.DataGram[InsertOneResult]]
            .data
            .getInsertedId
            .asObjectId()
            .getValue
            .toHexString shouldBe person._id.toHexString

          receiver.expectMessage(MongoSubscriber.Complete)
        }
      }

      "read requests" in {

        val receiver = testKit.createTestProbe[MongoSubscriber.Response[Person]]()

        personList.foreach { person =>
          MongoSubscriber.subscribe(collection.find(Filters.equal("_id", person._id)), receiver.ref)
          receiver.expectMessage(MongoSubscriber.DataGram(person))
          receiver.expectMessage(MongoSubscriber.Complete)
        }
        receiver.stop()
      }

      "update requests" in {
        val receiver = testKit.createTestProbe[MongoSubscriber.Response[UpdateResult]]()

        personList.foreach { person =>
          MongoSubscriber.subscribe(
            collection.updateOne(
              Filters.equal("_id", person._id),
              Updates.combine(
                Updates.set("firstName", person.firstName.toUpperCase),
                Updates.set("lastName", person.lastName.toUpperCase))),
            receiver.ref)

          val t = receiver.expectMessageType[MongoSubscriber.DataGram[UpdateResult]]
          assert(t.data.wasAcknowledged(), "but must be acknowledged")
          t.data.getMatchedCount shouldBe 1
          t.data.getModifiedCount shouldBe 1
          receiver.expectMessage(MongoSubscriber.Complete)
        }
      }

      "delete requests" in {
        val receiver = testKit.createTestProbe[MongoSubscriber.Response[DeleteResult]]()

        personList.foreach { person =>
          MongoSubscriber.subscribe(collection.deleteOne(Filters.equal("_id", person._id)), receiver.ref)
        }

        val r = receiver.expectMessageType[MongoSubscriber.DataGram[DeleteResult]].data
        assert(r.wasAcknowledged(), "but must be acknowledged")
        r.getDeletedCount shouldBe 1
        receiver.expectMessage(MongoSubscriber.Complete)
      }
    }

    "process batch" - {

      "create request" in {
        val receiver = testKit.createTestProbe[MongoSubscriber.Response[InsertManyResult]]()
        MongoSubscriber.subscribe(collection.insertMany(personList), receiver.ref)

        val r = receiver.expectMessageType[DataGram[InsertManyResult]].data
        assert(r.wasAcknowledged(), "but must be acknowledged")
        r.getInsertedIds.asScala
          .map(_._2.asObjectId().getValue.toHexString) should contain theSameElementsAs personList.map(
          _._id.toHexString)
        receiver.expectMessage(MongoSubscriber.Complete)
      }

      "read request" in {
        val receiver = testKit.createTestProbe[MongoSubscriber.Response[Person]]()
        MongoSubscriber.subscribe(collection.find(), receiver.ref, 9)

        var wait: Boolean = true
        while (wait) {
          receiver.expectMessageType[MongoSubscriber.Response[Person]] match {
            case DataGram(person)                 =>
              personList should contain(person)
            case MongoSubscriber.Error(throwable) =>
              fail(throwable)
            case MongoSubscriber.Complete         =>
              wait = false
              succeed
          }
        }
      }

      "delete request" in {
        val receiver = testKit.createTestProbe[MongoSubscriber.Response[DeleteResult]]()
        MongoSubscriber.subscribe(collection.deleteMany(Document()), receiver.ref)

        var wait: Boolean   = true
        var forRemove: Long = personList.length

        while (wait) {
          receiver.expectMessageType[MongoSubscriber.Response[DeleteResult]] match {
            case DataGram(r)                      =>
              assert(r.wasAcknowledged(), "but must be acknowledged")
              r.getDeletedCount should be >= 1L
              forRemove -= r.getDeletedCount
            case MongoSubscriber.Error(throwable) =>
              fail(throwable)
            case MongoSubscriber.Complete         =>
              wait = false
              forRemove shouldBe 0
              succeed
          }
        }
      }
    }

    "process bulk operations" in {
      val receiver       = testKit.createTestProbe[MongoSubscriber.Response[BulkWriteResult]]()
      val bulkOperations = personList.flatMap { person =>
        List(
          InsertOneModel(person),
          UpdateOneModel(
            Filters.equal("_id", person._id),
            Updates.combine(
              Updates.set("firstName", person.firstName.toUpperCase),
              Updates.set("lastName", person.lastName.toUpperCase))),
          DeleteOneModel(Filters.equal("_id", person._id)))
      }
      MongoSubscriber.subscribe(collection.bulkWrite(bulkOperations), receiver.ref)
      val r              = receiver.expectMessageType[MongoSubscriber.DataGram[BulkWriteResult]]
      assert(r.data.wasAcknowledged(), "but must be acknowledged")
      r.data.getInsertedCount shouldBe 20
      r.data.getMatchedCount shouldBe 20
      r.data.getModifiedCount shouldBe 20
      r.data.getDeletedCount shouldBe 20
      r.data.getInserts.asScala
        .map(_.getId.asObjectId().getValue.toHexString) should contain theSameElementsInOrderAs personList
        .map(_._id.toHexString)
      r.data.getUpserts.size() should be(0)
      receiver.expectMessage(MongoSubscriber.Complete)
    }
  }
}

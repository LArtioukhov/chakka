package lart.chakka.storage.mongo

object MongoOperation {

  sealed trait Command
  final case class InsertOne()

  sealed trait InsertResults

}

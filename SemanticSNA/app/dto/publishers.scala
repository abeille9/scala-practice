package dto

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Publisher(name:String, category:String)

object Publisher{
  implicit val publisherWrites: Writes[Publisher] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "category").write[String]
    )(unlift(Publisher.unapply))

  implicit val publisherReads: Reads[Publisher] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "category").read[String]
    )(Publisher.apply _)
}

case class Publishers(publishers:List[Publisher])

object Publishers {

  implicit val publishersDtoWrites: Writes[Publishers] = Json.writes[Publishers]
}

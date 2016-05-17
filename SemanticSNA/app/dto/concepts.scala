package dto

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

case class Concept (word:String, category:String, tfidf:Double){}

case class Word (text:String, size:Int)

object Word {
  implicit val wordWrites: Writes[Word] = (
    (JsPath \ "text").write[String] and
      (JsPath \ "size").write[Int]
    ) (unlift(Word.unapply))
}

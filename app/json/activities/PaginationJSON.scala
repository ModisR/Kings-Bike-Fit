package json.activities

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class PaginationJSON (next: String)

object PaginationJSON {
  implicit val format: RootJsonFormat[PaginationJSON] =
    jsonFormat1 (apply)
}
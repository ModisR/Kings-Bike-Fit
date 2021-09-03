package json.profile

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class UserJSON (firstName: String, lastName: String)

object UserJSON {
  implicit val format: RootJsonFormat[UserJSON] =
    jsonFormat2 (apply)
}
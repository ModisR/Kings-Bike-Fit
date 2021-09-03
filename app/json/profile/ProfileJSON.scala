package json.profile

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class ProfileJSON (user: UserJSON)
object ProfileJSON {
  implicit val format: RootJsonFormat[ProfileJSON] =
    jsonFormat1 (apply)
}
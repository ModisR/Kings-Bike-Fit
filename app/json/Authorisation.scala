package json

import play.api.libs.json.{Json, OFormat}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class Authorisation( access_token: String,
                          refresh_token: String,
                          user_id: String,
                          expires_in: Int
                        )
object Authorisation extends DefaultJsonProtocol {
  implicit val sprayFormat: RootJsonFormat[Authorisation] =
    jsonFormat4 (apply)

  implicit val playFormat: OFormat[Authorisation] =
    Json.format[Authorisation]
}
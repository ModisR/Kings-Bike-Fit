package json.activities

import java.time.LocalDateTime

import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat}

case class ActivityJSON (logId: Long,
                         activityName: String,
                         averageHeartRate: Option[Int],
                         calories: Int,
                         distance: Option[Double],
                         duration: Int,
                         startTime: LocalDateTime
                        )
object ActivityJSON extends DefaultJsonProtocol {
  implicit val dateTimeFormat: JsonFormat[LocalDateTime] =
    new JsonFormat[LocalDateTime]
    {
      def read(json: JsValue): LocalDateTime = json match {
        case JsString (value) => LocalDateTime.parse( value take 19 )
        case _ => throw new Exception("Only JsString is parsed to LocalDateTime.")
      }

      def write(obj: LocalDateTime): JsValue =
        JsString (obj.toString)
    }

  implicit val format: RootJsonFormat[ActivityJSON] = jsonFormat7( apply )
}
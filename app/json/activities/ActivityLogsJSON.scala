package json.activities

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class ActivityLogsJSON (activities: List[ActivityJSON],
                             pagination: PaginationJSON
                            ){
  def nextOpt: Option[String] =
    if (pagination.next.isEmpty) None
    else Some (pagination.next)
}

object ActivityLogsJSON extends DefaultJsonProtocol {
  implicit val format: RootJsonFormat[ActivityLogsJSON] = jsonFormat2 (apply)
}
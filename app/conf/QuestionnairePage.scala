package conf

import com.typesafe.config.Config
import play.api.ConfigLoader

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

case class QuestionnairePage( questions: Vector[String],
                              responses: Vector[String]
                            )
object QuestionnairePage {
  implicit val loader: ConfigLoader[Vector[QuestionnairePage]] =
    (config: Config, path: String) =>
      config.getConfigList( path ).asScala map { conf =>
        apply(
          conf.getStringList("questions").asScala.toVector,
          conf.getStringList("responses").asScala.toVector
        )
      } toVector
}
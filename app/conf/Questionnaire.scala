package conf

import java.time.LocalDate

import com.typesafe.config.Config
import io.methvin.play.autoconfig.AutoConfig
import play.api.ConfigLoader

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

case class Questionnaire( dates: Vector[LocalDate],
                          pages: Vector[QuestionnairePage]
                        )
object Questionnaire {
  implicit val dateLoader: ConfigLoader[Vector[LocalDate]] =
    (config: Config, path: String) =>
      config.getStringList(path).asScala map LocalDate.parse toVector

  implicit val loader: ConfigLoader[Questionnaire] = AutoConfig.loader
}

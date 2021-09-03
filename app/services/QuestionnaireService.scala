package services

import java.time.{Instant, LocalDate}

import conf.{Questionnaire, QuestionnairePage}
import javax.inject.{Inject, Singleton}
import models.{Participant, QuestionnaireResponse}
import play.api.Configuration

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

@Singleton
class QuestionnaireService @Inject()(configuration: Configuration) {
  import QuestionnaireService.Response

  val conf: Questionnaire = configuration.get[Questionnaire]("questionnaire")

  def listResponses: List[Response] =
    models.QuestionnaireResponse.find.all.asScala map Response.apply toList

  def listResponses(participant: Participant): List[Response] =
    participant.responses.asScala map Response.apply toList


  /** Map each question to the number of answer choices it has */
  private val numOfChoices =
    for (
      page <- conf.pages;
      _    <- page.questions
    ) yield page.responses.length.toByte

  val numOfQuestions: Int = numOfChoices.length

  def submitResponse(participant: Participant, answers: Vector[Byte]): Unit =
    new QuestionnaireResponse(
      participant,
      /** restrict answers to appropriate range of choices */
      answers zip numOfChoices map {
        case (a, c) => a min c max 1
      } mkString
    ).save()

  def listUpcoming: Vector[LocalDate] = conf.dates filter (_ isAfter LocalDate.now())

  def getCurrent(participant: Participant): Vector[QuestionnairePage] = {
    val hasCurrent = {
      conf.dates drop participant.responses.size() headOption
    } exists {
      _ isBefore LocalDate.now()
    }

    if(hasCurrent) conf.pages
    else Vector.empty
  }
}
object QuestionnaireService {
  case class Response(participantID: String, answers: Vector[Byte], completed: Instant)

  object Response {
    def apply(response: QuestionnaireResponse): Response =
      apply(
        response.participant.id,
        response.answers map (_.toString.toByte) toVector,
        response.completed.toInstant
      )
  }
}
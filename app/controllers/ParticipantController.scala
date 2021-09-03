package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import services.{ParticipantService, QuestionnaireService}
import views.html.{participant => participantViews}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.Try

@Singleton
class ParticipantController @Inject()(cc: ControllerComponents,
                                      ec: ExecutionContext,
                                      userAction: UserAction,
                                      participantService  : ParticipantService,
                                      questionnaireService: QuestionnaireService)
  extends AppController(cc, ec, userAction, participantService)
{
  def newParticipant: Action[AnyContent] =
    participantCreation(implicit req => Ok(participantViews.create()))

  def participantNew: Action[Map[String, Seq[String]]] =
    participantCreation (parse.formUrlEncoded) {
      implicit req =>
        req.body get "email" flatMap (_.headOption) map {
          email =>
            participantService.create(req.auth.user_id, email)
            Redirect(routes.FitbitController.sync(req.auth.user_id))
        } getOrElse
          BadRequest("No email parameter found in request.")
    }

  def viewActivities(id: String): Action[AnyContent] =
    participantAction(id)(implicit req => Ok(participantViews.activities()))

  def viewQuestionnaires(id: String): Action[AnyContent] =
    participantAction(id) { implicit req =>
      Ok(participantViews.questionnaire(
        questionnaireService.getCurrent(req.participant),
        questionnaireService.listUpcoming,
        questionnaireService.listResponses(req.participant)
      ))
    }

  def responseNew(id: String): Action[Map[String, Seq[String]]] =
    participantAction(id) (parse.formUrlEncoded) { implicit req =>
      Try {
        req.body collect { case k -> v if k startsWith "qu" => v.head.toByte }
      } filter {
        _.size == questionnaireService.numOfQuestions
      } fold (
        e => BadRequest(e.getMessage),
        r => {
          questionnaireService.submitResponse(req.participant, r.toVector)
          Redirect(routes.ParticipantController.viewQuestionnaires(req.participant.id))
        }
      )
    }

  def participantCreation: ActionBuilder[FitbitUserRequest, AnyContent] =
    fitbitUserAction andThen new ActionFilter[FitbitUserRequest] {
      def executionContext: ExecutionContext = ec

      def filter[A](request: FitbitUserRequest[A]): Future[Option[Result]] = Future.successful{
        participantService find request.auth.user_id map {
          participant =>
            Redirect(routes.FitbitController.sync(participant.id))
        }
      }
    }
}
package controllers

import javax.inject.{Inject, Singleton}
import models.{Activity, Participant}
import play.api.mvc._
import services.{ParticipantService, QuestionnaireService}
import views.html.{researcher => views}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.Try

@Singleton
class ResearcherController @Inject()(cc: ControllerComponents,
                                     ec: ExecutionContext,
                                     userAction: UserAction,
                                     participantService: ParticipantService,
                                     questionnaireService: QuestionnaireService
                                    ) extends AbstractController(cc) {
  private def prefix = "participant_"

  def viewParticipants: Action[AnyContent] =
    researcherAction ("view all participants"){ implicit req =>
      Ok(views.participants( Participant.find.all() ))
    }

  def editParticipants: Action[AnyContent] =
    researcherAction ("edit participants"){ implicit req =>
      Ok(views.participantsForm( Participant.find.all() ))
    }

  def participantsEdit: Action[Map[String, Seq[String]]] =
    researcherAction ("edit participants") (parse.formUrlEncoded) { implicit req =>
      req.body collect { case (k, v) if k startsWith prefix =>
        (k drop prefix.length) -> v
      } foreach { case (id, group) =>
        Try( participantService.assign(group.head, id))
      }
      Redirect(routes.ResearcherController.viewParticipants())
    }

  def viewActivities: Action[AnyContent] =
    researcherAction ("view all activities"){ implicit req =>
      Ok(views.activities( Activity.find.all() ))
    }

  def viewResponses: Action[AnyContent] =
    researcherAction ("view all questionnaire responses"){ implicit req =>
      Ok(views.responses( questionnaireService.listResponses ))
    }

  private def researcherAction(performAction: String) =
    userAction andThen new ActionFilter[UserRequest] {
      def executionContext: ExecutionContext = ec

      def filter[A](request: UserRequest[A]): Future[Option[Result]] =
        Future.successful{
          if(request.user.isLeft) None
          else Some(Unauthorized(s"Log in as a Researcher to $performAction."))
        }
    }
}

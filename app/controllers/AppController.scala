package controllers

import json.Authorisation
import models.Participant
import play.api.mvc._
import services.ParticipantService

import scala.concurrent.{ExecutionContext, Future}

abstract class AppController(cc: ControllerComponents,
                             ec: ExecutionContext,
                             userAction: UserAction,
                             participantService: ParticipantService
                            ) extends AbstractController(cc) {
  /** Action Builders */

  def fitbitUserAction[A]: ActionBuilder[FitbitUserRequest, AnyContent] =
    userAction andThen new FitbitUserAction

  def participantAction(id: String): ActionBuilder[ParticipantRequest, AnyContent] =
    fitbitUserAction andThen new ParticipantAction(id)

  /** Action Refiners */

  class FitbitUserAction extends ActionRefiner[UserRequest, FitbitUserRequest]
  {
    def executionContext: ExecutionContext = ec

    def refine[A](request: UserRequest[A]): Future[Either[Result, FitbitUserRequest[A]]] =
      Future.successful {
        request.user map { auth =>
          Right(new FitbitUserRequest(auth, request))
        } getOrElse
          Left(Unauthorized(s"Log in via Fitbit to view this page."))
      }
  }

  class ParticipantAction(id: String)
    extends ActionRefiner[FitbitUserRequest, ParticipantRequest]
  {
    def executionContext: ExecutionContext = ec

    def refine[A](request: FitbitUserRequest[A]): Future[Either[Result, ParticipantRequest[A]]] =
      Future.successful {
        participantService find request.auth.user_id match {
          case Some(participant) =>
            if (id == request.auth.user_id) Right(new ParticipantRequest(participant, request))
            else Left(Forbidden("You are not permitted to view other participants' data."))
          case None =>
            Left(NotFound("Participant with this ID doesn't exist."))
        }
      }
  }

  /** Wrapped Requests */

  class FitbitUserRequest[A](val auth: Authorisation, request: Request[A])
    extends WrappedRequest(request)

  class ParticipantRequest[A](val participant: Participant, request: FitbitUserRequest[A])
    extends WrappedRequest(request){
    def access_token: String = request.auth.access_token
  }
}
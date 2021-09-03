import java.time.Instant

import javax.inject.Inject
import json.{Authorisation, UserSession}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

package object controllers
{
  class UserAction @Inject() (val parser: BodyParsers.Default)
                             (implicit val executionContext: ExecutionContext)
    extends ActionBuilder [UserRequest, AnyContent]
      with ActionRefiner [Request, UserRequest]
      with Results
  {
    def invokeBlock[A](request: UserRequest[A],
                       block: UserRequest[A] => Future[Result]
                      ): Future[Result] =
      block(request) map (_ withSession request.refresh)

    def refine[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] =
      Future.successful {
        UserSession.getOpt(request) match {
          case Some(userSession) =>
            if (Instant.now() isBefore userSession.expires)
              Right(new UserRequest(userSession, request))
            else
              Left(Redirect(routes.HomeController.index()).withNewSession)
          case None =>
            Left(Unauthorized("You must be logged in to view this resource."))
        }
      }
  }


  class UserRequest[A](val userSession: UserSession, request: Request[A])
    extends WrappedRequest(request) {
    def refresh: Session = userSession.refreshed(request)
    def user: Either[Long, Authorisation] = userSession.user
  }
}

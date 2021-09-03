package controllers

import java.time.Instant

import json.UserSession
import javax.inject._
import play.api.mvc.{Session => _, _}
import services.ResearcherService

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               researchers: ResearcherService
                              ) extends AbstractController(cc)
{
  def index: Action[AnyContent] =
    Action {implicit req =>
      {for(
        session <- UserSession.getOpt if Instant.now() isBefore session.expires
      ) yield Redirect {
        if( session.user isLeft) routes.ResearcherController.viewParticipants()
        else                     routes.ParticipantController.newParticipant()
      }} getOrElse Ok( views.html.home() ).withNewSession
    }

  def login: Action[Map[String,Seq[String]]] =
    Action(parse.formUrlEncoded) { implicit req =>
      {
        for (
          username <- Try(req.body("username").head);
          password <- Try(req.body("password").head)
        ) yield username -> password
      } match {
        case Success(user -> pass) =>
          researchers.login(user, pass) match {
            case Some(session) =>
              Redirect(routes.ResearcherController.viewParticipants()) withSession session
            case None =>
              Unauthorized("Invalid username and password combination.")
          }
        case Failure(exception) => BadRequest(exception.getMessage)
      }
    }

  def logout: Action[AnyContent] = Action {
    Redirect( routes.HomeController.index() ).withNewSession
  }
}
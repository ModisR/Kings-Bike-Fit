package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, OAuth2BearerToken}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling._
import conf.FitbitAuth
import javax.inject.{Inject, Singleton}
import json.activities.ActivityLogsJSON
import json.{Authorisation, UserSession}
import play.api.Configuration
import play.api.mvc._
import services.ParticipantService
import spray.json.JsValue

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class FitbitController @Inject()(cc: ControllerComponents,
                                 conf: Configuration,
                                 userAction: UserAction,
                                 participantService: ParticipantService
                                )(implicit as: ActorSystem, ec: ExecutionContext)
  extends AppController(cc, ec, userAction, participantService) with SprayJsonSupport
{
  /** OAuth2 Config */
  private val fba = conf.get[FitbitAuth]("fitbit")

  /** Redirect Handler, obtains access token */

  def auth(code: Option[String]): Action[AnyContent] =
    Action.async { implicit req =>
      code match {
        case Some(authCode) =>
          val accessTokenReq = HttpRequest(
            method = HttpMethods.POST,
            uri = fba.tokenURI,
            entity = HttpEntity(
              ContentTypes.`application/x-www-form-urlencoded`,
              fba.tokenReqBody( authCode )
            ),
            headers = Seq(Authorization(BasicHttpCredentials(fba.credentials)))
          )

          for (
            resp <- Http().singleRequest(accessTokenReq);
            auth <- Unmarshal(resp).to[Authorisation]
          ) yield
            Redirect(routes.ParticipantController.newParticipant())
              .withSession(UserSession(auth))

        case None => Future.successful(Redirect(fba.fullAuthURI))
      }
    }

  /** API handler, syncs our database with Fitbit */

  def sync(id: String): Action[AnyContent] = participantAction(id).async {implicit req =>
    def apiCall(uri: String) = Http().singleRequest(
      HttpRequest(uri = uri,
        headers = Seq(Authorization(OAuth2BearerToken(req.access_token)))
      )
    )

    def syncActivities(uri: String, debug: Boolean = false): Future[Unit] =
      for(
        resp <- apiCall(uri);
        json <- Unmarshal(resp).to[JsValue]
      ) yield {
        if(debug) println( json.prettyPrint )

        val logs = json.convertTo[ActivityLogsJSON]
        participantService.update( req.participant, logs.activities )
        logs.nextOpt foreach (syncActivities(_, debug))
      }

    val uriParams = FitbitAuth.uriStringify(
      "offset" -> "0",
      "sort" -> "asc",
      "limit" -> "20",
      "afterDate" -> participantService.lastUpdated(req.participant).toString
    )

    for (
      //resp <- apiCall( apiURI + auth.user_id + "/profile.json");
      //json <- Unmarshal( resp ).to[JsValue];
      _ <- syncActivities(fba.apiURI + req.participant.id + "/activities/list.json?" + uriParams)
    ) yield {
      //val profile = json.convertTo[ProfileJSON]
      Redirect(routes.ParticipantController.viewQuestionnaires(req.participant.id))
    }
  }
}
package json

import java.time.Instant

import play.api.libs.json._
import play.api.mvc.{RequestHeader, Session}

import scala.language.postfixOps

/** user is either:
 *  - Long ID if Researcher
 *  - Fitbit Authorisation Fitbit if Participant
 */
case class UserSession(user: Either[Long, Authorisation],
                       expires: Instant
                  ){
  def refreshed(implicit req: RequestHeader): Session =
    UserSession( user, Instant.now() plusSeconds UserSession.timeOut).asPlay

  private def asPlay(implicit req: RequestHeader): Session =
    req.session + ("session" -> UserSession.format.writes(this).toString)
}
object UserSession
{
  implicit def eitherFormat[L, R](implicit lf: Format[L], rf: Format[R]): Format[Either[L, R]] =
    new Format[Either[L, R]]
    {
      def reads(json: JsValue): JsResult[Either[L, R]] = {
        rf.reads(json) map (Right(_))
      } orElse {
        lf.reads(json) map (Left(_))
      }

      def writes(o: Either[L, R]): JsValue =
        o.fold( lf.writes, rf.writes )
    }

  private implicit val format: OFormat[UserSession] = Json.format

  /** Seconds of inactivity for auto-logout */
  private val timeOut = 1800

  /** String conversions */
  def apply(id: Long)(implicit req: RequestHeader): Session =
    apply(
      Left(id),
      Instant.now() plusSeconds timeOut
    ).asPlay

  def apply(auth: Authorisation)(implicit req: RequestHeader): Session =
    apply(
      Right(auth),
      Instant.now() plusSeconds auth.expires_in/2
    ).asPlay

  def getOpt(implicit req: RequestHeader): Option[UserSession] =
    req.session get "session" map (Json.parse(_).as[UserSession])
}
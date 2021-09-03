package conf

import java.nio.charset.StandardCharsets
import java.util.Base64

import controllers.routes
import io.methvin.play.autoconfig._
import play.api.ConfigLoader
import play.api.mvc.RequestHeader

import scala.language.postfixOps

case class FitbitAuth( clientID: String,
                       clientSecret: String,
                       authURI: String,
                       authURIParams: Map[String, String],
                       tokenURI: String,
                       apiURI: String
                     ){
  def redirectURI(implicit req: RequestHeader) =
    s"http${if(req.secure) "s" else ""}://${req.host}${routes.FitbitController.auth()}"

  def fullAuthURI(implicit req: RequestHeader): String =
    authURI + "?" + FitbitAuth.uriStringify( authURIParams.toSeq: _* )

  def tokenReqBody(authCode: String)(implicit req: RequestHeader): String =
    FitbitAuth.uriStringify(
      "code" -> authCode,
      "grant_type" -> "authorization_code",
      "client_id" -> clientID,
      "redirect_uri" -> redirectURI
    )

  def credentials: String =
    Base64.getEncoder.encodeToString(
      s"$clientID:$clientSecret".getBytes( StandardCharsets.UTF_8 )
    )
}
object FitbitAuth {
  implicit val loader: ConfigLoader[FitbitAuth] = AutoConfig.loader

  def uriStringify(params: (String, String)*): String =
    params map { case (k, v) =>
      k + "=" + views.html.helper.urlEncode( v )
    } mkString "&"
}
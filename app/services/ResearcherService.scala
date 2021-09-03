package services

import json.UserSession
import javax.inject.{Inject, Singleton}
import models.Researcher
import org.mindrot.jbcrypt.BCrypt
import play.api.Configuration
import play.api.mvc.{RequestHeader, Session => PlaySession}

import scala.util.Try

@Singleton
class ResearcherService @Inject()(conf: Configuration) {

  def create(username: String, password: String): Researcher = {
    val researcher = new Researcher
    researcher.username = username
    researcher.passwordHash = BCrypt.hashpw( password, BCrypt.gensalt(12) )
    researcher.save()
    researcher
  }

  def login(username: String, password: String)(implicit req: RequestHeader): Option[PlaySession] =
    for(
      researcher <- Option(
        Researcher.find.query().where()
          .eq("username", username)
          .findOne()
      ) if BCrypt.checkpw( password, researcher.passwordHash )
    ) yield
      UserSession(researcher.id)

  for( researchers <- Try (Researcher.find.query().findCount()) if researchers < 1 ) {
    val username = conf.get[String]("username")
    val password = conf.get[String]("password")

    create (username, password)
  }
}
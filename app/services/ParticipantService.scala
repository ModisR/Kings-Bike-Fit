package services

import java.time.{LocalDate, LocalDateTime}

import javax.inject.{Inject, Singleton}
import json.activities.ActivityJSON
import models.Participant.ResearchGroup
import models.{Activity, Participant}
import play.api.Configuration

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

@Singleton
class ParticipantService @Inject()(conf: Configuration) {
  private val experimentStart = LocalDate.parse(
    conf.get[Seq[String]]("questionnaire.dates").head
  ) atStartOfDay

  def create(fitbitID: String, email: String ): Participant = {
    val participant = new Participant(fitbitID, email)
    participant.save()
    participant
  }

  def update(participant: Participant, activities: Seq[ActivityJSON] ): Unit =
    activities foreach { activity =>
      new Activity(
        participant,
        activity.activityName,
        activity.averageHeartRate map Integer.valueOf orNull,
        activity.calories,
        activity.distance map java.lang.Double.valueOf orNull,
        activity.duration / 1000,
        activity.startTime
      ).save()
    }

  def assign(group: String, id: String): Unit = {
    val p = Participant.find.byId( id )
    p.researchGroup = ResearchGroup.valueOf( group )
    p.save()
  }

  def lastUpdated(participant: Participant): LocalDateTime = {
    val activityStarts = participant.activities.asScala map (_.startDateTime) toList

    (experimentStart :: activityStarts max) plusSeconds 1
  }

  def find(id: String): Option[Participant] = Option( Participant.find.byId(id))
}

package models;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.NotNull;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
public class QuestionnaireResponse extends Model {
	static public Finder<Long, QuestionnaireResponse> find = new Finder<>(QuestionnaireResponse.class);

	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	public long id;

	@NotNull @ManyToOne
	public Participant participant;

	public String answers;

	public Timestamp completed;

	public QuestionnaireResponse(Participant participant, String answers) {
		this.participant = participant;
		this.answers = answers;
		this.completed = Timestamp.from(Instant.now());
	}
}
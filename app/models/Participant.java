package models;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.EnumValue;
import io.ebean.annotation.NotNull;
import org.checkerframework.common.aliasing.qual.Unique;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Participant extends Model {
	static public Finder<String, Participant> find = new Finder<>(Participant.class);

	public enum ResearchGroup {
		@EnumValue("U") UNASSIGNED,
		@EnumValue("N") CONTROL,
		@EnumValue("M") COMMUTING
	}

	@Id
	public String id;

	@NotNull @Unique
	public String email;

	@NotNull
	public ResearchGroup researchGroup;

	@OneToMany
	public List<Activity> activities;

	@OneToMany
	public List<QuestionnaireResponse> responses;

	public Participant(@Unique String id, @Unique String email) {
		this.id = id;
		this.email = email;
		this.researchGroup = ResearchGroup.UNASSIGNED;
	}
}
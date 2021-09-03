package models;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.NotNull;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Activity extends Model {
    static public Finder<Long, Activity> find = new Finder<>(Activity.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    @ManyToOne @NotNull
    public Participant participant;

    public String name;

    public Integer avgHeartRate;

    public int calories;

    public Double distance;

    public int duration;

    public LocalDateTime startDateTime;

    public Activity(Participant participant,
                    String name,
                    int avgHeartRate,
                    int calories,
                    Double distance,
                    int duration,
                    LocalDateTime startDateTime) {
        this.participant = participant;
        this.name = name;
        this.avgHeartRate = avgHeartRate;
        this.calories = calories;
        this.distance = distance;
        this.duration = duration;
        this.startDateTime = startDateTime;
    }
}
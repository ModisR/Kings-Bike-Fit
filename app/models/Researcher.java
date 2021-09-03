package models;

import io.ebean.Finder;
import io.ebean.Model;
import org.checkerframework.common.aliasing.qual.Unique;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.AUTO;

@Entity
public class Researcher extends Model {
    static public Finder<Long, Researcher> find = new Finder<>(Researcher.class);

    @Id @GeneratedValue(strategy = AUTO)
    public long id;

    @Unique  @Column(length = 16)
    public String username;

    @Column(length = 64)
    public String passwordHash;

    public Researcher() {}
}

package com.example.sqldelight.hockey.data;

import com.google.auto.value.AutoValue;
import java.util.Calendar;

@AutoValue
public abstract class Team implements TeamModel {
  private static final DateAdapter DATE_MM = new DateAdapter();

  public static final Mapper<Team> MAPPER = new Mapper<>(new Mapper.Creator<Team>() {
    @Override public Team create(long Id, String name, Calendar founded, String coach, Long captain,
        boolean wonCup) {
      return new AutoValue_Team(Id, name, founded, coach, captain, wonCup);
    }
  }, DATE_MM);

  public static TeamMarshal marshal() {
    return new TeamMarshal<>(DATE_MM);
  }
}

package edu.gdei.gdeiassistant.Pojo.ScheduleQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

import edu.gdei.gdeiassistant.Pojo.Entity.Schedule;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleQueryResult implements Serializable {

    private List<Schedule> scheduleList;

    private Integer week;

    public List<Schedule> getScheduleList() {
        return scheduleList;
    }

    public void setScheduleList(List<Schedule> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public ScheduleQueryResult() {
    }

    public ScheduleQueryResult(List<Schedule> scheduleList, int week) {
        this.scheduleList = scheduleList;
        this.week = week;
    }
}

package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Access implements Serializable {

    private Boolean grade;

    private Boolean schedule;

    private Boolean cet;

    private Boolean evaluate;

    private Boolean card;

    private Boolean bill;

    private Boolean lost;

    private Boolean charge;

    public Boolean getGrade() {
        return grade;
    }

    public void setGrade(Boolean grade) {
        this.grade = grade;
    }

    public Boolean getSchedule() {
        return schedule;
    }

    public void setSchedule(Boolean schedule) {
        this.schedule = schedule;
    }

    public Boolean getCet() {
        return cet;
    }

    public void setCet(Boolean cet) {
        this.cet = cet;
    }

    public Boolean getEvaluate() {
        return evaluate;
    }

    public void setEvaluate(Boolean evaluate) {
        this.evaluate = evaluate;
    }

    public Boolean getCard() {
        return card;
    }

    public void setCard(Boolean card) {
        this.card = card;
    }

    public Boolean getBill() {
        return bill;
    }

    public void setBill(Boolean bill) {
        this.bill = bill;
    }

    public Boolean getLost() {
        return lost;
    }

    public void setLost(Boolean lost) {
        this.lost = lost;
    }

    public Boolean getCharge() {
        return charge;
    }

    public void setCharge(Boolean charge) {
        this.charge = charge;
    }
}

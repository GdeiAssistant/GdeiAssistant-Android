package edu.gdei.gdeiassistant.Pojo.GradeQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

import edu.gdei.gdeiassistant.Pojo.Entity.Grade;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GradeQueryResult implements Serializable {

    private Integer year;

    private String firstTermGPA;

    private String secondTermGPA;

    private String firstTermIGP;

    private String secondTermIGP;

    private List<Grade> firstTermGradeList;

    private List<Grade> secondTermGradeList;

    public String getFirstTermGPA() {
        return firstTermGPA;
    }

    public void setFirstTermGPA(String firstTermGPA) {
        this.firstTermGPA = firstTermGPA;
    }

    public String getSecondTermGPA() {
        return secondTermGPA;
    }

    public void setSecondTermGPA(String secondTermGPA) {
        this.secondTermGPA = secondTermGPA;
    }

    public String getFirstTermIGP() {
        return firstTermIGP;
    }

    public void setFirstTermIGP(String firstTermIGP) {
        this.firstTermIGP = firstTermIGP;
    }

    public String getSecondTermIGP() {
        return secondTermIGP;
    }

    public void setSecondTermIGP(String secondTermIGP) {
        this.secondTermIGP = secondTermIGP;
    }

    public List<Grade> getFirstTermGradeList() {
        return firstTermGradeList;
    }

    public void setFirstTermGradeList(List<Grade> firstTermGradeList) {
        this.firstTermGradeList = firstTermGradeList;
    }

    public List<Grade> getSecondTermGradeList() {
        return secondTermGradeList;
    }

    public void setSecondTermGradeList(List<Grade> secondTermGradeList) {
        this.secondTermGradeList = secondTermGradeList;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}

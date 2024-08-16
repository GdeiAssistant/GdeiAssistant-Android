package cn.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book implements Serializable {

    //条形码
    private String id;

    //续借的SN号
    private String sn;

    //续借的Code号
    private String code;

    //书名
    private String name;

    //作者
    private String author;

    //借阅时间
    private String borrowDate;

    //应还时间
    private String returnDate;

    //续借次数
    private Integer renewTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(String borrowDate) {
        this.borrowDate = borrowDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public Integer getRenewTime() {
        return renewTime;
    }

    public void setRenewTime(Integer renewTime) {
        this.renewTime = renewTime;
    }
}

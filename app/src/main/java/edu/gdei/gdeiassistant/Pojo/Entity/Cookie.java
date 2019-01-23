package edu.gdei.gdeiassistant.Pojo.Entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by linguancheng on 2017/9/7.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cookie implements Serializable {

    @JSONField(ordinal = 1)
    private String name;

    @JSONField(ordinal = 2)
    private String value;

    @JSONField(ordinal = 3)
    private String domain;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}

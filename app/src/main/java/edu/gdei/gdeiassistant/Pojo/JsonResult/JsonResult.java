package edu.gdei.gdeiassistant.Pojo.JsonResult;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonResult implements Serializable {

    private Boolean success;

    private Integer code;

    private String message;

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonResult() {

    }

    public JsonResult(Boolean success) {
        this.success = success;
    }

    public JsonResult(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public JsonResult(Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}

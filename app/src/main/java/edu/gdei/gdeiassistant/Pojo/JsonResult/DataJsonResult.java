package edu.gdei.gdeiassistant.Pojo.JsonResult;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataJsonResult<T> extends JsonResult {

    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public DataJsonResult() {

    }

    public DataJsonResult(Boolean success, T data) {
        super(success);
        this.data = data;
    }

    public DataJsonResult(Boolean success, String message) {
        super(success, message);
    }

    public DataJsonResult(Boolean success, Integer code, String message) {
        super(success, code, message);
    }
}

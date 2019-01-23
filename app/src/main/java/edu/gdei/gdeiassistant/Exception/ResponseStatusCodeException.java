package edu.gdei.gdeiassistant.Exception;

/**
 * 访问API接口时，返回的HTTP状态码不是200时，抛出该异常
 */
public class ResponseStatusCodeException extends Exception {

    public ResponseStatusCodeException() {

    }

    public ResponseStatusCodeException(String message) {
        super(message);
    }
}

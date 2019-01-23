package edu.gdei.gdeiassistant.Pojo.Token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import edu.gdei.gdeiassistant.Pojo.Entity.Token;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenRefreshResult implements Serializable {

    private Token accessToken;

    private Token refreshToken;

    public Token getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(Token accessToken) {
        this.accessToken = accessToken;
    }

    public Token getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(Token refreshToken) {
        this.refreshToken = refreshToken;
    }
}

package cn.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardInfo implements Serializable {

    private String name;

    private String number;

    private String cardBalance;

    private String cardInterimBalance;

    private String cardNumber;

    private String cardLostState;

    private String cardFreezeState;

    public String getCardBalance() {
        return cardBalance;
    }

    public void setCardBalance(String cardBalance) {
        this.cardBalance = cardBalance;
    }

    public String getCardInterimBalance() {
        return cardInterimBalance;
    }

    public void setCardInterimBalance(String cardInterimBalance) {
        this.cardInterimBalance = cardInterimBalance;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardLostState() {
        return cardLostState;
    }

    public void setCardLostState(String cardLostState) {
        this.cardLostState = cardLostState;
    }

    public String getCardFreezeState() {
        return cardFreezeState;
    }

    public void setCardFreezeState(String cardFreezeState) {
        this.cardFreezeState = cardFreezeState;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

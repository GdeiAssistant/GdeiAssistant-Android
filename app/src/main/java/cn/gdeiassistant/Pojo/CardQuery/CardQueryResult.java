package cn.gdeiassistant.Pojo.CardQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

import cn.gdeiassistant.Pojo.Entity.Card;
import cn.gdeiassistant.Pojo.Entity.CardInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardQueryResult implements Serializable {

    private CardInfo cardInfo;

    private List<Card> cardList;

    public List<Card> getCardList() {
        return cardList;
    }

    public void setCardList(List<Card> cardList) {
        this.cardList = cardList;
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(CardInfo cardInfo) {
        this.cardInfo = cardInfo;
    }
}

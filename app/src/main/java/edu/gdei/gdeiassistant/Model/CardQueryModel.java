package edu.gdei.gdeiassistant.Model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.Serializable;

import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Exception.ResponseStatusCodeException;
import edu.gdei.gdeiassistant.NetWork.CardQueryNetWork;
import edu.gdei.gdeiassistant.Pojo.CardQuery.CardQueryResult;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;

public class CardQueryModel {

    private CardQueryNetWork cardQueryNetWork = new CardQueryNetWork();

    /**
     * 校园卡消费记录查询
     *
     * @param handler
     * @param year
     * @param month
     * @param date
     * @param context
     */
    public void CardQuery(final Handler handler, final int year, final int month, final int date, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                try {
                    message.what = RequestConstant.SHOW_PROGRESS;
                    handler.sendMessage(message);
                    message = handler.obtainMessage();
                    DataJsonResult<CardQueryResult> result = cardQueryNetWork.CardQuery(year, month, date, context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null) {
                            bundle.putString("Year", String.valueOf(year));
                            bundle.putString("Month", String.valueOf(month));
                            bundle.putString("Date", String.valueOf(date));
                            bundle.putSerializable("CardInfo", result.getData().getCardInfo());
                            bundle.putSerializable("CardList", (Serializable) result.getData().getCardList());
                            message.what = RequestConstant.REQUEST_SUCCESS;
                        } else {
                            message.what = RequestConstant.SERVER_ERROR;
                        }
                    } else {
                        bundle.putString("Message", result.getMessage());
                        message.what = RequestConstant.REQUEST_FAILURE;
                    }
                } catch (NullPointerException ignored) {

                } catch (IOException e) {
                    message.what = RequestConstant.REQUEST_TIMEOUT;
                } catch (ResponseStatusCodeException e) {
                    message.what = RequestConstant.SERVER_ERROR;
                } catch (Exception e) {
                    message.what = RequestConstant.UNKNOWN_ERROR;
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }.start();
    }
}

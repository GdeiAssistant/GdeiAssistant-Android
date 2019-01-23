package edu.gdei.gdeiassistant.Presenter;

import android.app.DatePickerDialog;
import android.os.Handler;
import android.os.Message;
import android.widget.DatePicker;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;

import edu.gdei.gdeiassistant.Activity.CardActivity;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Model.CardQueryModel;
import edu.gdei.gdeiassistant.Pojo.Entity.Card;
import edu.gdei.gdeiassistant.Pojo.Entity.CardInfo;

public class CardPresenter {

    private CardActivity cardActivity;

    private CardActivityHandler cardActivityHandler;

    private CardQueryModel cardQueryModel;

    /**
     * 当前的查询日期
     */
    private int currentSelectedYear;
    private int currentSelectedMonth;
    private int currentSelectedDate;

    public CardPresenter(CardActivity cardActivity) {
        this.cardActivity = cardActivity;
        this.cardQueryModel = new CardQueryModel();
        this.cardActivityHandler = new CardActivityHandler(cardActivity);
        Init();
    }

    /**
     * 移除所有的回调和消息，防止内存泄露
     */
    public void RemoveCallBacksAndMessages() {
        cardActivityHandler.removeCallbacksAndMessages(null);
    }

    public static class CardActivityHandler extends Handler {

        private CardActivity cardActivity;

        CardActivityHandler(CardActivity cardActivity) {
            this.cardActivity = new WeakReference<>(cardActivity).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.SHOW_PROGRESS:
                    //显示进度条
                    cardActivity.HideDataLayout();
                    cardActivity.HideFailTip();
                    cardActivity.ShowProgressbar();
                    break;

                case RequestConstant.REQUEST_SUCCESS:
                    //查询消费记录成功
                    cardActivity.HideFailTip();
                    cardActivity.SetCurrentDate(msg.getData().getString("Year")
                            , msg.getData().getString("Month"), msg.getData().getString("Date"));
                    cardActivity.LoadCardData((List<Card>) msg.getData().getSerializable("CardList")
                            , (CardInfo) msg.getData().getSerializable("CardInfo"));
                    cardActivity.HideProgressbar();
                    cardActivity.ShowDataLayout();
                    break;

                case RequestConstant.REQUEST_FAILURE:
                    //查询消费记录失败
                    cardActivity.HideProgressbar();
                    cardActivity.HideDataLayout();
                    cardActivity.ShowFailTip(msg.getData().getString("Message"));
                    break;

                case RequestConstant.REQUEST_TIMEOUT:
                    //网络连接超时
                    cardActivity.HideProgressbar();
                    cardActivity.HideDataLayout();
                    cardActivity.ShowFailTip("网络连接超时，点击重试");
                    break;

                case RequestConstant.SERVER_ERROR:
                    //服务暂不可用
                    cardActivity.HideProgressbar();
                    cardActivity.HideDataLayout();
                    cardActivity.ShowFailTip("服务暂不可用，点击重试");
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    //出现未知异常
                    cardActivity.HideProgressbar();
                    cardActivity.HideDataLayout();
                    cardActivity.ShowFailTip("出现未知异常，点击重试");
                    break;
            }
        }
    }

    private void Init() {
        //设置默认的查询日期
        Calendar calendar = Calendar.getInstance();
        this.currentSelectedYear = calendar.get(Calendar.YEAR);
        this.currentSelectedMonth = calendar.get(Calendar.MONTH) + 1;
        this.currentSelectedDate = calendar.get(Calendar.DATE);
        cardActivity.UpdateDateTextView(currentSelectedYear, currentSelectedMonth, currentSelectedDate);
        CardQuery();
    }

    /**
     * 弹出日期选择框
     */
    public void SelectDate() {
        //选择日期
        DatePickerDialog datePickerDialog = new DatePickerDialog(cardActivity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                currentSelectedYear = year;
                currentSelectedMonth = month + 1;
                currentSelectedDate = dayOfMonth;
                cardActivity.UpdateDateTextView(currentSelectedYear, currentSelectedMonth, currentSelectedDate);
                CardQuery();
            }
        }, currentSelectedYear, currentSelectedMonth - 1, currentSelectedDate);
        //最早查询前一年的记录
        Calendar minCalendar = Calendar.getInstance();
        minCalendar.add(Calendar.YEAR, -1);
        datePickerDialog.getDatePicker().setMinDate(minCalendar.getTimeInMillis());
        //最晚查询今天的记录
        Calendar calendar = Calendar.getInstance();
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    /**
     * 查询消费流水记录
     */
    public void CardQuery() {
        cardQueryModel.CardQuery(cardActivityHandler, currentSelectedYear, currentSelectedMonth
                , currentSelectedDate, cardActivity.getApplicationContext());
    }
}

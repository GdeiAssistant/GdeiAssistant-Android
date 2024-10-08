package cn.gdeiassistant.Presenter;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.gdeiassistant.Constant.IndexTagConstant;
import cn.gdeiassistant.Constant.RequestConstant;
import cn.gdeiassistant.Fragment.FragmentIndex;
import cn.gdeiassistant.Model.IndexModel;
import cn.gdeiassistant.Pojo.Entity.CardInfo;
import cn.gdeiassistant.Pojo.Entity.Schedule;

public class IndexPresenter {

    private FragmentIndex fragmentIndex;

    private IndexModel indexModel;

    private FragmentIndexHandler fragmentIndexHandler;

    public static class FragmentIndexHandler extends Handler {

        private FragmentIndex fragmentIndex;

        FragmentIndexHandler(FragmentIndex fragmentIndex) {
            this.fragmentIndex = new WeakReference<>(fragmentIndex).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.SHOW_PROGRESS:
                    switch (msg.getData().getInt("Tag")) {
                        case IndexTagConstant.SCHEDULE_QUERY:
                            //显示今日课表模块进度条
                            fragmentIndex.HideScheduleFailTip();
                            fragmentIndex.HideScheduleDataListView();
                            fragmentIndex.ShowScheduleProgressbar();
                            break;

                        case IndexTagConstant.CARD_QUERY:
                            //显示校园卡卡基本信息模块进度条
                            fragmentIndex.HideCardFailTip();
                            fragmentIndex.HideCardDataLayout();
                            fragmentIndex.ShowCardProgressbar();
                            break;
                    }
                    break;

                case RequestConstant.REQUEST_SUCCESS:
                    switch (msg.getData().getInt("Tag")) {
                        case IndexTagConstant.SCHEDULE_QUERY:
                            //查询今日课表成功
                            fragmentIndex.HideScheduleProgressbar();
                            fragmentIndex.HideRefreshingTip();
                            List<Schedule> todayScheduleList = (List<Schedule>) msg.getData().getSerializable("ScheduleList");
                            fragmentIndex.LoadTodaySchedule(todayScheduleList);
                            break;

                        case IndexTagConstant.CARD_QUERY:
                            //查询饭卡基本信息成功
                            fragmentIndex.HideCardProgressbar();
                            fragmentIndex.HideRefreshingTip();
                            fragmentIndex.HideCardFailTip();
                            CardInfo cardInfo = (CardInfo) msg.getData().getSerializable("CardInfo");
                            fragmentIndex.LoadCardInfo(cardInfo);
                            break;
                    }
                    break;

                case RequestConstant.REQUEST_FAILURE:
                    switch (msg.getData().getInt("Tag")) {
                        case IndexTagConstant.SCHEDULE_QUERY:
                            //查询今日课表失败
                            fragmentIndex.HideScheduleProgressbar();
                            fragmentIndex.HideRefreshingTip();
                            fragmentIndex.ShowScheduleFailTip(msg.getData().getString("Message"));
                            break;

                        case IndexTagConstant.CARD_QUERY:
                            //查询校园卡基本信息失败
                            fragmentIndex.HideCardProgressbar();
                            fragmentIndex.HideRefreshingTip();
                            fragmentIndex.ShowCardFailTip(msg.getData().getString("Message"));
                            break;
                    }
                    break;

                case RequestConstant.REQUEST_TIMEOUT:
                    switch (msg.getData().getInt("Tag")) {
                        case IndexTagConstant.SCHEDULE_QUERY:
                            //查询今日课表失败
                            fragmentIndex.HideScheduleProgressbar();
                            fragmentIndex.HideRefreshingTip();
                            fragmentIndex.ShowScheduleFailTip("网络连接超时，点击重试");
                            break;

                        case IndexTagConstant.CARD_QUERY:
                            //查询校园卡基本信息失败
                            fragmentIndex.HideCardProgressbar();
                            fragmentIndex.HideRefreshingTip();
                            fragmentIndex.ShowCardFailTip("网络连接超时，点击重试");
                            break;
                    }
                    break;

                case RequestConstant.SERVER_ERROR:
                    switch (msg.getData().getInt("Tag")) {
                        case IndexTagConstant.SCHEDULE_QUERY:
                            //查询今日课表失败
                            fragmentIndex.HideScheduleProgressbar();
                            fragmentIndex.HideRefreshingTip();
                            fragmentIndex.ShowScheduleFailTip("服务暂不可用，点击重试");
                            break;

                        case IndexTagConstant.CARD_QUERY:
                            //查询校园卡基本信息失败
                            fragmentIndex.HideCardProgressbar();
                            fragmentIndex.HideRefreshingTip();
                            fragmentIndex.ShowCardFailTip("服务暂不可用，点击重试");
                            break;
                    }
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    switch (msg.getData().getInt("Tag")) {
                        case IndexTagConstant.SCHEDULE_QUERY:
                            //查询今日课表失败
                            fragmentIndex.HideScheduleProgressbar();
                            fragmentIndex.HideRefreshingTip();
                            fragmentIndex.ShowScheduleFailTip("出现未知异常，点击重试");
                            break;

                        case IndexTagConstant.CARD_QUERY:
                            //查询校园卡基本信息失败
                            fragmentIndex.HideCardProgressbar();
                            fragmentIndex.HideRefreshingTip();
                            fragmentIndex.ShowCardFailTip("出现未知异常，点击重试");
                            break;
                    }
                    break;
            }
        }
    }

    /**
     * 移除所有的回调和消息，防止内存泄露
     */
    public void RemoveCallBacksAndMessages() {
        fragmentIndexHandler.removeCallbacksAndMessages(null);
    }

    public IndexPresenter(FragmentIndex fragmentIndex) {
        this.fragmentIndex = fragmentIndex;
        this.indexModel = new IndexModel();
        this.fragmentIndexHandler = new FragmentIndexHandler(fragmentIndex);
    }

    /**
     * 查询今日课表信息
     */
    public void TodayScheduleQuery() {
        indexModel.TodayScheduleQuery(fragmentIndexHandler, fragmentIndex.getActivity().getApplicationContext());
    }

    /**
     * 查询校园卡信息
     */
    public void CardInfoQuery() {
        indexModel.CardInfoQuery(fragmentIndexHandler, fragmentIndex.getActivity().getApplicationContext());
    }

}

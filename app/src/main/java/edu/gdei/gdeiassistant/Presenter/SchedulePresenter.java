package edu.gdei.gdeiassistant.Presenter;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Fragment.FragmentSchedule;
import edu.gdei.gdeiassistant.Model.ScheduleQueryModel;
import edu.gdei.gdeiassistant.Pojo.Entity.Schedule;

public class SchedulePresenter {

    private Integer week;

    private FragmentSchedule fragmentSchedule;

    private FragmentScheduleHandler fragmentScheduleHandler;

    private ScheduleQueryModel scheduleQueryModel;

    public SchedulePresenter(FragmentSchedule fragmentSchedule) {
        this.fragmentSchedule = fragmentSchedule;
        this.fragmentScheduleHandler = new FragmentScheduleHandler(fragmentSchedule);
        this.scheduleQueryModel = new ScheduleQueryModel();
        Init();
    }

    public static class FragmentScheduleHandler extends Handler {

        private FragmentSchedule fragmentSchedule;

        FragmentScheduleHandler(FragmentSchedule fragmentSchedule) {
            this.fragmentSchedule = new WeakReference<>(fragmentSchedule).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.SHOW_PROGRESS:
                    //显示进度条
                    fragmentSchedule.ShowProgressbar();
                    fragmentSchedule.HideFailTip();
                    break;

                case RequestConstant.REQUEST_SUCCESS:
                    //查询课表成功
                    fragmentSchedule.HideProgressbar();
                    fragmentSchedule.HideFailTip();
                    List<Schedule> scheduleList = (List<Schedule>) msg.getData().getSerializable("ScheduleList");
                    fragmentSchedule.LoadScheduleData(scheduleList);
                    break;

                case RequestConstant.REQUEST_FAILURE:
                    //查询课表失败
                    fragmentSchedule.HideProgressbar();
                    fragmentSchedule.ShowFailTip(msg.getData().getString("Message"));
                    break;

                case RequestConstant.REQUEST_TIMEOUT:
                    //网络连接超时
                    fragmentSchedule.HideProgressbar();
                    fragmentSchedule.ShowFailTip("网络连接超时，点击重试");
                    break;

                case RequestConstant.SERVER_ERROR:
                    //服务暂不可用
                    fragmentSchedule.HideProgressbar();
                    fragmentSchedule.ShowFailTip("服务暂不可用，点击重试");
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    //出现未知异常
                    fragmentSchedule.HideProgressbar();
                    fragmentSchedule.ShowFailTip("出现未知异常，点击重试");
                    break;
            }
        }
    }

    /**
     * 移除所有的回调和消息,防止内存泄露
     */
    public void RemoveCallBacksAndMessages() {
        fragmentScheduleHandler.removeCallbacksAndMessages(null);
    }

    private void Init() {
        ScheduleQuery();
    }

    public void ChangeSelectWeek(int week) {
        this.week = week;
        ScheduleQuery();
    }

    /**
     * 查询课表信息
     */
    public void ScheduleQuery() {
        scheduleQueryModel.ScheduleQuery(week, fragmentScheduleHandler, fragmentSchedule.getActivity().getApplicationContext());
    }
}

package cn.gdeiassistant.Model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.gdeiassistant.Constant.IndexTagConstant;
import cn.gdeiassistant.Constant.RequestConstant;
import cn.gdeiassistant.Exception.ResponseStatusCodeException;
import cn.gdeiassistant.NetWork.CardQueryNetWork;
import cn.gdeiassistant.NetWork.ScheduleQueryNetWork;
import cn.gdeiassistant.Pojo.Entity.CardInfo;
import cn.gdeiassistant.Pojo.Entity.Schedule;
import cn.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import cn.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;

public class IndexModel {

    private CardQueryNetWork cardQueryNetWork = new CardQueryNetWork();

    private ScheduleQueryNetWork scheduleQueryNetWork = new ScheduleQueryNetWork();

    /**
     * 查询今日的课表
     *
     * @param handler
     */
    public void TodayScheduleQuery(final Handler handler, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt("Tag", IndexTagConstant.SCHEDULE_QUERY);
                try {
                    message.what = RequestConstant.SHOW_PROGRESS;
                    message.setData(bundle);
                    handler.sendMessage(message);
                    message = handler.obtainMessage();
                    bundle = new Bundle();
                    bundle.putInt("Tag", IndexTagConstant.SCHEDULE_QUERY);
                    DataJsonResult<ScheduleQueryResult> result = scheduleQueryNetWork.ScheduleQuery(null, context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        if (result.getData() != null) {
                            //筛选出今天的课程
                            List<Schedule> scheduleList = result.getData().getScheduleList();
                            List<Schedule> todayScheduleList = new ArrayList<>();
                            if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                for (Schedule schedule : scheduleList) {
                                    if (schedule.getColumn() == 6) {
                                        todayScheduleList.add(schedule);
                                    }
                                }
                            } else {
                                for (Schedule schedule : scheduleList) {
                                    if (schedule.getColumn() == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2) {
                                        todayScheduleList.add(schedule);
                                    }
                                }
                            }
                            bundle.putSerializable("ScheduleList", (Serializable) todayScheduleList);
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

    /**
     * 加载饭卡基本信息
     *
     * @param handler
     */
    public void CardInfoQuery(final Handler handler, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt("Tag", IndexTagConstant.CARD_QUERY);
                try {
                    message.what = RequestConstant.SHOW_PROGRESS;
                    message.setData(bundle);
                    handler.sendMessage(message);
                    message = handler.obtainMessage();
                    bundle = new Bundle();
                    bundle.putInt("Tag", IndexTagConstant.CARD_QUERY);
                    DataJsonResult<CardInfo> result = cardQueryNetWork.CardInfoQuery(context);
                    if (result.isSuccess()) {
                        bundle.putSerializable("CardInfo", result.getData());
                        message.what = RequestConstant.REQUEST_SUCCESS;
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

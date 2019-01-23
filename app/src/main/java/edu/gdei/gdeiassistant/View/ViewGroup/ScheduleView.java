package edu.gdei.gdeiassistant.View.ViewGroup;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import edu.gdei.gdeiassistant.Pojo.Entity.Schedule;

public class ScheduleView extends ViewGroup {

    private Context context;
    private ArrayList<Schedule> list;

    public ScheduleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        list = new ArrayList<>();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //根据List中存储的课程信息,依次添加TextView
        int childNumber = getChildCount();
        for (int i = 0; i < childNumber; i++) {
            View child = getChildAt(i);
            Schedule childSchedule = list.get(i);
            int position = childSchedule.getPosition();
            //position从0开始,计算第几行和第几列
            int line = position / 7;
            int vertical = position % 7;
            //每个课程最小单元格的宽度和高度
            int item_width = getMeasuredWidth() / 7;
            int item_height = getMeasuredHeight() / 10;
            //给子View计算位置坐标,分别是左上角和右下角的坐标
            int left = vertical * item_width;
            int top = line * item_height;
            int right = (vertical + 1) * item_width;
            int bottom = (line + childSchedule.getScheduleLength()) * item_height;
            child.layout(left + 5, top + 5, right - 5, bottom - 5);
        }
    }

    public void clearList() {
        list.clear();
        removeAllViews();
    }

    public void addToList(Schedule schedule) {
        //外部调用的用于添加课程块的方法
        list.add(schedule);
        addView(schedule);
    }

    public void addView(final Schedule schedule) {
        //添加视图
        TextView textView = new TextView(context);
        textView.setText(schedule.getScheduleName() + "\n@" + schedule.getScheduleLocation());
        //设置背景随机颜色
        textView.setBackgroundColor(Color.parseColor(schedule.getColorCode()));
        textView.setGravity(Gravity.CENTER);
        //设置文字颜色为白色
        textView.setTextColor(Color.parseColor("#ffffff"));
        //设置课程块背景透明度
        textView.setAlpha(0.60f);
        //设置课程块点击事件
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击后弹出课程详细信息
                openAlter(schedule.getScheduleName(), schedule.getScheduleType()
                        , schedule.getScheduleLocation(), schedule.getScheduleTeacher()
                        , schedule.getScheduleLesson(), "第" + schedule.getMinScheduleWeek()
                                + "周至第" + schedule.getMaxScheduleWeek() + "周");
            }
        });
        //调用父类的添加组件方法
        addView(textView);
    }

    private void openAlter(String scheduleName, String scheduleType, String scheduleLocation, String scheduleTeacher, String scheduleLesson, String scheduleWeek) {
        //课程详细信息Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(scheduleName);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        String message = "课程类型:" + scheduleType + "\n" +
                "上课节数:" + scheduleLesson +
                "\n" +
                "上课周数:" + scheduleWeek +
                "\n" +
                "任课教师:" + scheduleTeacher +
                "\n" +
                "上课地点:" + scheduleLocation;
        builder.setMessage(message);
        builder.show();
    }
}

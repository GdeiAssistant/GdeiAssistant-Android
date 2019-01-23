package edu.gdei.gdeiassistant.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import edu.gdei.gdeiassistant.Pojo.Entity.Schedule;

public class TodayScheduleListViewAdapter extends BaseAdapter {

    //父列表视图容器
    private LayoutInflater listContainer;

    //成绩数据集合
    private List<Schedule> todayScheduleList;

    //列表项布局
    private int itemViewResource;

    //列表项组件集合
    private static class ListItemView {
        TextView scheduleName;
        TextView scheduleLocation;
        TextView scheduleTime;
        LinearLayout itemLayout;
        ImageView dividerLine;
    }

    //根据父列表构造列表项适配器，加载列表项界面、读取并设置列表项数据
    public TodayScheduleListViewAdapter(Context context, List<Schedule> todayScheduleList, int itemViewResource) {
        this.listContainer = LayoutInflater.from(context);
        this.itemViewResource = itemViewResource;
        this.todayScheduleList = todayScheduleList;
    }

    @Override
    public int getCount() {
        //根据今日课表数据条目返回列表项数目
        return todayScheduleList.size();
    }

    @Override
    public Object getItem(int position) {
        return todayScheduleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // 创建自定义列表项组件
        ListItemView listItemView;
        if (convertView == null) {
            //获取列表项布局
            convertView = listContainer.inflate(this.itemViewResource, null);
            //初始化列表项各组件
            listItemView = new ListItemView();
            listItemView.itemLayout = convertView.findViewById(edu.gdei.gdeiassistant.R.id.todayScheduleItemLayout);
            listItemView.dividerLine = convertView.findViewById(edu.gdei.gdeiassistant.R.id.todayScheduleItemDividerline);
            listItemView.scheduleTime = convertView.findViewById(edu.gdei.gdeiassistant.R.id.todayScheduleItemScheduleTime);
            listItemView.scheduleName = convertView.findViewById(edu.gdei.gdeiassistant.R.id.todayScheduleItemScheduleName);
            listItemView.scheduleLocation = convertView.findViewById(edu.gdei.gdeiassistant.R.id.todayScheduleItemScheduleLocation);
            //设置控件集到convertView
            convertView.setTag(listItemView);
        } else {
            listItemView = (ListItemView) convertView.getTag();
        }
        //依次读取列表项数据中的第position项数据赋于news对象
        Schedule schedule = todayScheduleList.get(position);
        listItemView.scheduleName.setText(schedule.getScheduleName());
        listItemView.scheduleLocation.setText(schedule.getScheduleLocation());
        listItemView.scheduleTime.setText((schedule.getPosition() / 7 + 1) + "-" + (schedule.getPosition() / 7 + schedule.getScheduleLength()));
        if (position == 0) {
            //隐藏第一行的分割线
            listItemView.dividerLine.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }
}

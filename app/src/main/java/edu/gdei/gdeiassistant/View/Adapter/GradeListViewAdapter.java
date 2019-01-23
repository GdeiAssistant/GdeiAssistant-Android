package edu.gdei.gdeiassistant.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import edu.gdei.gdeiassistant.Pojo.Entity.Grade;

public class GradeListViewAdapter extends BaseAdapter {

    //父列表视图容器
    private LayoutInflater listContainer;

    //成绩数据集合
    private List<Grade> gradeDataList;

    //列表项布局
    private int itemViewResource;

    //列表项组件集合
    private static class ListItemView {
        TextView subject_name;
        TextView subject_score;
        RelativeLayout item_layout;
    }

    //根据父列表构造列表项适配器，加载列表项界面、读取并设置列表项数据
    public GradeListViewAdapter(Context context, List<Grade> gradeDataList, int itemViewResource) {
        this.listContainer = LayoutInflater.from(context);
        this.itemViewResource = itemViewResource;
        this.gradeDataList = gradeDataList;
    }

    @Override
    public int getCount() {
        //根据成绩数据条目返回列表项数目
        return gradeDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return gradeDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 创建自定义列表项组件
        ListItemView listItemView;
        if (convertView == null) {
            //获取列表项布局
            convertView = listContainer.inflate(this.itemViewResource, null);
            //初始化列表项各组件
            listItemView = new ListItemView();
            listItemView.item_layout = convertView.findViewById(edu.gdei.gdeiassistant.R.id.book_item_layout);
            listItemView.subject_name = convertView.findViewById(edu.gdei.gdeiassistant.R.id.book_item_bookname);
            listItemView.subject_score = convertView.findViewById(edu.gdei.gdeiassistant.R.id.grade_item_subjectscore);
            //设置控件集到convertView
            convertView.setTag(listItemView);
        } else {
            listItemView = (ListItemView) convertView.getTag();
        }
        //依次读取列表项数据中的第position项数据赋于news对象
        Grade grade = gradeDataList.get(position);
        listItemView.subject_name.setText(grade.getGradeName());
        listItemView.subject_score.setText(grade.getGradeScore());
        return convertView;
    }
}

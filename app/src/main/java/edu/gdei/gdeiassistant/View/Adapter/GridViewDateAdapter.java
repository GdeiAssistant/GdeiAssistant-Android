package edu.gdei.gdeiassistant.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class GridViewDateAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<String> list;

    public GridViewDateAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
        this.list.add("课程");
        this.list.add("周一");
        this.list.add("周二");
        this.list.add("周三");
        this.list.add("周四");
        this.list.add("周五");
        this.list.add("周六");
        this.list.add("周日");
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(edu.gdei.gdeiassistant.R.layout.item_gridview_date, null);
            viewHolder.textViewDate = convertView.findViewById(edu.gdei.gdeiassistant.R.id.textviewDate);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textViewDate.setText(list.get(position));
        return convertView;
    }

    private static class ViewHolder {
        TextView textViewDate;
    }
}


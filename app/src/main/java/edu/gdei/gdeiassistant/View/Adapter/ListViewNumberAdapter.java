package edu.gdei.gdeiassistant.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewNumberAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<String> list;

    public ListViewNumberAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            this.list.add(i + "");
        }
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
            convertView = LayoutInflater.from(context).inflate(edu.gdei.gdeiassistant.R.layout.item_listview_number, null);
            viewHolder.textViewNumber = convertView.findViewById(edu.gdei.gdeiassistant.R.id.textviewNumber);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textViewNumber.setText(list.get(position));
        return convertView;
    }

    public static class ViewHolder {
        TextView textViewNumber;
    }
}

package cn.gdeiassistant.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.gdeiassistant.Pojo.Entity.Book;
import cn.gdeiassistant.R;

public class BookListViewAdapter extends BaseAdapter {

    //父列表视图容器
    private LayoutInflater listContainer;

    //借阅图书数据集合
    private List<Book> bookDataList;

    //列表项布局
    private int itemViewResource;

    //列表项组件集合
    private static class ListItemView {
        TextView bookName;
        TextView borrowDate;
        TextView returnDate;
        LinearLayout itemLayout;
    }

    //根据父列表构造列表项适配器，加载列表项界面、读取并设置列表项数据
    public BookListViewAdapter(Context context, List<Book> bookDataList, int itemViewResource) {
        this.listContainer = LayoutInflater.from(context);
        this.itemViewResource = itemViewResource;
        this.bookDataList = bookDataList;
    }

    @Override
    public int getCount() {
        //根据成绩数据条目返回列表项数目
        return bookDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 创建自定义列表项组件
        BookListViewAdapter.ListItemView listItemView;
        if (convertView == null) {
            //获取列表项布局
            convertView = listContainer.inflate(this.itemViewResource, null);
            //初始化列表项各组件
            listItemView = new BookListViewAdapter.ListItemView();
            listItemView.itemLayout = convertView.findViewById(R.id.bookItemLayout);
            listItemView.bookName = convertView.findViewById(R.id.bookItemBookName);
            listItemView.borrowDate = convertView.findViewById(R.id.bookItemBorrowDate);
            listItemView.returnDate = convertView.findViewById(R.id.bookItemReturnDate);
            //设置控件集到convertView
            convertView.setTag(listItemView);
        } else {
            listItemView = (BookListViewAdapter.ListItemView) convertView.getTag();
        }
        //依次读取列表项数据中的第position项数据赋于news对象
        Book book = bookDataList.get(position);
        listItemView.bookName.setText(book.getName());
        listItemView.borrowDate.setText("借阅时间：" + book.getBorrowDate());
        listItemView.returnDate.setText("应还时间：" + book.getReturnDate());
        return convertView;
    }
}

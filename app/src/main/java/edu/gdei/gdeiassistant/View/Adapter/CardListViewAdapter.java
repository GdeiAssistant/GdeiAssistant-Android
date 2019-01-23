package edu.gdei.gdeiassistant.View.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import edu.gdei.gdeiassistant.Pojo.Entity.Card;

public class CardListViewAdapter extends BaseAdapter {

    //父列表视图容器
    private LayoutInflater listContainer;

    //成绩数据集合
    private List<Card> cardDataList;

    //列表项布局
    private int itemViewResource;

    //列表项组件集合
    private static class ListItemView {
        TextView card_item_tradename;
        TextView card_item_tradeprice;
        TextView card_item_tradetime;
        TextView card_item_tradeaccountbalance;
        RelativeLayout item_layout;
        ImageView divider_line;
    }

    //根据父列表构造列表项适配器，加载列表项界面、读取并设置列表项数据
    public CardListViewAdapter(Context context, List<Card> cardDataList, int itemViewResource) {
        //运行上下文
        this.listContainer = LayoutInflater.from(context);
        this.cardDataList = cardDataList;
        this.itemViewResource = itemViewResource;
    }

    @Override
    public int getCount() {
        //根据流水记录数据条目返回列表项数目
        return cardDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return cardDataList.get(position);
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
            listItemView.item_layout = convertView.findViewById(edu.gdei.gdeiassistant.R.id.cardItemLayout);
            listItemView.card_item_tradename = convertView.findViewById(edu.gdei.gdeiassistant.R.id.cardItemTradeName);
            listItemView.card_item_tradeprice = convertView.findViewById(edu.gdei.gdeiassistant.R.id.cardItemTradePrice);
            listItemView.card_item_tradetime = convertView.findViewById(edu.gdei.gdeiassistant.R.id.cardItemTradeTime);
            listItemView.card_item_tradeaccountbalance = convertView.findViewById(edu.gdei.gdeiassistant.R.id.cardItemTradeAccountBalance);
            listItemView.divider_line = convertView.findViewById(edu.gdei.gdeiassistant.R.id.cardItemDividerLine);
            //设置控件集到convertView
            convertView.setTag(listItemView);
        } else {
            listItemView = (ListItemView) convertView.getTag();
        }
        //依次读取列表项数据中的第position项数据赋于news对象
        Card card = cardDataList.get(position);
        listItemView.card_item_tradename.setText(card.getMerchantName() + "【" + card.getTradeName() + "】");
        listItemView.card_item_tradetime.setText(card.getTradeTime());
        listItemView.card_item_tradeaccountbalance.setText("余额：" + card.getAccountBalance() + "元");
        if (Double.valueOf(card.getTradePrice()) < 0) {
            //流水记录为支出,文字颜色为红色
            listItemView.card_item_tradeprice.setTextColor(Color.RED);
            listItemView.card_item_tradeprice.setText(card.getTradePrice() + "元");
        } else {
            //流水记录为收入,文字颜色为蓝色
            listItemView.card_item_tradeprice.setTextColor(Color.parseColor("#18b3ec"));
            listItemView.card_item_tradeprice.setText("+" + card.getTradePrice() + "元");
        }
        return convertView;
    }
}

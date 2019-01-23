package edu.gdei.gdeiassistant.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import edu.gdei.gdeiassistant.Pojo.Entity.Card;
import edu.gdei.gdeiassistant.Pojo.Entity.CardInfo;
import edu.gdei.gdeiassistant.Presenter.CardPresenter;
import edu.gdei.gdeiassistant.R;
import edu.gdei.gdeiassistant.View.Adapter.CardListViewAdapter;
import edu.gdei.gdeiassistant.View.ListView.ListViewForScrollView;

import java.util.List;

public class CardActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar cardQueryToolbar;
    private TextView cardQueryTitle;

    private TextView cardQueryFailTip;
    private ProgressBar cardQueryProgressbar;

    private TextView cardQueryBalance;
    private TextView cardQueryCurrentDate;

    private ScrollView cardQueryDataLayout;
    private ListViewForScrollView cardQueryListview;

    private CardListViewAdapter cardListViewAdapter;

    private CardPresenter cardPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        //初始化控件
        InitView();
        //配置加载Presenter
        cardPresenter = new CardPresenter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cardPresenter.RemoveCallBacksAndMessages();
    }

    /**
     * 初始化控件
     */
    private void InitView() {
        cardQueryToolbar = (Toolbar) findViewById(R.id.cardQueryToolbar);
        setSupportActionBar(cardQueryToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        cardQueryTitle = (TextView) findViewById(R.id.cardQueryTitle);

        cardQueryFailTip = (TextView) findViewById(R.id.cardQueryFailTip);
        cardQueryProgressbar = (ProgressBar) findViewById(R.id.cardQueryProgressbar);

        cardQueryBalance = (TextView) findViewById(R.id.cardQueryBalance);
        cardQueryCurrentDate = (TextView) findViewById(R.id.cardQueryCurrentDate);

        cardQueryDataLayout = (ScrollView) findViewById(R.id.cardQueryDataLayout);

        cardQueryListview = (ListViewForScrollView) findViewById(R.id.cardQueryListview);

        SetOnClickEvent();
    }

    private void SetOnClickEvent() {
        cardQueryFailTip.setOnClickListener(this);
        cardQueryTitle.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //返回上一层
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cardQueryTitle:
                //选择查询日期
                cardPresenter.SelectDate();
                break;

            case R.id.cardQueryFailTip:
                //重试查询
                cardPresenter.CardQuery();
                break;
        }
    }

    /**
     * 显示进度条
     */
    public void ShowProgressbar() {
        cardQueryProgressbar.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏进度条
     */
    public void HideProgressbar() {
        cardQueryProgressbar.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示数据界面
     */
    public void ShowDataLayout() {
        cardQueryDataLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏数据界面
     */
    public void HideDataLayout() {
        cardQueryDataLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示失败提示
     *
     * @param text
     */
    public void ShowFailTip(String text) {
        cardQueryFailTip.setText(text);
        cardQueryFailTip.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏失败提示
     */
    public void HideFailTip() {
        cardQueryFailTip.setVisibility(View.INVISIBLE);
        cardQueryFailTip.setText("");
    }

    /**
     * 设置当前查询日期
     *
     * @param year
     * @param month
     * @param date
     */
    public void SetCurrentDate(String year, String month, String date) {
        cardQueryCurrentDate.setText("当前查询日期：" + year + "-" + month + "-" + date);
    }

    /**
     * 加载消费流水记录信息
     *
     * @param cardList
     */
    public void LoadCardData(List<Card> cardList, CardInfo cardInfo) {
        cardQueryBalance.setText(cardInfo.getCardBalance() + "元");
        cardListViewAdapter = new CardListViewAdapter(this, cardList, R.layout.card_item);
        cardQueryListview.setAdapter(cardListViewAdapter);
    }

    /**
     * 更新日期标题
     *
     * @param year
     * @param month
     * @param date
     */
    public void UpdateDateTextView(int year, int month, int date) {
        cardQueryTitle.setText(year + "年" + month + "月" + date + "日");
    }
}

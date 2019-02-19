package edu.gdei.gdeiassistant.Fragment;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import edu.gdei.gdeiassistant.Activity.CardActivity;
import edu.gdei.gdeiassistant.Activity.MainActivity;
import edu.gdei.gdeiassistant.Application.GdeiAssistantApplication;
import edu.gdei.gdeiassistant.Constant.MainItemTagConstant;
import edu.gdei.gdeiassistant.Pojo.Entity.Access;
import edu.gdei.gdeiassistant.Pojo.Entity.CardInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.Schedule;
import edu.gdei.gdeiassistant.Presenter.IndexPresenter;
import edu.gdei.gdeiassistant.R;
import edu.gdei.gdeiassistant.View.Adapter.TodayScheduleListViewAdapter;
import edu.gdei.gdeiassistant.View.ListView.ListViewForScrollView;

public class FragmentIndex extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private AlertDialog alertDialog;

    private RelativeLayout fragmentIndexScheduleTitleLayout;

    private LinearLayout fragmentIndexScheduleLayout;

    private FrameLayout fragmentIndexScheduleContentLayout;

    private TextView fragmentIndexDate;

    private SwipeRefreshLayout fragmentIndexSwipeContainer;

    private ListViewForScrollView fragmentIndexScheduleListview;

    private ProgressBar fragmentIndexScheduleProgress;

    private TextView fragmentIndexScheduleFailedTip;

    private LinearLayout fragmentIndexCardLayout;

    private FrameLayout fragmentIndexCardContentLayout;

    private RelativeLayout fragmentIndexCardTitleLayout;

    private RelativeLayout fragmentIndexCardDataLayout;

    private TextView fragmentIndexCardBalanceValue;

    private TextView fragmentIndexCardInterimBalanceValue;

    private TextView fragmentIndexCardLostStateValue;

    private TextView fragmentIndexCardFreezeStateValue;

    private ProgressBar fragmentIndexCardProgress;

    private TextView fragmentIndexCardFailedTip;

    private IndexPresenter indexPresenter;

    public IndexPresenter getIndexPresenter() {
        return this.indexPresenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //获取根VIEW
        View rootView = inflater.inflate(R.layout.fragment_index, container, false);
        //初始化控件
        InitView(rootView);
        //配置加载Presenter
        indexPresenter = new IndexPresenter(this);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        indexPresenter.RemoveCallBacksAndMessages();
    }

    private void InitView(View rootView) {
        fragmentIndexScheduleTitleLayout = rootView.findViewById(R.id.fragmentIndexScheduleTitleLayout);
        fragmentIndexScheduleLayout = rootView.findViewById(R.id.fragmentIndexScheduleLayout);
        fragmentIndexScheduleContentLayout = rootView.findViewById(R.id.fragmentIndexScheduleContentLayout);
        fragmentIndexDate = rootView.findViewById(R.id.fragmentIndexDate);
        fragmentIndexSwipeContainer = rootView.findViewById(R.id.fragmentIndexSwipeContainer);
        fragmentIndexSwipeContainer.setOnRefreshListener(this);
        fragmentIndexSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light, android.R.color.holo_purple);
        fragmentIndexScheduleListview = rootView.findViewById(R.id.fragmentIndexScheduleListview);
        fragmentIndexScheduleProgress = rootView.findViewById(R.id.fragmentIndexScheduleProgress);
        fragmentIndexScheduleFailedTip = rootView.findViewById(R.id.fragmentIndexScheduleFailedTip);

        fragmentIndexCardLayout = rootView.findViewById(R.id.fragmentIndexCardLayout);
        fragmentIndexCardContentLayout = rootView.findViewById(R.id.fragmentIndexCardContentLayout);
        fragmentIndexCardTitleLayout = rootView.findViewById(R.id.fragmentIndexCardTitleLayout);
        fragmentIndexCardDataLayout = rootView.findViewById(R.id.fragmentIndexCardDataLayout);
        fragmentIndexCardBalanceValue = rootView.findViewById(R.id.fragmentIndexCardBalanceValue);
        fragmentIndexCardInterimBalanceValue = rootView.findViewById(R.id.fragmentIndexCardInterimBalanceValue);
        fragmentIndexCardLostStateValue = rootView.findViewById(R.id.fragmentIndexCardLostStateValue);
        fragmentIndexCardFreezeStateValue = rootView.findViewById(R.id.fragmentIndexCardFreezeStateValue);
        fragmentIndexCardProgress = rootView.findViewById(R.id.fragmentIndexCardProgress);
        fragmentIndexCardFailedTip = rootView.findViewById(R.id.fragmentIndexCardFailedTip);
        //显示今日日期
        fragmentIndexDate.setText(new SimpleDateFormat("MM月dd日 E").format(Calendar.getInstance().getTime()));
        //设置点击事件
        SetOnClickEvent();
    }

    private void SetOnClickEvent() {
        fragmentIndexScheduleTitleLayout.setOnClickListener(this);
        fragmentIndexCardTitleLayout.setOnClickListener(this);
        fragmentIndexScheduleFailedTip.setOnClickListener(this);
        fragmentIndexCardFailedTip.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragmentIndexScheduleTitleLayout:
                ((MainActivity) getActivity()).SwitchFragment(MainItemTagConstant.TAG_SCHEDULE);
                ((MainActivity) getActivity()).ChangeCurrentItem(MainItemTagConstant.TAG_SCHEDULE);
                break;

            case R.id.fragmentIndexCardTitleLayout:
                getActivity().startActivity(new Intent(getActivity(), CardActivity.class));
                ((MainActivity) getActivity()).ChangeCurrentItem(MainItemTagConstant.TAG_CARD);
                break;

            case R.id.fragmentIndexScheduleFailedTip:
                indexPresenter.TodayScheduleQuery();
                break;

            case R.id.fragmentIndexCardFailedTip:
                indexPresenter.CardInfoQuery();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //进入课表查询界面
        ((MainActivity) getActivity()).SwitchFragment(MainItemTagConstant.TAG_SCHEDULE);
    }

    /**
     * 显示今日课表模块的进度条
     */
    public void ShowScheduleProgressbar() {
        SetScheduleLayoutHeight(0);
        fragmentIndexScheduleProgress.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏今日课表模块的进度条
     */
    public void HideScheduleProgressbar() {
        SetScheduleLayoutHeight(0);
        fragmentIndexScheduleProgress.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示今日课表模块
     */
    public void ShowScheduleModule() {
        fragmentIndexScheduleLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 显示校园卡信息模块
     */
    public void ShowCardModule() {
        fragmentIndexCardLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 显示今日课表模块的失败提示
     *
     * @param text
     */
    public void ShowScheduleFailTip(String text) {
        SetScheduleLayoutHeight(0);
        fragmentIndexScheduleFailedTip.setText(text);
        fragmentIndexScheduleFailedTip.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏今日课表模块的失败提示
     */
    public void HideScheduleFailTip() {
        SetScheduleLayoutHeight(0);
        fragmentIndexScheduleFailedTip.setVisibility(View.INVISIBLE);
        fragmentIndexScheduleFailedTip.setText("");
    }

    /**
     * 设置今日课表模块的高度
     *
     * @param scheduleLength
     */
    public void SetScheduleLayoutHeight(int scheduleLength) {
        GdeiAssistantApplication application = (GdeiAssistantApplication) getActivity().getApplication();
        if (application.getAccess() != null && Boolean.TRUE.equals(application.getAccess().getSchedule())) {
            if (scheduleLength == 0) {
                ViewGroup.LayoutParams layoutParams = fragmentIndexScheduleLayout.getLayoutParams();
                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 260, getResources().getDisplayMetrics());
                fragmentIndexScheduleLayout.setLayoutParams(layoutParams);
                layoutParams = fragmentIndexScheduleContentLayout.getLayoutParams();
                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
                fragmentIndexScheduleContentLayout.setLayoutParams(layoutParams);
            } else {
                ListAdapter listAdapter = fragmentIndexScheduleListview.getAdapter();
                if (listAdapter == null) {
                    ViewGroup.LayoutParams layoutParams = fragmentIndexScheduleLayout.getLayoutParams();
                    layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 260, getResources().getDisplayMetrics());
                    fragmentIndexScheduleLayout.setLayoutParams(layoutParams);
                    layoutParams = fragmentIndexScheduleContentLayout.getLayoutParams();
                    layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
                    fragmentIndexScheduleContentLayout.setLayoutParams(layoutParams);
                } else {
                    View listItem = listAdapter.getView(0, null, fragmentIndexScheduleListview);
                    listItem.measure(0, 0);
                    int listViewHeight = listItem.getMeasuredHeight();
                    ViewGroup.LayoutParams layoutParams = fragmentIndexScheduleLayout.getLayoutParams();
                    layoutParams.height = listViewHeight * (scheduleLength + 1);
                    fragmentIndexScheduleLayout.setLayoutParams(layoutParams);
                    ViewGroup.LayoutParams contentLayoutParams = fragmentIndexScheduleContentLayout.getLayoutParams();
                    contentLayoutParams.height = listViewHeight * (scheduleLength);
                    fragmentIndexScheduleContentLayout.setLayoutParams(contentLayoutParams);
                }
            }
        } else {
            ViewGroup.LayoutParams layoutParams = fragmentIndexScheduleLayout.getLayoutParams();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
            fragmentIndexScheduleLayout.setLayoutParams(layoutParams);
        }
    }

    /**
     * 设置校园卡信息模块的高度
     */
    public void SetCardLayoutHeight() {
        GdeiAssistantApplication application = (GdeiAssistantApplication) getActivity().getApplication();
        if (application.getAccess() != null && Boolean.TRUE.equals(application.getAccess().getSchedule())) {
            ViewGroup.LayoutParams layoutParams = fragmentIndexCardLayout.getLayoutParams();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 190, getResources().getDisplayMetrics());
            fragmentIndexCardLayout.setLayoutParams(layoutParams);
            layoutParams = fragmentIndexCardContentLayout.getLayoutParams();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 130, getResources().getDisplayMetrics());
            fragmentIndexCardContentLayout.setLayoutParams(layoutParams);
        } else {
            ViewGroup.LayoutParams layoutParams = fragmentIndexCardLayout.getLayoutParams();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
            fragmentIndexCardLayout.setLayoutParams(layoutParams);
        }
    }

    /**
     * 加载今日课表信息
     *
     * @param todayScheduleList
     */
    public void LoadTodaySchedule(final List<Schedule> todayScheduleList) {
        //判断今日有无课程
        if (todayScheduleList.size() > 0) {
            //配置ListView
            TodayScheduleListViewAdapter todayScheduleListViewAdapter = new TodayScheduleListViewAdapter(getActivity(), todayScheduleList, R.layout.todayschedule_item);
            fragmentIndexScheduleListview.setAdapter(todayScheduleListViewAdapter);
            fragmentIndexScheduleListview.setVisibility(View.VISIBLE);
            fragmentIndexScheduleListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    //课程详细信息Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(todayScheduleList.get(position).getScheduleName());
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    String message = "课程类型:" + todayScheduleList.get(position).getScheduleType() + "\n" +
                            "上课节数:" + todayScheduleList.get(position).getScheduleLesson() +
                            "\n" +
                            "上课周数:" + "第" + todayScheduleList.get(position).getMinScheduleWeek() + "周至第" + todayScheduleList.get(position).getMaxScheduleWeek() + "周" +
                            "\n" +
                            "任课教师:" + todayScheduleList.get(position).getScheduleTeacher() +
                            "\n" +
                            "上课地点:" + todayScheduleList.get(position).getScheduleLocation();
                    builder.setMessage(message);
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                    }
                    alertDialog = builder.create();
                    alertDialog.show();
                }
            });
            HideScheduleProgressbar();
            HideScheduleFailTip();
            SetScheduleLayoutHeight(todayScheduleList.size());
        } else {
            //今日无课程
            fragmentIndexScheduleFailedTip.setText("今天没有课程");
            fragmentIndexScheduleFailedTip.setVisibility(View.VISIBLE);
            fragmentIndexScheduleFailedTip.setEnabled(false);
        }
    }

    /**
     * 显示饭卡基本信息模块的进度条
     */
    public void ShowCardProgressbar() {
        SetCardLayoutHeight();
        fragmentIndexCardProgress.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏饭卡基本信息模块的进度条
     */
    public void HideCardProgressbar() {
        SetCardLayoutHeight();
        fragmentIndexCardProgress.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示饭卡基本信息模块的失败提示
     *
     * @param text
     */
    public void ShowCardFailTip(String text) {
        SetCardLayoutHeight();
        fragmentIndexCardFailedTip.setText(text);
        fragmentIndexCardFailedTip.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏饭卡基本信息模块的失败提示
     */
    public void HideCardFailTip() {
        SetCardLayoutHeight();
        fragmentIndexCardFailedTip.setVisibility(View.INVISIBLE);
        fragmentIndexCardFailedTip.setText("");
    }

    /**
     * 加载饭卡基本信息
     *
     * @param cardInfo
     */
    public void LoadCardInfo(CardInfo cardInfo) {
        SetCardLayoutHeight();
        fragmentIndexCardBalanceValue.setText(cardInfo.getCardBalance() + "元");
        fragmentIndexCardInterimBalanceValue.setText(cardInfo.getCardInterimBalance() + "元");
        fragmentIndexCardLostStateValue.setText(cardInfo.getCardLostState());
        fragmentIndexCardFreezeStateValue.setText(cardInfo.getCardFreezeState());
        HideCardFailTip();
        HideCardProgressbar();
        fragmentIndexCardDataLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏刷新提示
     */
    public void HideRefreshingTip() {
        fragmentIndexSwipeContainer.setRefreshing(false);
    }

    /**
     * 隐藏校园卡基本信息布局
     */
    public void HideCardDataLayout() {
        fragmentIndexCardDataLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * 隐藏今日课表的ListView
     */
    public void HideScheduleDataListView() {
        fragmentIndexScheduleListview.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRefresh() {
        if (fragmentIndexSwipeContainer.isRefreshing()) {
            GdeiAssistantApplication application = (GdeiAssistantApplication) getActivity().getApplication();
            Access access = application.getAccess();
            if (access == null) {
                ((MainActivity) getActivity()).getMainPresenter().GetUserAccess();
            } else {
                if (Boolean.TRUE.equals(access.getSchedule())) {
                    indexPresenter.TodayScheduleQuery();
                }
                if (Boolean.TRUE.equals(access.getCard())) {
                    indexPresenter.CardInfoQuery();
                }
            }
        }
    }
}

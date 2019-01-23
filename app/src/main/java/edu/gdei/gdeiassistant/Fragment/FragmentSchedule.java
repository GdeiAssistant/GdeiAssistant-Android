package edu.gdei.gdeiassistant.Fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import edu.gdei.gdeiassistant.Pojo.Entity.Schedule;
import edu.gdei.gdeiassistant.Presenter.SchedulePresenter;
import edu.gdei.gdeiassistant.R;
import edu.gdei.gdeiassistant.View.Adapter.GridViewDateAdapter;
import edu.gdei.gdeiassistant.View.Adapter.ListViewNumberAdapter;
import edu.gdei.gdeiassistant.View.ListView.ListViewForScrollView;
import edu.gdei.gdeiassistant.View.ViewGroup.ScheduleView;

public class FragmentSchedule extends Fragment implements View.OnClickListener {

    private String[] weekList = {"第1周", "第2周", "第3周", "第4周", "第5周"
            , "第6周", "第7周", "第8周", "第9周", "第10周", "第11周", "第12周"
            , "第13周", "第14周", "第15周", "第16周", "第17周", "第18周", "第19周", "第20周"};

    private AlertDialog alertDialog;

    private ProgressBar scheduleQueryProgressbar;

    private TextView scheduleQueryFailedTip;

    private LinearLayout scheduleQueryDataLayout;

    private GridView scheduleQueryGridViewDate;

    private ListViewForScrollView scheduleQueryListViewNumber;

    private ScheduleView scheduleQueryScheduleView;

    private SchedulePresenter schedulePresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //获取根VIEW
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);
        //使用OptionsMenu
        setHasOptionsMenu(true);
        //初始化控件
        InitView(rootView);
        //配置加载Presenter
        schedulePresenter = new SchedulePresenter(this);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        schedulePresenter.RemoveCallBacksAndMessages();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void InitView(View rootView) {
        scheduleQueryProgressbar = rootView.findViewById(R.id.scheduleQueryProgressbar);
        scheduleQueryFailedTip = rootView.findViewById(R.id.scheduleQueryFailedTip);
        scheduleQueryDataLayout = rootView.findViewById(R.id.scheduleQueryDataLayout);
        scheduleQueryGridViewDate = rootView.findViewById(R.id.scheduleQueryGridViewDate);
        scheduleQueryGridViewDate.setAdapter(new GridViewDateAdapter(getActivity()));
        scheduleQueryListViewNumber = rootView.findViewById(R.id.scheduleQueryListViewNumber);
        scheduleQueryScheduleView = rootView.findViewById(R.id.scheduleQueryScheduleView);
        scheduleQueryListViewNumber = rootView.findViewById(R.id.scheduleQueryListViewNumber);
        scheduleQueryListViewNumber.setAdapter(new ListViewNumberAdapter(getActivity()));
        SetOnClickEvent();
    }

    private void SetOnClickEvent() {
        scheduleQueryFailedTip.setOnClickListener(this);
    }

    /**
     * 显示进度条
     */
    public void ShowProgressbar() {
        scheduleQueryProgressbar.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏进度条
     */
    public void HideProgressbar() {
        scheduleQueryProgressbar.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示失败提示
     */
    public void ShowFailTip(String text) {
        scheduleQueryFailedTip.setText(text);
        scheduleQueryFailedTip.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏失败提示
     */
    public void HideFailTip() {
        scheduleQueryFailedTip.setVisibility(View.INVISIBLE);
        scheduleQueryFailedTip.setText("");
    }

    /**
     * 加载课表信息
     */
    public void LoadScheduleData(List<Schedule> scheduleList) {
        scheduleQueryScheduleView.clearList();
        for (Schedule schedule : scheduleList) {
            scheduleQueryScheduleView.addToList(schedule);
        }
        scheduleQueryDataLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_change_current_week:
                //修改当前周数
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(weekList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        schedulePresenter.ChangeSelectWeek(which + 1);
                    }
                });
                builder.setTitle("请选择需要查询的周数");
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                alertDialog = builder.create();
                alertDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scheduleQueryFailedTip:
                schedulePresenter.ScheduleQuery();
                break;
        }
    }
}

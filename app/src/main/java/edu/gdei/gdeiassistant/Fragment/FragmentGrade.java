package edu.gdei.gdeiassistant.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import edu.gdei.gdeiassistant.Activity.GradeDetailActivity;
import edu.gdei.gdeiassistant.Pojo.Entity.Grade;
import edu.gdei.gdeiassistant.Presenter.GradePresenter;
import edu.gdei.gdeiassistant.R;
import edu.gdei.gdeiassistant.View.Adapter.GradeListViewAdapter;
import edu.gdei.gdeiassistant.View.ListView.ListViewForScrollView;

public class FragmentGrade extends Fragment implements View.OnClickListener {

    private TextView gradeQueryOne;

    private TextView gradeQueryTwo;

    private TextView gradeQueryThree;

    private TextView gradeQueryFour;

    private TextView gradeQueryFailedTip;

    private ProgressBar gradeQueryProgressbar;

    private ScrollView gradeQueryDataLayout;

    private TextView firstTermGradeQueryGPAValue;

    private ListViewForScrollView firstTermGradeQueryListview;

    private TextView secondTermGradeQueryGPAValue;

    private ListViewForScrollView secondTermGradeQueryListview;

    private GradeListViewAdapter firstTermGradeListViewAdapter;

    private GradeListViewAdapter secondTermGradeListViewAdapter;

    private GradePresenter gradePresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //获取根VIEW
        View rootView = inflater.inflate(R.layout.fragment_grade, container, false);
        //初始化控件
        InitView(rootView);
        //配置加载Presenter
        gradePresenter = new GradePresenter(this);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gradePresenter.RemoveCallBacksAndMessages();
    }

    /**
     * 初始化控件
     *
     * @param rootView
     */
    private void InitView(View rootView) {
        gradeQueryOne = rootView.findViewById(R.id.gradeQueryOne);
        gradeQueryTwo = rootView.findViewById(R.id.gradeQueryTwo);
        gradeQueryThree = rootView.findViewById(R.id.gradeQueryThree);
        gradeQueryFour = rootView.findViewById(R.id.gradeQueryFour);

        gradeQueryFailedTip = rootView.findViewById(R.id.gradeQueryFailedTip);
        gradeQueryProgressbar = rootView.findViewById(R.id.gradeQueryProgressbar);

        gradeQueryDataLayout = rootView.findViewById(R.id.gradeQueryDataLayout);
        firstTermGradeQueryGPAValue = rootView.findViewById(R.id.firstTermGradeQueryGPAValue);
        firstTermGradeQueryListview = rootView.findViewById(R.id.firstTermGradeQueryListview);
        secondTermGradeQueryGPAValue = rootView.findViewById(R.id.secondTermGradeQueryGPAValue);
        secondTermGradeQueryListview = rootView.findViewById(R.id.secondTermGradeQueryListview);

        SetOnClickEvent();
    }

    /**
     * 设置点击事件
     */
    public void SetOnClickEvent() {
        gradeQueryOne.setOnClickListener(this);
        gradeQueryTwo.setOnClickListener(this);
        gradeQueryThree.setOnClickListener(this);
        gradeQueryFour.setOnClickListener(this);
        gradeQueryFailedTip.setOnClickListener(this);
    }

    /**
     * 显示进度条
     */
    public void ShowProgressbar() {
        gradeQueryProgressbar.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏进度条
     */
    public void HideProgressbar() {
        gradeQueryProgressbar.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示失败提示
     *
     * @param text
     */
    public void ShowFailTip(String text) {
        gradeQueryFailedTip.setText(text);
        gradeQueryFailedTip.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏失败提示
     */
    public void HideFailTip() {
        gradeQueryFailedTip.setVisibility(View.INVISIBLE);
        gradeQueryFailedTip.setText("");
    }

    /**
     * 禁用标题按钮
     */
    public void DisableTitle() {
        gradeQueryOne.setEnabled(false);
        gradeQueryTwo.setEnabled(false);
        gradeQueryThree.setEnabled(false);
        gradeQueryFour.setEnabled(false);
    }

    /**
     * 启用标题按钮
     */
    public void EnableTitile() {
        gradeQueryOne.setEnabled(true);
        gradeQueryTwo.setEnabled(true);
        gradeQueryThree.setEnabled(true);
        gradeQueryFour.setEnabled(true);
    }

    /**
     * 显示成绩信息
     */
    public void ShowDataLayout() {
        gradeQueryDataLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏成绩信息
     */
    public void HideDataLayout() {
        gradeQueryDataLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * 改变学年选中标题颜色
     */
    public void ChangeSelectedTitleColor(int year) {
        int colorCode = Color.parseColor("#ff00ddff");
        switch (year) {
            case 0:
                gradeQueryOne.setTextColor(colorCode);
                gradeQueryTwo.setTextColor(Color.parseColor("#000000"));
                gradeQueryThree.setTextColor(Color.parseColor("#000000"));
                gradeQueryFour.setTextColor(Color.parseColor("#000000"));
                break;

            case 1:
                gradeQueryOne.setTextColor(Color.parseColor("#000000"));
                gradeQueryTwo.setTextColor(colorCode);
                gradeQueryThree.setTextColor(Color.parseColor("#000000"));
                gradeQueryFour.setTextColor(Color.parseColor("#000000"));
                break;

            case 2:
                gradeQueryOne.setTextColor(Color.parseColor("#000000"));
                gradeQueryTwo.setTextColor(Color.parseColor("#000000"));
                gradeQueryThree.setTextColor(colorCode);
                gradeQueryFour.setTextColor(Color.parseColor("#000000"));
                break;

            case 3:
                gradeQueryOne.setTextColor(Color.parseColor("#000000"));
                gradeQueryTwo.setTextColor(Color.parseColor("#000000"));
                gradeQueryThree.setTextColor(Color.parseColor("#000000"));
                gradeQueryFour.setTextColor(colorCode);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gradeQueryOne:
                //查询大一成绩
                gradePresenter.GradeQuery(0);
                break;

            case R.id.gradeQueryTwo:
                //查询大二成绩
                gradePresenter.GradeQuery(1);
                break;

            case R.id.gradeQueryThree:
                //查询大三成绩
                gradePresenter.GradeQuery(2);
                break;

            case R.id.gradeQueryFour:
                //查询大四成绩
                gradePresenter.GradeQuery(3);
                break;

            case R.id.gradeQueryFailedTip:
                //重试查询
                gradePresenter.GradeQuery(null);
                break;
        }
    }

    /**
     * 加载成绩信息
     *
     * @param firstTermGPA
     * @param secondTermGPA
     * @param firstTermGradeList
     * @param secondTermGradeList
     */
    public void LoadGradeData(String firstTermGPA, String secondTermGPA, final List<Grade> firstTermGradeList, final List<Grade> secondTermGradeList) {
        firstTermGradeQueryGPAValue.setText(firstTermGPA);
        secondTermGradeQueryGPAValue.setText(secondTermGPA);
        firstTermGradeListViewAdapter = new GradeListViewAdapter(getActivity(), firstTermGradeList, R.layout.grade_item);
        firstTermGradeQueryListview.setAdapter(firstTermGradeListViewAdapter);
        secondTermGradeListViewAdapter = new GradeListViewAdapter(getActivity(), secondTermGradeList, R.layout.grade_item);
        secondTermGradeQueryListview.setAdapter(secondTermGradeListViewAdapter);
        firstTermGradeQueryListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getActivity(), GradeDetailActivity.class);
                intent.putExtra("SubjectName", firstTermGradeList.get(position).getGradeName());
                intent.putExtra("SubjectScore", firstTermGradeList.get(position).getGradeScore());
                intent.putExtra("SubjectCredit", firstTermGradeList.get(position).getGradeCredit());
                intent.putExtra("SubjectGPA", firstTermGradeList.get(position).getGradeGpa());
                intent.putExtra("SubjectId", firstTermGradeList.get(position).getGradeId());
                intent.putExtra("SubjectYear", firstTermGradeList.get(position).getGradeYear());
                intent.putExtra("SubjectType", firstTermGradeList.get(position).getGradeType());
                intent.putExtra("SubjectTerm", firstTermGradeList.get(position).getGradeTerm());
                startActivity(intent);
            }
        });
        secondTermGradeQueryListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getActivity(), GradeDetailActivity.class);
                intent.putExtra("SubjectName", secondTermGradeList.get(position).getGradeName());
                intent.putExtra("SubjectScore", secondTermGradeList.get(position).getGradeScore());
                intent.putExtra("SubjectCredit", secondTermGradeList.get(position).getGradeCredit());
                intent.putExtra("SubjectGPA", secondTermGradeList.get(position).getGradeGpa());
                intent.putExtra("SubjectId", secondTermGradeList.get(position).getGradeId());
                intent.putExtra("SubjectYear", secondTermGradeList.get(position).getGradeYear());
                intent.putExtra("SubjectType", secondTermGradeList.get(position).getGradeType());
                intent.putExtra("SubjectTerm", secondTermGradeList.get(position).getGradeTerm());
                startActivity(intent);
            }
        });
    }

}

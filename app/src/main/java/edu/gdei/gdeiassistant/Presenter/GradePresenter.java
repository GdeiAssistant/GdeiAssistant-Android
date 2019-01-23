package edu.gdei.gdeiassistant.Presenter;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Fragment.FragmentGrade;
import edu.gdei.gdeiassistant.Model.GradeQueryModel;
import edu.gdei.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;

public class GradePresenter {

    private FragmentGrade fragmentGrade;

    private FragmentGradeHandler fragmentGradeHandler;

    private GradeQueryModel gradeQueryModel;

    private Integer currentYear;

    public GradePresenter(FragmentGrade fragmentGrade) {
        this.fragmentGrade = fragmentGrade;
        this.gradeQueryModel = new GradeQueryModel();
        this.fragmentGradeHandler = new FragmentGradeHandler(fragmentGrade);
        Init();
    }

    public static class FragmentGradeHandler extends Handler {

        private FragmentGrade fragmentGrade;

        FragmentGradeHandler(FragmentGrade fragmentGrade) {
            this.fragmentGrade = new WeakReference<>(fragmentGrade).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.SHOW_PROGRESS:
                    //显示进度条
                    fragmentGrade.ShowProgressbar();
                    fragmentGrade.HideDataLayout();
                    fragmentGrade.HideFailTip();
                    break;

                case RequestConstant.REQUEST_SUCCESS:
                    //查询成绩成功
                    GradeQueryResult result = (GradeQueryResult) msg.getData().getSerializable("GradeData");
                    //加载成绩数据
                    fragmentGrade.LoadGradeData(result.getFirstTermGPA(), result.getSecondTermGPA()
                            , result.getFirstTermGradeList(), result.getSecondTermGradeList());
                    //修改当前选中的学年
                    fragmentGrade.ChangeSelectedTitleColor(result.getYear());
                    fragmentGrade.HideFailTip();
                    fragmentGrade.EnableTitile();
                    fragmentGrade.ShowDataLayout();
                    fragmentGrade.HideProgressbar();
                    break;

                case RequestConstant.REQUEST_FAILURE:
                    //查询成绩失败
                    fragmentGrade.ShowFailTip(msg.getData().getString("Message"));
                    fragmentGrade.EnableTitile();
                    fragmentGrade.HideProgressbar();
                    fragmentGrade.HideDataLayout();
                    break;

                case RequestConstant.SERVER_ERROR:
                    //服务暂不可用
                    fragmentGrade.ShowFailTip("服务暂不可用，点击重试");
                    fragmentGrade.EnableTitile();
                    fragmentGrade.HideProgressbar();
                    fragmentGrade.HideDataLayout();
                    break;

                case RequestConstant.REQUEST_TIMEOUT:
                    //网络连接超时
                    fragmentGrade.ShowFailTip("网络连接超时，点击重试");
                    fragmentGrade.EnableTitile();
                    fragmentGrade.HideProgressbar();
                    fragmentGrade.HideDataLayout();
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    //出现未知异常
                    fragmentGrade.ShowFailTip("出现未知异常，点击重试");
                    fragmentGrade.EnableTitile();
                    fragmentGrade.HideProgressbar();
                    fragmentGrade.HideDataLayout();
                    break;
            }
        }
    }

    /**
     * 移除所有的回调和消息，防止内存泄露
     */
    public void RemoveCallBacksAndMessages() {
        fragmentGradeHandler.removeCallbacksAndMessages(null);
    }

    private void Init() {
        //进行默认学年成绩查询
        GradeQuery(null);
    }

    /**
     * 查询特定学年的成绩
     *
     * @param year
     */
    public void GradeQuery(@Nullable Integer year) {
        if (year == null) {
            if (currentYear == null) {
                //查询默认学年
                gradeQueryModel.GradeQuery(fragmentGradeHandler, null, fragmentGrade.getActivity().getApplicationContext());
            } else {
                //重新查询当前选中的学年
                gradeQueryModel.GradeQuery(fragmentGradeHandler, currentYear, fragmentGrade.getActivity().getApplicationContext());
            }
        } else {
            if (!year.equals(currentYear)) {
                currentYear = year;
                fragmentGrade.ChangeSelectedTitleColor(year);
                fragmentGrade.DisableTitle();
                gradeQueryModel.GradeQuery(fragmentGradeHandler, year, fragmentGrade.getActivity().getApplicationContext());
            }
        }

    }
}

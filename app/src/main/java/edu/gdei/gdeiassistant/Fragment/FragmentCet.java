package edu.gdei.gdeiassistant.Fragment;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import edu.gdei.gdeiassistant.Pojo.Entity.Cet;
import edu.gdei.gdeiassistant.Presenter.CetPresenter;
import edu.gdei.gdeiassistant.R;

public class FragmentCet extends Fragment implements View.OnClickListener {

    private ProgressBar cetQueryProgressbar;

    private LinearLayout cetQueryInputLayout;

    private EditText cetQueryNumber;

    private EditText cetQueryName;

    private EditText cetQueryCheckCode;

    private ImageView cetQueryCheckCodeImage;

    private Button cetQuerySubmit;

    private ScrollView cetQueryDataLayout;

    private TextView cetQueryDataName;

    private TextView cetQueryDataSchool;

    private TextView cetqueryDataType;

    private TextView cetQueryDataAdmissionCard;

    private TextView cetQueryTotalScoreValue;

    private TextView cetQueryListeningScore;

    private TextView cetQueryReadingScore;

    private TextView cetQueryWritingAndTranslatingScore;

    private Button cetQueryReset;

    private CetPresenter cetPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //获取根VIEW
        View rootView = inflater.inflate(R.layout.fragment_cet, container, false);
        //初始化控件
        InitView(rootView);
        //配置加载Presenter
        cetPresenter = new CetPresenter(this);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cetPresenter.RemoveCallBacksAndMessages();
    }

    private void InitView(View rootView) {
        cetQueryProgressbar = rootView.findViewById(R.id.cetQueryProgressbar);
        cetQueryInputLayout = rootView.findViewById(R.id.cetQueryInputLayout);
        cetQueryNumber = rootView.findViewById(R.id.cetQueryNumber);
        cetQueryName = rootView.findViewById(R.id.cetQueryName);
        cetQueryCheckCode = rootView.findViewById(R.id.cetQueryCheckCode);
        cetQueryCheckCodeImage = rootView.findViewById(R.id.cetQueryCheckCodeImage);
        cetQuerySubmit = rootView.findViewById(R.id.cetQuerySubmit);
        cetQueryDataLayout = rootView.findViewById(R.id.cetQueryDataLayout);
        cetQueryDataName = rootView.findViewById(R.id.cetQueryDataName);
        cetQueryDataSchool = rootView.findViewById(R.id.cetQueryDataSchool);
        cetqueryDataType = rootView.findViewById(R.id.cetqueryDataType);
        cetQueryDataAdmissionCard = rootView.findViewById(R.id.cetQueryDataAdmissionCard);
        cetQueryTotalScoreValue = rootView.findViewById(R.id.cetQueryTotalScoreValue);
        cetQueryListeningScore = rootView.findViewById(R.id.cetQueryListeningScore);
        cetQueryReadingScore = rootView.findViewById(R.id.cetQueryReadingScore);
        cetQueryWritingAndTranslatingScore = rootView.findViewById(R.id.cetQueryWritingAndTranslatingScore);
        cetQueryReset = rootView.findViewById(R.id.cetQueryReset);

        SetOnClickEvent();
    }

    private void SetOnClickEvent() {
        cetQueryCheckCodeImage.setOnClickListener(this);
        cetQuerySubmit.setOnClickListener(this);
        cetQueryReset.setOnClickListener(this);
    }

    /**
     * 显示Toast消息
     *
     * @param text
     */
    public void ShowToastTip(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示进度条
     */
    public void ShowProgressbar() {
        cetQueryProgressbar.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏进度条
     */
    public void HideProgressbar() {
        cetQueryProgressbar.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示数据界面
     */
    public void ShowDataLayout() {
        cetQueryDataLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏数据界面
     */
    public void HideDataLayout() {
        cetQueryDataLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示输入界面
     */
    public void ShowInputLayout() {
        cetQueryInputLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏输入界面
     */
    public void HideInputLayout() {
        cetQueryInputLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示验证码图片
     *
     * @param drawable
     */
    public void ShowCheckCode(Drawable drawable) {
        cetQueryCheckCodeImage.setImageDrawable(drawable);
    }

    /**
     * 显示验证码加载失败的图片
     */
    public void ShowCheckCodeNotAvailableImage() {
        cetQueryCheckCodeImage.setImageResource(R.drawable.checkcode_not_available);
    }

    /**
     * 重置验证码输入
     */
    public void ResetCheckCode() {
        cetQueryCheckCode.setText("");
    }

    /**
     * 加载四六级成绩信息
     *
     * @param cet
     */
    public void LoadCetData(Cet cet) {
        cetQueryDataName.setText("姓名：" + cet.getName());
        cetQueryDataSchool.setText("学校：" + cet.getSchool());
        cetqueryDataType.setText("考试类型：" + cet.getType());
        cetQueryDataAdmissionCard.setText("准考证号：" + cet.getAdmissionCard());
        cetQueryTotalScoreValue.setText(cet.getTotalScore());
        cetQueryListeningScore.setText("听力：" + cet.getListeningScore());
        cetQueryReadingScore.setText("阅读：" + cet.getReadingScore());
        cetQueryWritingAndTranslatingScore.setText("写作与翻译：" + cet.getWritingAndTranslatingScore());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cetQueryCheckCodeImage:
                cetPresenter.CetCheckCode();
                break;

            case R.id.cetQuerySubmit:
                cetPresenter.CetQuery(cetQueryNumber.getText().toString(), cetQueryName.getText().toString()
                        , cetQueryCheckCode.getText().toString());
                break;

            case R.id.cetQueryReset:
                HideDataLayout();
                cetQueryName.setText("");
                cetQueryNumber.setText("");
                ShowInputLayout();
                break;
        }
    }
}

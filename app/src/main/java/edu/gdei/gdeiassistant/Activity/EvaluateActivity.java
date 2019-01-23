package edu.gdei.gdeiassistant.Activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import edu.gdei.gdeiassistant.Presenter.EvaluatePresenter;
import edu.gdei.gdeiassistant.R;
import edu.gdei.gdeiassistant.View.Dialog.ProgressDialogCreator;

public class EvaluateActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar evaluateToolbar;

    private SwitchCompat switchCompat;

    private Button evaluateSubmit;

    private Dialog dialog;

    private EvaluatePresenter evaluatePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate);
        //初始化控件
        InitView();
        //配置加载Presenter
        evaluatePresenter = new EvaluatePresenter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
        }
        evaluatePresenter.RemoveCallBacksAndMessages();
    }

    private void InitView() {
        evaluateToolbar = findViewById(R.id.evaluateToolbar);
        setSupportActionBar(evaluateToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        switchCompat = findViewById(R.id.evaluateSwitchCompat);
        evaluateSubmit = findViewById(R.id.evaluateSubmit);
        evaluateSubmit.setOnClickListener(this);
    }

    /**
     * 显示Toast消息
     *
     * @param text
     */
    public void ShowToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示进度条
     */
    public void ShowProgressDialog() {
        if (dialog == null) {
            dialog = new ProgressDialogCreator().GetProgressDialogCreator(this);
            dialog.show();
        } else {
            dialog.show();
        }
    }

    /**
     * 隐藏进度条
     */
    public void HideProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
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
            case R.id.evaluateSubmit:
                //提交一键评教请求
                evaluatePresenter.SubmitEvaluate(switchCompat.isChecked());
                break;
        }
    }
}

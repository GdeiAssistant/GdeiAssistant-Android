package edu.gdei.gdeiassistant.Activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.gdei.gdeiassistant.Presenter.LostPresenter;
import edu.gdei.gdeiassistant.R;
import edu.gdei.gdeiassistant.View.Dialog.ProgressDialogCreator;

public class LostActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar lostToolbar;

    private EditText lostPassword;

    private Button lostButton;

    private Dialog dialog;

    private LostPresenter lostPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost);
        //初始化控件
        InitView();
        //配置加载Presenter
        lostPresenter = new LostPresenter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
        }
        lostPresenter.RemoveCallBacksAndMessages();
    }

    private void InitView() {
        lostToolbar = findViewById(R.id.lostToolbar);
        setSupportActionBar(lostToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        lostPassword = findViewById(R.id.lostPassword);
        lostButton = findViewById(R.id.lostButton);
        lostButton.setOnClickListener(this);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lostButton:
                lostPresenter.CardLost(lostPassword.getText().toString());
                break;
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
}

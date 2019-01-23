package edu.gdei.gdeiassistant.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import edu.gdei.gdeiassistant.Pojo.Entity.Cookie;
import edu.gdei.gdeiassistant.Presenter.ChargePresenter;
import edu.gdei.gdeiassistant.R;

public class ChargeActivity extends AppCompatActivity implements View.OnClickListener {

    private AlertDialog alertDialog;

    private Toolbar chargeToolbar;

    private WebView chargeWebView;

    private TextView chargeFailTip;

    private ProgressBar chargeProgressbar;

    private LinearLayout chargeInputLayout;

    private TextView chargeName;

    private TextView chargeNumber;

    private TextView chargeBalance;

    private EditText chargeAmount;

    private Button chargeSubmit;

    private TextView chargeAgreement;

    private ChargePresenter chargePresenter;

    private boolean canCancelWithoutAsking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge);
        //初始化控件
        InitView();
        //加载配置Presenter
        chargePresenter = new ChargePresenter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        if (chargeWebView != null) {
            chargeWebView.removeAllViews();
            chargeWebView.destroy();
        }
        chargePresenter.RemoveCallBacksAndMessages();
    }

    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }

    private void InitView() {
        chargeToolbar = findViewById(R.id.chargeToolbar);
        setSupportActionBar(chargeToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        chargeWebView = findViewById(R.id.chargeWebView);
        chargeFailTip = findViewById(R.id.chargeFailTip);
        chargeProgressbar = findViewById(R.id.chargeProgressbar);

        chargeInputLayout = findViewById(R.id.chargeInputLayout);
        chargeName = findViewById(R.id.chargeName);
        chargeNumber = findViewById(R.id.chargeNumber);
        chargeBalance = findViewById(R.id.chargeBalance);

        chargeAmount = findViewById(R.id.chargeAmount);
        chargeSubmit = findViewById(R.id.chargeSubmit);

        chargeAgreement = findViewById(R.id.chargeAgreement);

        SetOnClickEvent();
    }

    private void SetOnClickEvent() {
        chargeSubmit.setOnClickListener(this);
        chargeAgreement.setOnClickListener(this);
    }

    /**
     * 显示进度条
     */
    public void ShowProgressbar() {
        chargeProgressbar.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏进度条
     */
    public void HideProgressbar() {
        chargeProgressbar.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示失败提示
     *
     * @param text
     */
    public void ShowFailTip(String text) {
        chargeFailTip.setText(text);
        chargeFailTip.setVisibility(View.VISIBLE);
        canCancelWithoutAsking = true;
    }

    /**
     * 隐藏失败提示
     */
    public void HideFailTip() {
        chargeFailTip.setVisibility(View.INVISIBLE);
        chargeFailTip.setText("");
    }

    /**
     * 显示输入界面
     */
    public void ShowInputLayout() {
        chargeInputLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏输入界面
     */
    public void HideInputLayout() {
        chargeInputLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * 加载校园卡基本信息
     *
     * @param name
     * @param number
     * @param balance
     */
    public void LoadCardInfo(String name, String number, String balance) {
        chargeName.setText("姓名：" + name);
        chargeNumber.setText("卡号：" + number);
        chargeBalance.setText(balance);
    }

    /**
     * 加载充值界面
     *
     * @param cookieList
     * @param alipayURL
     */
    public void LoadWebView(List<Cookie> cookieList, String alipayURL) {
        //同步Cookies信息
        CookieSyncManager.createInstance(getApplicationContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        for (Cookie cookie : cookieList) {
            String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
            cookieManager.setCookie(cookie.getDomain(), cookieString);
        }
        CookieSyncManager.getInstance().sync();
        //设置支持JavaScript
        chargeWebView.getSettings().setJavaScriptEnabled(true);
        //设置支持缩放
        chargeWebView.getSettings().setBuiltInZoomControls(true);
        //设置禁用缓存
        chargeWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //设置Web视图
        chargeWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    if (url.contains("alipays://platformapi")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } else {
                        view.loadUrl(url);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        chargeWebView.setVisibility(View.VISIBLE);
        //加载需要显示的用户协议内容
        chargeWebView.loadUrl(alipayURL);
        Toast.makeText(this, "正在启动支付宝,请稍后...", Toast.LENGTH_SHORT).show();
        canCancelWithoutAsking = true;
    }

    /**
     * 显示Toast信息
     *
     * @param text
     */
    public void ShowToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                CheckConfirmCancel();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 防止用户意外返回键退出
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            CheckConfirmCancel();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出前让用户进行确认，防止意外退出
     */
    private void CheckConfirmCancel() {
        if (canCancelWithoutAsking) {
            //直接退出
            finish();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("退出充值页面");
            builder.setMessage("你确定要退出饭卡充值吗?");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alertDialog.dismiss();
                    finish();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chargeSubmit:
                //提交充值请求
                chargePresenter.ChargeSubmit(chargeAmount.getText().toString(), chargeWebView.getSettings().getUserAgentString());
                break;

            case R.id.chargeAgreement:
                //打开用户协议
                chargePresenter.ShowAgreement();
                break;
        }
    }
}

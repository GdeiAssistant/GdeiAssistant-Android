package edu.gdei.gdeiassistant.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import edu.gdei.gdeiassistant.Presenter.WebViewPresenter;
import edu.gdei.gdeiassistant.R;

public class WebViewActivity extends AppCompatActivity {

    private TextView webviewTitle;

    private WebView webviewWebView;

    private Toolbar webviewToolbar;

    private WebViewPresenter webViewPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        //初始化控件
        InitView();
        //配置加载Presenter
        webViewPresenter = new WebViewPresenter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webviewWebView != null) {
            webviewWebView.removeAllViews();
            webviewWebView.destroy();
        }
    }

    /**
     * 初始化控件
     */
    private void InitView() {
        webviewTitle = findViewById(R.id.chargeTitle);
        webviewWebView = findViewById(R.id.chargeWebView);
        webviewToolbar = findViewById(R.id.chargeToolbar);
        setSupportActionBar(webviewToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 加载网页
     *
     * @param url
     */
    public void LoadWeb(String url) {
        if (webviewWebView != null) {
            //设置支持JavaScript脚本
            webviewWebView.getSettings().setJavaScriptEnabled(true);
            //设置WEB视图
            webviewWebView.setWebViewClient(new WebViewClient());
            //加载需要显示的用户协议内容
            webviewWebView.loadUrl(url);
        }
    }

    /**
     * 设置标题名
     *
     * @param title
     */
    public void SetTitleName(String title) {
        webviewTitle.setText(title);
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

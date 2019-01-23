package edu.gdei.gdeiassistant.Presenter;

import android.content.Intent;

import edu.gdei.gdeiassistant.Activity.WebViewActivity;

public class WebViewPresenter {

    private WebViewActivity webViewActivity;

    public WebViewPresenter(WebViewActivity webViewActivity) {
        this.webViewActivity = webViewActivity;
        Init();
    }

    private void Init() {
        Intent intent = webViewActivity.getIntent();
        String title = intent.getStringExtra("title");
        String url = intent.getStringExtra("url");
        if (title != null) {
            webViewActivity.SetTitleName(title);
        }
        if (url != null) {
            webViewActivity.LoadWeb(url);
        }
    }

}

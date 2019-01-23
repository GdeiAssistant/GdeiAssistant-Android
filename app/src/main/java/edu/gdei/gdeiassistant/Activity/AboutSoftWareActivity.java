package edu.gdei.gdeiassistant.Activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import edu.gdei.gdeiassistant.R;

public class AboutSoftWareActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextView aboutSoftWareVersionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutsoftware);
        InitView();
    }

    private void InitView() {
        toolbar = findViewById(R.id.aboutSoftWareToolbar);
        aboutSoftWareVersionCode = findViewById(R.id.aboutSoftWareVersionCode);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //更新版本号
        try {
            String versionName = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            aboutSoftWareVersionCode.setText(versionName);
        } catch (PackageManager.NameNotFoundException ignored) {

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

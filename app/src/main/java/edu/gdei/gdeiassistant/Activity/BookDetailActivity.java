package edu.gdei.gdeiassistant.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import edu.gdei.gdeiassistant.Presenter.BookDetailPresenter;
import edu.gdei.gdeiassistant.R;
import edu.gdei.gdeiassistant.View.Dialog.ProgressDialogCreator;

public class BookDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private Integer position;

    private Toolbar toolbar;

    private TextView bookDetailBookName;

    private TextView bookDetailBorrowDate;

    private TextView bookDetailReturnDate;

    private TextView bookDetailID;

    private TextView bookDetailSN;

    private TextView bookDetailCode;

    private TextView bookDetailAuthor;

    private TextView bookDetailRenewTime;

    private Button bookRenew;

    private Dialog dialog;

    private BookDetailPresenter bookDetailPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        //初始化控件
        InitView();
        //配置加载Presenter
        bookDetailPresenter = new BookDetailPresenter(this);
    }

    private void InitView() {
        toolbar = findViewById(R.id.bookDetailToolbar);
        bookDetailBookName = findViewById(R.id.bookDetailBookName);
        bookDetailBorrowDate = findViewById(R.id.bookDetailBorrowDate);
        bookDetailReturnDate = findViewById(R.id.bookDetailReturnDate);
        bookDetailID = findViewById(R.id.bookDetailID);
        bookDetailSN = findViewById(R.id.bookDetailSN);
        bookDetailCode = findViewById(R.id.bookDetailCode);
        bookDetailAuthor = findViewById(R.id.bookDetailAuthor);
        bookDetailRenewTime = findViewById(R.id.bookDetailRenewTime);
        bookRenew = findViewById(R.id.bookRenew);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        bookRenew.setOnClickListener(this);
        position = Integer.parseInt(getIntent().getStringExtra("Position"));
        bookDetailID.setText(getIntent().getStringExtra("ID"));
        bookDetailSN.setText(getIntent().getStringExtra("SN"));
        bookDetailCode.setText(getIntent().getStringExtra("Code"));
        bookDetailBookName.setText(getIntent().getStringExtra("Name"));
        bookDetailAuthor.setText(getIntent().getStringExtra("Author"));
        bookDetailBorrowDate.setText(getIntent().getStringExtra("BorrowDate"));
        bookDetailReturnDate.setText(getIntent().getStringExtra("ReturnDate"));
        bookDetailRenewTime.setText(getIntent().getStringExtra("RenewTime"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
        }
        bookDetailPresenter.RemoveCallBacksAndMessages();
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

    /**
     * 显示Toast消息
     *
     * @param text
     */
    public void ShowToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 续借成功后调用以使续借次数加一
     */
    public void AddRenewTime() {
        bookDetailRenewTime.setText(String.valueOf(Integer.parseInt(bookDetailRenewTime.getText().toString()) + 1));
        Intent intent = new Intent();
        intent.putExtra("Position", String.valueOf(position));
        intent.putExtra("Time", bookDetailRenewTime.getText().toString());
        setResult(RESULT_OK, intent);
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
            case R.id.bookRenew:
                //提交图书续借请求
                bookDetailPresenter.SubmitBookRenew(getIntent().getStringExtra("Password")
                        , getIntent().getStringExtra("SN"), getIntent().getStringExtra("Code"));
                break;
        }
    }
}

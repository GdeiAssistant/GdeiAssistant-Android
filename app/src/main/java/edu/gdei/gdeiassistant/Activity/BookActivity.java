package edu.gdei.gdeiassistant.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

import edu.gdei.gdeiassistant.Constant.ActivityRequestCodeConstant;
import edu.gdei.gdeiassistant.Pojo.Entity.Book;
import edu.gdei.gdeiassistant.Presenter.BookPresenter;
import edu.gdei.gdeiassistant.R;
import edu.gdei.gdeiassistant.View.Adapter.BookListViewAdapter;
import edu.gdei.gdeiassistant.View.Dialog.ProgressDialogCreator;
import edu.gdei.gdeiassistant.View.ListView.ListViewForScrollView;

public class BookActivity extends AppCompatActivity implements View.OnClickListener {

    private List<Book> bookList;

    private Toolbar toolbar;

    private LinearLayout bookQueryInputLayout;

    private LinearLayout bookQueryDataLayout;

    private EditText bookQueryPassword;

    private Button bookQuerySubmit;

    private ListViewForScrollView bookQueryListview;

    private BookListViewAdapter bookListViewAdapter;

    private Dialog dialog;

    private BookPresenter bookPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        //初始化控件
        InitView();
        //配置加载Presenter
        bookPresenter = new BookPresenter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
        }
        bookPresenter.RemoveCallBacksAndMessages();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ActivityRequestCodeConstant.RESULT_BOOK_RENEW) {
            if (resultCode == RESULT_OK && data != null) {
                //更新借阅图书列表信息
                int position = Integer.parseInt(data.getStringExtra("Position"));
                int time = Integer.parseInt(data.getStringExtra("Time"));
                bookList.get(position).setRenewTime(time);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void InitView() {
        toolbar = findViewById(R.id.bookToolbar);
        bookQueryInputLayout = findViewById(R.id.bookQueryInputLayout);
        bookQueryDataLayout = findViewById(R.id.bookQueryDataLayout);
        bookQueryPassword = findViewById(R.id.bookQueryPassword);
        bookQuerySubmit = findViewById(R.id.bookQuerySubmit);
        bookQueryListview = findViewById(R.id.bookQueryListview);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        bookQuerySubmit.setOnClickListener(this);
    }

    /**
     * 保存借阅图书列表信息
     *
     * @param bookList
     */
    public void SaveBookList(List<Book> bookList) {
        this.bookList = bookList;
    }

    /**
     * 加载借阅图书信息
     */
    public void LoadBookData() {
        if (bookListViewAdapter == null) {
            bookListViewAdapter = new BookListViewAdapter(this, bookList, R.layout.book_item);
            bookQueryListview.setAdapter(bookListViewAdapter);
            bookQueryListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(BookActivity.this, BookDetailActivity.class);
                    intent.putExtra("Position", String.valueOf(position));
                    intent.putExtra("ID", bookList.get(position).getId());
                    intent.putExtra("SN", bookList.get(position).getSn());
                    intent.putExtra("Code", bookList.get(position).getCode());
                    intent.putExtra("Name", bookList.get(position).getName());
                    intent.putExtra("Author", bookList.get(position).getAuthor());
                    intent.putExtra("BorrowDate", bookList.get(position).getBorrowDate());
                    intent.putExtra("ReturnDate", bookList.get(position).getReturnDate());
                    intent.putExtra("RenewTime", String.valueOf(bookList.get(position).getRenewTime() == null
                            ? 0 : bookList.get(position).getRenewTime()));
                    intent.putExtra("Password", bookQueryPassword.getText().toString());
                    startActivityForResult(intent, ActivityRequestCodeConstant.RESULT_BOOK_RENEW);
                }
            });
        } else {
            bookListViewAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 显示图书证借阅密码输入界面
     */
    public void ShowInputLayout() {
        bookQueryDataLayout.setVisibility(View.INVISIBLE);
        bookQueryInputLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 显示借阅图书列表节目
     */
    public void ShowDataLayout() {
        bookQueryInputLayout.setVisibility(View.INVISIBLE);
        bookQueryDataLayout.setVisibility(View.VISIBLE);
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
            case R.id.bookQuerySubmit:
                //提交图书借阅请求
                bookPresenter.SubmitBookQuery(bookQueryPassword.getText().toString());
                break;
        }
    }
}

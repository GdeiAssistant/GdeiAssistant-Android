package edu.gdei.gdeiassistant.Presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.inputmethod.InputMethodManager;

import java.lang.ref.WeakReference;
import java.util.List;

import edu.gdei.gdeiassistant.Activity.BookActivity;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Model.BookModel;
import edu.gdei.gdeiassistant.Pojo.Entity.Book;
import edu.gdei.gdeiassistant.Tools.StringUtils;

public class BookPresenter {

    private BookActivity bookActivity;

    private BookActivityHandler bookActivityHandler;

    private BookModel bookModel;

    public BookPresenter(BookActivity bookActivity) {
        this.bookActivity = bookActivity;
        this.bookModel = new BookModel();
        this.bookActivityHandler = new BookActivityHandler(bookActivity);
    }

    public static class BookActivityHandler extends Handler {

        private BookActivity bookActivity;

        BookActivityHandler(BookActivity bookActivity) {
            this.bookActivity = new WeakReference<>(bookActivity).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.REQUEST_SUCCESS:
                    //查询图书借阅信息成功
                    bookActivity.HideProgressDialog();
                    List<Book> bookList = (List<Book>) msg.getData().getSerializable("BookList");
                    bookActivity.HideProgressDialog();
                    bookActivity.ShowDataLayout();
                    bookActivity.SaveBookList(bookList);
                    bookActivity.LoadBookData();
                    break;

                case RequestConstant.REQUEST_FAILURE:
                    //查询图书借阅信息或续借图书失败
                    bookActivity.HideProgressDialog();
                    bookActivity.ShowToast(msg.getData().getString("Message"));
                    break;

                case RequestConstant.REQUEST_TIMEOUT:
                    //网络连接超时
                    bookActivity.HideProgressDialog();
                    bookActivity.ShowToast("网络连接超时，请重试");
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    //出现未知异常
                    bookActivity.HideProgressDialog();
                    bookActivity.ShowToast("出现未知异常，请联系管理员");
                    break;

                case RequestConstant.SHOW_PROGRESS:
                    //显示进度条
                    bookActivity.ShowProgressDialog();
                    break;
            }
        }
    }

    /**
     * 移除所有的回调和消息，防止内存泄露
     */
    public void RemoveCallBacksAndMessages() {
        bookActivityHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 提交图书借阅查询请求
     */
    public void SubmitBookQuery(String password) {
        if (StringUtils.isBlank(password)) {
            bookActivity.ShowToast("图书证借阅密码不能为空");
        } else {
            InputMethodManager inputMethodManager = (InputMethodManager) bookActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                //收起虚拟键盘
                inputMethodManager.hideSoftInputFromWindow(bookActivity.getWindow().getDecorView().getWindowToken(), 0);
            }
            bookModel.SubmitBookQuery(password, bookActivityHandler, bookActivity.getApplicationContext());
        }
    }
}

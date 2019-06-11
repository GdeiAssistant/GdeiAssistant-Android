package edu.gdei.gdeiassistant.Presenter;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import edu.gdei.gdeiassistant.Activity.BookDetailActivity;
import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.Model.BookModel;

public class BookDetailPresenter {

    private BookDetailActivity bookDetailActivity;

    private BookDetailActivityHandler bookDetailActivityHandler;

    private BookModel bookModel;

    public BookDetailPresenter(BookDetailActivity bookDetailActivity) {
        this.bookDetailActivity = bookDetailActivity;
        this.bookModel = new BookModel();
        this.bookDetailActivityHandler = new BookDetailActivityHandler(bookDetailActivity);
    }

    public static class BookDetailActivityHandler extends Handler {

        private BookDetailActivity bookDetailActivity;

        BookDetailActivityHandler(BookDetailActivity bookDetailActivity) {
            this.bookDetailActivity = new WeakReference<>(bookDetailActivity).get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RequestConstant.REQUEST_SUCCESS:
                    //续借图书成功
                    bookDetailActivity.HideProgressDialog();
                    bookDetailActivity.ShowToast("续借图书成功");
                    bookDetailActivity.AddRenewTime();
                    break;

                case RequestConstant.REQUEST_FAILURE:
                    //续借图书失败
                    bookDetailActivity.HideProgressDialog();
                    bookDetailActivity.ShowToast(msg.getData().getString("Message"));
                    break;

                case RequestConstant.REQUEST_TIMEOUT:
                    //网络连接超时
                    bookDetailActivity.HideProgressDialog();
                    bookDetailActivity.ShowToast("网络连接超时，请重试");
                    break;

                case RequestConstant.UNKNOWN_ERROR:
                    //出现未知异常
                    bookDetailActivity.HideProgressDialog();
                    bookDetailActivity.ShowToast("出现未知异常，请联系管理员");
                    break;

                case RequestConstant.SHOW_PROGRESS:
                    //显示进度条
                    bookDetailActivity.ShowProgressDialog();
                    break;
            }
        }
    }

    /**
     * 移除所有的回调和消息，防止内存泄露
     */
    public void RemoveCallBacksAndMessages() {
        bookDetailActivityHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 提交图书借阅查询请求
     */
    public void SubmitBookRenew(String password, String sn, String code) {
        bookModel.SubmitBookRenew(password, sn, code, bookDetailActivityHandler, bookDetailActivity.getApplicationContext());
    }
}

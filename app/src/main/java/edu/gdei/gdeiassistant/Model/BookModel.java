package edu.gdei.gdeiassistant.Model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import edu.gdei.gdeiassistant.Constant.RequestConstant;
import edu.gdei.gdeiassistant.NetWork.BookNetWork;
import edu.gdei.gdeiassistant.Pojo.Entity.Book;
import edu.gdei.gdeiassistant.Pojo.JsonResult.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.JsonResult.JsonResult;

public class BookModel {

    private BookNetWork bookNetWork = new BookNetWork();

    /**
     * 提交图书借阅查询请求
     *
     * @param password
     * @param handler
     * @param context
     */
    public void SubmitBookQuery(final String password, final Handler handler, final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                try {
                    message.what = RequestConstant.SHOW_PROGRESS;
                    handler.sendMessage(message);
                    message = handler.obtainMessage();
                    DataJsonResult<List<Book>> result = bookNetWork.BookQuery(password, context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        bundle.putSerializable("BookList", (Serializable) result.getData());
                        message.what = RequestConstant.REQUEST_SUCCESS;
                    } else {
                        bundle.putString("Message", result.getMessage());
                        message.what = RequestConstant.REQUEST_FAILURE;
                    }
                } catch (NullPointerException ignored) {

                } catch (IOException e) {
                    message.what = RequestConstant.REQUEST_TIMEOUT;
                } catch (Exception e) {
                    message.what = RequestConstant.UNKNOWN_ERROR;
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }.start();
    }

    /**
     * 提交图书续借请求
     *
     * @param password
     * @param handler
     * @param context
     */
    public void SubmitBookRenew(final String password, final String sn, final String code, final Handler handler
            , final Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                try {
                    message.what = RequestConstant.SHOW_PROGRESS;
                    handler.sendMessage(message);
                    message = handler.obtainMessage();
                    JsonResult result = bookNetWork.BookRenew(password, sn, code, context);
                    if (Boolean.TRUE.equals(result.isSuccess())) {
                        message.what = RequestConstant.REQUEST_SUCCESS;
                    } else {
                        bundle.putString("Message", result.getMessage());
                        message.what = RequestConstant.REQUEST_FAILURE;
                    }
                } catch (NullPointerException ignored) {

                } catch (IOException e) {
                    message.what = RequestConstant.REQUEST_TIMEOUT;
                } catch (Exception e) {
                    message.what = RequestConstant.UNKNOWN_ERROR;
                }
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }.start();
    }
}

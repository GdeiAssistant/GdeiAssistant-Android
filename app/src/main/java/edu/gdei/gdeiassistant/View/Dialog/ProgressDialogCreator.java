package edu.gdei.gdeiassistant.View.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

public class ProgressDialogCreator {

    /**
     * 创建ProgressDialog
     *
     * @param activity
     * @return
     */
    public Dialog GetProgressDialogCreator(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(activity.getLayoutInflater().inflate(edu.gdei.gdeiassistant.R.layout.progressdialog, (ViewGroup) activity.findViewById(edu.gdei.gdeiassistant.R.id.progressDialog)));
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        dialog.getWindow().setLayout(point.x / 2, point.y / 5);
        return dialog;
    }
}

package edu.gdei.gdeiassistant.Model;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapFileModel {

    /**
     * 将Bitmap图片保存到本地路径，并返回路径
     *
     * @param context
     * @param fileName 文件名称
     * @param bitmap   图片
     * @return
     */

    public static String saveFile(Context context, String fileName, Bitmap bitmap) {
        return saveFile(context, "", fileName, bitmap);
    }

    public static String saveFile(Context context, String filePath, String fileName, Bitmap bitmap) {
        byte[] bytes = bitmapToBytes(bitmap);
        return saveFile(context, filePath, fileName, bytes);
    }

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static String saveFile(Context context, String filePath, String fileName, byte[] bytes) {
        String fileFullName = "";
        FileOutputStream fileOutputStream = null;
        try {
            String suffix = "";
            if (filePath == null || filePath.trim().length() == 0) {
                filePath = context.getCacheDir() + "/gdeiassistant/";
            }
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            File fullFile = new File(filePath, fileName + suffix);
            fileFullName = fullFile.getPath();
            fileOutputStream = new FileOutputStream(new File(filePath, fileName + suffix));
            fileOutputStream.write(bytes);
        } catch (Exception e) {
            fileFullName = "";
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    fileFullName = "";
                }
            }
        }
        return fileFullName;
    }
}

package com.simple;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by mrsimple on 28/5/16.
 */
public final class StorageUtils {

    private StorageUtils() {
    }

    /**
     * 有sd卡的路径
     */
    public static String CRASH_DIR = "/crashcanary/log/";

    public static boolean isSDCardMounted() {
        return Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getCrashDir(Context context) {
        initLogDir(context);
        String dir;
        if (isSDCardMounted()) {
            dir = Environment.getExternalStorageDirectory().toString() + CRASH_DIR;
        } else {
            dir = context.getCacheDir() + CRASH_DIR;
        }
        checkCrashLogDir(dir);
        return dir;
    }

    private static void initLogDir(Context context) {
        CRASH_DIR = "/crashcanary/" + context.getPackageName() + "/log/";
    }

    private static void checkCrashLogDir(String crashFileDirs) {
        File file = new File(crashFileDirs);
        if (!file.exists()) {
            try {
                // 按照指定的路径创建文件夹
                file.mkdirs();
            } catch (Exception e) {
                return;
            }
        }
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public static void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                deleteFile(f);
            }
            file.delete();
        }
    }
}

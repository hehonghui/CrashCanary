package com.simple;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by mrsimple on 28/5/16.
 */
public class LogReader {
    String mLogDir = "";

    /**
     * 获取log根目录下的所有log文件路径
     */
    public String[] getCrashLogFileNames() {
        File[] files = new File(mLogDir).listFiles();
        if (files == null || files.length <= 0) {
            return new String[]{};
        }

        int size = files.length - 1;
        String[] logFiles = new String[files.length];
        for (int i = size; i >= 0; i--) {
            // 将文件逆序排
            logFiles[size - i] = files[i].getName();
        }
        return logFiles;
    }

    /**
     * 通过txt文件的路径获取其内容
     *
     * @param filepath
     * @return
     */
    public String readLog(String filepath) {
        FileInputStream fileInputStream = null;
        String log = "";
        try {
            File file = new File(filepath);
            fileInputStream = new FileInputStream(file);
            log = IOUtils.streamToString(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeSilently(fileInputStream);
        }
        return log;
    }

    void setLogDir(String dir) {
        this.mLogDir = dir;
    }

    public String getLogDir() {
        return mLogDir;
    }
}

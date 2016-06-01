package com.simple;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mrsimple on 28/5/16.
 */
public class LogWriter {
    /**
     * 用于格式化日期,作为日志文件名的一部分
     */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

    String mLogDir = "";

    /**
     * 写入crash 日志, 并且返回log文件的文件名 [ 将日志写入到文件 ]
     *
     * @param t
     * @param e
     * @return log完整的文件名,如果没有写入到本地文件,直接返回""即可
     */
    public String write(Thread t, final Throwable e) {
        final Writer stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        // 存储log
        String fileName = writeToFile(stringWriter.toString());
        IOUtils.closeSilently(printWriter);
        return fileName;
    }

    /**
     * 将crash log写入到文件,并且返回该文件的完整路径
     *
     * @param stacktrace
     * @return log的完整路径
     */
    private String writeToFile(String stacktrace) {
        String fileName = generateLogFileName(mLogDir);
        BufferedWriter bos = null;
        try {
            bos = new BufferedWriter(new FileWriter(fileName));
            bos.write(stacktrace);
            bos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeSilently(bos);
        }
        return fileName;
    }

    void setLogDir(String dir) {
        this.mLogDir = dir;
    }

    /**
     * @param dir
     * @return
     */
    private static String generateLogFileName(String dir) {
        String time = DATE_FORMAT.format(new Date());
        return dir + "log_" + time + ".txt";
    }
}

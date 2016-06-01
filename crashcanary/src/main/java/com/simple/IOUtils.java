package com.simple;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 将stream 转换 String的工具类
 */
public final class IOUtils {

    private IOUtils() {
    }

    /**
     * stream to string
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String streamToString(InputStream inputStream)
            throws IOException {
        if ( inputStream == null ) {
            return "";
        }
        StringBuilder sBuilder = new StringBuilder();
        String line ;
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream));
        while ((line = bufferedReader.readLine()) != null) {
            sBuilder.append(line).append("\n");
        }
        return sBuilder.toString();
    }

    /**
     * 关闭Closeable对象
     * @param closeable
     */
    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

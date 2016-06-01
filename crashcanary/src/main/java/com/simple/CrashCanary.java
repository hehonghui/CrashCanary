package com.simple;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.simple.ui.LogDetailActivity;

import java.io.File;

/**
 * 崩溃日志收集、显示处理类,在 内部设置了 UncaughtExceptionHandler,
 * 因此如果在应用中使用了其他 crash 收集sdk,需要注意 CrashCanary 与这些sdk的兼容。
 * <p/>
 * crash 日志会在下次进入应用时自动提示到通知栏，或者用户可以手动调用
 * {@link #showLogListDialog(Context)} 函数显示crash 日志列表
 * <p/>
 * Created by ly on 16/5/10.
 */
public final class CrashCanary {
    private static CrashCanary sInstance;
    /**
     * 是否正在写crash log, 避免同一个crash log 写入多个文件
     */
    private boolean isWriting = false;
    private LogWriter mWriter;
    private LogReader mReader;
    private Context mContext;
    /**
     * 用户打开crash 日志列表时选中的log 项
     */
    private String mSelectedItem = "";

    private CrashCanary() {
    }

    public static CrashCanary getInstance() {
        if (sInstance == null) {
            synchronized (CrashCanary.class) {
                if (sInstance == null) {
                }
                sInstance = new CrashCanary();
            }
        }
        return sInstance;
    }

    /**
     * dump the crash log
     *
     * @param thread
     * @param exception
     */
    private void dump(final Thread thread, final Throwable exception) {
        if (isWriting) {
            return;
        }
        isWriting = true;
        // 存储这次的crash 文件名,下次进入应用时弹出通知
        SharePref.saveLastCrashLog(mContext, mWriter.write(thread, exception));
        isWriting = false;
    }

    private void showNotification(String fileName) {
        Intent intent = new Intent(mContext, LogDetailActivity.class);
        intent.putExtra("log", fileName);

        PendingIntent pendingintent =
                PendingIntent.getActivity(mContext, 0, intent, 0);
        //设置各项参数
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setContentTitle(mContext.getString(R.string.crash_occur))
                .setContentText(fileName)
                .setSmallIcon(R.drawable.crash_icon)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.crash_icon))
                .setContentIntent(pendingintent);

        NotificationManager notifyMgr =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyMgr.notify(intent.hashCode(), mBuilder.build());
    }

    /**
     * 展示崩溃日志列表
     */
    public void showLogListDialog(final Context context) {
        // 获取日志列表
        final String[] allList = mReader.getCrashLogFileNames();
        if (allList.length > 0) {
            mSelectedItem = allList[0];
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.log_detail);
        builder.setSingleChoiceItems(allList, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSelectedItem = allList[which];
            }
        });
        builder.setPositiveButton(R.string.show_log, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(context, LogDetailActivity.class);
                intent.putExtra("log", mReader.getLogDir() + mSelectedItem);
                context.startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.clear_all, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StorageUtils.deleteFile(new File(mReader.getLogDir()));
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mSelectedItem = "";
            }
        });
        alertDialog.show();
    }

    private void checkLastCrash() {
        String lastLog = SharePref.getLastCrashLog(mContext);
        if (!TextUtils.isEmpty(lastLog)) {
            showNotification(lastLog);
            SharePref.saveLastCrashLog(mContext, "");
        }
    }

    public LogReader getReader() {
        return mReader;
    }

    public LogWriter getWriter() {
        return mWriter;
    }

    /**
     * CrashCanary的Builder,用于配置 CrashCanaey
     */
    public static class Builder {
        private Context mContext;
        private String mLogDir;
        private Thread.UncaughtExceptionHandler mExpHandler;
        private LogReader mLogReader;
        private LogWriter mLogWriter;
        private boolean killProcessWhenCrash = true;

        public Builder(Context context) {
            this.mContext = context.getApplicationContext();
        }

        public Builder setLogDir(String dir) {
            this.mLogDir = dir;
            return this;
        }

        public Builder setLogReader(LogReader mLogReader) {
            this.mLogReader = mLogReader;
            return this;
        }

        public Builder setLogWriter(LogWriter mLogWriter) {
            this.mLogWriter = mLogWriter;
            return this;
        }

        public Builder setExceptionHandler(Thread.UncaughtExceptionHandler handler) {
            this.mExpHandler = handler;
            return this;
        }

        public Builder setKillProcessWhenCrash(boolean isKill) {
            this.killProcessWhenCrash = isKill;
            return this;
        }

        private void killTheProcess() {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }

        private void handleCrash(CrashCanary crashCanary, Thread thread, Throwable ex) {
            if (mExpHandler != null) {
                mExpHandler.uncaughtException(thread, ex);
            }
            crashCanary.dump(thread, ex);
            ex.printStackTrace();
            if (killProcessWhenCrash) {
                killTheProcess();
            }
        }

        public CrashCanary build() {
            final CrashCanary crashCanary = CrashCanary.getInstance();
            if (TextUtils.isEmpty(mLogDir)) {
                mLogDir = StorageUtils.getCrashDir(mContext);
            }
            crashCanary.mContext = mContext;
            crashCanary.mReader = mLogReader == null ? new LogReader() : mLogReader;
            crashCanary.mWriter = mLogWriter == null ? new LogWriter() : mLogWriter;
            crashCanary.mReader.setLogDir(mLogDir);
            crashCanary.mWriter.setLogDir(mLogDir);
            // 设置异常处理
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    handleCrash(crashCanary, thread, ex);
                }
            });
            crashCanary.checkLastCrash();
            return crashCanary;
        }
    }
}

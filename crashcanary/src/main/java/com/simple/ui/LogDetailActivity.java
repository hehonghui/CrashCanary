package com.simple.ui;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.simple.CrashCanary;
import com.simple.LogReader;
import com.simple.R;

/**
 * 日志详情页面
 * Created by mrsimple on 28/5/16.
 */
public class LogDetailActivity extends Activity {

    TextView mReasonTv;
    TextView mLogTextView;
    String mLogFile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_log_layout);
        mLogFile = getIntent().getExtras().getString("log");
        if (TextUtils.isEmpty(mLogFile)) {
            Toast.makeText(this, "no log file", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mReasonTv = (TextView) findViewById(R.id.crash_reason_tv);
        mLogTextView = (TextView) findViewById(R.id.crash_log_tv);
    }

    private void setupCopy(final String logContent) {
        findViewById(R.id.copy_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copy(logContent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        submitReadLogDetailTask(mLogFile, getApplicationContext(), CrashCanary.getInstance().getReader());
    }

    /**
     * @param selectedFile
     * @param context
     * @param reader
     */
    private void submitReadLogDetailTask(final String selectedFile,
                                         final Context context,
                                         final LogReader reader) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                if (!TextUtils.isEmpty(selectedFile)) {
                    return reader.readLog(selectedFile);
                }
                return "";
            }

            @Override
            protected void onPostExecute(String logContent) {
                if (!TextUtils.isEmpty(logContent)) {
                    mReasonTv.setText(parseReason(logContent));
                    mLogTextView.setText(logContent);
                    setupCopy(logContent);
                } else {
                    Toast.makeText(context, R.string.empty_log, Toast.LENGTH_SHORT).show();
                }
            }

            private String parseReason(String logContent) {
                int firstIndex = logContent.indexOf(")");
                if (firstIndex < 0) {
                    return "Not Found.";
                }
                return logContent.substring(0, firstIndex + 1);
            }
        }.execute();
    }

    private void copy(String logContent) {
        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(logContent);
        Toast.makeText(this, R.string.copy_success, Toast.LENGTH_SHORT).show();
    }
}

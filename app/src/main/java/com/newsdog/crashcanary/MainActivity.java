package com.newsdog.crashcanary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.simple.CrashCanary;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CrashCanary crashCanary = new CrashCanary.Builder(this)
                .setExceptionHandler(new Thread.UncaughtExceptionHandler() {  // 设置自己的异常处理 Handler
                    @Override
                    public void uncaughtException(Thread thread, Throwable ex) {

                    }
                })
                .setKillProcessWhenCrash(true)      // 发生crash之后关闭应用,再次进入应用时会有crash通知
                .build();

        Button btn = (Button) findViewById(R.id.submit_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new NullPointerException("Crash  Canary Test");
            }
        });

        findViewById(R.id.show_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crashCanary.showLogListDialog(MainActivity.this);
            }
        });
    }
}

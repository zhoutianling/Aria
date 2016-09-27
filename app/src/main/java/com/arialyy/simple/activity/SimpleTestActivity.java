package com.arialyy.simple.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arialyy.downloadutil.core.DownloadManager;
import com.arialyy.downloadutil.core.command.CommandFactory;
import com.arialyy.downloadutil.core.command.IDownloadCommand;
import com.arialyy.downloadutil.entity.DownloadEntity;
import com.arialyy.downloadutil.util.DownLoadUtil;
import com.arialyy.downloadutil.util.Util;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivitySimpleBinding;
import com.arialyy.simple.module.DownloadModule;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class SimpleTestActivity extends BaseActivity<ActivitySimpleBinding> {
    private static final int DOWNLOAD_PRE      = 0x01;
    private static final int DOWNLOAD_STOP     = 0x02;
    private static final int DOWNLOAD_FAILE    = 0x03;
    private static final int DOWNLOAD_CANCEL   = 0x04;
    private static final int DOWNLOAD_RESUME   = 0x05;
    private static final int DOWNLOAD_COMPLETE = 0x06;
    private ProgressBar mPb;
    private String mDownloadUrl = "http://static.gaoshouyou.com/d/12/0d/7f120f50c80d2e7b8c4ba24ece4f9cdd.apk";
    private DownLoadUtil mUtil;
    private Button       mStart, mStop, mCancel;
    private             TextView mSize;
    @Bind(R.id.toolbar) Toolbar  toolbar;

    private Handler mUpdateHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DOWNLOAD_PRE:
                    mSize.setText(Util.formatFileSize((Long) msg.obj));
                    mStart.setEnabled(false);
                    break;
                case DOWNLOAD_FAILE:
                    Toast.makeText(SimpleTestActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                    break;
                case DOWNLOAD_STOP:
                    Toast.makeText(SimpleTestActivity.this, "暂停下载", Toast.LENGTH_SHORT).show();
                    mStart.setText("恢复");
                    mStart.setEnabled(true);
                    break;
                case DOWNLOAD_CANCEL:
                    mPb.setProgress(0);
                    Toast.makeText(SimpleTestActivity.this, "取消下载", Toast.LENGTH_SHORT).show();
                    mStart.setEnabled(true);
                    mStart.setText("开始");
                    break;
                case DOWNLOAD_RESUME:
                    Toast.makeText(SimpleTestActivity.this,
                            "恢复下载，恢复位置 ==> " + Util.formatFileSize((Long) msg.obj),
                            Toast.LENGTH_SHORT).show();
                    mStart.setEnabled(false);
                    break;
                case DOWNLOAD_COMPLETE:
                    Toast.makeText(SimpleTestActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                    mStart.setEnabled(true);
                    mCancel.setEnabled(false);
                    mStop.setEnabled(false);
                    break;
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {

        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, getModule(DownloadModule.class).getDownloadFilter());
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override protected int setLayoutId() {
        return R.layout.activity_simple;
    }

    @Override protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        setSupportActionBar(toolbar);
        init();
    }

    private void init() {
        mPb = (ProgressBar) findViewById(R.id.progressBar);
        mStart = (Button) findViewById(R.id.start);
        mStop = (Button) findViewById(R.id.stop);
        mCancel = (Button) findViewById(R.id.cancel);
        mSize = (TextView) findViewById(R.id.size);
        mUtil = new DownLoadUtil();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                start();
                break;
            case R.id.stop:
                stop();
                break;
            case R.id.cancel:
                cancel();
                break;
        }
    }

    private void start() {
        DownloadEntity entity = new DownloadEntity();
        entity.setFileName("test.apk");
        entity.setDownloadUrl(mDownloadUrl);
        entity.setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/test.apk");
        List<IDownloadCommand> commands = new ArrayList<>();
        IDownloadCommand addCommand = CommandFactory.getInstance()
                .createCommand(this, entity, CommandFactory.TASK_CREATE);
        IDownloadCommand startCommand = CommandFactory.getInstance()
                .createCommand(this, entity, CommandFactory.TASK_START);
        commands.add(addCommand);
        commands.add(startCommand);
        DownloadManager.getInstance(this).setCommands(commands).exe();

    }

    private void stop() {
        mUtil.stopDownload();
    }

    private void cancel() {
        mUtil.cancelDownload();
    }
}

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
import com.arialyy.downloadutil.orm.DbEntity;
import com.arialyy.downloadutil.util.Util;
import com.arialyy.frame.util.show.L;
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
    private Button mStart, mStop, mCancel;
    private             TextView        mSize;
    @Bind(R.id.toolbar) Toolbar         toolbar;
    private             CommandFactory  mFactory;
    private             DownloadManager mManager;
    private             DownloadEntity  mEntity;

    private Handler mUpdateHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DOWNLOAD_PRE:
                    mSize.setText(Util.formatFileSize((Long) msg.obj));
                    setBtState(false);
                    break;
                case DOWNLOAD_FAILE:
                    Toast.makeText(SimpleTestActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                    setBtState(true);
                    break;
                case DOWNLOAD_STOP:
                    Toast.makeText(SimpleTestActivity.this, "暂停下载", Toast.LENGTH_SHORT).show();
                    mStart.setText("恢复");
                    setBtState(true);
                    break;
                case DOWNLOAD_CANCEL:
                    mPb.setProgress(0);
                    Toast.makeText(SimpleTestActivity.this, "取消下载", Toast.LENGTH_SHORT).show();
                    mStart.setText("开始");
                    setBtState(true);
                    break;
                case DOWNLOAD_RESUME:
                    Toast.makeText(SimpleTestActivity.this,
                                   "恢复下载，恢复位置 ==> " + Util.formatFileSize((Long) msg.obj),
                                   Toast.LENGTH_SHORT).show();
                    setBtState(false);
                    break;
                case DOWNLOAD_COMPLETE:
                    Toast.makeText(SimpleTestActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                    mStart.setText("重新开始？");
                    mCancel.setEnabled(false);
                    setBtState(true);
                    break;
            }
        }
    };

    /**
     * 设置start 和 stop 按钮状态
     *
     * @param state
     */
    private void setBtState(boolean state) {
        mStart.setEnabled(state);
        mStop.setEnabled(!state);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        long len = 0;

        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case DownloadManager.ACTION_PRE:
                    DownloadEntity entity = intent.getParcelableExtra(DownloadManager.ACTION_PRE);
                    len = entity.getFileSize();
                    L.d(TAG, "download pre");
                    mUpdateHandler.obtainMessage(DOWNLOAD_PRE, len).sendToTarget();
                    break;
                case DownloadManager.ACTION_START:
                    L.d(TAG, "download start");
                    break;
                case DownloadManager.ACTION_RESUME:
                    L.d(TAG, "download resume");
                    long location = intent.getLongExtra(DownloadManager.CURRENT_LOCATION, 1);
                    mUpdateHandler.obtainMessage(DOWNLOAD_RESUME, location).sendToTarget();
                    break;
                case DownloadManager.ACTION_RUNNING:
                    long current = intent.getLongExtra(DownloadManager.ACTION_RUNNING, 0);
                    if (len == 0) {
                        mPb.setProgress(0);
                    } else {
                        mPb.setProgress((int) ((current * 100) / len));
                    }
                    break;
                case DownloadManager.ACTION_STOP:
                    L.d(TAG, "download stop");
                    mUpdateHandler.sendEmptyMessage(DOWNLOAD_STOP);
                    break;
                case DownloadManager.ACTION_COMPLETE:
                    mUpdateHandler.sendEmptyMessage(DOWNLOAD_COMPLETE);
                    break;
                case DownloadManager.ACTION_CANCEL:
                    mUpdateHandler.sendEmptyMessage(DOWNLOAD_CANCEL);
                    break;
                case DownloadManager.ACTION_FAIL:
                    mUpdateHandler.sendEmptyMessage(DOWNLOAD_FAILE);
                    break;
            }
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
        mFactory = CommandFactory.getInstance();
        mManager = DownloadManager.getInstance();
        mEntity = DbEntity.findData(DownloadEntity.class, new String[]{"downloadUrl"},
                                    new String[]{mDownloadUrl});
        if (mEntity != null) {
            mPb.setProgress((int) ((mEntity.getCurrentProgress() * 100) / mEntity.getFileSize()));
            mSize.setText(Util.formatFileSize(mEntity.getFileSize()));
            if (mEntity.getState() == DownloadEntity.STATE_DOWNLOAD_ING) {
                setBtState(false);
            } else if (mEntity.isDownloadComplete()) {
                mStart.setText("重新开始？");
                setBtState(true);
            }
        } else {
            mEntity = new DownloadEntity();
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                start();
//                if (PermissionManager.getInstance()
//                        .checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                    start();
//                } else {
//                    PermissionManager.getInstance()
//                            .requestPermission(this, new OnPermissionCallback() {
//                                @Override public void onSuccess(String... permissions) {
//                                    start();
//                                }
//
//                                @Override public void onFail(String... permissions) {
//
//                                }
//                            }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                }
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
        mEntity.setFileName("test.apk");
        mEntity.setDownloadUrl(mDownloadUrl);
        mEntity.setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/test.apk");
        List<IDownloadCommand> commands = new ArrayList<>();
        IDownloadCommand addCommand = mFactory.createCommand(this, mEntity,
                                                             CommandFactory.TASK_CREATE);
        IDownloadCommand startCommand = mFactory.createCommand(this, mEntity,
                                                               CommandFactory.TASK_START);
        commands.add(addCommand);
        commands.add(startCommand);
        mManager.setCommands(commands).exe();
    }

    private void stop() {
        IDownloadCommand stopCommand = mFactory.createCommand(this, mEntity,
                                                              CommandFactory.TASK_STOP);
        mManager.setCommand(stopCommand).exe();
    }

    private void cancel() {
        IDownloadCommand cancelCommand = mFactory.createCommand(this, mEntity,
                                                                CommandFactory.TASK_CANCEL);
        mManager.setCommand(cancelCommand).exe();
    }
}

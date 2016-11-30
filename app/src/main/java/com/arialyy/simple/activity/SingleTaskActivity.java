/*
 * Copyright (C) 2016 AriaLyy(DownloadUtil)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arialyy.simple.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import butterknife.Bind;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.DownloadManager;
import com.arialyy.downloadutil.core.command.CmdFactory;
import com.arialyy.downloadutil.core.command.IDownloadCmd;
import com.arialyy.downloadutil.core.scheduler.OnSchedulerListener;
import com.arialyy.downloadutil.core.task.Task;
import com.arialyy.downloadutil.orm.DbEntity;
import com.arialyy.downloadutil.util.CommonUtil;
import com.arialyy.frame.util.show.L;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivitySingleBinding;
import com.arialyy.simple.module.DownloadModule;
import java.util.ArrayList;
import java.util.List;

public class SingleTaskActivity extends BaseActivity<ActivitySingleBinding> {
  public static final int DOWNLOAD_PRE = 0x01;
  public static final int DOWNLOAD_STOP = 0x02;
  public static final int DOWNLOAD_FAILE = 0x03;
  public static final int DOWNLOAD_CANCEL = 0x04;
  public static final int DOWNLOAD_RESUME = 0x05;
  public static final int DOWNLOAD_COMPLETE = 0x06;
  public static final int DOWNLOAD_RUNNING = 0x07;
  private ProgressBar mPb;
  private String mDownloadUrl =
      "http://static.gaoshouyou.com/d/12/0d/7f120f50c80d2e7b8c4ba24ece4f9cdd.apk";
  private Button mStart, mStop, mCancel;
  private TextView mSize;
  @Bind(R.id.toolbar) Toolbar toolbar;
  private CmdFactory mFactory;
  private DownloadManager mManager;
  private DownloadEntity mEntity;
  private BroadcastReceiver mReceiver;

  private Handler mUpdateHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case DOWNLOAD_RUNNING:
          mPb.setProgress((Integer) msg.obj);
          break;
        case DOWNLOAD_PRE:
          mSize.setText(CommonUtil.formatFileSize((Long) msg.obj));
          setBtState(false);
          break;
        case DOWNLOAD_FAILE:
          Toast.makeText(SingleTaskActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
          setBtState(true);
          break;
        case DOWNLOAD_STOP:
          Toast.makeText(SingleTaskActivity.this, "暂停下载", Toast.LENGTH_SHORT).show();
          mStart.setText("恢复");
          setBtState(true);
          break;
        case DOWNLOAD_CANCEL:
          mPb.setProgress(0);
          Toast.makeText(SingleTaskActivity.this, "取消下载", Toast.LENGTH_SHORT).show();
          mStart.setText("开始");
          setBtState(true);
          break;
        case DOWNLOAD_RESUME:
          //Toast.makeText(SingleTaskActivity.this,
          //    "恢复下载，恢复位置 ==> " + CommonUtil.formatFileSize((Long) msg.obj), Toast.LENGTH_SHORT)
          //    .show();
          setBtState(false);
          break;
        case DOWNLOAD_COMPLETE:
          Toast.makeText(SingleTaskActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
          mStart.setText("重新开始？");
          mCancel.setEnabled(false);
          setBtState(true);
          break;
      }
    }
  };

  /**
   * 设置start 和 stop 按钮状态
   */
  private void setBtState(boolean state) {
    mStart.setEnabled(state);
    mStop.setEnabled(!state);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    init();
  }

  @Override protected void onResume() {
    super.onResume();
    //IntentFilter filter = getModule(DownloadModule.class).getDownloadFilter();
    //mReceiver = getModule(DownloadModule.class).createReceiver(mUpdateHandler);
    //registerReceiver(mReceiver, filter);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    //unregisterReceiver(mReceiver);
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_single;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setSupportActionBar(toolbar);
    toolbar.setTitle("单任务下载");
    init();
  }

  private void init() {
    mPb = (ProgressBar) findViewById(R.id.progressBar);
    mStart = (Button) findViewById(R.id.start);
    mStop = (Button) findViewById(R.id.stop);
    mCancel = (Button) findViewById(R.id.cancel);
    mSize = (TextView) findViewById(R.id.size);
    mFactory = CmdFactory.getInstance();
    mManager = DownloadManager.getInstance();
    mEntity = DbEntity.findData(DownloadEntity.class, new String[] { "downloadUrl" },
        new String[] { mDownloadUrl });
    if (mEntity != null) {
      mPb.setProgress((int) ((mEntity.getCurrentProgress() * 100) / mEntity.getFileSize()));
      mSize.setText(CommonUtil.formatFileSize(mEntity.getFileSize()));
      if (mEntity.getState() == DownloadEntity.STATE_DOWNLOAD_ING) {
        setBtState(false);
      } else if (mEntity.isDownloadComplete()) {
        mStart.setText("重新开始？");
        setBtState(true);
      }
    } else {
      mEntity = new DownloadEntity();
      mEntity.setFileName("test.apk");
      mEntity.setDownloadUrl(mDownloadUrl);
      mEntity.setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/test.apk");
    }
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        String text = ((TextView) view).getText().toString();
        if (text.equals("重新开始？") || text.equals("开始")) {
          start();
        }else if (text.equals("恢复")){
          resume();
        }
        break;
      case R.id.stop:
        stop();
        break;
      case R.id.cancel:
        cancel();
        break;
    }
  }

  private void resume(){
    IDownloadCmd startCmd = mFactory.createCmd(mEntity, CmdFactory.TASK_START);
    mManager.setCmd(startCmd).exe();
    mUpdateHandler.obtainMessage(DOWNLOAD_RESUME, mEntity.getCurrentProgress()).sendToTarget();
  }

  private void start() {
    mEntity.setFileName("test.apk");
    mEntity.setDownloadUrl(mDownloadUrl);
    mEntity.setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/test.apk");
    //List<IDownloadCmd> commands = new ArrayList<>();
    //IDownloadCmd addCMD = mFactory.createCmd(mEntity, CmdFactory.TASK_CREATE);
    //IDownloadCmd startCmd = mFactory.createCmd(mEntity, CmdFactory.TASK_START);
    //commands.add(addCMD);
    //commands.add(startCmd);
    //mManager.setCmds(commands).exe();
    mManager.setCmd(CmdFactory.getInstance().createCmd(mEntity, CmdFactory.TASK_SINGLE))
        .regSchedulerListener(new OnSchedulerListener() {
          @Override public void onTaskStart(Task task) {
            mUpdateHandler.obtainMessage(DOWNLOAD_PRE, task.getDownloadEntity().getFileSize())
                .sendToTarget();
          }

          @Override public void onTaskStop(Task task) {
            mUpdateHandler.sendEmptyMessage(DOWNLOAD_STOP);
          }

          @Override public void onTaskCancel(Task task) {
            mUpdateHandler.sendEmptyMessage(DOWNLOAD_CANCEL);
          }

          @Override public void onTaskFail(Task task) {
            mUpdateHandler.sendEmptyMessage(DOWNLOAD_FAILE);
          }

          @Override public void onTaskComplete(Task task) {
            mUpdateHandler.sendEmptyMessage(DOWNLOAD_COMPLETE);
          }

          @Override public void onTaskRunning(Task task) {
            //L.d(TAG, task.getDownloadEntity().getCurrentProgress() + "");
            long current = task.getDownloadEntity().getCurrentProgress();
            long len = task.getDownloadEntity().getFileSize();
            if (len == 0) {
              mPb.setProgress(0);
            } else {
              mPb.setProgress((int) ((current * 100) / len));
            }
          }
        })
        .exe();
  }

  private void stop() {
    IDownloadCmd stopCmd = mFactory.createCmd(mEntity, CmdFactory.TASK_STOP);
    mManager.setCmd(stopCmd).exe();
  }

  private void cancel() {
    IDownloadCmd cancelCmd = mFactory.createCmd(mEntity, CmdFactory.TASK_CANCEL);
    mManager.setCmd(cancelCmd).exe();
  }
}
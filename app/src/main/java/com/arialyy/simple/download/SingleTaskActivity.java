/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
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

package com.arialyy.simple.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.download.DownloadTarget;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.util.FileUtil;
import com.arialyy.frame.util.show.L;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivitySingleBinding;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;
import java.io.File;

public class SingleTaskActivity extends BaseActivity<ActivitySingleBinding> {
  public static final int DOWNLOAD_PRE = 0x01;
  public static final int DOWNLOAD_STOP = 0x02;
  public static final int DOWNLOAD_FAILE = 0x03;
  public static final int DOWNLOAD_CANCEL = 0x04;
  public static final int DOWNLOAD_RESUME = 0x05;
  public static final int DOWNLOAD_COMPLETE = 0x06;
  public static final int DOWNLOAD_RUNNING = 0x07;
  public static final int DOWNLOAD_START = 0x08;

  private static final String DOWNLOAD_URL =
      //"http://kotlinlang.org/docs/kotlin-docs.pdf";
      //"https://atom-installer.github.com/v1.13.0/AtomSetup.exe?s=1484074138&ext=.exe";
      "http://static.gaoshouyou.com/d/22/94/822260b849944492caadd2983f9bb624.apk";
  //"http://tinghuaapp.oss-cn-shanghai.aliyuncs.com/20170612201739607815";
  //"http://static.gaoshouyou.com/d/36/69/2d3699acfa69e9632262442c46516ad8.apk";
  //"http://oqcpqqvuf.bkt.clouddn.com/ceshi.txt";
  //"http://down8.androidgame-store.com/201706122321/97967927DD4E53D9905ECAA7874C8128/new/game1/19/45319/com.neuralprisma-2.5.2.174-2000174_1494784835.apk?f=web_1";
  //不支持断点的链接
  //"http://ox.konsung.net:5555/ksdc-web/download/downloadFile/?fileName=ksdc_1.0.2.apk&rRange=0-";
  //"http://172.18.104.50:8080/download/_302turn";
  @Bind(R.id.progressBar) HorizontalProgressBarWithNumber mPb;
  @Bind(R.id.start) Button mStart;
  @Bind(R.id.stop) Button mStop;
  @Bind(R.id.cancel) Button mCancel;
  @Bind(R.id.size) TextView mSize;
  @Bind(R.id.speed) TextView mSpeed;
  @Bind(R.id.speeds) RadioGroup mRg;

  private Handler mUpdateHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case DOWNLOAD_RUNNING:
          DownloadTask task = (DownloadTask) msg.obj;
          long current = task.getCurrentProgress();
          long len = task.getFileSize();
          if (len == 0) {
            mPb.setProgress(0);
          } else {
            mPb.setProgress((int) ((current * 100) / len));
          }
          mSpeed.setText(task.getConvertSpeed());
          break;
        case DOWNLOAD_PRE:
          setBtState(false);
          break;
        case DOWNLOAD_START:
          mSize.setText(CommonUtil.formatFileSize((Long) msg.obj));
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
          mStart.setText("暂停");
          setBtState(false);
          break;
        case DOWNLOAD_COMPLETE:
          mPb.setProgress(100);
          Toast.makeText(SingleTaskActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
          mStart.setText("重新开始？");
          mCancel.setEnabled(false);
          setBtState(true);
          break;
      }
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Aria.download(this).register();
  }

  /**
   * 设置start 和 stop 按钮状态
   */
  private void setBtState(boolean state) {
    mStart.setEnabled(state);
    mStop.setEnabled(!state);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_single_task_activity, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    double speed = -1;
    String msg = "";
    switch (item.getItemId()) {
      case R.id.help:
        msg = "一些小知识点：\n"
            + "1、你可以在注解中增加链接，用于指定被注解的方法只能被特定的下载任务回调，以防止progress乱跳\n"
            + "2、当遇到网络慢的情况时，你可以先使用onPre()更新UI界面，待连接成功时，再在onTaskPre()获取完整的task数据，然后给UI界面设置正确的数据\n"
            + "3、你可以在界面初始化时通过Aria.download(this).load(DOWNLOAD_URL).getPercent()等方法快速获取相关任务的一些数据";
        showMsgDialog("tip", msg);
        break;
      case R.id.speed_0:
        speed = 0.0;
        break;
      case R.id.speed_128:
        speed = 128.0;
        break;
      case R.id.speed_256:
        speed = 256.0;
        break;
      case R.id.speed_512:
        speed = 512.0;
        break;
      case R.id.speed_1m:
        speed = 1024.0;
        break;
    }
    if (speed > -1) {
      msg = item.getTitle().toString();
      Aria.download(this).setMaxSpeed(speed);
      T.showShort(this, msg);
    }
    return true;
  }

  @Download.onPre(DOWNLOAD_URL) protected void onPre(DownloadTask task) {
    mUpdateHandler.obtainMessage(DOWNLOAD_PRE, task.getDownloadEntity().getFileSize())
        .sendToTarget();
  }

  @Download.onTaskStart(DOWNLOAD_URL) void taskStart(DownloadTask task) {
    mUpdateHandler.obtainMessage(DOWNLOAD_START, task.getDownloadEntity().getFileSize())
        .sendToTarget();
  }

  @Download.onTaskRunning(DOWNLOAD_URL) protected void running(DownloadTask task) {
    mUpdateHandler.obtainMessage(DOWNLOAD_RUNNING, task).sendToTarget();
  }

  @Download.onTaskResume(DOWNLOAD_URL) void taskResume(DownloadTask task) {
    mUpdateHandler.obtainMessage(DOWNLOAD_START, task.getFileSize()).sendToTarget();
  }

  @Download.onTaskStop(DOWNLOAD_URL) void taskStop(DownloadTask task) {
    mUpdateHandler.sendEmptyMessage(DOWNLOAD_STOP);
    L.d(TAG, "task__stop");
  }

  @Download.onTaskCancel(DOWNLOAD_URL) void taskCancel(DownloadTask task) {
    mUpdateHandler.sendEmptyMessage(DOWNLOAD_CANCEL);
    L.d(TAG, "task__cancel");
  }

  @Download.onTaskFail(DOWNLOAD_URL) void taskFail(DownloadTask task) {
    L.d(TAG, "task__fail");
    mUpdateHandler.sendEmptyMessage(DOWNLOAD_FAILE);
  }

  @Download.onTaskComplete(DOWNLOAD_URL) void taskComplete(DownloadTask task) {
    mUpdateHandler.sendEmptyMessage(DOWNLOAD_COMPLETE);
  }

  @Download.onNoSupportBreakPoint(DOWNLOAD_URL)
  public void onNoSupportBreakPoint(DownloadTask task) {
    T.showShort(SingleTaskActivity.this, "该下载链接不支持断点");
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_single;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setTitle("单任务下载");
    DownloadTarget target = Aria.download(this).load(DOWNLOAD_URL);
    mPb.setProgress(target.getPercent());
    if (target.getTaskState() == IEntity.STATE_STOP) {
      mStart.setText("恢复");
      mStart.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
      setBtState(true);
    } else if (target.isDownloading()) {
      setBtState(false);
    }
    mSize.setText(target.getConvertFileSize());
    Aria.get(this).getDownloadConfig().setOpenBreadCast(true);
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        String text = ((TextView) view).getText().toString();
        if (text.equals("重新开始？") || text.equals("开始")) {
          Aria.download(this)
              .load(DOWNLOAD_URL)
              .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/test.apk")
              .start();
        } else if (text.equals("恢复")) {
          Aria.download(this).load(DOWNLOAD_URL).resume();
        }
        break;
      case R.id.stop:
        Aria.download(this).load(DOWNLOAD_URL).pause();
        break;
      case R.id.cancel:
        Aria.download(this).load(DOWNLOAD_URL).cancel();
        break;
    }
  }
}
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

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.arialyy.aria.core.download.DownloadTarget;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.core.AbsDialog;
import com.arialyy.simple.R;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;

/**
 * Created by AriaL on 2017/1/2.
 */
public class DownloadDialog extends AbsDialog {
  @Bind(R.id.progressBar) HorizontalProgressBarWithNumber mPb;
  @Bind(R.id.start) Button mStart;
  @Bind(R.id.stop) Button mStop;
  @Bind(R.id.cancel) Button mCancel;
  @Bind(R.id.size) TextView mSize;
  @Bind(R.id.speed) TextView mSpeed;

  private static final String DOWNLOAD_URL =
      "http://static.gaoshouyou.com/d/3a/93/573ae1db9493a801c24bf66128b11e39.apk";

  public DownloadDialog(Context context) {
    super(context);
    init();
  }

  @Override protected int setLayoutId() {
    return R.layout.dialog_download;
  }

  private void init() {
    if (Aria.download(this).taskExists(DOWNLOAD_URL)) {
      DownloadTarget target = Aria.download(this).load(DOWNLOAD_URL);
      int p = (int) (target.getCurrentProgress() * 100 / target.getFileSize());
      mPb.setProgress(p);
    }
    Aria.download(this).addSchedulerListener(new MyDialogDownloadCallback());
    DownloadEntity entity = Aria.download(this).getDownloadEntity(DOWNLOAD_URL);
    if (entity != null) {
      mSize.setText(CommonUtil.formatFileSize(entity.getFileSize()));
      int state = entity.getState();
      setBtState(state != DownloadEntity.STATE_RUNNING);
    } else {
      setBtState(true);
    }
  }

  @OnClick({ R.id.start, R.id.stop, R.id.cancel }) public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        Aria.download(this)
            .load(DOWNLOAD_URL)
            .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/daialog.apk")
            .start();
        break;
      case R.id.stop:
        Aria.download(this).load(DOWNLOAD_URL).pause();
        break;
      case R.id.cancel:
        Aria.download(this).load(DOWNLOAD_URL).cancel();
        break;
    }
  }

  @Override protected void dataCallback(int result, Object obj) {

  }

  private void setBtState(boolean startEnable) {
    mStart.setEnabled(startEnable);
    mCancel.setEnabled(!startEnable);
    mStop.setEnabled(!startEnable);
  }

  private class MyDialogDownloadCallback extends Aria.DownloadSchedulerListener {

    @Override public void onTaskPre(DownloadTask task) {
      super.onTaskPre(task);
      mSize.setText(CommonUtil.formatFileSize(task.getFileSize()));
      setBtState(false);
    }

    @Override public void onTaskStop(DownloadTask task) {
      super.onTaskStop(task);
      setBtState(true);
      mSpeed.setText(task.getConvertSpeed());
    }

    @Override public void onTaskCancel(DownloadTask task) {
      super.onTaskCancel(task);
      setBtState(true);
      mPb.setProgress(0);
      mSpeed.setText(task.getConvertSpeed());
    }

    @Override public void onTaskRunning(DownloadTask task) {
      super.onTaskRunning(task);
      long current = task.getCurrentProgress();
      long len = task.getFileSize();
      if (len == 0) {
        mPb.setProgress(0);
      } else {
        mPb.setProgress((int) ((current * 100) / len));
      }
      mSpeed.setText(task.getConvertSpeed());
    }
  }
}

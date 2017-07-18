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

package com.arialyy.simple.upload;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import butterknife.Bind;
import butterknife.OnClick;
import com.arialyy.annotations.Upload;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.upload.UploadTask;
import com.arialyy.frame.util.FileUtil;
import com.arialyy.frame.util.show.L;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityUploadMeanBinding;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;
import java.lang.ref.WeakReference;

/**
 * Created by Aria.Lao on 2017/2/9.
 */
public class UploadActivity extends BaseActivity<ActivityUploadMeanBinding> {
  private static final String TAG = "UploadActivity";
  @Bind(R.id.pb) HorizontalProgressBarWithNumber mPb;
  private static final int START = 0;
  private static final int STOP = 1;
  private static final int CANCEL = 2;
  private static final int RUNNING = 3;
  private static final int COMPLETE = 4;

  private static final String FILE_PATH = "/sdcard/Download/test.zip";

  private Handler mHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      UploadTask task = (UploadTask) msg.obj;
      switch (msg.what) {
        case START:
          getBinding().setFileSize(FileUtil.formatFileSize(task.getFileSize()));
          break;
        case STOP:
          mPb.setProgress(0);
          break;
        case CANCEL:
          mPb.setProgress(0);
          break;
        case RUNNING:
          int p = (int) (task.getCurrentProgress() * 100 / task.getFileSize());
          mPb.setProgress(p);
          break;
        case COMPLETE:
          T.showShort(UploadActivity.this, "上传完成");
          mPb.setProgress(100);
          break;
      }
    }
  };

  @Override protected int setLayoutId() {
    return R.layout.activity_upload_mean;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.upload(this).register();
  }

  @OnClick(R.id.upload) void upload() {
    Aria.upload(this)
        .load(FILE_PATH)
        .setUploadUrl("http://172.18.104.129:8080/upload/sign_file")
        .setAttachment("file")
        .start();
  }

  @OnClick(R.id.stop) void stop() {
    Aria.upload(this).load(FILE_PATH).cancel();
  }

  @OnClick(R.id.remove) void remove() {
    Aria.upload(this).load(FILE_PATH).cancel();
  }

  @Upload.onPre public void onPre(UploadTask task) {
  }

  @Upload.onTaskPre public void taskPre(UploadTask task) {
    L.d(TAG, "fileSize = " + task.getConvertFileSize());
  }

  @Upload.onTaskStart public void taskStart(UploadTask task) {
    mHandler.obtainMessage(START, task).sendToTarget();
  }

  @Upload.onTaskStop public void taskStop(UploadTask task) {
    mHandler.obtainMessage(STOP, task).sendToTarget();
  }

  @Upload.onTaskCancel public void taskCancel(UploadTask task) {
    mHandler.obtainMessage(CANCEL, task).sendToTarget();
  }

  @Upload.onTaskRunning public void taskRunning(UploadTask task) {
    mHandler.obtainMessage(RUNNING, task).sendToTarget();
  }

  @Upload.onTaskComplete public void taskComplete(UploadTask task) {
    mHandler.obtainMessage(COMPLETE, task).sendToTarget();
  }
}

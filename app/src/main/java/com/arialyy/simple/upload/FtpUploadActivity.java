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
import android.util.Log;
import android.view.View;
import com.arialyy.annotations.Upload;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.upload.UploadTask;
import com.arialyy.frame.util.show.L;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityFtpUploadBinding;

/**
 * Created by Aria.Lao on 2017/7/28.
 * Ftp 文件上传demo
 */
public class FtpUploadActivity extends BaseActivity<ActivityFtpUploadBinding> {
  private final String FILE_PATH = "/mnt/sdcard/王者军团.apk";
  private final String URL = "ftp://172.18.104.129:21/upload/";

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.upload(this).register();
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_ftp_upload;
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        Aria.upload(this).loadFtp(FILE_PATH).setUploadUrl(URL).login("lao", "123456").start();
        break;
      case R.id.stop:
        Aria.upload(this).loadFtp(FILE_PATH).stop();
        break;
      case R.id.cancel:
        Aria.upload(this).load(FILE_PATH).cancel();
        break;
    }
  }

  @Upload.onPre public void onPre(UploadTask task) {
    getBinding().setFileSize(task.getConvertFileSize());
  }

  @Upload.onTaskPre public void taskPre(UploadTask task) {
    L.d(TAG, "fileSize = " + task.getConvertFileSize());
  }

  @Upload.onTaskStart public void taskStart(UploadTask task) {

  }

  @Upload.onTaskStop public void taskStop(UploadTask task) {
    getBinding().setSpeed("");
    Log.d(TAG, "停止上传");
  }

  @Upload.onTaskCancel public void taskCancel(UploadTask task) {
    getBinding().setSpeed("");
    Log.d(TAG, "取消上传");
  }

  @Upload.onTaskRunning public void taskRunning(UploadTask task) {
    getBinding().setProgress(task.getPercent());
    getBinding().setSpeed(task.getConvertSpeed());
  }

  @Upload.onTaskComplete public void taskComplete(UploadTask task) {
    getBinding().setProgress(100);
    getBinding().setSpeed("");
    T.showShort(this, "上传完成");
  }
}

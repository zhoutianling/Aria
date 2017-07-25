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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.util.show.L;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityFtpDownloadBinding;
import java.io.File;

/**
 * Created by Aria.Lao on 2017/7/25.
 * Ftp下载测试
 */
public class FtpDownloadActivity extends BaseActivity<ActivityFtpDownloadBinding> {

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.download(this).register();
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        Aria.download(this)
            //.load("172.18.104.129", "21", "cd.mp3")
            //.load("192.168.1.8", "21", "gg.png")
            .load("192.168.1.8", "21", "23.pdf")
            //.load("192.168.1.8", "21", "heh.txt")
            .login("lao", "123456")
            .setDownloadPath("/mnt/sdcard/tt.png")
            .charSet("gbk")
            .start();
        break;
      case R.id.stop:
        break;
      case R.id.cancel:
        break;
    }
  }

  @Download.onPre() protected void onPre(DownloadTask task) {
    L.d(TAG, "ftp pre");
  }

  @Download.onTaskPre() protected void onTaskPre(DownloadTask task) {
    L.d(TAG, "ftp task pre");
    getBinding().setFileSize(task.getConvertFileSize());
  }

  @Download.onTaskStart() void taskStart(DownloadTask task) {
    L.d(TAG, "ftp task start");
  }

  @Download.onTaskRunning() protected void running(DownloadTask task) {
    getBinding().setProgress(task.getPercent());
    getBinding().setSpeed(task.getConvertSpeed());
  }

  @Download.onTaskResume() void taskResume(DownloadTask task) {
    L.d(TAG, "ftp task resume");
  }

  @Download.onTaskStop() void taskStop(DownloadTask task) {
    L.d(TAG, "ftp task stop");
    getBinding().setSpeed("");
  }

  @Download.onTaskCancel() void taskCancel(DownloadTask task) {
    getBinding().setSpeed("");
    getBinding().setProgress(0);
  }

  @Download.onTaskFail() void taskFail(DownloadTask task) {
    L.d(TAG, "ftp task fail");
  }

  @Download.onTaskComplete() void taskComplete(DownloadTask task) {
    getBinding().setProgress(100);
    Log.d(TAG, "md5 ==> " + CommonUtil.getFileMD5(new File(task.getDownloadPath())));
    T.showShort(this, "FTP下载完成");
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_ftp_download;
  }
}

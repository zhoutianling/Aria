package com.arialyy.simple.test;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.util.show.L;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityTestBinding;
import java.io.File;

/**
 * Created by Administrator on 2018/4/12.
 */

public class TestActivity extends BaseActivity<ActivityTestBinding> {
  String TAG = "TestActivity";
  String URL = "http://58.210.9.131/tpk/sipgt//TDLYZTGH.tpk"; //chunked 下载

  @Override protected int setLayoutId() {
    return R.layout.activity_test;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    mBar.setVisibility(View.GONE);
    Aria.download(this).register();
  }

  @Download.onWait void onWait(DownloadTask task) {
    Log.d(TAG, "wait ==> " + task.getDownloadEntity().getFileName());
  }

  @Download.onPre protected void onPre(DownloadTask task) {
    Log.d(TAG, "onPre");
  }

  @Download.onTaskStart void taskStart(DownloadTask task) {
    Log.d(TAG, "onStart");
  }

  @Download.onTaskRunning protected void running(DownloadTask task) {
    Log.d(TAG, "running");
  }

  @Download.onTaskResume void taskResume(DownloadTask task) {
    Log.d(TAG, "resume");
  }

  @Download.onTaskStop void taskStop(DownloadTask task) {
    Log.d(TAG, "stop");
  }

  @Download.onTaskCancel void taskCancel(DownloadTask task) {
    Log.d(TAG, "cancel");
  }

  @Download.onTaskFail void taskFail(DownloadTask task) {
    Log.d(TAG, "fail");
  }

  @Download.onTaskComplete void taskComplete(DownloadTask task) {
    L.d(TAG, "path ==> " + task.getDownloadEntity().getDownloadPath());
    L.d(TAG, "md5Code ==> " + CommonUtil.getFileMD5(new File(task.getDownloadPath())));
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        Aria.download(this)
            .load(URL)
            .addHeader("Accept-Encoding", "gzip, deflate")
            .setRequestMode(RequestEnum.GET)
            .setFilePath(Environment.getExternalStorageDirectory().getPath() + "/ggsg1.apk")
            .resetState()
            .start();
        break;
      case R.id.stop:
        Aria.download(this).load(URL).stop();
        break;
      case R.id.cancel:
        Aria.download(this).load(URL).cancel();
        break;
    }
  }
}

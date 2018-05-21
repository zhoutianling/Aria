package com.arialyy.simple.test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.arialyy.annotations.Upload;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.upload.UploadTask;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityTestBinding;
import java.io.File;

/**
 * Created by Administrator on 2018/4/12.
 */

public class TestActivity extends BaseActivity<ActivityTestBinding> {
  String TAG = "TestActivity";
  //String URL = "http://58.210.9.131/tpk/sipgt//TDLYZTGH.tpk"; //chunked 下载
  //private final String URL = "ftp://192.168.1.3:21/download//AriaPrj.rar";
  private final String FILE_PATH = "/mnt/sdcard/AriaPrj.rar";
  //private final String URL = "ftp://192.168.29.140:21/aa//你好";
  private final String URL = "ftp://192.168.29.140:21/upload/";

  @Override protected int setLayoutId() {
    return R.layout.activity_test;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    mBar.setVisibility(View.GONE);
    Aria.upload(this).register();

  }

  @Upload.onWait void onWait(UploadTask task) {
    Log.d(TAG, "wait ==> " + task.getEntity().getFileName());
  }

  @Upload.onPre protected void onPre(UploadTask task) {
    Log.d(TAG, "onPre");
  }

  @Upload.onTaskStart void taskStart(UploadTask task) {
    Log.d(TAG, "onStart");
  }

  @Upload.onTaskRunning protected void running(UploadTask task) {
    Log.d(TAG, "running");
  }

  @Upload.onTaskResume void taskResume(UploadTask task) {
    Log.d(TAG, "resume");
  }

  @Upload.onTaskStop void taskStop(UploadTask task) {
    Log.d(TAG, "stop");
  }

  @Upload.onTaskCancel void taskCancel(UploadTask task) {
    Log.d(TAG, "cancel");
  }

  @Upload.onTaskFail void taskFail(UploadTask task) {
    Log.d(TAG, "fail");
  }

  @Upload.onTaskComplete void taskComplete(UploadTask task) {
    Log.d(TAG, "complete, md5 => " + CommonUtil.getFileMD5(new File(task.getKey())));
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        Aria.upload(this)
            .loadFtp(FILE_PATH)
            .login("lao", "123456")
            .setUploadUrl(URL)
            .setExtendField("韩寒哈大双")
            .start();
        break;
      case R.id.stop:
        Aria.upload(this).loadFtp(FILE_PATH).stop();
        break;
      case R.id.cancel:
        Aria.upload(this).loadFtp(FILE_PATH).cancel();
        break;
    }
  }
}

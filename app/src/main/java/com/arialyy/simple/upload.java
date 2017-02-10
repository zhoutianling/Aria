package com.arialyy.simple;

import android.view.View;
import android.widget.ProgressBar;
import butterknife.Bind;
import com.arialyy.aria.core.upload.IUploadListener;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.core.upload.UploadUtil;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityUploadBinding;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;

/**
 * Created by Aria.Lao on 2017/2/9.
 */

public class upload extends BaseActivity<ActivityUploadBinding> {

  @Bind(R.id.pb) HorizontalProgressBarWithNumber mPb;

  @Override protected int setLayoutId() {
    return R.layout.activity_upload;
  }

  public void onClick(View view) {
    UploadEntity entity = new UploadEntity();
    entity.setFilePath("/sdcard/Download/test.zip");
    entity.setFileName("test.pdf");
    UploadTaskEntity taskEntity = new UploadTaskEntity(entity);
    taskEntity.uploadUrl = "http://172.18.104.189:8080/upload/sign_file";
    taskEntity.attachment = "file";
    UploadUtil util = new UploadUtil(taskEntity, new IUploadListener() {
      long fileSize = 0;

      @Override public void onPre() {

      }

      @Override public void onStart(long fileSize) {
        this.fileSize = fileSize;
      }

      @Override public void onResume(long resumeLocation) {

      }

      @Override public void onStop(long stopLocation) {

      }

      @Override public void onProgress(long currentLocation) {
        int p = (int) (currentLocation * 100 / fileSize);
        mPb.setProgress(p);
      }

      @Override public void onCancel() {

      }

      @Override public void onComplete() {

      }

      @Override public void onFail() {

      }
    });
    util.start();
  }
}

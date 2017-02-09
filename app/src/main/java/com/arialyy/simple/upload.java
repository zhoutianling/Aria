package com.arialyy.simple;

import android.view.View;
import com.arialyy.aria.core.upload.IUploadListener;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.core.upload.UploadUtil;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityUploadBinding;

/**
 * Created by Aria.Lao on 2017/2/9.
 */

public class upload extends BaseActivity<ActivityUploadBinding>{
  @Override protected int setLayoutId() {
    return R.layout.activity_upload;

  }

  public void onClick(View view){
    UploadEntity entity = new UploadEntity();
    entity.setFilePath("/sdcard/Download/test.pdf");
    entity.setFileName("test.pdf");
    UploadTaskEntity taskEntity = new UploadTaskEntity(entity);
    taskEntity.uploadUrl = "http://192.168.1.9:8080/upload/sign_file";
    taskEntity.attachment = "file";
    UploadUtil util = new UploadUtil(taskEntity, new IUploadListener() {
      @Override public void onFail() {

      }
    });
    util.start();
  }

}

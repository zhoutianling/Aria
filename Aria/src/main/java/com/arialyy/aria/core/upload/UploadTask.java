package com.arialyy.aria.core.upload;

import android.os.Handler;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.ITask;

/**
 * Created by Aria.Lao on 2017/2/23.
 * 上传任务
 */
public class UploadTask implements ITask {
  private Handler mOutHandler;
  private UploadTaskEntity mTaskEntity;
  private UploadEntity mUploadEntity;

  UploadTask(UploadTaskEntity taskEntity, Handler outHandler) {
    mTaskEntity = taskEntity;
    mOutHandler = outHandler;
    mUploadEntity = mTaskEntity.uploadEntity;
  }

  @Override public String getKey() {
    return null;
  }

  @Override public boolean isRunning() {
    return false;
  }

  @Override public IEntity getEntity() {
    return mUploadEntity;
  }

  @Override public void start() {

  }

  @Override public void stop() {

  }

  @Override public void cancel() {

  }

  @Override public long getSpeed() {
    return 0;
  }

  @Override public long getFileSize() {
    return 0;
  }

  @Override public long getCurrentProgress() {
    return 0;
  }

  private static class  UListener extends UploadListener{
    @Override public void onPre() {

    }

    @Override public void onStart(long fileSize) {

    }

    @Override public void onResume(long resumeLocation) {

    }

    @Override public void onStop(long stopLocation) {

    }

    @Override public void onProgress(long currentLocation) {

    }

    @Override public void onCancel() {

    }

    @Override public void onComplete() {

    }

    @Override public void onFail() {

    }
  }

  static class Builder {
    private Handler mOutHandler;
    private UploadTaskEntity mTaskEntity;

    public void setOutHandler(Handler outHandler){
      mOutHandler = outHandler;
    }

    public void setUploadTaskEntity(UploadTaskEntity taskEntity){
      mTaskEntity = taskEntity;
    }

    public Builder(){

    }

    public UploadTask build(){
      UploadTask task = new UploadTask(mTaskEntity, mOutHandler);
      return task;
    }

  }
}

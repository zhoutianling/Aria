package com.arialyy.aria.core.upload;

import com.arialyy.aria.core.queue.ITaskQueue;

/**
 * Created by Aria.Lao on 2017/2/23.
 */

public class UploadTaskQueue implements ITaskQueue<UploadTask, UploadTaskEntity, UploadEntity>{
  @Override public void startTask(UploadTask task) {

  }

  @Override public void stopTask(UploadTask task) {

  }

  @Override public void cancelTask(UploadTask task) {

  }

  @Override public void reTryStart(UploadTask task) {

  }

  @Override public int size() {
    return 0;
  }

  @Override public void setDownloadNum(int downloadNum) {

  }

  @Override public UploadTask createTask(String targetName, UploadTaskEntity entity) {
    return null;
  }

  @Override public UploadTask getTask(UploadEntity entity) {
    return null;
  }

  @Override public void removeTask(UploadEntity entity) {

  }

  @Override public UploadTask getNextTask() {
    return null;
  }
}

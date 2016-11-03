package com.arialyy.downloadutil.core.command;

import android.content.Context;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.Task;

/**
 * Created by lyy on 2016/9/20.
 * 取消命令
 */
class CancelCmd extends IDownloadCmd {

  CancelCmd(DownloadEntity entity) {
    super(entity);
  }

  @Override public void executeCmd() {
    Task task = mQueue.getTask(mEntity);
    if (task == null) {
      task = mQueue.createTask(mEntity);
    }
    if (task != null) {
      mQueue.cancelTask(task);
    }
  }
}

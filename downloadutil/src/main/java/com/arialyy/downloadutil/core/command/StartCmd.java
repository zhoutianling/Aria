package com.arialyy.downloadutil.core.command;

import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.task.Task;

/**
 * Created by lyy on 2016/8/22.
 * 开始命令
 */
class StartCmd extends IDownloadCmd {

  StartCmd(DownloadEntity entity) {
    super(entity);
  }

  @Override public void executeCmd() {
    Task task = mQueue.getTask(mEntity);
    if (task == null) {
      task = mQueue.createTask(mEntity);
    }
    if (task != null) {
      mQueue.startTask(task);
    }
  }
}

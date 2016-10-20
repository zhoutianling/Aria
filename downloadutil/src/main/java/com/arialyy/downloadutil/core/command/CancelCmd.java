package com.arialyy.downloadutil.core.command;

import android.content.Context;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.Task;

/**
 * Created by lyy on 2016/9/20.
 * 取消命令
 */
class CancelCmd extends IDownloadCmd {

  CancelCmd(Context context, DownloadEntity entity) {
    super(context, entity);
  }

  @Override public void executeComment() {
    Task task = target.getTask(mEntity);
    if (task == null) {
      task = target.createTask(mEntity);
    }
    if (task != null) {
      target.cancelTask(task);
    }
  }
}

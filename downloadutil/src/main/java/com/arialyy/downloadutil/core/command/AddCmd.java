package com.arialyy.downloadutil.core.command;

import android.util.Log;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.Task;

/**
 * Created by lyy on 2016/8/22.
 * 添加任务的命令
 */
class AddCmd extends IDownloadCmd {

  AddCmd(DownloadEntity entity) {
    super(entity);
  }

  @Override public void executeCmd() {
    Task task = mQueue.getTask(mEntity);
    if (task == null) {
      mQueue.createTask(mEntity);
    } else {
      Log.w(TAG, "添加命令执行失败，【该任务已经存在】");
    }
  }
}

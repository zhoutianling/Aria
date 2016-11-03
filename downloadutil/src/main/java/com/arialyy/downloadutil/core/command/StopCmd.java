package com.arialyy.downloadutil.core.command;

import android.util.Log;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.task.Task;

/**
 * Created by lyy on 2016/9/20.
 * 停止命令
 */
class StopCmd extends IDownloadCmd {

  /**
   * @param entity 下载实体
   */
  StopCmd(DownloadEntity entity) {
    super(entity);
  }

  @Override public void executeCmd() {
    Task task = mQueue.getTask(mEntity);
    if (task == null) {
      if (mEntity.getState() == DownloadEntity.STATE_DOWNLOAD_ING) {
        task = mQueue.createTask(mEntity);
        mQueue.stopTask(task);
      } else {
        Log.w(TAG, "停止命令执行失败，【调度器中没有该任务】");
      }
    } else {
      mQueue.stopTask(task);
    }
  }
}
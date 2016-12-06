package com.arialyy.aria.core.command;

import android.util.Log;
import com.arialyy.aria.core.DownloadEntity;
import com.arialyy.aria.core.task.Task;

/**
 * Created by lyy on 2016/11/30.
 * 获取任务状态命令
 */
class SingleCmd extends IDownloadCmd {
  /**
   * @param entity 下载实体
   */
  SingleCmd(Object target, DownloadEntity entity) {
    super(target, entity);
  }

  SingleCmd(DownloadEntity entity) {
    super(entity);
  }

  @Override public void executeCmd() {
    Task task = mQueue.getTask(mEntity);
    if (task == null) {
      task = mQueue.createTask(mTarget, mEntity);
    } else {
      Log.w(TAG, "添加命令执行失败，【该任务已经存在】");
    }
    task.setmTargetName(mTarget.getClass().getName());
    mQueue.startTask(task);
  }
}

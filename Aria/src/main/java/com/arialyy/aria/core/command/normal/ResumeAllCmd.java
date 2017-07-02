package com.arialyy.aria.core.command.normal;

import android.util.Log;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsNormalTask;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.orm.DbEntity;
import java.util.List;

/**
 * Created by AriaL on 2017/6/13.
 * 恢复所有停止的任务
 * 1.如果执行队列没有满，则开始下载任务，直到执行队列满
 * 2.如果队列执行队列已经满了，则将所有任务添加到等待队列中
 */
final class ResumeAllCmd<T extends AbsTaskEntity> extends AbsNormalCmd<T> {
  /**
   * @param targetName 产生任务的对象名
   */
  ResumeAllCmd(String targetName, T entity) {
    super(targetName, entity);
  }

  @Override public void executeCmd() {
    List<DownloadEntity> allEntity =
        DbEntity.findDatas(DownloadEntity.class, "state=?", IEntity.STATE_STOP + "");
    for (DownloadEntity entity : allEntity) {
      int exeNum = mQueue.getExePoolSize();
      if (exeNum == 0 || exeNum < mQueue.getMaxTaskNum()) {
        AbsNormalTask task = createTask(entity);
        mQueue.startTask(task);
      } else {
        entity.setState(IEntity.STATE_WAIT);
        createTask(entity);
      }
    }
  }

  private AbsNormalTask createTask(DownloadEntity entity) {
    AbsNormalTask task = (AbsNormalTask) mQueue.getTask(entity);
    if (task == null) {
      DownloadTaskEntity taskEntity = new DownloadTaskEntity(entity);
      task = (AbsNormalTask) mQueue.createTask(mTargetName, taskEntity);
    } else {
      Log.w(TAG, "添加命令执行失败，【该任务已经存在】");
    }
    return task;
  }
}

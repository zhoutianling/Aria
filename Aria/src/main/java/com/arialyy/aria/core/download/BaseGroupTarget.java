/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.core.download;

import android.text.TextUtils;
import com.arialyy.aria.core.manager.SubTaskManager;
import com.arialyy.aria.core.queue.DownloadGroupTaskQueue;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;
import java.io.File;
import java.util.List;

/**
 * Created by Aria.Lao on 2017/7/26.
 */
abstract class BaseGroupTarget<TARGET extends BaseGroupTarget>
    extends AbsDownloadTarget<TARGET, DownloadGroupEntity, DownloadGroupTaskEntity> {

  /**
   * 组任务名
   */
  String mGroupName;
  /**
   * 文件夹临时路径
   */
  protected String mDirPathTemp;

  private SubTaskManager mSubTaskManager;

  /**
   * 获取子任务管理器
   *
   * @return 子任务管理器
   */
  public SubTaskManager getSubTaskManager() {
    if (mSubTaskManager == null) {
      mSubTaskManager = new SubTaskManager(mTargetName, mTaskEntity);
    }
    return mSubTaskManager;
  }

  /**
   * 设置任务组别名
   */
  public TARGET setGroupAlias(String alias) {
    if (TextUtils.isEmpty(alias)) return (TARGET) this;
    mEntity.setAlias(alias);
    return (TARGET) this;
  }

  @Override public boolean taskExists() {
    return DownloadGroupTaskQueue.getInstance().getTask(mEntity.getGroupName()) != null;
  }

  /**
   * 设置任务组的文件夹路径，该api后续会删除
   *
   * @param groupDirPath 任务组保存文件夹路径
   * @deprecated {@link #setDirPath(String)} 请使用这个api
   */
  @Deprecated
  public TARGET setDownloadDirPath(String groupDirPath) {
    return setDirPath(groupDirPath);
  }

  /**
   * 设置任务组的文件夹路径，在Aria中，任务组的所有子任务都会下载到以任务组组名的文件夹中。
   * 如：groupDirPath = "/mnt/sdcard/download/group_test"
   * <pre>
   *   {@code
   *      + mnt
   *        + sdcard
   *          + download
   *            + group_test
   *              - task1.apk
   *              - task2.apk
   *              - task3.apk
   *              ....
   *
   *   }
   * </pre>
   *
   * @param dirPath 任务组保存文件夹路径
   */
  public TARGET setDirPath(String dirPath) {
    mDirPathTemp = dirPath;
    return (TARGET) this;
  }

  @Override public boolean isRunning() {
    DownloadGroupTask task = DownloadGroupTaskQueue.getInstance().getTask(mEntity.getKey());
    return task != null && task.isRunning();
  }

  /**
   * 改变任务组文件夹路径，修改文件夹路径会将子任务所有路径更换
   *
   * @param newDirPath 新的文件夹路径
   */
  private void reChangeDirPath(String newDirPath) {
    List<DownloadEntity> subTask = mEntity.getSubTask();
    if (subTask != null && !subTask.isEmpty()) {
      for (DownloadEntity entity : subTask) {
        String oldPath = entity.getDownloadPath();
        String newPath = newDirPath + "/" + entity.getFileName();
        File file = new File(oldPath);
        file.renameTo(new File(newPath));
        DbEntity.exeSql("UPDATE DownloadEntity SET downloadPath='"
            + newPath
            + "' WHERE downloadPath='"
            + oldPath
            + "'");
        DbEntity.exeSql(
            "UPDATE DownloadTaskEntity SET key='" + newPath + "' WHERE key='" + oldPath + "'");
      }
    }
  }

  /**
   * 检查并设置文件夹路径
   *
   * @return {@code true} 合法
   */
  boolean checkDirPath() {
    final String dirPath = mDirPathTemp;
    if (TextUtils.isEmpty(dirPath)) {
      ALog.e(TAG, "文件夹路径不能为null");
      return false;
    } else if (!dirPath.startsWith("/")) {
      ALog.e(TAG, "文件夹路径【" + dirPath + "】错误");
      return false;
    }
    File file = new File(dirPath);
    if (file.isFile()) {
      ALog.e(TAG, "路径【" + dirPath + "】是文件，请设置文件夹路径");
      return false;
    }

    if (!mEntity.getDirPath().equals(dirPath)) {
      if (!file.exists()) {
        file.mkdirs();
      }
      mEntity.setDirPath(dirPath);
      reChangeDirPath(dirPath);
    }

    return true;
  }
}

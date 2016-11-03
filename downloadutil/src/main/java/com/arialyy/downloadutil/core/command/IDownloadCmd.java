package com.arialyy.downloadutil.core.command;

import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.DownloadManager;
import com.arialyy.downloadutil.core.inf.ITaskQueue;
import com.arialyy.downloadutil.help.CheckHelp;
import com.arialyy.downloadutil.util.Util;

/**
 * Created by lyy on 2016/8/22.
 * 下载命令
 */
public abstract class IDownloadCmd {
  ITaskQueue     mQueue;
  DownloadEntity mEntity;
  String         TAG;

  /**
   * @param entity 下载实体
   */
  IDownloadCmd(DownloadEntity entity) {
    if (!CheckHelp.checkDownloadEntity(entity)) {
      return;
    }
    mEntity = entity;
    TAG = Util.getClassName(this);
    mQueue = DownloadManager.getInstance().getTaskQueue();
  }

  /**
   * 执行命令
   */
  public abstract void executeCmd();
}

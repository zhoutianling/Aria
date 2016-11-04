package com.arialyy.downloadutil.core.command;

import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.DownloadManager;
import com.arialyy.downloadutil.core.queue.ITaskQueue;
import com.arialyy.downloadutil.util.CheckUtil;
import com.arialyy.downloadutil.util.CommonUtil;

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
    if (!CheckUtil.checkDownloadEntity(entity)) {
      return;
    }
    mEntity = entity;
    TAG = CommonUtil.getClassName(this);
    mQueue = DownloadManager.getInstance().getTaskQueue();
  }

  /**
   * 执行命令
   */
  public abstract void executeCmd();
}

package com.arialyy.downloadutil.core.command;

import android.content.Context;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.DownloadTaskQueue;
import com.arialyy.downloadutil.help.CheckHelp;
import com.arialyy.downloadutil.util.Util;

/**
 * Created by lyy on 2016/8/22.
 * 下载命令
 */
public abstract class IDownloadCmd {
  DownloadTaskQueue target;
  Context           mContext;
  DownloadEntity    mEntity;
  String            TAG;

  /**
   * @param context context
   * @param entity 下载实体
   */
  protected IDownloadCmd(Context context, DownloadEntity entity) {
    if (!CheckHelp.checkDownloadEntity(entity)) {
      return;
    }
    target = DownloadTaskQueue.getInstance();
    mContext = context;
    mEntity = entity;
    TAG = Util.getClassName(this);
  }

  public Context getContext() {
    return mContext;
  }

  /**
   * 执行命令
   */
  public abstract void executeComment();

  /**
   * 设置下载器
   *
   * @param downloadTarget {@link DownloadTaskQueue}
   */
  public void setDownloadQueue(DownloadTaskQueue downloadTarget) {
    target = downloadTarget;
  }
}

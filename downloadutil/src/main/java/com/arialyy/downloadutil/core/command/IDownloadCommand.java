package com.arialyy.downloadutil.core.command;

import android.content.Context;
import com.arialyy.downloadutil.core.DownloadTarget;
import com.arialyy.downloadutil.core.IDownloadTarget;
import com.arialyy.downloadutil.entity.DownloadEntity;
import com.arialyy.downloadutil.help.CheckHelp;

/**
 * Created by lyy on 2016/8/22.
 * 下载命令
 */
public abstract class IDownloadCommand {
  protected IDownloadTarget target;
  protected Context mContext;
  protected DownloadEntity mEntity;

  /**
   * @param context context
   * @param entity 下载实体
   */
  protected IDownloadCommand(Context context, DownloadEntity entity) {
    if (!CheckHelp.checkDownloadEntity(entity)) {
      return;
    }
    target = DownloadTarget.getInstance(context);
    mContext = context;
    mEntity = entity;
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
   * @param downloadTarget {@link IDownloadTarget}
   */
  public void setDownloadTarget(IDownloadTarget downloadTarget) {
    target = downloadTarget;
  }
}

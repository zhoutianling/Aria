package com.arialyy.downloadutil.core.command;

import android.content.Context;
import com.arialyy.downloadutil.core.DownloadTarget;
import com.arialyy.downloadutil.core.IDownloadTarget;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.help.CheckHelp;
import com.arialyy.downloadutil.util.Util;

/**
 * Created by lyy on 2016/8/22.
 * 下载命令
 */
public abstract class IDownloadCmd {
  protected IDownloadTarget target;
  protected Context         mContext;
  protected DownloadEntity  mEntity;
  protected String          TAG;

  /**
   * @param context context
   * @param entity 下载实体
   */
  protected IDownloadCmd(Context context, DownloadEntity entity) {
    if (!CheckHelp.checkDownloadEntity(entity)) {
      return;
    }
    target = DownloadTarget.getInstance();
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
   * @param downloadTarget {@link IDownloadTarget}
   */
  public void setDownloadTarget(IDownloadTarget downloadTarget) {
    target = downloadTarget;
  }
}

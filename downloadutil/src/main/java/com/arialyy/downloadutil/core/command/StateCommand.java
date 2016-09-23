package com.arialyy.downloadutil.core.command;

import android.content.Context;
import com.arialyy.downloadutil.entity.DownloadEntity;

/**
 * Created by lyy on 2016/9/20.
 * 获取下载状态的命令
 */
public class StateCommand extends IDownloadCommand {

  /**
   * @param context context
   * @param entity 下载实体
   */
  protected StateCommand(Context context, DownloadEntity entity) {
    super(context, entity);
  }

  @Override public void executeComment() {
    target.getTaskState(mEntity);
  }
}

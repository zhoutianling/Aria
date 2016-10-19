package com.arialyy.downloadutil.core.command;

import android.content.Context;
import com.arialyy.downloadutil.core.DownloadEntity;

/**
 * Created by lyy on 2016/9/20.
 * 获取下载状态的命令
 */
class StateCommand extends IDownloadCommand {

  /**
   * @param context context
   * @param entity 下载实体
   */
  StateCommand(Context context, DownloadEntity entity) {
    super(context, entity);
  }

  @Override public void executeComment() {

    target.getTaskState(mEntity);
  }
}

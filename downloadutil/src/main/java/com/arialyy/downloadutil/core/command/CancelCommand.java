package com.arialyy.downloadutil.core.command;

import android.content.Context;
import com.arialyy.downloadutil.entity.DownloadEntity;

/**
 * Created by lyy on 2016/9/20.
 * 取消命令
 */
class CancelCommand extends IDownloadCommand {

  CancelCommand(Context context, DownloadEntity entity) {
    super(context, entity);
  }

  @Override public void executeComment() {
    target.cancelTask(target.getTask(mEntity));
  }
}

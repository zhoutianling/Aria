package com.arialyy.downloadutil.core.command;

import android.content.Context;

import com.arialyy.downloadutil.entity.DownloadEntity;

/**
 * Created by lyy on 2016/8/22.
 * 开始命令
 */
class StartCommand extends IDownloadCommand {

    StartCommand(Context context, DownloadEntity entity) {
        super(context, entity);
    }

    @Override public void executeComment() {
        target.startTask(target.getTask(mEntity));
    }
}

package com.arialyy.downloadutil.core.command;

import android.content.Context;
import com.arialyy.downloadutil.entity.DownloadEntity;

/**
 * Created by lyy on 2016/8/22.
 * 添加任务的命令
 */
class AddCommand extends IDownloadCommand {

    AddCommand(Context context, DownloadEntity entity) {
        super(context, entity);
    }

    @Override
    public void executeComment() {
        target.createTask(mEntity);
    }
}

package com.arialyy.downloadutil.core.command;

import android.content.Context;
import android.util.Log;

import com.arialyy.downloadutil.util.Task;
import com.arialyy.downloadutil.entity.DownloadEntity;

/**
 * Created by lyy on 2016/8/22.
 * 添加任务的命令
 */
class AddCommand extends IDownloadCommand {

    AddCommand(Context context, DownloadEntity entity) {
        super(context, entity);
    }

    @Override public void executeComment() {
        Task task = target.getTask(mEntity);
        if (task == null) {
            target.createTask(mEntity);
        }else {
            Log.w(TAG, "添加命令执行失败，【该任务已经存在】");
        }
    }
}

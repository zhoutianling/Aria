package com.arialyy.downloadutil.core.command;

import android.support.annotation.NonNull;

import com.arialyy.downloadutil.core.IDownloadTarget;
import com.arialyy.downloadutil.core.Task;

/**
 * Created by lyy on 2016/8/22.
 * 开始命令
 */
public class StartCommand extends IDownloadCommand{
    public StartCommand(@NonNull IDownloadTarget target) {
        super(target);
    }

    @Override
    public void executeComment() {
//        target.startTask();
    }
}

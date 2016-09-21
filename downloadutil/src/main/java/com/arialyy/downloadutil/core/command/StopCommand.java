package com.arialyy.downloadutil.core.command;

import com.arialyy.downloadutil.core.IDownloadTarget;

/**
 * Created by lyy on 2016/9/20.
 * 停止命令
 */
public class StopCommand extends IDownloadCommand {
    public StopCommand(IDownloadTarget target) {
        super(target);
    }

    @Override
    public void executeComment() {

    }
}

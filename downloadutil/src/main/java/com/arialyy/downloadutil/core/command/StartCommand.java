package com.arialyy.downloadutil.core.command;

import com.arialyy.downloadutil.core.IDownloadTarget;

/**
 * Created by lyy on 2016/8/22.
 * 开始命令
 */
public class StartCommand extends IDownloadCommand{
    public StartCommand(IDownloadTarget target) {
        super(target);
    }

    @Override
    public void executeComment() {

    }
}

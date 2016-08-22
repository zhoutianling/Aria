package com.arialyy.downloadutil.core.command;

import com.arialyy.downloadutil.core.IDownloadTarget;

/**
 * Created by lyy on 2016/8/22.
 * 添加任务的命令
 */
public class AddCommand extends IDownloadCommand {
    public AddCommand(IDownloadTarget target) {
        super(target);
    }

    @Override
    public void executeComment() {

    }
}

package com.arialyy.downloadutil.core.command;

import com.arialyy.downloadutil.core.IDownloadTarget;

/**
 * Created by lyy on 2016/9/20.
 * 获取下载状态的命令
 */
public class GetStateCommand extends IDownloadCommand {
    public GetStateCommand(IDownloadTarget target) {
        super(target);
    }

    @Override
    public void executeComment() {

    }
}

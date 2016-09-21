package com.arialyy.downloadutil.core.command;

import com.arialyy.downloadutil.core.IDownloadTarget;

/**
 * Created by lyy on 2016/9/20.
 * 取消命令
 */
public class CancelCommand extends IDownloadCommand{
    public CancelCommand(IDownloadTarget target) {
        super(target);
    }

    @Override
    public void executeComment() {

    }
}

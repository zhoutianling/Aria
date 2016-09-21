package com.arialyy.downloadutil.core.command;

import android.support.annotation.NonNull;

import com.arialyy.downloadutil.core.DownloadTarget;
import com.arialyy.downloadutil.core.IDownloadTarget;

import java.util.List;

/**
 * Created by lyy on 2016/8/22.
 * 下载命令
 */
public abstract class IDownloadCommand {
    protected IDownloadTarget target;

    public IDownloadCommand(@NonNull IDownloadTarget target) {
        this.target = target;
    }

    /**
     * 执行命令
     */
    public abstract void executeComment();

    /**
     * 设置下载器
     *
     * @param downloadTarget {@link IDownloadTarget}
     */
    public void setDownloadTarget(IDownloadTarget downloadTarget) {
        target = downloadTarget;
    }


}

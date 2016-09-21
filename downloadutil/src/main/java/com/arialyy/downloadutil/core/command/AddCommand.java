package com.arialyy.downloadutil.core.command;

import android.support.annotation.NonNull;

import com.arialyy.downloadutil.core.IDownloadTarget;

/**
 * Created by lyy on 2016/8/22.
 * 添加任务的命令
 */
public class AddCommand extends IDownloadCommand {
    String mDownloadUrl, mDownloadPath;

    /**
     *
     * @param target        下载调度器
     * @param downloadUrl   下载链接
     * @param downloadPath  文件保存地址
     */
    public AddCommand(@NonNull IDownloadTarget target, String downloadUrl, String downloadPath) {
        super(target);
        mDownloadUrl = downloadUrl;
        mDownloadPath = downloadPath;
    }

    @Override
    public void executeComment() {
        target.createTask(mDownloadUrl, mDownloadPath);
    }
}

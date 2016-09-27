package com.arialyy.downloadutil.core.inf;

import com.arialyy.downloadutil.core.Task;

/**
 * Created by lyy on 2016/8/16.
 * 下载功能接口
 */
public interface IDownloader {
    /**
     * 开始任务
     *
     * @param task {@link Task}
     */
    public void startTask(Task task);

    /**
     * 停止任务
     *
     * @param task {@link Task}
     */
    public void stopTask(Task task);

    /**
     * 取消任务
     *
     * @param task {@link Task}
     */
    public void cancelTask(Task task);

    /**
     * 重试下载
     *
     * @param task {@link Task}
     */
    public void reTryStart(Task task);
}

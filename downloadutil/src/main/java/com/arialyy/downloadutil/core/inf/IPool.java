package com.arialyy.downloadutil.core.inf;

import com.arialyy.downloadutil.core.Task;

/**
 * Created by lyy on 2016/8/14.
 * 任务池
 */
public interface IPool {
    /**
     * 将下载任务添加到任务池中
     *
     * @param task
     */
    public void putTask(Task task);

    /**
     * 通过下载链接获取下载任务
     *
     * @param downloadUrl 下载链接
     * @return 下载任务
     */
    public Task getTask(String downloadUrl);

    /**
     * 删除任务池中的下载任务
     *
     * @param task 下载任务
     * @return true:移除成功
     */
    public boolean removeTask(Task task);
}

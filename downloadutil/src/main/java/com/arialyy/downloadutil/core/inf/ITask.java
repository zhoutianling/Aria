package com.arialyy.downloadutil.core.inf;

import com.arialyy.downloadutil.core.Task;

/**
 * Created by lyy on 2016/8/16.
 * 任务功能接口
 */
public interface ITask {

    /**
     * 创建一个新的下载任务，创建时只是将新任务存储到缓存池
     *
     * @param downloadUrl  下载链接
     * @param downloadPath 保存路径
     */
    public void createTask(String downloadUrl, String downloadPath);

    /**
     * 通过下载链接从缓存池或任务池搜索下载任务
     *
     * @param downloadUrl 下载链接
     * @return {@link Task}
     */
    public Task getTask(String downloadUrl);

    /**
     * 通过下载链接搜索下载任务
     *
     * @param downloadUrl 下载链接
     * @return {@code -1 ==> 错误}，{@link com.arialyy.downloadutil.entity.DownloadEntity#STATE_FAIL}
     */
    public int getTaskState(String downloadUrl);

    /**
     * 通过下载链接删除任务
     *
     * @param downloadUrl 下载链接
     */
    public void removeTask(String downloadUrl);

    /**
     * 获取缓存池的下一个任务
     *
     * @return 下载任务 or null
     */
    public Task getNextTask();

}

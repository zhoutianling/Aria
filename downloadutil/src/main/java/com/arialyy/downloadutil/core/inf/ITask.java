package com.arialyy.downloadutil.core.inf;

import com.arialyy.downloadutil.core.Task;

/**
 * Created by lyy on 2016/8/16.
 * 任务功能接口
 */
public interface ITask {

    /**
     * 创建一个新的下载任务
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
    public Task searchTask(String downloadUrl);

    /**
     * 通过下载链接搜索下载任务
     *
     * @param downloadUrl 下载链接
     * @return {@link com.arialyy.downloadutil.entity.DownloadEntity#STATE_FAIL}
     */
    public int getTaskState(String downloadUrl);
}

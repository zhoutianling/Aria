package com.arialyy.downloadutil.core.inf;

import com.arialyy.downloadutil.core.Task;
import com.arialyy.downloadutil.entity.DownloadEntity;

/**
 * Created by lyy on 2016/8/16.
 * 任务功能接口
 */
public interface ITask {

    /**
     * 创建一个新的下载任务，创建时只是将新任务存储到缓存池
     *
     * @param entity 下载实体{@link DownloadEntity}
     * @return {@link Task}
     */
    public Task createTask(DownloadEntity entity);

    /**
     * 通过下载链接从缓存池或任务池搜索下载任务，如果缓存池或任务池都没有任务，则创建新任务
     *
     * @param entity 下载实体{@link DownloadEntity}
     * @return {@link Task}
     */
    public Task getTask(DownloadEntity entity);

    /**
     * 通过下载链接搜索下载任务
     *
     * @param entity 下载实体{@link DownloadEntity}
     * @return {@code -1 ==> 错误}，{@link com.arialyy.downloadutil.entity.DownloadEntity#STATE_FAIL}
     */
    public int getTaskState(DownloadEntity entity);

    /**
     * 通过下载链接删除任务
     *
     * @param entity 下载实体{@link DownloadEntity}
     */
    public void removeTask(DownloadEntity entity);

    /**
     * 获取缓存池的下一个任务
     *
     * @return 下载任务 or null
     */
    public Task getNextTask();
}

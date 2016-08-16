package com.arialyy.downloadutil.core;

import com.arialyy.downloadutil.core.inf.IDownloader;
import com.arialyy.downloadutil.core.inf.ITask;
import com.arialyy.downloadutil.core.pool.CachePool;
import com.arialyy.downloadutil.core.pool.ExecutePool;

/**
 * Created by lyy on 2016/8/16.
 * 任务下载器，提供抽象的方法供具体的实现类操作
 */
public abstract class DownloadTarget implements IDownloader, ITask {
    protected CachePool   mCachePool   = CachePool.getInstance();
    protected ExecutePool mExecutePool = ExecutePool.getInstance();

    /**
     * 获取当前运行的任务数
     *
     * @return 当前正在执行的任务数
     */
    public int getCurrentTaskNum() {
        return mExecutePool.size();
    }

    /**
     * 获取缓存任务数
     *
     * @return 获取缓存的任务数
     */
    public int getCacheTaskNum() {
        return mCachePool.size();
    }
}

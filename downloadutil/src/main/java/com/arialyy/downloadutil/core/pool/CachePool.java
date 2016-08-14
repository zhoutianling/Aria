package com.arialyy.downloadutil.core.pool;

import com.arialyy.downloadutil.core.Task;
import com.arialyy.downloadutil.core.inf.IPool;

/**
 * Created by lyy on 2016/8/14.
 * 任务缓存池，所有下载任务最先缓存在这个池中
 */
public class CachePool implements IPool {


    @Override
    public synchronized void putTask(Task task) {

    }

    @Override
    public synchronized Task getTask(String downloadUrl) {
        return null;
    }

    @Override
    public synchronized boolean removeTask(Task task) {
        return false;
    }
}

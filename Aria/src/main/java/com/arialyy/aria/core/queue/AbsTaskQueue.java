package com.arialyy.aria.core.queue;

import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.ITask;
import com.arialyy.aria.core.inf.ITaskEntity;
import com.arialyy.aria.core.queue.pool.CachePool;
import com.arialyy.aria.core.queue.pool.ExecutePool;

/**
 * Created by Aria.Lao on 2017/2/23.
 */
abstract class AbsTaskQueue<TASK extends ITask, TASK_ENTITY extends ITaskEntity, ENTITY extends IEntity>
    implements ITaskQueue<TASK, TASK_ENTITY, ENTITY> {
  CachePool<DownloadTask> mCachePool = new CachePool<>();
  ExecutePool<DownloadTask> mExecutePool = new ExecutePool<>();
}

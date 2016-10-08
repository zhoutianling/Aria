package com.arialyy.downloadutil.core.pool;

import android.text.TextUtils;
import android.util.Log;

import com.arialyy.downloadutil.util.Task;
import com.arialyy.downloadutil.core.inf.IPool;
import com.arialyy.downloadutil.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by lyy on 2016/8/15.
 * 任务执行池，所有当前下载任务都该任务池中，默认下载大小为2
 */
public class ExecutePool implements IPool {
    private static final    String      TAG      = "ExecutePool";
    private static final    Object      LOCK     = new Object();
    private static final    long        TIME_OUT = 1000;
    private static volatile ExecutePool INSTANCE = null;
    private static          int         SIZE     = 2;
    private ArrayBlockingQueue<Task> mExecuteQueue;
    private Map<String, Task>        mExecuteArray;

    public static ExecutePool getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                INSTANCE = new ExecutePool();
            }
        }
        return INSTANCE;
    }

    private ExecutePool() {
        mExecuteQueue = new ArrayBlockingQueue<>(SIZE);
        mExecuteArray = new HashMap<>();
    }

    @Override public boolean putTask(Task task) {
        synchronized (LOCK) {
            if (task == null) {
                Log.e(TAG, "下载任务不能为空！！");
                return false;
            }
            String url = task.getDownloadEntity().getDownloadUrl();
            if (mExecuteQueue.contains(task)) {
                Log.e(TAG, "队列中已经包含了该任务，任务下载链接【" + url + "】");
                return false;
            } else {
                if (mExecuteQueue.size() >= SIZE) {
                    if (pollFirstTask()) {
                        return putNewTask(task);
                    }
                } else {
                    return putNewTask(task);
                }
            }
        }
        return false;
    }

    /**
     * 添加新任务
     *
     * @param newTask 新下载任务
     */
    private boolean putNewTask(Task newTask) {
        String  url = newTask.getDownloadEntity().getDownloadUrl();
        boolean s   = mExecuteQueue.offer(newTask);
        Log.w(TAG, "任务添加" + (s ? "成功" : "失败，【" + url + "】"));
        if (s) {
            newTask.start();
            mExecuteArray.put(Util.keyToHashKey(url), newTask);
        }
        return s;
    }

    /**
     * 队列满时，将移除下载队列中的第一个任务
     */
    private boolean pollFirstTask() {
        try {
            Task oldTask = mExecuteQueue.poll(TIME_OUT, TimeUnit.MICROSECONDS);
            if (oldTask == null) {
                Log.e(TAG, "移除任务失败");
                return false;
            }
            oldTask.stop();
//            wait(200);
            String key = Util.keyToHashKey(oldTask.getDownloadEntity().getDownloadUrl());
            mExecuteArray.remove(key);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override public Task pollTask() {
        synchronized (LOCK) {
            Task task = mExecuteQueue.poll();
            if (task != null) {
                String url = task.getDownloadEntity().getDownloadUrl();
                mExecuteArray.remove(Util.keyToHashKey(url));
            }
            return task;
        }
    }

    @Override public Task getTask(String downloadUrl) {
        synchronized (LOCK) {
            if (TextUtils.isEmpty(downloadUrl)) {
                Log.e(TAG, "请传入有效的下载链接");
                return null;
            }
            String key = Util.keyToHashKey(downloadUrl);
            return mExecuteArray.get(key);
        }
    }

    @Override public boolean removeTask(Task task) {
        synchronized (LOCK) {
            if (task == null) {
                Log.e(TAG, "任务不能为空");
                return false;
            } else {
                String key = Util.keyToHashKey(task.getDownloadEntity().getDownloadUrl());
                mExecuteArray.remove(key);
                return mExecuteQueue.remove(task);
            }
        }
    }

    @Override public boolean removeTask(String downloadUrl) {
        synchronized (LOCK) {
            if (TextUtils.isEmpty(downloadUrl)) {
                Log.e(TAG, "请传入有效的下载链接");
                return false;
            }
            String key  = Util.keyToHashKey(downloadUrl);
            Task   task = mExecuteArray.get(key);
            mExecuteArray.remove(key);
            return mExecuteQueue.remove(task);
        }
    }

    @Override public int size() {
        return mExecuteQueue.size();
    }
}

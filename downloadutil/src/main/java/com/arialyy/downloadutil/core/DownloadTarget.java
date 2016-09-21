package com.arialyy.downloadutil.core;

import android.content.Context;
import android.util.Log;

import com.arialyy.downloadutil.entity.DownloadEntity;

/**
 * Created by lyy on 2016/8/17.
 * 下载任务调度类
 */
public class DownloadTarget extends IDownloadTarget {
    private static final    String         TAG      = "DownloadTarget";
    private static final    Object         LOCK     = new Object();
    private static volatile DownloadTarget INSTANCE = null;
    private Context mContext;

    public static DownloadTarget getInstance(Context context) {
//        if (INSTANCE == null) {
//            Log.e(TAG, "请在Application中调用DownloadTarget.init()方法注册下载器");
//            return null;
//        }
        if (INSTANCE == null) {
            synchronized (LOCK) {
                INSTANCE = new DownloadTarget(context.getApplicationContext());
            }
        }
        return INSTANCE;
    }
//
//    /**
//     * 初始化下载器
//     *
//     * @param context 全局Context
//     */
//    public static void init(Context context) {
//        if (INSTANCE == null) {
//            synchronized (LOCK) {
//                INSTANCE = new DownloadTarget(context.getApplicationContext());
//            }
//        }
//    }

    private DownloadTarget() {
        super();
    }

    private DownloadTarget(Context context) {
        super();
        mContext = context;
    }

    @Override
    public void startTask(Task task) {
        if (mExecutePool.putTask(task)) {
            task.start();
        }
    }

    @Override
    public void stopTask(Task task) {
        if (mExecutePool.removeTask(task)) {
            task.stop();
        }
    }

    @Override
    public void cancelTask(Task task) {
        if (mExecutePool.removeTask(task)) {
            task.cancel();
        }
    }

    @Override
    public void reTryStart(Task task) {
        if (!task.getDownloadUtil().isDownloading()) {
            task.start();
        } else {
            Log.w(TAG, "任务没有完全停止，重试下载失败");
        }
    }

    @Override
    public void createTask(String downloadUrl, String downloadPath) {
        DownloadEntity entity = new DownloadEntity();
        entity.setDownloadUrl(downloadUrl);
        entity.setDownloadPath(downloadPath);
        Task task = TaskFactory.getInstance().createTask(mContext, entity, mTaskHandler);
        mCachePool.putTask(task);
    }

    @Override
    public Task getTask(String downloadUrl) {
        Task task = mCachePool.getTask(downloadUrl);
        if (task == null) {
            task = mExecutePool.getTask(downloadUrl);
        }

        return task;
    }

    @Override
    public int getTaskState(String downloadUrl) {
        Task task = getTask(downloadUrl);
        if (task == null) {
            Log.e(TAG, "没有找到下载链接为【" + downloadUrl + "】的下载任务");
            return -1;
        }
        return task.getDownloadEntity().getState();
    }

    @Override
    public void removeTask(String downloadUrl) {
        Task task = mCachePool.getTask(downloadUrl);
        if (task != null) {
            Log.d(TAG, "任务删除" + (mCachePool.removeTask(task) ? "成功" : "失败"));
        } else {
            task = mExecutePool.getTask(downloadUrl);
        }
        if (task != null) {
            Log.d(TAG, "任务删除" + (mCachePool.removeTask(task) ? "成功" : "失败"));
        } else {
            Log.w(TAG, "没有找到下载链接为【" + downloadUrl + "】的任务");
        }
    }

    @Override
    public Task getNextTask() {
        return mCachePool.pollTask();
    }
}

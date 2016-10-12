package com.arialyy.downloadutil.core;

import android.content.Context;
import android.os.Handler;

import com.arialyy.downloadutil.entity.DownloadEntity;
import com.arialyy.downloadutil.util.Task;

/**
 * Created by lyy on 2016/8/18.
 * 任务工厂
 */
public class TaskFactory {
    private static final String TAG = "TaskFactory";

    private static final    Object      LOCK     = new Object();
    private static volatile TaskFactory INSTANCE = null;

    private TaskFactory() {

    }

    public static TaskFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                INSTANCE = new TaskFactory();
            }
        }
        return INSTANCE;
    }

    /**
     * 创建普通下载任务
     *
     * @param entity  下载实体
     * @param handler "com.arialyy.downloadutil.core.IDownloadTarget.AutoTaskHandler"
     */
    public Task createTask(Context context, DownloadEntity entity, Handler handler) {
        Task.Builder builder = new Task.Builder(context, entity);
        builder.setOutHandler(handler);
        return builder.build();
    }
}

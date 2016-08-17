package com.arialyy.downloadutil.core;

/**
 * Created by lyy on 2016/8/17.
 * 下载任务调度类
 */
public class DownloadTarget extends IDownloadTarget {

    private static final    Object         LOCK     = new Object();
    private static volatile DownloadTarget INSTANCE = null;

    public static DownloadTarget getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                INSTANCE = new DownloadTarget();
            }
        }
        return INSTANCE;
    }

    private DownloadTarget() {

    }

    @Override
    public void startTask(Task task) {

    }

    @Override
    public void stopTask(Task task) {

    }

    @Override
    public void cancelTask(Task task) {

    }

    @Override
    public void reTryStart(Task task) {

    }

    @Override
    public void createTask(String downloadUrl, String downloadPath) {

    }

    @Override
    public Task getTask(String downloadUrl) {
        return null;
    }

    @Override
    public int getTaskState(String downloadUrl) {
        return 0;
    }

    @Override
    public void removeTask(String downloadUrl) {

    }

    @Override
    public Task getNextTask() {
        return null;
    }
}

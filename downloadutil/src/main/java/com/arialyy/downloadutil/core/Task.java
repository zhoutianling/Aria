package com.arialyy.downloadutil.core;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.arialyy.downloadutil.DownloadManager;
import com.arialyy.downloadutil.entity.DownloadEntity;
import com.arialyy.downloadutil.inf.IDownloadListener;
import com.arialyy.downloadutil.util.DownLoadUtil;
import com.arialyy.downloadutil.util.SQLHelper;

import java.net.HttpURLConnection;

/**
 * Created by lyy on 2016/8/11.
 * 下载任务类
 */
public class Task {
    public static final String TAG = "Task";
    DownloadEntity    downloadEntity;
    IDownloadListener listener;
    Handler           outHandler;
    Context           context;
    DownLoadUtil      util;
    SQLHelper         sqlHelper;

    private Task() {
        util = new DownLoadUtil();
    }

    /**
     * 开始下载
     */
    public void start() {
        if (util.isDownloading()) {
            Log.d(TAG, "任务正在下载");
        } else {
            if (listener == null) {
                listener = new DownloadListener(context, downloadEntity, outHandler);
            }
            util.download(context, downloadEntity.getDownloadUrl(), downloadEntity.getDownloadPath(), listener);
        }
    }

    /**
     * 停止下载
     */
    public void stop() {
        if (util.isDownloading()) {
            util.stopDownload();
        }
    }

    /**
     * 取消下载
     */
    public void cancel() {
        util.cancelDownload();
    }

    /**
     * 存储下载实体
     */
    private void saveDownloadEntity(DownloadEntity entity) {

    }

    /**
     * 下载监听类
     */
    static class DownloadListener extends DownLoadUtil.DownloadListener {
        Handler outHandler;
        Context context;
        Intent  sendIntent;
        long INTERVAL = 1024 * 10;   //10k大小的间隔
        long lastLen  = 0;   //上一次发送长度
        DownloadEntity downloadEntity;

        public DownloadListener(Context context, DownloadEntity downloadEntity, Handler outHandler) {
            this.context = context;
            this.outHandler = outHandler;
            this.downloadEntity = downloadEntity;
            sendIntent = new Intent();
            sendIntent.addCategory(context.getPackageName());
        }

        @Override
        public void onPreDownload(HttpURLConnection connection) {
            super.onPreDownload(connection);
            long   len       = connection.getContentLength();
            Intent preIntent = new Intent();
            preIntent.addCategory(context.getPackageName());
            downloadEntity.setFileSize(len);
        }

        @Override
        public void onResume(long resumeLocation) {
            super.onResume(resumeLocation);
        }

        @Override
        public void onStart(long startLocation) {
            super.onStart(startLocation);
        }

        @Override
        public void onProgress(long currentLocation) {
            super.onProgress(currentLocation);
            if (currentLocation - lastLen > INTERVAL) { //不要太过于频繁发送广播
                sendIntent.putExtra(DownloadManager.ACTION_RUNNING, currentLocation);
                lastLen = currentLocation;
            }
        }

        @Override
        public void onStop(long stopLocation) {
            super.onStop(stopLocation);
        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

        @Override
        public void onComplete() {
            super.onComplete();
        }

        @Override
        public void onFail() {
            super.onFail();
        }
    }

    public static class Builder {
        DownloadEntity    downloadEntity;
        IDownloadListener listener;
        Handler           outHandler;
        Context           context;

        public Builder(Context context, DownloadEntity downloadEntity) {
            this.context = context;
            this.downloadEntity = downloadEntity;
        }

        public Builder setDownloadListener(IDownloadListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setOutHandler(Handler outHandler) {
            this.outHandler = outHandler;
            return this;
        }

        public Task builder() {
            Task task = new Task();
            task.context = context;
            task.downloadEntity = downloadEntity;
            task.listener = listener;
            task.outHandler = outHandler;
            return task;
        }
    }

}

package com.arialyy.downloadutil.core;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.arialyy.downloadutil.DownloadManager;
import com.arialyy.downloadutil.inf.IDownloadListener;
import com.arialyy.downloadutil.util.DownLoadUtil;

import java.net.HttpURLConnection;

/**
 * Created by lyy on 2016/8/11.
 * 下载任务类
 */
public class Task {
    public static final String TAG = "Task";
    /**
     * 下载路径
     */
    String            downloadUrl;
    /**
     * 保存路径
     */
    String            downloadPath;
    /**
     * 下载监听
     */
    IDownloadListener listener;
    Handler           outHandler;
    Context           context;
    DownLoadUtil      util;

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
                listener = new DownloadListener(context, outHandler);
            }
            util.download(context, downloadUrl, downloadPath, listener);
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
     * 下载监听类
     */
    static class DownloadListener extends DownLoadUtil.DownloadListener {
        Handler outHandler;
        Context context;
        Intent  sendIntent;
        long INTERVAL = 1024 * 10;   //10k大小的间隔
        long lastLen = 0;   //上一次发送长度

        public DownloadListener(Context context, Handler outHandler) {
            this.context = context;
            this.outHandler = outHandler;
            sendIntent = new Intent();
            sendIntent.addCategory(context.getPackageName());
        }

        @Override
        public void onPreDownload(HttpURLConnection connection) {
            super.onPreDownload(connection);
            long len = connection.getContentLength();
            Intent preIntent = new Intent();
            preIntent.addCategory(context.getPackageName());

        }

        @Override
        public void onResume(long resumeLocation) {
            super.onResume(resumeLocation);
            sendIntent.putExtra(DownloadManager.RESUME_LOCATION, resumeLocation);
        }

        @Override
        public void onStart(long startLocation) {
            super.onStart(startLocation);
            sendIntent.putExtra(DownloadManager.START_LOCATION, startLocation);
        }

        @Override
        public void onProgress(long currentLocation) {
            super.onProgress(currentLocation);
            if (currentLocation - lastLen > INTERVAL) { //不要太过于频繁发送广播
                sendIntent.putExtra("progress", currentLocation);
                sendBroadcast(sendIntent);
                lastLen = currentLocation;
            }
        }

        @Override
        public void onStop(long stopLocation) {
            super.onStop(stopLocation);
            sendIntent.putExtra(DownloadManager.STOP_LOCATION, stopLocation);
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
        String            downloadUrl;
        String            downloadPath;
        IDownloadListener listener;
        Handler           outHandler;
        Context           context;

        public Builder(Context context, String downloadUrl, String downloadPath) {
            this.context = context;
            this.downloadUrl = downloadUrl;
            this.downloadPath = downloadPath;
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
            task.downloadUrl = downloadUrl;
            task.downloadPath = downloadPath;
            task.listener = listener;
            task.outHandler = outHandler;
            return task;
        }

    }

}

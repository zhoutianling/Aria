package com.arialyy.downloadutil.core;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.arialyy.downloadutil.entity.DownloadEntity;
import com.arialyy.downloadutil.util.DownLoadUtil;
import com.arialyy.downloadutil.util.IDownloadListener;

import java.net.HttpURLConnection;

/**
 * Created by lyy on 2016/8/11.
 * 下载任务类
 */
public class Task {
    public static final String TAG = "Task";

    private DownloadEntity    downloadEntity;
    private IDownloadListener listener;
    private Handler           outHandler;
    private Context           context;
    private DownLoadUtil      util;

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
            util.download(context, downloadEntity.getDownloadUrl(),
                          downloadEntity.getDownloadPath(), listener);
        }
    }

    public DownloadEntity getDownloadEntity() {
        return downloadEntity;
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
     * 获取下载工具
     */
    public DownLoadUtil getDownloadUtil() {
        return util;
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
    private static class DownloadListener extends DownLoadUtil.DownloadListener {
        Handler outHandler;
        Context context;
        Intent  sendIntent;
        long INTERVAL = 1024 * 10;   //10k大小的间隔
        long lastLen  = 0;   //上一次发送长度
        DownloadEntity downloadEntity;

        public DownloadListener(Context context, DownloadEntity downloadEntity,
                                Handler outHandler) {
            this.context = context;
            this.outHandler = outHandler;
            this.downloadEntity = downloadEntity;
            sendIntent = new Intent(DownloadManager.ACTION_RUNNING);
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(context.getPackageName());
            Uri uri = builder.build();
            sendIntent.setData(uri);

        }

        @Override public void onPreDownload(HttpURLConnection connection) {
            super.onPreDownload(connection);
            long len = connection.getContentLength();
            downloadEntity.setFileSize(len);
            downloadEntity.setState(DownloadEntity.STATE_DOWNLOAD_ING);
            sendIntent(DownloadManager.ACTION_PRE, -1);
        }

        @Override public void onResume(long resumeLocation) {
            super.onResume(resumeLocation);
            downloadEntity.setState(DownloadEntity.STATE_DOWNLOAD_ING);
            sendIntent(DownloadManager.ACTION_RESUME, resumeLocation);
        }

        @Override public void onStart(long startLocation) {
            super.onStart(startLocation);
            downloadEntity.setState(DownloadEntity.STATE_DOWNLOAD_ING);
            sendInState2Target(IDownloadTarget.START);
            sendIntent(DownloadManager.ACTION_START, startLocation);
        }

        @Override public void onProgress(long currentLocation) {
            super.onProgress(currentLocation);
            if (currentLocation - lastLen > INTERVAL) { //不要太过于频繁发送广播
                sendIntent.putExtra(DownloadManager.ACTION_RUNNING, currentLocation);
                lastLen = currentLocation;
                context.sendBroadcast(sendIntent);
            }
        }

        @Override public void onStop(long stopLocation) {
            super.onStop(stopLocation);
            downloadEntity.setState(DownloadEntity.STATE_STOP);
            sendInState2Target(IDownloadTarget.STOP);
            sendIntent(DownloadManager.ACTION_STOP, stopLocation);
        }

        @Override public void onCancel() {
            super.onCancel();
            downloadEntity.setState(DownloadEntity.STATE_CANCEL);
            sendInState2Target(IDownloadTarget.CANCEL);
            sendIntent(DownloadManager.ACTION_CANCEL, -1);
            downloadEntity.deleteData();
        }

        @Override public void onComplete() {
            super.onComplete();
            downloadEntity.setState(DownloadEntity.STATE_COMPLETE);
            downloadEntity.setDownloadComplete(true);
            sendInState2Target(IDownloadTarget.COMPLETE);
            sendIntent(DownloadManager.ACTION_COMPLETE, -1);
        }

        @Override public void onFail() {
            super.onFail();
            downloadEntity.setState(DownloadEntity.STATE_FAIL);
            sendInState2Target(IDownloadTarget.FAIL);
            sendIntent(DownloadManager.ACTION_FAIL, -1);
        }

        /**
         * 将任务状态发送给下载器
         *
         * @param state {@link IDownloadTarget#START}
         */
        private void sendInState2Target(int state) {
            if (outHandler != null) {
                outHandler.obtainMessage(state, downloadEntity).sendToTarget();
            }
        }

        private void sendIntent(String action, long location) {
            downloadEntity.save();
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(context.getPackageName());
            Uri    uri    = builder.build();
            Intent intent = new Intent(action);
            intent.setData(uri);
            intent.putExtra(action, downloadEntity);
            if (location != -1) {
                intent.putExtra(DownloadManager.CURRENT_LOCATION, location);
            }
            context.sendBroadcast(intent);
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

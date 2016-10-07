package com.arialyy.downloadutil.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.arialyy.downloadutil.core.DownloadManager;
import com.arialyy.downloadutil.core.IDownloadTarget;
import com.arialyy.downloadutil.entity.DownloadEntity;

import java.net.HttpURLConnection;

/**
 * Created by lyy on 2016/8/11.
 * 下载任务类
 */
public class Task {
    public static final String TAG = "Task";

    private DownloadEntity    mEntity;
    private IDownloadListener mListener;
    private Handler           mOutHandler;
    private Context           mContext;
    private DownLoadUtil      mUtil;

    private Task(Context context, DownloadEntity entity) {
        mContext = context.getApplicationContext();
        mEntity = entity;
        mUtil = new DownLoadUtil(context, entity);
    }

    /**
     * 开始下载
     */
    public void start() {
        if (mUtil.isDownloading()) {
            Log.d(TAG, "任务正在下载");
        } else {
            if (mListener == null) {
                mListener = new DownloadListener(mContext, mEntity, mOutHandler);
            }
            mUtil.start(mListener);
        }
    }

    public DownloadEntity getDownloadEntity() {
        return mEntity;
    }

    /**
     * 停止下载
     */
    public void stop() {
        if (mUtil.isDownloading()) {
            mUtil.stopDownload();
        } else {
            mEntity.setState(DownloadEntity.STATE_STOP);
            mEntity.save();
            sendInState2Target(IDownloadTarget.STOP);

            // 发送停止下载的广播
            Intent intent = createIntent(DownloadManager.ACTION_STOP);
            intent.putExtra(DownloadManager.CURRENT_LOCATION, mEntity.getCurrentProgress());
            intent.putExtra(DownloadManager.ENTITY, mEntity);
            mContext.sendBroadcast(intent);
        }
    }

    /**
     * 获取下载工具
     */
    public DownLoadUtil getDownloadUtil() {
        return mUtil;
    }

    /**
     * 任务下载状态
     */
    public boolean isDownloading() {
        return mUtil.isDownloading();
    }

    /**
     * 取消下载
     */
    public void cancel() {
        if (mUtil.isDownloading()) {
            mUtil.cancelDownload();
        } else {
            // 如果任务不是下载状态
            mUtil.cancelDownload();
            mUtil.delConfigFile();
            mUtil.delTempFile();
            mEntity.deleteData();
            sendInState2Target(IDownloadTarget.CANCEL);

            //发送取消下载的广播
            Intent intent = createIntent(DownloadManager.ACTION_CANCEL);
            intent.putExtra(DownloadManager.ENTITY, mEntity);
            mContext.sendBroadcast(intent);
        }
    }

    /**
     * 创建特定的Intent
     *
     * @param action
     * @return
     */
    private Intent createIntent(String action) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(mContext.getPackageName());
        Uri    uri    = builder.build();
        Intent intent = new Intent(action);
        intent.setData(uri);
        return intent;
    }

    /**
     * 将任务状态发送给下载器
     *
     * @param state {@link IDownloadTarget#START}
     */
    private void sendInState2Target(int state) {
        if (mOutHandler != null) {
            mOutHandler.obtainMessage(state, mEntity).sendToTarget();
        }
    }

    /**
     * 下载监听类
     */
    private class DownloadListener extends DownLoadUtil.DownloadListener {
        Handler outHandler;
        Context context;
        Intent  sendIntent;
        long INTERVAL = 1024 * 10;   //10k大小的间隔
        long lastLen  = 0;   //上一次发送长度
        long lastTime = 0;
        long INTERVAL_TIME = 60 * 1000;   //10k大小的间隔
        DownloadEntity downloadEntity;

        DownloadListener(Context context, DownloadEntity downloadEntity,
                Handler outHandler) {
            this.context = context;
            this.outHandler = outHandler;
            this.downloadEntity = downloadEntity;
            sendIntent = createIntent(DownloadManager.ACTION_RUNNING);
            sendIntent.putExtra(DownloadManager.ENTITY, downloadEntity);
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
//            if (currentLocation - lastLen > INTERVAL) { //不要太过于频繁发送广播
//                sendIntent.putExtra(DownloadManager.CURRENT_LOCATION, currentLocation);
//                lastLen = currentLocation;
//                context.sendBroadcast(sendIntent);
//            }
            if (System.currentTimeMillis() - lastLen > INTERVAL_TIME){
                sendIntent.putExtra(DownloadManager.CURRENT_LOCATION, currentLocation);
                lastTime = System.currentTimeMillis();
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

        private void sendIntent(String action, long location) {
            downloadEntity.setDownloadComplete(action.equals(DownloadManager.ACTION_COMPLETE));
            downloadEntity.setCurrentProgress(location);
            downloadEntity.update();
            Intent intent = createIntent(action);
            intent.putExtra(DownloadManager.ENTITY, downloadEntity);
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

        public Task build() {
            Task task = new Task(context, downloadEntity);
            task.mListener = listener;
            task.mOutHandler = outHandler;
            downloadEntity.save();
            return task;
        }
    }
}

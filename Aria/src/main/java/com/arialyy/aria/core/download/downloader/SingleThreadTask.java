/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.core.download.downloader;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by lyy on 2017/1/18.
 * 下载线程
 */
final class SingleThreadTask implements Runnable {
  private static final String TAG = "SingleThreadTask";
  private ChildThreadConfigEntity mConfigEntity;
  private String mConfigFPath;
  private long mChildCurrentLocation = 0;
  private int mBufSize;
  private IDownloadListener mListener;
  private StateConstance CONSTANCE;
  private long mSleepTime = 0;

  SingleThreadTask(StateConstance constance, IDownloadListener listener,
      ChildThreadConfigEntity downloadInfo) {
    AriaManager manager = AriaManager.getInstance(AriaManager.APP);
    CONSTANCE = constance;
    CONSTANCE.CONNECT_TIME_OUT = manager.getDownloadConfig().getConnectTimeOut();
    CONSTANCE.READ_TIME_OUT = manager.getDownloadConfig().getIOTimeOut();
    mListener = listener;
    this.mConfigEntity = downloadInfo;
    if (mConfigEntity.IS_SUPPORT_BREAK_POINT) {
      mConfigFPath = downloadInfo.CONFIG_FILE_PATH;
    }
    mBufSize = manager.getDownloadConfig().getBuffSize();
    setMaxSpeed(AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getMsxSpeed());
  }

  void setMaxSpeed(double maxSpeed) {
    if (-0.9999 < maxSpeed && maxSpeed < 0.00001) {
      mSleepTime = 0;
    } else {
      BigDecimal db = new BigDecimal(
          ((mBufSize / 1024) * (filterVersion() ? 1 : CONSTANCE.THREAD_NUM) / maxSpeed) * 1000);
      mSleepTime = db.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
    }
  }

  private boolean filterVersion() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
  }

  @Override public void run() {
    HttpURLConnection conn = null;
    InputStream is = null;
    BufferedRandomAccessFile file = null;
    try {
      URL url = new URL(mConfigEntity.DOWNLOAD_URL);
      conn = ConnectionHelp.handleConnection(url);
      if (mConfigEntity.IS_SUPPORT_BREAK_POINT) {
        Log.d(TAG, "任务【"
            + mConfigEntity.TEMP_FILE.getName()
            + "】线程__"
            + mConfigEntity.THREAD_ID
            + "__开始下载【开始位置 : "
            + mConfigEntity.START_LOCATION
            + "，结束位置："
            + mConfigEntity.END_LOCATION
            + "】");
        //在头里面请求下载开始位置和结束位置
        conn.setRequestProperty("Range",
            "bytes=" + mConfigEntity.START_LOCATION + "-" + (mConfigEntity.END_LOCATION - 1));
      } else {
        Log.w(TAG, "该下载不支持断点");
      }
      conn = ConnectionHelp.setConnectParam(mConfigEntity.DOWNLOAD_TASK_ENTITY, conn);
      conn.setConnectTimeout(CONSTANCE.CONNECT_TIME_OUT);
      conn.setReadTimeout(CONSTANCE.READ_TIME_OUT);  //设置读取流的等待时间,必须设置该参数
      is = conn.getInputStream();
      //创建可设置位置的文件
      file = new BufferedRandomAccessFile(mConfigEntity.TEMP_FILE, "rwd", mBufSize);
      //设置每条线程写入文件的位置
      file.seek(mConfigEntity.START_LOCATION);
      byte[] buffer = new byte[mBufSize];
      int len;
      //当前子线程的下载位置
      mChildCurrentLocation = mConfigEntity.START_LOCATION;
      while ((len = is.read(buffer)) != -1) {
        if (CONSTANCE.isCancel) {
          break;
        }
        if (CONSTANCE.isStop) {
          break;
        }
        Thread.sleep(mSleepTime);
        file.write(buffer, 0, len);
        progress(len);
      }
      if (CONSTANCE.isCancel) {
        return;
      }
      //停止状态不需要删除记录文件
      if (CONSTANCE.isStop) {
        return;
      }
      //支持断点的处理
      if (mConfigEntity.IS_SUPPORT_BREAK_POINT) {
        Log.i(TAG, "任务【"
            + mConfigEntity.TEMP_FILE.getName()
            + "】线程__"
            + mConfigEntity.THREAD_ID
            + "__下载完毕");
        writeConfig(true, 1);
        mListener.onChildComplete(mConfigEntity.END_LOCATION);
        CONSTANCE.COMPLETE_THREAD_NUM++;
        if (CONSTANCE.isComplete()) {
          File configFile = new File(mConfigFPath);
          if (configFile.exists()) {
            configFile.delete();
          }
          CONSTANCE.isDownloading = false;
          mListener.onComplete();
        }
      } else {
        Log.i(TAG, "下载任务完成");
        CONSTANCE.isDownloading = false;
        mListener.onComplete();
      }
    } catch (MalformedURLException e) {
      failDownload(mChildCurrentLocation, "下载链接异常", e);
    } catch (IOException e) {
      failDownload(mChildCurrentLocation, "下载失败【" + mConfigEntity.DOWNLOAD_URL + "】", e);
    } catch (Exception e) {
      failDownload(mChildCurrentLocation, "获取流失败", e);
    } finally {
      try {
        if (file != null) {
          file.close();
        }
        if (is != null) {
          is.close();
        }
        if (conn != null) {
          conn.disconnect();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 停止下载
   */
  protected void stop() {
    synchronized (AriaManager.LOCK) {
      try {
        if (mConfigEntity.IS_SUPPORT_BREAK_POINT) {
          CONSTANCE.STOP_NUM++;
          Log.d(TAG, "任务【"
              + mConfigEntity.TEMP_FILE.getName()
              + "】thread__"
              + mConfigEntity.THREAD_ID
              + "__停止, stop location ==> "
              + mChildCurrentLocation);
          writeConfig(false, mChildCurrentLocation);
          if (CONSTANCE.isStop()) {
            Log.d(TAG, "任务【" + mConfigEntity.TEMP_FILE.getName() + "】已停止");
            CONSTANCE.isDownloading = false;
            mListener.onStop(CONSTANCE.CURRENT_LOCATION);
          }
        } else {
          Log.d(TAG, "任务【" + mConfigEntity.TEMP_FILE.getName() + "】已停止");
          CONSTANCE.isDownloading = false;
          mListener.onStop(CONSTANCE.CURRENT_LOCATION);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 下载中
   */
  private void progress(long len) {
    synchronized (AriaManager.LOCK) {
      mChildCurrentLocation += len;
      CONSTANCE.CURRENT_LOCATION += len;
      mListener.onProgress(CONSTANCE.CURRENT_LOCATION);
    }
  }

  /**
   * 取消下载
   */
  protected void cancel() {
    synchronized (AriaManager.LOCK) {
      if (mConfigEntity.IS_SUPPORT_BREAK_POINT) {
        CONSTANCE.CANCEL_NUM++;
        Log.d(TAG, "任务【"
            + mConfigEntity.TEMP_FILE.getName()
            + "】thread__"
            + mConfigEntity.THREAD_ID
            + "__取消下载");
        if (CONSTANCE.isCancel()) {
          File configFile = new File(mConfigFPath);
          if (configFile.exists()) {
            configFile.delete();
          }
          if (mConfigEntity.TEMP_FILE.exists()) {
            mConfigEntity.TEMP_FILE.delete();
          }
          Log.d(TAG, "任务【" + mConfigEntity.TEMP_FILE.getName() + "】已取消");
          CONSTANCE.isDownloading = false;
          mListener.onCancel();
        }
      } else {
        Log.d(TAG, "任务【" + mConfigEntity.TEMP_FILE.getName() + "】已取消");
        CONSTANCE.isDownloading = false;
        mListener.onCancel();
      }
    }
  }

  /**
   * 下载失败
   */
  private void failDownload(long currentLocation, String msg, Exception ex) {
    synchronized (AriaManager.LOCK) {
      try {
        CONSTANCE.FAIL_NUM++;
        CONSTANCE.isDownloading = false;
        CONSTANCE.isStop = true;
        if (ex != null) {
          Log.e(TAG, msg + "\n" + CommonUtil.getPrintException(ex));
        }
        if (mConfigEntity.IS_SUPPORT_BREAK_POINT) {
          writeConfig(false, currentLocation);
          if (CONSTANCE.isFail()) {
            Log.e(TAG, "任务【" + mConfigEntity.TEMP_FILE.getName() + "】下载失败");
            mListener.onFail();
          }
        } else {
          Log.e(TAG, "任务【" + mConfigEntity.TEMP_FILE.getName() + "】下载失败");
          mListener.onFail();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 将记录写入到配置文件
   */
  private void writeConfig(boolean isComplete, long record) throws IOException {
    synchronized (AriaManager.LOCK) {
      String key = null, value = null;
      if (0 < record && record < mConfigEntity.END_LOCATION) {
        key = mConfigEntity.TEMP_FILE.getName() + "_record_" + mConfigEntity.THREAD_ID;
        value = String.valueOf(record);
      } else if (record >= mConfigEntity.END_LOCATION || isComplete) {
        key = mConfigEntity.TEMP_FILE.getName() + "_state_" + mConfigEntity.THREAD_ID;
        value = "1";
      }
      if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
        File configFile = new File(mConfigFPath);
        Properties pro = CommonUtil.loadConfig(configFile);
        pro.setProperty(key, value);
        CommonUtil.saveConfig(configFile, pro);
      }
    }
  }
}

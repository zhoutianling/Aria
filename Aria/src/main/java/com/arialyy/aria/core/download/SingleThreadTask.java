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
package com.arialyy.aria.core.download;

import android.util.Log;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.Configuration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
  private DownloadUtil.ConfigEntity mConfigEntity;
  private String mConfigFPath;
  private long mChildCurrentLocation = 0;
  private static final Object LOCK = new Object();
  private int mBufSize = 8192;
  //private int mBufSize = 64;
  private IDownloadListener mListener;
  private DownloadStateConstance mConstance;

  SingleThreadTask(DownloadStateConstance constance, IDownloadListener listener,
      DownloadUtil.ConfigEntity downloadInfo) {
    mConstance = constance;
    mListener = listener;
    this.mConfigEntity = downloadInfo;
    if (mConfigEntity.isSupportBreakpoint) {
      mConfigFPath = downloadInfo.CONFIG_FILE_PATH;
    }
    //mBufSize = Configuration.getInstance().getMaxSpeed();
  }

  @Override public void run() {
    HttpURLConnection conn = null;
    InputStream is = null;
    try {
      URL url = new URL(mConfigEntity.DOWNLOAD_URL);
      //conn = (HttpURLConnection) url.openConnection();
      conn = ConnectionHelp.handleConnection(url);
      if (mConfigEntity.isSupportBreakpoint) {
        Log.d(TAG, "线程_"
            + mConfigEntity.THREAD_ID
            + "_正在下载【开始位置 : "
            + mConfigEntity.START_LOCATION
            + "，结束位置："
            + mConfigEntity.END_LOCATION
            + "】");
        //在头里面请求下载开始位置和结束位置
        conn.setRequestProperty("Range",
            "bytes=" + mConfigEntity.START_LOCATION + "-" + mConfigEntity.END_LOCATION);
      } else {
        Log.w(TAG, "该下载不支持断点");
      }
      conn = ConnectionHelp.setConnectParam(mConfigEntity.DOWNLOAD_TASK_ENTITY, conn);
      conn.setConnectTimeout(mConstance.CONNECT_TIME_OUT);
      conn.setReadTimeout(mConstance.READ_TIME_OUT);  //设置读取流的等待时间,必须设置该参数
      is = conn.getInputStream();
      //创建可设置位置的文件
      BufferedRandomAccessFile file =
          new BufferedRandomAccessFile(mConfigEntity.TEMP_FILE, "rwd", mBufSize);
      //设置每条线程写入文件的位置
      file.seek(mConfigEntity.START_LOCATION);
      byte[] buffer = new byte[mBufSize];
      int len;
      //当前子线程的下载位置
      mChildCurrentLocation = mConfigEntity.START_LOCATION;
      while ((len = is.read(buffer)) != -1) {
        if (mConstance.isCancel) {
          break;
        }
        if (mConstance.isStop) {
          break;
        }
        //把下载数据数据写入文件
        file.write(buffer, 0, len);
        progress(len);
      }
      file.close();
      //close 为阻塞的，需要使用线程池来处理
      is.close();
      conn.disconnect();
      if (mConstance.isCancel) {
        return;
      }
      //停止状态不需要删除记录文件
      if (mConstance.isStop) {
        return;
      }
      //支持断点的处理
      if (mConfigEntity.isSupportBreakpoint) {
        Log.i(TAG, "线程【" + mConfigEntity.THREAD_ID + "】下载完毕");
        writeConfig(mConfigEntity.TEMP_FILE.getName() + "_state_" + mConfigEntity.THREAD_ID,
            1 + "");
        mListener.onChildComplete(mConfigEntity.END_LOCATION);
        mConstance.COMPLETE_THREAD_NUM++;
        if (mConstance.isComplete()) {
          File configFile = new File(mConfigFPath);
          if (configFile.exists()) {
            configFile.delete();
          }
          mConstance.isDownloading = false;
          mListener.onComplete();
        }
      } else {
        Log.i(TAG, "下载任务完成");
        mConstance.isDownloading = false;
        mListener.onComplete();
      }
    } catch (MalformedURLException e) {
      mConstance.FAIL_NUM++;
      failDownload(mConfigEntity, mChildCurrentLocation, "下载链接异常", e);
    } catch (IOException e) {
      mConstance.FAIL_NUM++;
      failDownload(mConfigEntity, mChildCurrentLocation, "下载失败【" + mConfigEntity.DOWNLOAD_URL + "】",
          e);
    } catch (Exception e) {
      mConstance.FAIL_NUM++;
      failDownload(mConfigEntity, mChildCurrentLocation, "获取流失败", e);
    }
  }

  /**
   * 停止下载
   */
  protected void stop() {
    synchronized (LOCK) {
      try {
        if (mConfigEntity.isSupportBreakpoint) {
          mConstance.STOP_NUM++;
          String location = String.valueOf(mChildCurrentLocation);
          Log.d(TAG, "thread_"
              + mConfigEntity.THREAD_ID
              + "_stop, stop location ==> "
              + mChildCurrentLocation);
          writeConfig(mConfigEntity.TEMP_FILE.getName() + "_record_" + mConfigEntity.THREAD_ID,
              location);
          if (mConstance.isStop()) {
            Log.d(TAG, "++++++++++++++++ onStop +++++++++++++++++");
            mConstance.isDownloading = false;
            mListener.onStop(mConstance.CURRENT_LOCATION);
          }
        } else {
          Log.d(TAG, "++++++++++++++++ onStop +++++++++++++++++");
          mConstance.isDownloading = false;
          mListener.onStop(mConstance.CURRENT_LOCATION);
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
    synchronized (LOCK) {
      mChildCurrentLocation += len;
      mConstance.CURRENT_LOCATION += len;
      mListener.onProgress(mConstance.CURRENT_LOCATION);
    }
  }

  /**
   * 取消下载
   */
  protected void cancel() {
    synchronized (LOCK) {
      if (mConfigEntity.isSupportBreakpoint) {
        mConstance.CANCEL_NUM++;
        Log.d(TAG, "++++++++++ thread_" + mConfigEntity.THREAD_ID + "_cancel ++++++++++");
        if (mConstance.isCancel()) {
          File configFile = new File(mConfigFPath);
          if (configFile.exists()) {
            configFile.delete();
          }
          if (mConfigEntity.TEMP_FILE.exists()) {
            mConfigEntity.TEMP_FILE.delete();
          }
          Log.d(TAG, "++++++++++++++++ onCancel +++++++++++++++++");
          mConstance.isDownloading = false;
          mListener.onCancel();
        }
      } else {
        Log.d(TAG, "++++++++++++++++ onCancel +++++++++++++++++");
        mConstance.isDownloading = false;
        mListener.onCancel();
      }
    }
  }

  /**
   * 下载失败
   */
  private void failDownload(DownloadUtil.ConfigEntity dEntity, long currentLocation, String msg,
      Exception ex) {
    synchronized (LOCK) {
      try {
        mConstance.isDownloading = false;
        mConstance.isStop = true;
        if (ex != null) {
          Log.e(TAG, CommonUtil.getPrintException(ex));
        }
        if (mConfigEntity.isSupportBreakpoint) {
          if (currentLocation != -1) {
            String location = String.valueOf(currentLocation);
            writeConfig(dEntity.TEMP_FILE.getName() + "_record_" + dEntity.THREAD_ID, location);
          }
          if (mConstance.isFail()) {
            Log.d(TAG, "++++++++++++++++ onFail +++++++++++++++++");
            mListener.onFail();
          }
        } else {
          Log.d(TAG, "++++++++++++++++ onFail +++++++++++++++++");
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
  private void writeConfig(String key, String record) throws IOException {
    File configFile = new File(mConfigFPath);
    Properties pro = CommonUtil.loadConfig(configFile);
    pro.setProperty(key, record);
    CommonUtil.saveConfig(configFile, pro);
  }
}

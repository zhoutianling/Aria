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
package com.arialyy.aria.core.download.downloader.ftp;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.download.downloader.ChildThreadConfigEntity;
import com.arialyy.aria.core.download.downloader.IDownloadListener;
import com.arialyy.aria.core.download.downloader.StateConstance;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Created by Aria.Lao on 2017/7/24.
 * Ftp下载任务
 */
class FtpThreadTask implements Runnable {
  private final String TAG = "FtpThreadTask";
  private ChildThreadConfigEntity mConfig;
  private String mConfigFPath;
  private long mChildCurrentLocation = 0;
  private int mBufSize;
  private IDownloadListener mListener;
  private StateConstance STATE;
  private long mSleepTime = 0;
  private DownloadTaskEntity mTaskEntity;
  private DownloadEntity mEntity;

  FtpThreadTask(StateConstance constance, IDownloadListener listener,
      ChildThreadConfigEntity downloadInfo) {
    AriaManager manager = AriaManager.getInstance(AriaManager.APP);
    STATE = constance;
    STATE.CONNECT_TIME_OUT = manager.getDownloadConfig().getConnectTimeOut();
    STATE.READ_TIME_OUT = manager.getDownloadConfig().getIOTimeOut();
    mListener = listener;
    this.mConfig = downloadInfo;
    mConfigFPath = downloadInfo.CONFIG_FILE_PATH;
    mTaskEntity = mConfig.DOWNLOAD_TASK_ENTITY;
    mEntity = mTaskEntity.getEntity();
    mBufSize = manager.getDownloadConfig().getBuffSize();
    setMaxSpeed(AriaManager.getInstance(AriaManager.APP).getDownloadConfig().getMsxSpeed());
  }

  void setMaxSpeed(double maxSpeed) {
    if (-0.9999 < maxSpeed && maxSpeed < 0.00001) {
      mSleepTime = 0;
    } else {
      BigDecimal db = new BigDecimal(
          ((mBufSize / 1024) * (filterVersion() ? 1 : STATE.THREAD_NUM) / maxSpeed) * 1000);
      mSleepTime = db.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
    }
  }

  private boolean filterVersion() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
  }

  @Override public void run() {
    FTPClient client = null;
    InputStream is = null;
    BufferedRandomAccessFile file = null;
    try {
      Log.d(TAG, "任务【"
          + mConfig.TEMP_FILE.getName()
          + "】线程__"
          + mConfig.THREAD_ID
          + "__开始下载【开始位置 : "
          + mConfig.START_LOCATION
          + "，结束位置："
          + mConfig.END_LOCATION
          + "】");
      client = new FTPClient();
      //ip和端口
      String[] temp = mEntity.getDownloadUrl().split("/");
      String[] pp = temp[2].split(":");
      String dir = temp[temp.length - 2];
      String fileName = temp[temp.length - 1];
      client.connect(pp[0], Integer.parseInt(pp[1]));
      if (!TextUtils.isEmpty(mTaskEntity.account)) {
        client.login(mTaskEntity.userName, mTaskEntity.userPw);
      } else {
        client.login(mTaskEntity.userName, mTaskEntity.userPw, mTaskEntity.account);
      }
      int reply = client.getReplyCode();
      if (!FTPReply.isPositiveCompletion(reply)) {
        client.disconnect();
        failDownload(STATE.CURRENT_LOCATION, "无法连接到ftp服务器，错误码为：" + reply, null);
        return;
      }
      client.enterLocalPassiveMode();
      client.setFileType(FTP.BINARY_FILE_TYPE);
      FTPFile[] files = client.listFiles(fileName);
      files[0].getSize();
      client.setRestartOffset(mConfig.START_LOCATION);
      is = client.retrieveFileStream(fileName);
      //创建可设置位置的文件
      file = new BufferedRandomAccessFile(mConfig.TEMP_FILE, "rwd", mBufSize);
      //设置每条线程写入文件的位置
      file.seek(mConfig.START_LOCATION);
      byte[] buffer = new byte[mBufSize];
      int len;
      //当前子线程的下载位置
      mChildCurrentLocation = mConfig.START_LOCATION;
      while ((len = is.read(buffer)) != -1) {
        if (STATE.isCancel) break;
        if (STATE.isStop) break;
        if (mSleepTime > 0) Thread.sleep(mSleepTime);
        file.write(buffer, 0, len);
        progress(len);
      }
      if (STATE.isCancel) return;
      //停止状态不需要删除记录文件
      if (STATE.isStop) return;
      //支持断点的处理
      if (mConfig.IS_SUPPORT_BREAK_POINT) {
        Log.i(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】线程__" + mConfig.THREAD_ID + "__下载完毕");
        writeConfig(true, 1);
        STATE.COMPLETE_THREAD_NUM++;
        if (STATE.isComplete()) {
          File configFile = new File(mConfigFPath);
          if (configFile.exists()) {
            configFile.delete();
          }
          STATE.isDownloading = false;
          mListener.onComplete();
        }
      } else {
        Log.i(TAG, "下载任务完成");
        STATE.isDownloading = false;
        mListener.onComplete();
      }
    } catch (IOException e) {
      failDownload(mChildCurrentLocation, "下载失败【" + mConfig.DOWNLOAD_URL + "】", e);
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
        if (client != null) {
          client.logout();
          client.disconnect();
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
        STATE.STOP_NUM++;
        Log.d(TAG, "任务【"
            + mConfig.TEMP_FILE.getName()
            + "】thread__"
            + mConfig.THREAD_ID
            + "__停止, stop location ==> "
            + mChildCurrentLocation);
        writeConfig(false, mChildCurrentLocation);
        if (STATE.isStop()) {
          Log.d(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】已停止");
          STATE.isDownloading = false;
          mListener.onStop(STATE.CURRENT_LOCATION);
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
      STATE.CURRENT_LOCATION += len;
    }
  }

  /**
   * 取消下载
   */
  protected void cancel() {
    synchronized (AriaManager.LOCK) {
      STATE.CANCEL_NUM++;
      Log.d(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】thread__" + mConfig.THREAD_ID + "__取消下载");
      if (STATE.isCancel()) {
        File configFile = new File(mConfigFPath);
        if (configFile.exists()) {
          configFile.delete();
        }
        if (mConfig.TEMP_FILE.exists()) {
          mConfig.TEMP_FILE.delete();
        }
        Log.d(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】已取消");
        STATE.isDownloading = false;
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
        STATE.FAIL_NUM++;
        STATE.isDownloading = false;
        STATE.isStop = true;
        if (ex != null) {
          Log.e(TAG, msg + "\n" + CommonUtil.getPrintException(ex));
        }
        writeConfig(false, currentLocation);
        if (STATE.isFail()) {
          Log.e(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】下载失败");
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
      if (0 < record && record < mConfig.END_LOCATION) {
        key = mConfig.TEMP_FILE.getName() + "_record_" + mConfig.THREAD_ID;
        value = String.valueOf(record);
      } else if (record >= mConfig.END_LOCATION || isComplete) {
        key = mConfig.TEMP_FILE.getName() + "_state_" + mConfig.THREAD_ID;
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

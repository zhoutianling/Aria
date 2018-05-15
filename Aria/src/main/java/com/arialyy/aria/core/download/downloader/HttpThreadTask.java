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

import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.common.AbsThreadTask;
import com.arialyy.aria.core.common.StateConstance;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.IDownloadListener;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.CommonUtil;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by lyy on 2017/1/18.
 * 下载线程
 */
final class HttpThreadTask extends AbsThreadTask<DownloadEntity, DownloadTaskEntity> {
  private final String TAG = "HttpThreadTask";
  /**
   * 2M的动态长度
   */
  private final int LEN_INTERVAL = 1024 * 1024 * 2;
  private boolean useVirtualFile = false;

  HttpThreadTask(StateConstance constance, IDownloadListener listener,
      SubThreadConfig<DownloadTaskEntity> downloadInfo) {
    super(constance, listener, downloadInfo);
    AriaManager manager = AriaManager.getInstance(AriaManager.APP);
    mConnectTimeOut = manager.getDownloadConfig().getConnectTimeOut();
    mReadTimeOut = manager.getDownloadConfig().getIOTimeOut();
    mBufSize = manager.getDownloadConfig().getBuffSize();
    isNotNetRetry = manager.getDownloadConfig().isNotNetRetry();
    useVirtualFile = STATE.TASK_RECORD.isUseVirtualFile;
    setMaxSpeed(manager.getDownloadConfig().getMaxSpeed());
  }

  @Override public void run() {
    HttpURLConnection conn = null;
    BufferedInputStream is = null;
    BufferedRandomAccessFile file = null;
    //当前子线程的下载位置
    mChildCurrentLocation = mConfig.START_LOCATION;
    try {
      URL url = new URL(CommonUtil.convertUrl(mConfig.URL));
      conn = ConnectionHelp.handleConnection(url);
      if (mConfig.SUPPORT_BP) {
        ALog.d(TAG, "任务【"
            + mConfig.TEMP_FILE.getName()
            + "】线程__"
            + mConfig.THREAD_ID
            + "__开始下载【开始位置 : "
            + mConfig.START_LOCATION
            + "，结束位置："
            + mConfig.END_LOCATION
            + "】");
        //在头里面请求下载开始位置和结束位置
        conn.setRequestProperty("Range",
            "bytes=" + mConfig.START_LOCATION + "-" + (mConfig.END_LOCATION - 1));
      } else {
        ALog.w(TAG, "该下载不支持断点");
      }
      conn = ConnectionHelp.setConnectParam(mConfig.TASK_ENTITY, conn);
      conn.setConnectTimeout(mConnectTimeOut);
      conn.setReadTimeout(mReadTimeOut);  //设置读取流的等待时间,必须设置该参数

      is = new BufferedInputStream(ConnectionHelp.convertInputStream(conn));
      //创建可设置位置的文件
      file = new BufferedRandomAccessFile(mConfig.TEMP_FILE, "rwd", mBufSize);
      //设置每条线程写入文件的位置
      file.seek(mConfig.START_LOCATION);

      if (mTaskEntity.isChunked()) {
        readChunk(is, file);
      } else {
        readNormal(is, file);
      }

      if (STATE.isCancel || STATE.isStop) {
        return;
      }
      handleComplete();
    } catch (MalformedURLException e) {
      fail(mChildCurrentLocation, "下载链接异常", e);
    } catch (IOException e) {
      fail(mChildCurrentLocation, "下载失败【" + mConfig.URL + "】", e);
    } catch (Exception e) {
      fail(mChildCurrentLocation, "获取流失败", e);
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
   * 读取chunk模式的文件流
   *
   * @deprecated 暂时先这样处理，无chun
   */
  private void readChunk(InputStream is, BufferedRandomAccessFile file)
      throws IOException, InterruptedException {
    readNormal(is, file);
  }

  /**
   * 读取普通的文件流
   */
  private void readNormal(InputStream is, BufferedRandomAccessFile file)
      throws IOException, InterruptedException {
    byte[] buffer = new byte[mBufSize];
    int len;
    while ((len = is.read(buffer)) != -1) {
      if (STATE.isCancel || STATE.isStop) {
        break;
      }
      if (mSleepTime > 0) {
        Thread.sleep(mSleepTime);
      }
      if (useVirtualFile) {
        file.setLength(
            STATE.CURRENT_LOCATION + LEN_INTERVAL < mEntity.getFileSize() ? STATE.CURRENT_LOCATION
                + LEN_INTERVAL : mEntity.getFileSize());
      }
      file.write(buffer, 0, len);
      progress(len);
    }
  }

  /**
   * 处理完成配置文件的更新或事件回调
   *
   * @throws IOException
   */
  private void handleComplete() throws IOException {
    //支持断点的处理
    if (mConfig.SUPPORT_BP) {
      if (mChildCurrentLocation == mConfig.END_LOCATION) {
        ALog.i(TAG, "任务【" + mConfig.TEMP_FILE.getName() + "】线程__" + mConfig.THREAD_ID + "__下载完毕");
        writeConfig(true, 1);
        STATE.COMPLETE_THREAD_NUM++;
        if (STATE.isComplete()) {
          STATE.TASK_RECORD.deleteData();
          STATE.isRunning = false;
          mListener.onComplete();
        }
      } else {
        STATE.FAIL_NUM++;
      }
      if (STATE.isFail()) {
        STATE.isRunning = false;
        mListener.onFail(false);
      }
    } else {
      ALog.i(TAG, "任务下载完成");
      STATE.isRunning = false;
      mListener.onComplete();
    }
  }

  @Override protected String getTaskType() {
    return "HTTP_DOWNLOAD";
  }
}

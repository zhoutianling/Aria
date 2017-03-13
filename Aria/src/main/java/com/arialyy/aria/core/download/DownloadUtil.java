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

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lyy on 2015/8/25.
 * 下载工具类
 */
final class DownloadUtil implements IDownloadUtil, Runnable {
  private static final String TAG = "DownloadUtil";
  /**
   * 线程数
   */
  private final int THREAD_NUM;
  private static final long SUB_LEN = 1024 * 100;
  //下载监听
  private IDownloadListener mListener;
  private int mConnectTimeOut = 5000 * 4; //连接超时时间
  private int mReadTimeOut = 5000 * 20; //流读取的超时时间
  private boolean isNewTask = true;
  private boolean isSupportBreakpoint = true;
  private Context mContext;
  private DownloadEntity mDownloadEntity;
  private DownloadTaskEntity mDownloadTaskEntity;
  private ExecutorService mFixedThreadPool;
  private File mDownloadFile; //下载的文件
  private File mConfigFile;//下载信息配置文件
  private SparseArray<Runnable> mTask = new SparseArray<>();
  private DownloadStateConstance mConstance;

  DownloadUtil(Context context, DownloadTaskEntity entity, IDownloadListener downloadListener) {
    this(context, entity, downloadListener, 3);
  }

  DownloadUtil(Context context, DownloadTaskEntity entity, IDownloadListener downloadListener,
      int threadNum) {
    CheckUtil.checkDownloadEntity(entity.downloadEntity);
    mDownloadEntity = entity.downloadEntity;
    mContext = context.getApplicationContext();
    mDownloadTaskEntity = entity;
    mListener = downloadListener;
    THREAD_NUM = threadNum;
    mFixedThreadPool = Executors.newFixedThreadPool(Integer.MAX_VALUE);
    mConstance = new DownloadStateConstance(THREAD_NUM);
    init();
  }

  private void init() {
    mDownloadFile = new File(mDownloadTaskEntity.downloadEntity.getDownloadPath());
    //读取已完成的线程数
    mConfigFile = new File(
        mContext.getFilesDir().getPath() + "/temp/" + mDownloadFile.getName() + ".properties");
    try {
      if (!mConfigFile.exists()) { //记录文件被删除，则重新下载
        isNewTask = true;
        CommonUtil.createFile(mConfigFile.getPath());
      } else {
        isNewTask = !mDownloadFile.exists();
      }
    } catch (Exception e) {
      e.printStackTrace();
      failDownload("下载失败，记录文件被删除");
    }
  }

  public IDownloadListener getListener() {
    return mListener;
  }

  /**
   * 设置连接超时时间
   */
  public void setConnectTimeOut(int timeOut) {
    mConnectTimeOut = timeOut;
  }

  /**
   * 设置流读取的超时时间
   */
  public void setReadTimeOut(int readTimeOut) {
    mReadTimeOut = readTimeOut;
  }

  /**
   * 获取当前下载位置
   */
  @Override public long getCurrentLocation() {
    return mConstance.CURRENT_LOCATION;
  }

  @Override public boolean isDownloading() {
    return mConstance.isDownloading;
  }

  /**
   * 取消下载
   */
  @Override public void cancelDownload() {
    mConstance.isCancel = true;
    mConstance.isDownloading = false;
    mFixedThreadPool.shutdown();
    for (int i = 0; i < THREAD_NUM; i++) {
      SingleThreadTask task = (SingleThreadTask) mTask.get(i);
      if (task != null) {
        task.cancel();
      }
    }
  }

  /**
   * 停止下载
   */
  @Override public void stopDownload() {
    mConstance.isStop = true;
    mConstance.isDownloading = false;
    mFixedThreadPool.shutdown();
    for (int i = 0; i < THREAD_NUM; i++) {
      SingleThreadTask task = (SingleThreadTask) mTask.get(i);
      if (task != null) {
        task.stop();
      }
    }
  }

  /**
   * 删除下载记录文件
   */
  @Override public void delConfigFile() {
    if (mContext != null && mDownloadEntity != null) {
      File dFile = new File(mDownloadEntity.getDownloadPath());
      File config =
          new File(mContext.getFilesDir().getPath() + "/temp/" + dFile.getName() + ".properties");
      if (config.exists()) {
        config.delete();
      }
    }
  }

  /**
   * 删除temp文件
   */
  @Override public void delTempFile() {
    if (mContext != null && mDownloadEntity != null) {
      File dFile = new File(mDownloadEntity.getDownloadPath());
      if (dFile.exists()) {
        dFile.delete();
      }
    }
  }

  /**
   * 多线程断点续传下载文件，开始下载
   */
  @Override public void startDownload() {
    mConstance.cleanState();
    mListener.onPre();
    new Thread(this).start();
  }

  @Override public void resumeDownload() {
    startDownload();
  }

  private void failDownload(String msg) {
    Log.e(TAG, msg);
    mConstance.isDownloading = false;
    stopDownload();
    mListener.onFail();
  }

  @Override public void run() {
    try {
      URL url = new URL(mDownloadEntity.getDownloadUrl());
      HttpURLConnection conn = ConnectionHelp.handleConnection(url);
      conn = ConnectionHelp.setConnectParam(mDownloadTaskEntity, conn);
      conn.setRequestProperty("Range", "bytes=" + 0 + "-");
      conn.setConnectTimeout(mConnectTimeOut * 4);
      conn.connect();
      int len = conn.getContentLength();
      //if (len < 0) {  //网络被劫持时会出现这个问题
      //  failDownload("下载失败，网络被劫持");
      //  return;
      //}
      int code = conn.getResponseCode();
      //https://zh.wikipedia.org/wiki/HTTP%E7%8A%B6%E6%80%81%E7%A0%81
      //206支持断点
      if (code == HttpURLConnection.HTTP_PARTIAL) {
        isSupportBreakpoint = true;
        mListener.supportBreakpoint(true);
        handleBreakpoint(conn);
      } else if (code == HttpURLConnection.HTTP_OK || len < 0) {
        //在conn.setRequestProperty("Range", "bytes=" + 0 + "-");下，200为不支持断点状态
        if (len < 0) {
          failDownload("任务【" + mDownloadEntity.getDownloadUrl() + "】下载失败，文件长度小于0");
          return;
        }
        isSupportBreakpoint = false;
        mListener.supportBreakpoint(false);
        Log.w(TAG, "该下载链接不支持断点下载");
        handleBreakpoint(conn);
      } else {
        failDownload("任务【" + mDownloadEntity.getDownloadUrl() + "】下载失败，返回码：" + code);
      }
    } catch (IOException e) {
      failDownload("下载失败【downloadUrl:"
          + mDownloadEntity.getDownloadUrl()
          + "】\n【filePath:"
          + mDownloadFile.getPath()
          + "】"
          + CommonUtil.getPrintException(e));
    }
  }

  /**
   * 处理断点
   */
  private void handleBreakpoint(HttpURLConnection conn) throws IOException {

    //不支持断点只能单线程下载
    if (!isSupportBreakpoint) {
      ConfigEntity entity = new ConfigEntity();
      entity.FILE_SIZE = conn.getContentLength();
      entity.DOWNLOAD_URL = mDownloadEntity.getDownloadUrl();
      entity.TEMP_FILE = mDownloadFile;
      entity.THREAD_ID = 0;
      entity.START_LOCATION = 0;
      entity.END_LOCATION = entity.FILE_SIZE;
      entity.CONFIG_FILE_PATH = mConfigFile.getPath();
      entity.isSupportBreakpoint = isSupportBreakpoint;
      entity.DOWNLOAD_TASK_ENTITY = mDownloadTaskEntity;
      SingleThreadTask task = new SingleThreadTask(mConstance, mListener, entity);
      mFixedThreadPool.execute(task);
      mListener.onStart(0);
      return;
    }
    int fileLength = conn.getContentLength();
    //必须建一个文件
    CommonUtil.createFile(mDownloadFile.getPath());
    BufferedRandomAccessFile file =
        new BufferedRandomAccessFile(new File(mDownloadFile.getPath()), "rwd", 8192);
    //设置文件长度
    file.setLength(fileLength);
    mListener.onPostPre(fileLength);
    //分配每条线程的下载区间
    Properties pro = null;
    pro = CommonUtil.loadConfig(mConfigFile);
    if (pro.isEmpty()) {
      isNewTask = true;
    } else {
      for (int i = 0; i < THREAD_NUM; i++) {
        if (pro.getProperty(mDownloadFile.getName() + "_record_" + i) == null) {
          Object state = pro.getProperty(mDownloadFile.getName() + "_state_" + i);
          if (state != null && Integer.parseInt(state + "") == 1) {
            continue;
          }
          isNewTask = true;
          break;
        }
      }
    }
    int blockSize = fileLength / THREAD_NUM;
    int[] recordL = new int[THREAD_NUM];
    int rl = 0;
    for (int i = 0; i < THREAD_NUM; i++) {
      recordL[i] = -1;
    }
    for (int i = 0; i < THREAD_NUM; i++) {
      long startL = i * blockSize, endL = (i + 1) * blockSize;
      Object state = pro.getProperty(mDownloadFile.getName() + "_state_" + i);
      if (state != null && Integer.parseInt(state + "") == 1) {  //该线程已经完成
        mConstance.CURRENT_LOCATION += endL - startL;
        Log.d(TAG, "++++++++++ 线程_" + i + "_已经下载完成 ++++++++++");
        mConstance.COMPLETE_THREAD_NUM++;
        mConstance.STOP_NUM++;
        mConstance.CANCEL_NUM++;
        if (mConstance.isComplete()) {
          if (mConfigFile.exists()) {
            mConfigFile.delete();
          }
          mListener.onComplete();
          mConstance.isDownloading = false;
          return;
        }
        continue;
      }
      //分配下载位置
      Object record = pro.getProperty(mDownloadFile.getName() + "_record_" + i);
      //如果有记录，则恢复下载
      if (!isNewTask && record != null && Long.parseLong(record + "") > 0) {
        Long r = Long.parseLong(record + "");
        mConstance.CURRENT_LOCATION += r - startL;
        Log.d(TAG, "++++++++++ 线程_" + i + "_恢复下载 ++++++++++");
        mListener.onChildResume(r);
        startL = r;
        recordL[rl] = i;
        rl++;
      } else {
        isNewTask = true;
      }
      if (isNewTask) {
        recordL[rl] = i;
        rl++;
      }
      if (i == (THREAD_NUM - 1)) {
        //如果整个文件的大小不为线程个数的整数倍，则最后一个线程的结束位置即为文件的总长度
        endL = fileLength;
      }
      ConfigEntity entity = new ConfigEntity();
      entity.FILE_SIZE = fileLength;
      entity.DOWNLOAD_URL = mDownloadEntity.getDownloadUrl();
      entity.TEMP_FILE = mDownloadFile;
      entity.THREAD_ID = i;
      entity.START_LOCATION = startL;
      entity.END_LOCATION = endL;
      entity.CONFIG_FILE_PATH = mConfigFile.getPath();
      entity.isSupportBreakpoint = isSupportBreakpoint;
      entity.DOWNLOAD_TASK_ENTITY = mDownloadTaskEntity;
      SingleThreadTask task = new SingleThreadTask(mConstance, mListener, entity);
      mTask.put(i, task);
    }
    if (mConstance.CURRENT_LOCATION > 0) {
      mListener.onResume(mConstance.CURRENT_LOCATION);
    } else {
      mListener.onStart(mConstance.CURRENT_LOCATION);
    }
    for (int l : recordL) {
      if (l == -1) continue;
      Runnable task = mTask.get(l);
      if (task != null && !mFixedThreadPool.isShutdown()) {
        mFixedThreadPool.execute(task);
      }
    }
  }

  /**
   * 子线程下载信息类
   */
  final static class ConfigEntity {
    //文件大小
    long FILE_SIZE;
    String DOWNLOAD_URL;
    int THREAD_ID;
    long START_LOCATION;
    long END_LOCATION;
    File TEMP_FILE;
    boolean isSupportBreakpoint = true;
    String CONFIG_FILE_PATH;
    DownloadTaskEntity DOWNLOAD_TASK_ENTITY;
  }
}
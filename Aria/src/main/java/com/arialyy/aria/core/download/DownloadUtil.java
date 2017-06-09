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
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lyy on 2015/8/25.
 * 下载工具类
 */
class DownloadUtil implements IDownloadUtil, Runnable {
  private static final String TAG = "DownloadUtil";
  /**
   * 线程数
   */
  private int THREAD_NUM;
  /**
   * 小于1m的文件不启用多线程
   */
  private static final long SUB_LEN = 1024 * 1024;
  //下载监听
  private IDownloadListener mListener;
  private int mConnectTimeOut = 0; //连接超时时间
  private boolean isNewTask = true;
  private boolean isSupportBreakpoint = true;
  private Context mContext;
  private DownloadEntity mDownloadEntity;
  private DownloadTaskEntity mDownloadTaskEntity;
  private ExecutorService mFixedThreadPool;
  private File mDownloadFile; //下载的文件
  private File mConfigFile;//下载信息配置文件
  private SparseArray<Runnable> mTask = new SparseArray<>();
  private DownloadStateConstance CONSTANCE;

  DownloadUtil(Context context, DownloadTaskEntity entity, IDownloadListener downloadListener) {
    mDownloadEntity = entity.downloadEntity;
    mContext = context.getApplicationContext();
    mDownloadTaskEntity = entity;
    mListener = downloadListener;
    // 线程下载数改变后，新的下载才会生效
    //mFixedThreadPool = Executors.newFixedThreadPool(Integer.MAX_VALUE);
    CONSTANCE = new DownloadStateConstance();
    init();
  }

  private void init() {
    mConnectTimeOut = AriaManager.getInstance(mContext).getDownloadConfig().getConnectTimeOut();
    mDownloadFile = new File(mDownloadTaskEntity.downloadEntity.getDownloadPath());
    //读取已完成的线程数
    mConfigFile = new File(mContext.getFilesDir().getPath()
        + AriaManager.DOWNLOAD_TEMP_DIR
        + mDownloadFile.getName()
        + ".properties");
    try {
      if (!mConfigFile.exists()) { //记录文件被删除，则重新下载
        handleNewTask();
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
   * 获取当前下载位置
   */
  @Override public long getCurrentLocation() {
    return CONSTANCE.CURRENT_LOCATION;
  }

  @Override public boolean isDownloading() {
    return CONSTANCE.isDownloading;
  }

  public void setMaxSpeed(double maxSpeed) {
    for (int i = 0; i < THREAD_NUM; i++) {
      SingleThreadTask task = (SingleThreadTask) mTask.get(i);
      if (task != null) {
        task.setMaxSpeed(maxSpeed);
      }
    }
  }

  /**
   * 取消下载
   */
  @Override public void cancelDownload() {
    CONSTANCE.isCancel = true;
    CONSTANCE.isDownloading = false;
    if (mFixedThreadPool != null) {
      mFixedThreadPool.shutdown();
    }
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
    CONSTANCE.isStop = true;
    CONSTANCE.isDownloading = false;
    if (mFixedThreadPool != null) {
      mFixedThreadPool.shutdown();
    }
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
    CONSTANCE.cleanState();
    mListener.onPre();
    new Thread(this).start();
  }

  @Override public void resumeDownload() {
    startDownload();
  }

  private void failDownload(String msg) {
    Log.e(TAG, msg);
    CONSTANCE.isDownloading = false;
    stopDownload();
    mListener.onFail();
  }

  @Override public void run() {
    try {
      URL url = new URL(mDownloadEntity.getDownloadUrl());
      HttpURLConnection conn = ConnectionHelp.handleConnection(url);
      conn = ConnectionHelp.setConnectParam(mDownloadTaskEntity, conn);
      conn.setRequestProperty("Range", "bytes=" + 0 + "-");
      conn.setConnectTimeout(mConnectTimeOut);
      conn.connect();
      handleConnect(conn);
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
   * 处理状态吗
   */
  private void handleConnect(HttpURLConnection conn) throws IOException {
    int len = conn.getContentLength();
    //if (len < 0) {  //网络被劫持时会出现这个问题
    //  failDownload("下载失败，网络被劫持");
    //  return;
    //}
    int code = conn.getResponseCode();
    //https://zh.wikipedia.org/wiki/HTTP%E7%8A%B6%E6%80%81%E7%A0%81
    //206支持断点
    if (code == HttpURLConnection.HTTP_PARTIAL) {
      if (!checkLen(len)) return;
      isSupportBreakpoint = true;
      mListener.supportBreakpoint(true);
      handleBreakpoint(conn);
    } else if (code == HttpURLConnection.HTTP_OK) {
      //在conn.setRequestProperty("Range", "bytes=" + 0 + "-");下，200为不支持断点状态
      if (!checkLen(len)) return;
      isSupportBreakpoint = false;
      mListener.supportBreakpoint(false);
      Log.w(TAG, "该下载链接不支持断点下载");
      handleBreakpoint(conn);
    } else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
      Log.w(TAG, "任务【" + mDownloadEntity.getDownloadUrl() + "】下载失败，错误码：404");
      mListener.onCancel();
    } else if (code == HttpURLConnection.HTTP_MOVED_TEMP
        || code == HttpURLConnection.HTTP_MOVED_PERM
        || code == HttpURLConnection.HTTP_SEE_OTHER) {
      handle302Turn(conn);
    } else {
      failDownload("任务【" + mDownloadEntity.getDownloadUrl() + "】下载失败，错误码：" + code);
    }
  }

  /**
   * 检查长度是否合法
   *
   * @param len 从服务器获取的文件长度
   * @return true, 合法
   */
  private boolean checkLen(long len) {
    if (len < 0) {
      failDownload("任务【" + mDownloadEntity.getDownloadUrl() + "】下载失败，文件长度小于0");
      return false;
    }
    return true;
  }

  /**
   * 处理30x跳转
   */
  private void handle302Turn(HttpURLConnection conn) throws IOException {
    String newUrl = conn.getHeaderField(mDownloadTaskEntity.redirectUrlKey);
    Log.d(TAG, "30x跳转，新url为【" + newUrl + "】");
    mDownloadEntity.setRedirect(true);
    mDownloadEntity.setRedirectUrl(newUrl);
    mDownloadEntity.update();
    String cookies = conn.getHeaderField("Set-Cookie");
    conn = (HttpURLConnection) new URL(newUrl).openConnection();
    conn = ConnectionHelp.setConnectParam(mDownloadTaskEntity, conn);
    conn.setRequestProperty("Cookie", cookies);
    conn.setRequestProperty("Range", "bytes=" + 0 + "-");
    conn.setConnectTimeout(mConnectTimeOut);
    conn.connect();

    handleConnect(conn);
  }

  /**
   * 处理断点
   */
  private void handleBreakpoint(HttpURLConnection conn) throws IOException {
    //不支持断点只能单线程下载
    if (!isSupportBreakpoint) {
      handleNoSupportBreakpointDownload(conn);
      return;
    }
    int fileLength = conn.getContentLength();
    if (fileLength < SUB_LEN) {
      THREAD_NUM = 1;
      CONSTANCE.THREAD_NUM = THREAD_NUM;
    }
    Properties pro = createConfigFile(fileLength);
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
        if (resumeRecordLocation(i, startL, endL)) return;
        continue;
      }
      //分配下载位置
      Object record = pro.getProperty(mDownloadFile.getName() + "_record_" + i);
      //如果有记录，则恢复下载
      if (!isNewTask && record != null && Long.parseLong(record + "") > 0) {
        Long r = Long.parseLong(record + "");
        CONSTANCE.CURRENT_LOCATION += r - startL;
        Log.d(TAG, "任务【" + mDownloadEntity.getFileName() + "】线程__" + i + "__恢复下载");
        mListener.onChildResume(r);
        startL = r;
        recordL[rl] = i;
        rl++;
      } else {
        handleNewTask();
      }
      if (isNewTask) {
        recordL[rl] = i;
        rl++;
      }
      if (i == (THREAD_NUM - 1)) {
        //如果整个文件的大小不为线程个数的整数倍，则最后一个线程的结束位置即为文件的总长度
        endL = fileLength;
      }
      addSingleTask(i, startL, endL, fileLength);
    }
    startSingleTask(recordL);
  }

  /**
   * 处理不支持断点的下载
   */
  private void handleNoSupportBreakpointDownload(HttpURLConnection conn) {
    ConfigEntity entity = new ConfigEntity();
    long len = conn.getContentLength();
    entity.FILE_SIZE = len;
    entity.DOWNLOAD_URL = mDownloadEntity.isRedirect() ? mDownloadEntity.getRedirectUrl()
        : mDownloadEntity.getDownloadUrl();
    entity.TEMP_FILE = mDownloadFile;
    entity.THREAD_ID = 0;
    entity.START_LOCATION = 0;
    entity.END_LOCATION = entity.FILE_SIZE;
    entity.CONFIG_FILE_PATH = mConfigFile.getPath();
    entity.isSupportBreakpoint = isSupportBreakpoint;
    entity.DOWNLOAD_TASK_ENTITY = mDownloadTaskEntity;
    THREAD_NUM = 1;
    CONSTANCE.THREAD_NUM = THREAD_NUM;
    SingleThreadTask task = new SingleThreadTask(CONSTANCE, mListener, entity);
    mTask.put(0, task);
    mFixedThreadPool.execute(task);
    mListener.onPostPre(len);
    mListener.onStart(0);
  }

  /**
   * 创建配置文件
   */
  private Properties createConfigFile(long fileLength) throws IOException {
    Properties pro = null;
    //必须建一个文件
    CommonUtil.createFile(mDownloadFile.getPath());
    BufferedRandomAccessFile file =
        new BufferedRandomAccessFile(new File(mDownloadFile.getPath()), "rwd", 8192);
    //设置文件长度
    file.setLength(fileLength);
    mListener.onPostPre(fileLength);
    //分配每条线程的下载区间
    pro = CommonUtil.loadConfig(mConfigFile);
    if (pro.isEmpty()) {
      handleNewTask();
    } else {
      Set<Object> keys = pro.keySet();
      int num = 0;
      for (Object key : keys) {
        if (String.valueOf(key).contains("_record_")) {
          num++;
        }
      }
      if (num == 0) {
        handleNewTask();
        return pro;
      }
      THREAD_NUM = num;
      for (int i = 0; i < THREAD_NUM; i++) {
        if (pro.getProperty(mDownloadFile.getName() + "_record_" + i) == null) {
          Object state = pro.getProperty(mDownloadFile.getName() + "_state_" + i);
          if (state != null && Integer.parseInt(state + "") == 1) {
            continue;
          }
          handleNewTask();
          return pro;
        }
      }
      isNewTask = false;
    }
    return pro;
  }

  /**
   * 处理新任务
   */
  private void handleNewTask() {
    isNewTask = true;
    THREAD_NUM = AriaManager.getInstance(mContext).getDownloadConfig().getThreadNum();
  }

  /**
   * 恢复记录地址
   *
   * @return true 表示下载完成
   */
  private boolean resumeRecordLocation(int i, long startL, long endL) {
    CONSTANCE.CURRENT_LOCATION += endL - startL;
    Log.d(TAG, "++++++++++ 线程_" + i + "_已经下载完成 ++++++++++");
    CONSTANCE.COMPLETE_THREAD_NUM++;
    CONSTANCE.STOP_NUM++;
    CONSTANCE.CANCEL_NUM++;
    if (CONSTANCE.isComplete()) {
      if (mConfigFile.exists()) {
        mConfigFile.delete();
      }
      mListener.onComplete();
      CONSTANCE.isDownloading = false;
      return true;
    }
    return false;
  }

  /**
   * 创建单线程任务
   */
  private void addSingleTask(int i, long startL, long endL, long fileLength) {
    ConfigEntity entity = new ConfigEntity();
    entity.FILE_SIZE = fileLength;
    entity.DOWNLOAD_URL = mDownloadEntity.isRedirect() ? mDownloadEntity.getRedirectUrl()
        : mDownloadEntity.getDownloadUrl();
    entity.TEMP_FILE = mDownloadFile;
    entity.THREAD_ID = i;
    entity.START_LOCATION = startL;
    entity.END_LOCATION = endL;
    entity.CONFIG_FILE_PATH = mConfigFile.getPath();
    entity.isSupportBreakpoint = isSupportBreakpoint;
    entity.DOWNLOAD_TASK_ENTITY = mDownloadTaskEntity;
    CONSTANCE.THREAD_NUM = THREAD_NUM;
    SingleThreadTask task = new SingleThreadTask(CONSTANCE, mListener, entity);
    mTask.put(i, task);
  }

  /**
   * 启动单线程下载任务
   */
  private void startSingleTask(int[] recordL) {
    if (CONSTANCE.CURRENT_LOCATION > 0) {
      mListener.onResume(CONSTANCE.CURRENT_LOCATION);
    } else {
      mListener.onStart(CONSTANCE.CURRENT_LOCATION);
    }
    mFixedThreadPool = Executors.newFixedThreadPool(recordL.length);
    for (int l : recordL) {
      if (l == -1) continue;
      Runnable task = mTask.get(l);
      if (task != null) {
        mFixedThreadPool.execute(task);
      }
    }
  }

  /**
   * 子线程下载信息类
   */
  final static class ConfigEntity {
    //文件大小
    int THREAD_ID;
    long FILE_SIZE;
    long START_LOCATION;
    long END_LOCATION;
    File TEMP_FILE;
    String DOWNLOAD_URL;
    String CONFIG_FILE_PATH;
    DownloadTaskEntity DOWNLOAD_TASK_ENTITY;
    boolean isSupportBreakpoint = true;
  }
}
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

package com.arialyy.aria.core.task;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import com.arialyy.aria.core.DownloadEntity;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lyy on 2015/8/25.
 * 下载工具类
 */
final class DownloadUtil implements IDownloadUtil, Runnable {
  private static final String TAG  = "DownloadUtil";
  private static final Object LOCK = new Object();
  /**
   * 线程数
   */
  private final int               THREAD_NUM;
  //下载监听
  private       IDownloadListener mListener;
  private int     mConnectTimeOut     = 5000 * 4; //连接超时时间
  private int     mReadTimeOut        = 5000 * 20; //流读取的超时时间
  /**
   * 已经完成下载任务的线程数量
   */
  private boolean isDownloading       = false;
  private boolean isStop              = false;
  private boolean isCancel            = false;
  private boolean isNewTask           = true;
  private boolean isSupportBreakpoint = true;
  private int     mCompleteThreadNum  = 0;
  private int     mCancelNum          = 0;
  private long    mCurrentLocation    = 0;
  private int     mStopNum            = 0;
  private int     mFailNum            = 0;
  private Context         mContext;
  private DownloadEntity  mDownloadEntity;
  private ExecutorService mFixedThreadPool;
  private File            mDownloadFile; //下载的文件
  private File            mConfigFile;//下载信息配置文件
  private SparseArray<Runnable> mTask = new SparseArray<>();

  DownloadUtil(Context context, DownloadEntity entity, IDownloadListener downloadListener) {
    this(context, entity, downloadListener, 3);
  }

  DownloadUtil(Context context, DownloadEntity entity, IDownloadListener downloadListener,
      int threadNum) {
    mContext = context.getApplicationContext();
    mDownloadEntity = entity;
    mListener = downloadListener;
    THREAD_NUM = threadNum;
    mFixedThreadPool = Executors.newFixedThreadPool(Integer.MAX_VALUE);
    init();
  }

  private void init() {
    mDownloadFile = new File(mDownloadEntity.getDownloadPath());
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
    return mCurrentLocation;
  }

  @Override public boolean isDownloading() {
    return isDownloading;
  }

  /**
   * 取消下载
   */
  @Override public void cancelDownload() {
    isCancel = true;
    isDownloading = false;
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
    isStop = true;
    isDownloading = false;
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
    isDownloading = true;
    mCurrentLocation = 0;
    isStop = false;
    isCancel = false;
    mCancelNum = 0;
    mStopNum = 0;
    mFailNum = 0;
    mListener.onPre();
    new Thread(this).start();
  }

  @Override public void resumeDownload() {
    startDownload();
  }

  private void failDownload(String msg) {
    Log.e(TAG, msg);
    isDownloading = false;
    stopDownload();
    mListener.onFail();
  }

  private void setConnectParam(HttpURLConnection conn) throws ProtocolException {
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Charset", "UTF-8");
    conn.setRequestProperty("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
    conn.setRequestProperty("Accept",
        "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
  }

  @Override public void run() {
    try {
      URL               url  = new URL(mDownloadEntity.getDownloadUrl());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      setConnectParam(conn);
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
        if (len < 0){
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
      entity.fileSize = conn.getContentLength();
      entity.downloadUrl = mDownloadEntity.getDownloadUrl();
      entity.tempFile = mDownloadFile;
      entity.threadId = 0;
      entity.startLocation = 0;
      entity.endLocation = entity.fileSize;
      SingleThreadTask task = new SingleThreadTask(entity);
      mFixedThreadPool.execute(task);
      mListener.onStart(0);
      return;
    }
    int fileLength = conn.getContentLength();
    //必须建一个文件
    CommonUtil.createFile(mDownloadFile.getPath());
    RandomAccessFile file = new RandomAccessFile(mDownloadFile.getPath(), "rwd");
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
    int   blockSize = fileLength / THREAD_NUM;
    int[] recordL   = new int[THREAD_NUM];
    int   rl        = 0;
    for (int i = 0; i < THREAD_NUM; i++) {
      recordL[i] = -1;
    }
    for (int i = 0; i < THREAD_NUM; i++) {
      long   startL = i * blockSize, endL = (i + 1) * blockSize;
      Object state  = pro.getProperty(mDownloadFile.getName() + "_state_" + i);
      if (state != null && Integer.parseInt(state + "") == 1) {  //该线程已经完成
        mCurrentLocation += endL - startL;
        Log.d(TAG, "++++++++++ 线程_" + i + "_已经下载完成 ++++++++++");
        mCompleteThreadNum++;
        mStopNum++;
        mCancelNum++;
        if (mCompleteThreadNum == THREAD_NUM) {
          if (mConfigFile.exists()) {
            mConfigFile.delete();
          }
          mListener.onComplete();
          isDownloading = false;
          return;
        }
        continue;
      }
      //分配下载位置
      Object record = pro.getProperty(mDownloadFile.getName() + "_record_" + i);
      //如果有记录，则恢复下载
      if (!isNewTask && record != null && Long.parseLong(record + "") > 0) {
        Long r = Long.parseLong(record + "");
        mCurrentLocation += r - startL;
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
      entity.fileSize = fileLength;
      entity.downloadUrl = mDownloadEntity.getDownloadUrl();
      entity.tempFile = mDownloadFile;
      entity.threadId = i;
      entity.startLocation = startL;
      entity.endLocation = endL;
      SingleThreadTask task = new SingleThreadTask(entity);
      mTask.put(i, task);
    }
    if (mCurrentLocation > 0) {
      mListener.onResume(mCurrentLocation);
    } else {
      mListener.onStart(mCurrentLocation);
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
  private static class ConfigEntity {
    //文件大小
    long    fileSize;
    String  downloadUrl;
    int     threadId;
    long    startLocation;
    long    endLocation;
    File    tempFile;
  }

  /**
   * 单个线程的下载任务
   */
  private class SingleThreadTask implements Runnable {
    private static final String TAG = "SingleThreadTask";
    private ConfigEntity configEntity;
    private String       configFPath;
    private long currentLocation = 0;

    private SingleThreadTask(ConfigEntity downloadInfo) {
      this.configEntity = downloadInfo;
      if (isSupportBreakpoint) {
        configFPath = mContext.getFilesDir().getPath()
            + "/temp/"
            + configEntity.tempFile.getName()
            + ".properties";
      }
    }

    @Override public void run() {
      HttpURLConnection conn = null;
      InputStream       is   = null;
      try {
        URL url = new URL(configEntity.downloadUrl);
        conn = (HttpURLConnection) url.openConnection();
        if (isSupportBreakpoint) {
          Log.d(TAG, "线程_"
              + configEntity.threadId
              + "_正在下载【开始位置 : "
              + configEntity.startLocation
              + "，结束位置："
              + configEntity.endLocation
              + "】");
          //在头里面请求下载开始位置和结束位置
          conn.setRequestProperty("Range",
              "bytes=" + configEntity.startLocation + "-" + configEntity.endLocation);
        } else {
          Log.w(TAG, "该下载不支持断点，即将重新下载");
        }
        setConnectParam(conn);
        conn.setConnectTimeout(mConnectTimeOut);
        conn.setReadTimeout(mReadTimeOut);  //设置读取流的等待时间,必须设置该参数
        is = conn.getInputStream();
        //创建可设置位置的文件
        RandomAccessFile file = new RandomAccessFile(configEntity.tempFile, "rwd");
        //设置每条线程写入文件的位置
        file.seek(configEntity.startLocation);
        byte[] buffer = new byte[1024];
        int    len;
        //当前子线程的下载位置
        currentLocation = configEntity.startLocation;
        while ((len = is.read(buffer)) != -1) {
          if (isCancel) {
            Log.d(TAG, "++++++++++ thread_" + configEntity.threadId + "_cancel ++++++++++");
            break;
          }
          if (isStop) {
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

        if (isCancel) {
          return;
        }
        //停止状态不需要删除记录文件
        if (isStop) {
          return;
        }
        //支持断点的处理
        if (isSupportBreakpoint) {
          Log.i(TAG, "线程【" + configEntity.threadId + "】下载完毕");
          writeConfig(configEntity.tempFile.getName() + "_state_" + configEntity.threadId, 1 + "");
          mListener.onChildComplete(configEntity.endLocation);
          mCompleteThreadNum++;
          if (mCompleteThreadNum == THREAD_NUM) {
            File configFile = new File(configFPath);
            if (configFile.exists()) {
              configFile.delete();
            }
            isDownloading = false;
            mListener.onComplete();
          }
        } else {
          Log.i(TAG, "下载任务完成");
          isDownloading = false;
          mListener.onComplete();
        }
      } catch (MalformedURLException e) {
        mFailNum++;
        failDownload(configEntity, currentLocation, "下载链接异常", e);
      } catch (IOException e) {
        mFailNum++;
        failDownload(configEntity, currentLocation, "下载失败【" + configEntity.downloadUrl + "】", e);
      } catch (Exception e) {
        mFailNum++;
        failDownload(configEntity, currentLocation, "获取流失败", e);
      }
    }

    /**
     * 停止下载
     */
    protected void stop() {
      synchronized (LOCK) {
        try {
          if (isSupportBreakpoint) {
            mStopNum++;
            String location = String.valueOf(currentLocation);
            Log.i(TAG,
                "thread_" + configEntity.threadId + "_stop, stop location ==> " + currentLocation);
            writeConfig(configEntity.tempFile.getName() + "_record_" + configEntity.threadId,
                location);
            if (mStopNum == THREAD_NUM) {
              Log.d(TAG, "++++++++++++++++ onStop +++++++++++++++++");
              isDownloading = false;
              mListener.onStop(mCurrentLocation);
            }
          } else {
            Log.d(TAG, "++++++++++++++++ onStop +++++++++++++++++");
            isDownloading = false;
            mListener.onStop(mCurrentLocation);
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
        currentLocation += len;
        mCurrentLocation += len;
        mListener.onProgress(mCurrentLocation);
      }
    }

    /**
     * 取消下载
     */
    private void cancel() {
      synchronized (LOCK) {
        if (isSupportBreakpoint) {
          mCancelNum++;
          if (mCancelNum == THREAD_NUM) {
            File configFile = new File(configFPath);
            if (configFile.exists()) {
              configFile.delete();
            }
            if (configEntity.tempFile.exists()) {
              configEntity.tempFile.delete();
            }
            Log.d(TAG, "++++++++++++++++ onCancel +++++++++++++++++");
            isDownloading = false;
            mListener.onCancel();
          }
        } else {
          Log.d(TAG, "++++++++++++++++ onCancel +++++++++++++++++");
          isDownloading = false;
          mListener.onCancel();
        }
      }
    }

    /**
     * 下载失败
     */
    private void failDownload(ConfigEntity dEntity, long currentLocation, String msg,
        Exception ex) {
      synchronized (LOCK) {
        try {
          isDownloading = false;
          isStop = true;
          if (ex != null) {
            Log.e(TAG, CommonUtil.getPrintException(ex));
          }
          if (isSupportBreakpoint) {
            if (currentLocation != -1) {
              String location = String.valueOf(currentLocation);
              writeConfig(dEntity.tempFile.getName() + "_record_" + dEntity.threadId, location);
            }
            if (mFailNum == THREAD_NUM) {
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
      File       configFile = new File(configFPath);
      Properties pro        = CommonUtil.loadConfig(configFile);
      pro.setProperty(key, record);
      CommonUtil.saveConfig(configFile, pro);
    }
  }
}
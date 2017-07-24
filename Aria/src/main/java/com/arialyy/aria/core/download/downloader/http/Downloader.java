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
package com.arialyy.aria.core.download.downloader.http;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.download.downloader.ChildThreadConfigEntity;
import com.arialyy.aria.core.download.downloader.IDownloadListener;
import com.arialyy.aria.core.download.downloader.IDownloadUtil;
import com.arialyy.aria.core.download.downloader.StateConstance;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by AriaL on 2017/7/1.
 * 文件下载器
 */
class Downloader implements Runnable, IDownloadUtil {
  private final String TAG = "Downloader";
  private IDownloadListener mListener;
  private DownloadTaskEntity mTaskEntity;
  private DownloadEntity mEntity;
  private ExecutorService mFixedThreadPool;
  private File mConfigFile;//下载信息配置文件
  private Context mContext;
  private File mTempFile; //下载的文件
  private boolean isNewTask = true;
  private int mThreadNum, mRealThreadNum;
  private StateConstance mConstance;
  private SparseArray<Runnable> mTask = new SparseArray<>();

  /**
   * 小于1m的文件不启用多线程
   */
  private static final long SUB_LEN = 1024 * 1024;
  private Timer mTimer;

  Downloader(IDownloadListener listener, DownloadTaskEntity taskEntity) {
    mListener = listener;
    mTaskEntity = taskEntity;
    mEntity = mTaskEntity.getEntity();
    mContext = AriaManager.APP;
    mConstance = new StateConstance();
  }

  @Override
  public void setMaxSpeed(double maxSpeed) {
    for (int i = 0; i < mThreadNum; i++) {
      HttpThreadTask task = (HttpThreadTask) mTask.get(i);
      if (task != null) {
        task.setMaxSpeed(maxSpeed);
      }
    }
  }

  public StateConstance getConstance() {
    return mConstance;
  }

  @Override public void run() {
    startFlow();
  }

  /**
   * 开始下载流程
   */
  private void startFlow() {
    checkTask();
    mListener.onPostPre(mEntity.getFileSize());
    mConstance.cleanState();
    mConstance.isDownloading = true;
    if (!mTaskEntity.isSupportBP) {
      mThreadNum = 1;
      mConstance.THREAD_NUM = mThreadNum;
      handleNoSupportBreakpointDownload();
    } else {
      mThreadNum = isNewTask ? (mEntity.getFileSize() <= SUB_LEN ? 1
          : AriaManager.getInstance(mContext).getDownloadConfig().getThreadNum()) : mRealThreadNum;
      mConstance.THREAD_NUM = mThreadNum;
      mFixedThreadPool = Executors.newFixedThreadPool(mThreadNum);
      handleBreakpoint();
    }
    startTimer();
  }

  /**
   * 启动进度获取定时器
   */
  private void startTimer() {
    mTimer = new Timer(true);
    mTimer.schedule(new TimerTask() {
      @Override public void run() {
        if (mConstance.isComplete() || !mConstance.isDownloading) {
          closeTimer();
        } else if (mConstance.CURRENT_LOCATION >= 0) {
          mListener.onProgress(mConstance.CURRENT_LOCATION);
        }
      }
    }, 0, 1000);
  }

  private void closeTimer() {
    if (mTimer != null) {
      mTimer.purge();
      mTimer.cancel();
    }
  }

  @Override public long getFileSize() {
    return mEntity.getFileSize();
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

  @Override public void cancelDownload() {
    closeTimer();
    mConstance.isCancel = true;
    mConstance.isDownloading = false;
    if (mFixedThreadPool != null) {
      mFixedThreadPool.shutdown();
    }
    for (int i = 0; i < mThreadNum; i++) {
      HttpThreadTask task = (HttpThreadTask) mTask.get(i);
      if (task != null) {
        task.cancel();
      }
    }
    CommonUtil.delDownloadTaskConfig(mTaskEntity.removeFile, mTaskEntity);
  }

  @Override public void stopDownload() {
    closeTimer();
    if (mConstance.isComplete()) return;
    mConstance.isStop = true;
    mConstance.isDownloading = false;
    if (mFixedThreadPool != null) {
      mFixedThreadPool.shutdown();
    }
    for (int i = 0; i < mThreadNum; i++) {
      HttpThreadTask task = (HttpThreadTask) mTask.get(i);
      if (task != null) {
        task.stop();
      }
    }
  }

  /**
   * 直接调用的时候会自动启动线程执行
   */
  @Override public void startDownload() {
    new Thread(this).start();
  }

  @Override public void resumeDownload() {
    startDownload();
  }

  /**
   * 返回该下载器的
   */
  public IDownloadListener getListener() {
    return mListener;
  }

  /**
   * 检查任务是否是新任务，新任务条件：
   * 1、文件不存在
   * 2、下载记录文件不存在
   * 3、下载记录文件缺失或不匹配
   * 4、数据库记录不存在
   * 5、不支持断点，则是新任务
   */
  private void checkTask() {
    if (!mTaskEntity.isSupportBP) {
      isNewTask = true;
      return;
    }
    mConfigFile = new File(mContext.getFilesDir().getPath()
        + AriaManager.DOWNLOAD_TEMP_DIR
        + mEntity.getFileName()
        + ".properties");
    mTempFile = new File(mEntity.getDownloadPath());
    if (!mConfigFile.exists()) { //记录文件被删除，则重新下载
      isNewTask = true;
      CommonUtil.createFile(mConfigFile.getPath());
    } else if (!mTempFile.exists()) {
      isNewTask = true;
    } else if (DbEntity.findFirst(DownloadEntity.class, "downloadUrl=?", mEntity.getDownloadUrl())
        == null) {
      isNewTask = true;
    } else {
      isNewTask = checkConfigFile();
    }
  }

  /**
   * 检查记录文件，如果是新任务返回{@code true}，否则返回{@code false}
   */
  private boolean checkConfigFile() {
    Properties pro = CommonUtil.loadConfig(mConfigFile);
    if (pro.isEmpty()) {
      return true;
    }
    Set<Object> keys = pro.keySet();
    int num = 0;
    for (Object key : keys) {
      if (String.valueOf(key).contains("_record_")) {
        num++;
      }
    }
    if (num == 0) {
      return true;
    }
    mRealThreadNum = num;
    for (int i = 0; i < mRealThreadNum; i++) {
      if (pro.getProperty(mTempFile.getName() + "_record_" + i) == null) {
        Object state = pro.getProperty(mTempFile.getName() + "_state_" + i);
        if (state != null && Integer.parseInt(state + "") == 1) {
          continue;
        }
        return true;
      }
    }
    return false;
  }

  /**
   * 恢复记录地址
   *
   * @return true 表示下载完成
   */
  private boolean resumeRecordLocation(int i, long startL, long endL) {
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
      return true;
    }
    return false;
  }

  /**
   * 创建单线程任务
   */
  private void addSingleTask(int i, long startL, long endL, long fileLength) {
    ChildThreadConfigEntity entity = new ChildThreadConfigEntity();
    entity.FILE_SIZE = fileLength;
    entity.DOWNLOAD_URL =
        mEntity.isRedirect() ? mEntity.getRedirectUrl() : mEntity.getDownloadUrl();
    entity.TEMP_FILE = mTempFile;
    entity.THREAD_ID = i;
    entity.START_LOCATION = startL;
    entity.END_LOCATION = endL;
    entity.CONFIG_FILE_PATH = mConfigFile.getPath();
    entity.IS_SUPPORT_BREAK_POINT = mTaskEntity.isSupportBP;
    entity.DOWNLOAD_TASK_ENTITY = mTaskEntity;
    HttpThreadTask task = new HttpThreadTask(mConstance, mListener, entity);
    mTask.put(i, task);
  }

  /**
   * 启动单线程下载任务
   */
  private void startSingleTask(int[] recordL) {
    if (mConstance.CURRENT_LOCATION > 0) {
      mListener.onResume(mConstance.CURRENT_LOCATION);
    } else {
      mListener.onStart(mConstance.CURRENT_LOCATION);
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
   * 处理断点
   */
  private void handleBreakpoint() {
    long fileLength = mEntity.getFileSize();
    Properties pro = CommonUtil.loadConfig(mConfigFile);
    int blockSize = (int) (fileLength / mThreadNum);
    int[] recordL = new int[mThreadNum];
    for (int i = 0; i < mThreadNum; i++) {
      recordL[i] = -1;
    }
    int rl = 0;
    if (isNewTask) {
      createNewFile(fileLength);
    }
    for (int i = 0; i < mThreadNum; i++) {
      long startL = i * blockSize, endL = (i + 1) * blockSize;
      Object state = pro.getProperty(mTempFile.getName() + "_state_" + i);
      if (state != null && Integer.parseInt(state + "") == 1) {  //该线程已经完成
        if (resumeRecordLocation(i, startL, endL)) return;
        continue;
      }
      //分配下载位置
      Object record = pro.getProperty(mTempFile.getName() + "_record_" + i);
      //如果有记录，则恢复下载
      if (!isNewTask && record != null && Long.parseLong(record + "") >= 0) {
        Long r = Long.parseLong(record + "");
        mConstance.CURRENT_LOCATION += r - startL;
        Log.d(TAG, "任务【" + mEntity.getFileName() + "】线程__" + i + "__恢复下载");
        startL = r;
        recordL[rl] = i;
        rl++;
      } else {
        recordL[rl] = i;
        rl++;
      }
      if (i == (mThreadNum - 1)) {
        //最后一个线程的结束位置即为文件的总长度
        endL = fileLength;
      }
      addSingleTask(i, startL, endL, fileLength);
    }
    startSingleTask(recordL);
  }

  /**
   * 创建新的下载文件
   */
  private void createNewFile(long fileLength) {
    CommonUtil.createFile(mTempFile.getPath());
    BufferedRandomAccessFile file = null;
    try {
      file = new BufferedRandomAccessFile(new File(mTempFile.getPath()), "rwd", 8192);
      //设置文件长度
      file.setLength(fileLength);
    } catch (IOException e) {
      failDownload("下载失败【downloadUrl:"
          + mEntity.getDownloadUrl()
          + "】\n【filePath:"
          + mEntity.getDownloadPath()
          + "】\n"
          + CommonUtil.getPrintException(e));
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * 处理不支持断点的下载
   */
  private void handleNoSupportBreakpointDownload() {
    ChildThreadConfigEntity entity = new ChildThreadConfigEntity();
    long len = mEntity.getFileSize();
    entity.FILE_SIZE = len;
    entity.DOWNLOAD_URL =
        mEntity.isRedirect() ? mEntity.getRedirectUrl() : mEntity.getDownloadUrl();
    entity.TEMP_FILE = mTempFile;
    entity.THREAD_ID = 0;
    entity.START_LOCATION = 0;
    entity.END_LOCATION = entity.FILE_SIZE;
    entity.CONFIG_FILE_PATH = mConfigFile.getPath();
    entity.IS_SUPPORT_BREAK_POINT = mTaskEntity.isSupportBP;
    entity.DOWNLOAD_TASK_ENTITY = mTaskEntity;
    HttpThreadTask task = new HttpThreadTask(mConstance, mListener, entity);
    mTask.put(0, task);
    mFixedThreadPool.execute(task);
    mListener.onPostPre(len);
    mListener.onStart(0);
  }

  private void failDownload(String errorMsg) {
    closeTimer();
    Log.e(TAG, errorMsg);
    mConstance.isDownloading = false;
    mListener.onFail();
  }
}

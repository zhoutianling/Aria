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
package com.arialyy.aria.core.common;

import android.os.Build;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.inf.AbsNormalEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IEventListener;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.ErrorHelp;
import com.arialyy.aria.util.NetUtils;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lyy on 2017/1/18.
 * 任务线程
 */
public abstract class AbsThreadTask<ENTITY extends AbsNormalEntity, TASK_ENTITY extends AbsTaskEntity<ENTITY>>
    implements Runnable {
  /**
   * 线程重试次数
   */
  private final int RETRY_NUM = 2;
  /**
   * 线程重试间隔
   */
  private final int RETRY_INTERVAL = 5000;
  private final String TAG = "AbsThreadTask";
  protected long mChildCurrentLocation = 0, mSleepTime = 0;
  protected int mBufSize;
  protected IEventListener mListener;
  protected StateConstance STATE;
  protected SubThreadConfig<TASK_ENTITY> mConfig;
  protected ENTITY mEntity;
  protected TASK_ENTITY mTaskEntity;
  private int mFailTimes = 0;
  private Timer mFailTimer;
  private long mLastSaveTime;
  private ExecutorService mConfigThreadPool;
  protected int mConnectTimeOut; //连接超时时间
  protected int mReadTimeOut; //流读取的超时时间
  protected boolean isNotNetRetry = false;  //断网情况是否重试
  private boolean taskBreak = false;  //任务中断

  private Thread mConfigThread = new Thread(new Runnable() {
    @Override public void run() {
      final long currentTemp = mChildCurrentLocation;
      writeConfig(false, currentTemp);
    }
  });

  protected AbsThreadTask(StateConstance constance, IEventListener listener,
      SubThreadConfig<TASK_ENTITY> info) {
    STATE = constance;
    mListener = listener;
    mConfig = info;
    mTaskEntity = mConfig.TASK_ENTITY;
    mEntity = mTaskEntity.getEntity();
    mLastSaveTime = System.currentTimeMillis();
    mConfigThreadPool = Executors.newCachedThreadPool();
  }

  public void setMaxSpeed(double maxSpeed) {
    if (-0.9999 < maxSpeed && maxSpeed < 0.00001) {
      mSleepTime = 0;
    } else {
      BigDecimal db = new BigDecimal(
          ((mBufSize / 1024) * (filterVersion() ? 1 : STATE.START_THREAD_NUM) / maxSpeed) * 1000);
      mSleepTime = db.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
    }
  }

  /**
   * 当前线程是否完成，对于不支持断点的任务，一律未完成
   * {@code true} 完成；{@code false} 未完成
   */
  public boolean isThreadComplete() {
    return mConfig.THREAD_RECORD.isComplete;
  }

  /**
   * 获取线程配置信息
   */
  public SubThreadConfig getConfig() {
    return mConfig;
  }

  /**
   * 当前线程下载进度
   */
  public long getCurrentLocation() {
    return mChildCurrentLocation;
  }

  private boolean filterVersion() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
  }

  @Override protected void finalize() throws Throwable {
    super.finalize();
    if (mConfigThreadPool != null) {
      mConfigThreadPool.shutdown();
    }
  }

  /**
   * 任务是否中断，中断条件：
   * 1、任务取消
   * 2、任务停止
   * 3、手动中断 {@link #taskBreak}
   *
   * @return {@code true} 中断，{@code false} 不是中断
   */
  protected boolean isBreak() {
    return STATE.isCancel || STATE.isStop || taskBreak;
  }

  /**
   * 停止任务
   */
  public void stop() {
    synchronized (AriaManager.LOCK) {
      if (mConfig.SUPPORT_BP) {
        final long currentTemp = mChildCurrentLocation;
        STATE.STOP_NUM++;
        ALog.d(TAG, String.format("任务【%s】thread__%s__停止【停止位置：%s】", mConfig.TEMP_FILE.getName(),
            mConfig.THREAD_ID, currentTemp));
        writeConfig(false, currentTemp);
        if (STATE.isStop()) {
          ALog.i(TAG, String.format("任务【%s】已停止", mConfig.TEMP_FILE.getName()));
          STATE.isRunning = false;
          mListener.onStop(STATE.CURRENT_LOCATION);
        }
      } else {
        ALog.i(TAG, String.format("任务【%s】已停止", mConfig.TEMP_FILE.getName()));
        STATE.isRunning = false;
        mListener.onStop(STATE.CURRENT_LOCATION);
      }
    }
  }

  /**
   * 执行中
   */
  protected void progress(long len) {
    synchronized (AriaManager.LOCK) {
      if (STATE.CURRENT_LOCATION > mEntity.getFileSize()) {
        taskBreak = true;
        fail(mChildCurrentLocation, "下载失败，下载长度超出文件大小", null, false);
        return;
      }
      mChildCurrentLocation += len;
      STATE.CURRENT_LOCATION += len;
      if (System.currentTimeMillis() - mLastSaveTime > 5000
          && mChildCurrentLocation < mConfig.END_LOCATION) {
        mLastSaveTime = System.currentTimeMillis();
        if (!mConfigThreadPool.isShutdown()) {
          mConfigThreadPool.execute(mConfigThread);
        }
      }
    }
  }

  /**
   * 取消任务
   */
  public void cancel() {
    synchronized (AriaManager.LOCK) {
      if (mConfig.SUPPORT_BP) {
        STATE.CANCEL_NUM++;
        ALog.d(TAG,
            String.format("任务【%s】thread__%s__取消", mConfig.TEMP_FILE.getName(), mConfig.THREAD_ID));
        if (STATE.isCancel()) {
          if (mConfig.TEMP_FILE.exists() && !(mEntity instanceof UploadEntity)) {
            mConfig.TEMP_FILE.delete();
          }
          ALog.d(TAG, String.format("任务【%s】已取消", mConfig.TEMP_FILE.getName()));
          STATE.isRunning = false;
          mListener.onCancel();
        }
      } else {
        ALog.d(TAG, String.format("任务【%s】已取消", mConfig.TEMP_FILE.getName()));
        STATE.isRunning = false;
        mListener.onCancel();
      }
    }
  }

  /**
   * 线程任务失败
   *
   * @param subCurrentLocation 当前线程下载进度
   * @param msg 自定义信息
   * @param ex 异常信息
   */
  protected void fail(final long subCurrentLocation, String msg, Exception ex) {
    fail(subCurrentLocation, msg, ex, true);
  }

  /**
   * 任务失败
   *
   * @param subCurrentLocation 当前子线程进度
   */
  protected void fail(final long subCurrentLocation, String msg, Exception ex, boolean needRetry) {
    synchronized (AriaManager.LOCK) {
      if (ex != null) {
        ALog.e(TAG, msg + "\n" + ALog.getExceptionString(ex));
      } else {
        ALog.e(TAG, msg);
      }
      if (mConfig.SUPPORT_BP) {
        writeConfig(false, subCurrentLocation);
        retryThis(needRetry && STATE.START_THREAD_NUM != 1);
      } else {
        ALog.e(TAG, String.format("任务【%s】执行失败", mConfig.TEMP_FILE.getName()));
        mListener.onFail(true);
        ErrorHelp.saveError(TAG, "", ALog.getExceptionString(ex));
      }
    }
  }

  /**
   * 重试当前线程，如果其中一条线程已经下载失败，则任务该任务下载失败，并且停止该任务的所有线程
   *
   * @param needRetry 是否可以重试
   */
  private void retryThis(boolean needRetry) {
    if (mFailTimer != null) {
      mFailTimer.purge();
      mFailTimer.cancel();
    }
    if (!NetUtils.isConnected(AriaManager.APP) && !isNotNetRetry) {
      ALog.w(TAG, String.format("任务【%s】thread__%s__重试失败，网络未连接", mConfig.TEMP_FILE.getName(),
          mConfig.THREAD_ID));
    }
    if (mFailTimes < RETRY_NUM
        && needRetry
        && (NetUtils.isConnected(AriaManager.APP) || isNotNetRetry)
        && isBreak()) {
      mFailTimer = new Timer(true);
      mFailTimer.schedule(new TimerTask() {
        @Override public void run() {
          mFailTimes++;
          ALog.w(TAG, String.format("任务【%s】thread__%s__正在重试", mConfig.TEMP_FILE.getName(),
              mConfig.THREAD_ID));
          final long temp = mChildCurrentLocation;
          mConfig.START_LOCATION = mChildCurrentLocation == 0 ? mConfig.START_LOCATION : temp;
          AbsThreadTask.this.run();
        }
      }, RETRY_INTERVAL);
    } else {
      STATE.FAIL_NUM++;
      if (STATE.isFail()) {
        STATE.isRunning = false;
        STATE.isStop = true;
        ALog.e(TAG, String.format("任务【%s】执行失败", mConfig.TEMP_FILE.getName()));
        mListener.onFail(true);
      }
    }
  }

  /**
   * 将记录写入到配置文件
   *
   * @param isComplete 当前线程是否完成 {@code true}完成
   * @param record 当前下载进度
   */
  protected void writeConfig(boolean isComplete, final long record) {
    if (mConfig.THREAD_RECORD != null) {
      mConfig.THREAD_RECORD.isComplete = isComplete;
      if (0 < record && record < mConfig.END_LOCATION) {
        mConfig.THREAD_RECORD.startLocation = record;
      }
      mConfig.THREAD_RECORD.update();
    }
  }
}

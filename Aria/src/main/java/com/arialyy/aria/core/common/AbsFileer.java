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

import android.content.Context;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsNormalEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IDownloadListener;
import com.arialyy.aria.core.inf.IEventListener;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.aria.util.DbHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by AriaL on 2017/7/1.
 * 任务处理器
 */
public abstract class AbsFileer<ENTITY extends AbsNormalEntity, TASK_ENTITY extends AbsTaskEntity<ENTITY>>
    implements Runnable, IUtil {
  private static final String STATE = "_state_";
  private static final String RECORD = "_record_";
  /**
   * 分块文件路径
   */
  public static final String SUB_PATH = "%s.%s.part";
  /**
   * 小于1m的文件不启用多线程
   */
  protected static final long SUB_LEN = 1024 * 1024;

  private final String TAG = "AbsFileer";
  protected IEventListener mListener;
  protected TASK_ENTITY mTaskEntity;
  protected ENTITY mEntity;
  protected Context mContext;
  protected File mTempFile; //文件
  protected StateConstance mConstance;
  private ExecutorService mFixedThreadPool;
  //总线程数
  protected int mTotalThreadNum;
  //启动线程数
  private int mStartThreadNum;
  //已完成的线程数
  private int mCompleteThreadNum;
  private Map<Integer, AbsThreadTask> mTask = new HashMap<>();

  private Timer mTimer;
  @Deprecated private File mConfigFile;
  /**
   * 进度刷新间隔
   */
  private long mUpdateInterval = 1000;
  protected TaskRecord mRecord;

  protected AbsFileer(IEventListener listener, TASK_ENTITY taskEntity) {
    mListener = listener;
    mTaskEntity = taskEntity;
    mEntity = mTaskEntity.getEntity();
    mContext = AriaManager.APP;
    mConstance = new StateConstance();
  }

  public void setNewTask(boolean newTask) {
    mTaskEntity.setNewTask(newTask);
  }

  @Override public void setMaxSpeed(double maxSpeed) {
    for (int i = 0; i < mTotalThreadNum; i++) {
      AbsThreadTask task = mTask.get(i);
      if (task != null) {
        task.setMaxSpeed(maxSpeed);
      }
    }
  }

  @Override public void run() {
    if (mConstance.isRunning) {
      return;
    }
    startFlow();
  }

  /**
   * 开始流程
   */
  private void startFlow() {
    if (isBreak()) {
      return;
    }
    mConstance.resetState();
    checkTask();
    mConstance.TASK_RECORD = mRecord;
    if (mListener instanceof IDownloadListener) {
      ((IDownloadListener) mListener).onPostPre(mEntity.getFileSize());
    }
    if (!mTaskEntity.isSupportBP()) {
      mTotalThreadNum = 1;
      mStartThreadNum = 1;
    } else {
      mTotalThreadNum =
          mTaskEntity.isNewTask() ? (mStartThreadNum = setNewTaskThreadNum()) : mTotalThreadNum;
    }
    mConstance.START_THREAD_NUM = mTotalThreadNum;

    // 处理分块和动态文件参数
    if (mTaskEntity.isNewTask() && mTaskEntity instanceof DownloadTaskEntity) {
      AriaManager manager = AriaManager.getInstance(AriaManager.APP);
      mRecord.isBlock = mTotalThreadNum > 1 && manager.getDownloadConfig().isUseBlock();
      // 线程数不等1并且没有使用块下载，则认为没有使用动态文件
      mRecord.isOpenDynamicFile = mTotalThreadNum == 1 || mRecord.isBlock;
    }

    /*
     * mTaskEntity.getEntity().getFileSize() != mTempFile.length()为兼容以前老版本代码
     * 动态长度条件：
     * 1、总线程数为1，并且是新任务
     * 2、总线程数为1，不是新任务，但是长度不是文件全长度
     */
    if (mTotalThreadNum == 1 && (mTaskEntity.getEntity().getFileSize() != mTempFile.length())) {
      mRecord.isOpenDynamicFile = true;
    }

    if (!mTaskEntity.isSupportBP()) {
      handleNoSupportBP();
    } else {
      handleBreakpoint();
    }

    startTimer();
  }

  /**
   * 设置新任务的最大线程数
   */
  protected abstract int setNewTaskThreadNum();

  /**
   * 启动进度获取定时器
   */
  private void startTimer() {
    mTimer = new Timer(true);
    mTimer.schedule(new TimerTask() {
      @Override public void run() {
        if (mConstance.isComplete()
            || mConstance.isStop()
            || mConstance.isCancel()
            || !mConstance.isRunning) {
          closeTimer();
        } else if (mConstance.CURRENT_LOCATION >= 0) {
          mListener.onProgress(mConstance.CURRENT_LOCATION);
        }
      }
    }, 0, mUpdateInterval);
  }

  protected void closeTimer() {
    if (mTimer != null) {
      mTimer.purge();
      mTimer.cancel();
      mTimer = null;
    }
  }

  /**
   * 设置定时器更新间隔
   *
   * @param interval 单位毫秒，不能小于0
   */
  protected void setUpdateInterval(long interval) {
    if (interval < 0) {
      ALog.w(TAG, "更新间隔不能小于0，默认为1000毫秒");
      return;
    }
    mUpdateInterval = interval;
  }

  @Override public long getFileSize() {
    return mEntity.getFileSize();
  }

  /**
   * 获取当前任务位置
   */
  @Override public long getCurrentLocation() {
    return mConstance.CURRENT_LOCATION;
  }

  @Override public boolean isRunning() {
    return mConstance.isRunning;
  }

  @Override public void cancel() {
    closeTimer();
    mConstance.isRunning = false;
    mConstance.isCancel = true;
    if (mFixedThreadPool != null) {
      mFixedThreadPool.shutdown();
    }
    for (int i = 0; i < mStartThreadNum; i++) {
      AbsThreadTask task = mTask.get(i);
      if (task != null) {
        task.cancel();
      }
    }
  }

  @Override public void stop() {
    closeTimer();
    mConstance.isRunning = false;
    mConstance.isStop = true;
    if (mConstance.isComplete()) return;
    if (mFixedThreadPool != null) {
      mFixedThreadPool.shutdown();
    }
    for (int i = 0; i < mStartThreadNum; i++) {
      AbsThreadTask task = mTask.get(i);
      if (task != null) {
        task.stop();
      }
    }
  }

  /**
   * 直接调用的时候会自动启动线程执行
   */
  @Override public void start() {
    new Thread(this).start();
  }

  @Override public void resume() {
    start();
  }

  /**
   * 检查任务、检查线程数
   * 新任务条件：
   * 1、文件不存在
   * 2、任务记录文件缺失或不匹配
   * 3、数据库记录不存在
   * 4、不支持断点，则是新任务
   */
  private void checkTask() {
    mConfigFile = new File(CommonUtil.getFileConfigPath(false, mEntity.getFileName()));
    if (mConfigFile.exists()) {
      convertDb();
    } else {
      mRecord = DbHelper.getTaskRecord(mTaskEntity.getKey());
      if (mRecord == null) {
        initRecord(true);
      } else {
        if (mRecord.threadRecords == null || mRecord.threadRecords.isEmpty()) {
          initRecord(true);
        }
        //else if (mTempFile.length() == 0) {
        //  mRecord.deleteData();
        //  initRecord(true);
        //}
        else {
          for (ThreadRecord tr : mRecord.threadRecords) {
            if (tr.isComplete) {
              mCompleteThreadNum++;
            } else {
              mStartThreadNum++;
            }
          }
          mTotalThreadNum = mRecord.threadRecords.size();
          mTaskEntity.setNewTask(false);
        }
      }
    }
  }

  /**
   * convertDb 为兼容性代码
   * 从3.4.1开始，线程配置信息将存储在数据库中。
   * 将配置文件的内容复制到数据库中，并将配置文件删除
   */
  private void convertDb() {
    List<RecordWrapper> records =
        DbEntity.findRelationData(RecordWrapper.class, "TaskRecord.filePath=?",
            mTaskEntity.getKey());
    if (records == null || records.size() == 0) {
      Properties pro = CommonUtil.loadConfig(mConfigFile);
      if (pro.isEmpty()) {
        mTaskEntity.setNewTask(true);
        return;
      }
      initRecord(false);
      Set<Object> keys = pro.keySet();
      // 老版本记录是5s存一次，但是5s中内，如果线程执行完成，record记录是没有的，只有state记录...
      // 第一步应该是record 和 state去重取正确的线程数
      Set<Integer> set = new HashSet<>();
      for (Object key : keys) {
        String str = String.valueOf(key);
        int i = Integer.parseInt(str.substring(str.length() - 1, str.length()));
        set.add(i);
      }
      int threadNum = set.size();
      if (threadNum == 0) {
        mTaskEntity.setNewTask(true);
        return;
      }
      mRecord.threadNum = threadNum;
      mTotalThreadNum = threadNum;

      for (int i = 0; i < threadNum; i++) {
        ThreadRecord tRecord = new ThreadRecord();
        tRecord.key = mRecord.filePath;
        Object state = pro.getProperty(mTempFile.getName() + STATE + i);
        Object record = pro.getProperty(mTempFile.getName() + RECORD + i);
        if (state != null && Integer.parseInt(String.valueOf(state)) == 1) {
          mCompleteThreadNum++;
          tRecord.isComplete = true;
          continue;
        }
        mStartThreadNum++;
        if (record != null) {
          Long temp = Long.parseLong(String.valueOf(record));
          tRecord.startLocation = temp > 0 ? temp : 0;
        } else {
          tRecord.startLocation = 0;
        }
        mRecord.threadRecords.add(tRecord);
      }
      mConfigFile.delete();
    }
  }

  /**
   * 初始化记录
   */
  private void initRecord(boolean isNewTask) {
    mRecord = new TaskRecord();
    mRecord.fileName = mEntity.getFileName();
    mRecord.filePath = mTaskEntity.getKey();
    mRecord.threadRecords = new ArrayList<>();
    mRecord.isGroupRecord = mTaskEntity.getEntity().isGroupChild();
    if (mRecord.isGroupRecord) {
      if (mTaskEntity.getEntity() instanceof DownloadEntity) {
        mRecord.dGroupName = ((DownloadEntity) mTaskEntity.getEntity()).getGroupName();
      }
    }
    mTaskEntity.setNewTask(isNewTask);
  }

  /**
   * 保存任务记录
   */
  private void saveRecord() {
    mRecord.threadNum = mRecord.threadRecords.size();
    mRecord.save();
    for (ThreadRecord tr : mRecord.threadRecords) {
      tr.save();
    }
  }

  public TaskRecord getRecord() {
    return mRecord;
  }

  /**
   * 恢复记录地址
   *
   * @return {@code true}任务已完成
   */
  private boolean resumeRecordLocation(int i, long startL, long endL) {
    mConstance.CURRENT_LOCATION += endL - startL;
    ALog.d(TAG, String.format("任务【%s】线程__%s__已完成", mTaskEntity.getEntity().getFileName(), i));
    mConstance.COMPLETE_THREAD_NUM = mCompleteThreadNum;
    mConstance.STOP_NUM++;
    mConstance.CANCEL_NUM++;
    if (mConstance.isComplete()) {
      mRecord.deleteData();
      mListener.onComplete();
      mConstance.isRunning = false;
      return true;
    }
    return false;
  }

  /**
   * 启动断点任务时，创建单线程任务
   *
   * @param i 线程id
   * @param startL 该任务起始位置
   * @param endL 该任务结束位置
   * @param fileLength 该任务需要处理的文件长度
   */
  private AbsThreadTask createSingThreadTask(int i, long startL, long endL, long fileLength,
      ThreadRecord record) {
    SubThreadConfig<TASK_ENTITY> config = new SubThreadConfig<>();
    config.FILE_SIZE = fileLength;
    config.URL = mEntity.isRedirect() ? mEntity.getRedirectUrl() : mEntity.getUrl();
    config.TEMP_FILE =
        mRecord.isBlock ? new File(String.format(SUB_PATH, mTempFile.getPath(), i)) : mTempFile;
    config.THREAD_ID = i;
    config.START_LOCATION = startL;
    config.END_LOCATION = endL;
    config.SUPPORT_BP = mTaskEntity.isSupportBP();
    config.TASK_ENTITY = mTaskEntity;
    config.THREAD_RECORD = record;
    return selectThreadTask(config);
  }

  private void handleBreakpoint() {
    long fileLength = mEntity.getFileSize();
    long blockSize = fileLength / mTotalThreadNum;
    int[] threadId = new int[mTotalThreadNum];
    int rl = 0;

    mRecord.fileLength = fileLength;
    for (int i = 0; i < mTotalThreadNum; i++) {
      threadId[i] = -1;
    }
    if (mTaskEntity.isNewTask() && !handleNewTask()) {
      return;
    }
    for (int i = 0; i < mTotalThreadNum; i++) {
      long startL = i * blockSize, endL = (i + 1) * blockSize;
      ThreadRecord tr;
      boolean isNewTr = false;  // 是否是新的线程记录
      if (mTaskEntity.isNewTask()) {
        tr = new ThreadRecord();
        tr.key = mRecord.filePath;
        tr.threadId = i;
        isNewTr = true;
      } else {
        tr = mRecord.threadRecords.get(i);
      }
      if (tr.isComplete) {//该线程已经完成
        if (resumeRecordLocation(i, startL, endL)) return;
        continue;
      }

      //如果有记录，则恢复任务
      if (tr.startLocation >= 0) {
        Long r = tr.startLocation;
        //记录的位置需要在线程区间中
        if (startL < r && r < (i == (mTotalThreadNum - 1) ? fileLength : endL)) {
          mConstance.CURRENT_LOCATION += r - startL;
          startL = r;
        }
        ALog.d(TAG, String.format("任务【%s】线程__%s__恢复任务", mEntity.getFileName(), i));
      }
      //最后一个线程的结束位置即为文件的总长度
      if (i == (mTotalThreadNum - 1)) {
        endL = fileLength;
      }
      // 更新记录
      tr.startLocation = startL;
      tr.endLocation = endL;
      if (isNewTr) {
        mRecord.threadRecords.add(tr);
      }
      AbsThreadTask task = createSingThreadTask(i, startL, endL, fileLength, tr);
      if (task == null) return;
      mTask.put(i, task);
      threadId[rl] = i;
      rl++;
    }
    if (mConstance.CURRENT_LOCATION != 0
        && mConstance.CURRENT_LOCATION != mEntity.getCurrentProgress()) {
      ALog.d(TAG, "进度修正");
      mEntity.setCurrentProgress(mConstance.CURRENT_LOCATION);
    }
    saveRecord();
    startThreadTask(threadId);
  }

  /**
   * 启动单线程任务
   */
  private void startThreadTask(int[] recordL) {
    if (isBreak()) {
      return;
    }
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
   * 重试线程任务，只有线程创建成功才能重试
   */
  public void retryThreadTask() {
    if (isBreak()) {
      return;
    }

    if (mTask == null || mTask.size() == 0) {
      ALog.w(TAG, "没有线程任务");
      return;
    }
    Set<Integer> keys = mTask.keySet();
    for (Integer key : keys) {
      AbsThreadTask task = mTask.get(key);
      if (task != null && !task.isThreadComplete()) {
        task.getConfig().START_LOCATION = task.getConfig().THREAD_RECORD.startLocation;
        mConstance.isStop = false;
        mConstance.isCancel = false;
        mConstance.isRunning = true;
        startTimer();
        ALog.d(TAG, String.format("任务【%s】开始重试，线程__%s__【开始位置：%s，结束位置：%s】", mEntity.getFileName(),
            key, task.getConfig().START_LOCATION, task.getConfig().END_LOCATION));
        if (!mFixedThreadPool.isShutdown()) {
          mFixedThreadPool.execute(task);
        } else {
          ALog.w(TAG, "线程池已关闭");
          mListener.onFail(true);
        }
      }
    }
  }

  /**
   * 处理新任务
   *
   * @return {@code true}创建新任务失败
   */
  protected abstract boolean handleNewTask();

  /**
   * 处理不支持断点的任务
   */
  private void handleNoSupportBP() {
    SubThreadConfig<TASK_ENTITY> config = new SubThreadConfig<>();
    config.FILE_SIZE = mEntity.getFileSize();
    config.URL = mEntity.isRedirect() ? mEntity.getRedirectUrl() : mEntity.getUrl();
    config.TEMP_FILE = mTempFile;
    config.THREAD_ID = 0;
    config.START_LOCATION = 0;
    config.END_LOCATION = config.FILE_SIZE;
    config.SUPPORT_BP = mTaskEntity.isSupportBP();
    config.TASK_ENTITY = mTaskEntity;
    ThreadRecord record = new ThreadRecord();
    record.startLocation = 0;
    record.endLocation = config.FILE_SIZE;
    record.key = mTempFile.getPath();
    config.THREAD_RECORD = record;
    AbsThreadTask task = selectThreadTask(config);
    if (task == null) return;
    mTask.put(0, task);
    mFixedThreadPool = Executors.newFixedThreadPool(1);
    mFixedThreadPool.execute(task);
    mListener.onStart(0);
  }

  /**
   * 选择单任务线程的类型
   */
  protected abstract AbsThreadTask selectThreadTask(SubThreadConfig<TASK_ENTITY> config);

  /**
   * 任务是否已经中断
   *
   * @return {@code true}中断
   */
  public boolean isBreak() {
    if (mConstance.isCancel || mConstance.isStop) {
      closeTimer();
      ALog.d(TAG, String.format("任务【%s】已停止或取消了", mEntity.getFileName()));
      return true;
    }
    return false;
  }
}

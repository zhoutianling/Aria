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

import android.util.SparseArray;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.common.IUtil;
import com.arialyy.aria.core.common.OnFileInfoCallback;
import com.arialyy.aria.core.download.DownloadGroupTaskEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.util.ALog;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by AriaL on 2017/6/30.
 * 任务组下载工具
 */
public class DownloadGroupUtil extends AbsGroupUtil implements IUtil {
  private final String TAG = "DownloadGroupUtil";
  private ExecutorService mInfoPool;
  private int mInitCompleteNum, mInitFailNum;
  private boolean isStop = false;

  /**
   * 文件信息回调组
   */
  private SparseArray<OnFileInfoCallback> mFileInfoCallbacks = new SparseArray<>();

  public DownloadGroupUtil(IDownloadGroupListener listener, DownloadGroupTaskEntity taskEntity) {
    super(listener, taskEntity);
    mInfoPool = Executors.newCachedThreadPool();
    onPre();
  }

  @Override int getTaskType() {
    return HTTP_GROUP;
  }

  @Override public void onCancel() {
    super.onCancel();
    isStop = true;
    if (!mInfoPool.isShutdown()) {
      mInfoPool.shutdown();
    }
  }

  @Override protected void onStop() {
    super.onStop();
    isStop = true;
    if (!mInfoPool.isShutdown()) {
      mInfoPool.shutdown();
    }
  }

  @Override protected void onStart() {
    super.onStart();
    isStop = false;
    if (mCompleteNum == mGroupSize) {
      mListener.onComplete();
      return;
    }

    if (mExeMap.size() == 0) {
      ALog.e(TAG, "任务组无可执行任务");
      mListener.onFail(false);
      return;
    }
    Set<String> keys = mExeMap.keySet();
    for (String key : keys) {
      DownloadTaskEntity taskEntity = mExeMap.get(key);
      if (taskEntity != null) {
        if (taskEntity.getState() != IEntity.STATE_FAIL
            && taskEntity.getState() != IEntity.STATE_WAIT) {
          mInitCompleteNum++;
          createChildDownload(taskEntity);
          checkStartFlow();
        } else {
          mInfoPool.execute(createFileInfoThread(taskEntity));
        }
      }
    }
    if (mCurrentLocation == mTotalLen) {
      mListener.onComplete();
    }
  }

  /**
   * 创建文件信息获取线程
   */
  private HttpFileInfoThread createFileInfoThread(DownloadTaskEntity taskEntity) {
    OnFileInfoCallback callback = mFileInfoCallbacks.get(taskEntity.hashCode());

    if (callback == null) {
      callback = new OnFileInfoCallback() {
        int failNum = 0;

        @Override public void onComplete(String url, CompleteInfo info) {
          if (isStop) return;
          DownloadTaskEntity te = mExeMap.get(url);
          if (te != null) {
            if (isNeedLoadFileSize) {
              mTotalLen += te.getEntity().getFileSize();
            }
            createChildDownload(te);
          }
          mInitCompleteNum++;

          checkStartFlow();
        }

        @Override public void onFail(String url, String errorMsg, boolean needRetry) {
          if (isStop) return;
          ALog.e(TAG, "任务【" + url + "】初始化失败。");
          DownloadTaskEntity te = mExeMap.get(url);
          if (te != null) {
            mFailMap.put(url, te);
            mFileInfoCallbacks.put(te.hashCode(), this);
            mExeMap.remove(url);
          }
          //404链接不重试下载
          //if (failNum < 3 && !errorMsg.contains("错误码：404") && !errorMsg.contains(
          //    "UnknownHostException")) {
          //  mInfoPool.execute(createFileInfoThread(te));
          //} else {
          //  mInitFailNum++;
          //}
          //failNum++;
          mInitFailNum++;
          checkStartFlow();
        }
      };
    }
    return new HttpFileInfoThread(taskEntity, callback);
  }

  /**
   * 检查能否启动下载流程
   */
  private void checkStartFlow() {
    synchronized (DownloadGroupUtil.class) {
      if (mInitCompleteNum + mInitFailNum >= mGroupSize || !isNeedLoadFileSize) {
        startRunningFlow();
        updateFileSize();
      }
    }
  }
}
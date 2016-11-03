package com.arialyy.downloadutil.core.scheduler;

import android.os.Handler;
import com.arialyy.downloadutil.core.DownloadEntity;

/**
 * Created by “AriaLyy@outlook.com” on 2016/11/2.
 * 下载调度器接口
 */
public interface IDownloadSchedulers extends Handler.Callback {

  /**
   * 处理下载任务下载失败的情形
   *
   * @param entity 下载实体
   */
  public void handleFailTask(DownloadEntity entity);

  /**
   * 启动下一个任务，条件：任务停止，取消下载，任务完成
   *
   * @param entity 通过Handler传递的下载实体
   */
  public void startNextTask(DownloadEntity entity);
}

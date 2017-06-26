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
package com.arialyy.aria.core.inf;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.RequestEnum;
import com.arialyy.aria.core.command.AbsCmd;
import com.arialyy.aria.core.command.CmdFactory;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lyy on 2017/2/28.
 */
public abstract class AbsTarget<ENTITY extends AbsEntity, TASK_ENTITY extends AbsTaskEntity> {
  protected ENTITY entity;
  protected TASK_ENTITY taskEntity;
  protected String targetName;

  /**
   * 设置扩展字段，用来保存你的其它数据，如果你的数据比较多，你可以把你的数据转换为JSON字符串，然后再存到Aria中
   *
   * @param str 扩展数据
   */
  public AbsTarget setExtendField(String str) {
    entity.setStr(str);
    return this;
  }

  /**
   * 获取存放的扩展字段
   * 设置扩展字段{@link #setExtendField(String)}
   */
  public String getExtendField() {
    return entity.getStr();
  }

  /**
   * 获取任务状态
   *
   * @return {@link IEntity}
   */
  public int getTaskState() {
    return entity.getState();
  }

  /**
   * 将任务设置为最高优先级任务，最高优先级任务有以下特点：
   * 1、在下载队列中，有且只有一个最高优先级任务
   * 2、最高优先级任务会一直存在，直到用户手动暂停或任务完成
   * 3、任务调度器不会暂停最高优先级任务
   * 4、用户手动暂停或任务完成后，第二次重新执行该任务，该命令将失效
   * 5、如果下载队列中已经满了，则会停止队尾的任务，当高优先级任务完成后，该队尾任务将自动执行
   * 6、把任务设置为最高优先级任务后，将自动执行任务，不需要重新调用start()启动任务
   */
  protected void setHighestPriority() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_HIGHEST_PRIORITY))
        .exe();
  }

  /**
   * 重定向后，新url的key，默认为location
   */
  protected void _setRedirectUrlKey(String redirectUrlKey) {
    if (TextUtils.isEmpty(redirectUrlKey)) {
      Log.w("AbsTarget", "重定向后，新url的key不能为null");
      return;
    }
    taskEntity.redirectUrlKey = redirectUrlKey;
  }

  /**
   * 删除记录
   */
  public void removeRecord() {
    entity.deleteData();
  }

  /**
   * 获取任务文件大小
   *
   * @return 文件大小
   */
  public long getFileSize() {
    if (entity instanceof DownloadEntity) {
      DownloadEntity entity = (DownloadEntity) this.entity;
      return entity.getFileSize();
    } else if (entity instanceof UploadEntity) {
      UploadEntity entity = (UploadEntity) this.entity;
      return entity.getFileSize();
    }
    return 0;
  }

  /**
   * 获取单位转换后的文件大小
   *
   * @return 文件大小{@code xxx mb}
   */
  public String getConvertFileSize() {
    if (entity instanceof DownloadEntity) {
      DownloadEntity entity = (DownloadEntity) this.entity;
      return CommonUtil.formatFileSize(entity.getFileSize());
    } else if (entity instanceof UploadEntity) {
      UploadEntity entity = (UploadEntity) this.entity;
      return CommonUtil.formatFileSize(entity.getFileSize());
    }
    return "0b";
  }

  /**
   * 下载任务是否存在
   */
  public boolean taskExists() {
    return false;
  }

  /**
   * 获取任务进度百分比
   *
   * @return 返回任务进度
   */
  protected int getPercent() {
    return 0;
  }

  /**
   * 获取任务进度，如果任务存在，则返回当前进度
   *
   * @return 该任务进度
   */
  public long getCurrentProgress() {
    if (entity instanceof DownloadEntity) {
      DownloadEntity entity = (DownloadEntity) this.entity;
      return entity.getCurrentProgress();
    } else if (entity instanceof UploadEntity) {
      UploadEntity entity = (UploadEntity) this.entity;
      return entity.getCurrentProgress();
    }
    return -1;
  }

  /**
   * 给url请求添加头部
   *
   * @param key 头部key
   * @param header 头部value
   */
  protected void _addHeader(@NonNull String key, @NonNull String header) {
    taskEntity.headers.put(key, header);
  }

  /**
   * 给url请求添加头部
   */
  protected void _addHeaders(Map<String, String> headers) {
    if (headers != null && headers.size() > 0) {
      Set<String> keys = headers.keySet();
      for (String key : keys) {
        taskEntity.headers.put(key, headers.get(key));
      }
    }
  }

  /**
   * 设置请求类型
   *
   * @param requestEnum {@link RequestEnum}
   */
  protected void _setRequestMode(RequestEnum requestEnum) {
    taskEntity.requestEnum = requestEnum;
  }

  /**
   * 添加任务
   */
  public void add() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_CREATE))
        .exe();
  }

  /**
   * 开始下载
   */
  public void start() {
    //List<AbsCmd> cmds = new ArrayList<>();
    //cmds.add(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_CREATE));
    //cmds.add(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_START));
    //cmds.clear();
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_START))
        .exe();
  }

  /**
   * 停止下载
   */
  protected void pause() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_STOP))
        .exe();
  }

  /**
   * 恢复下载
   */
  protected void resume() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_START))
        .exe();
  }

  /**
   * 取消下载
   */
  public void cancel() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(CommonUtil.createCmd(targetName, taskEntity, CmdFactory.TASK_CANCEL))
        .exe();
  }

  /**
   * 重新下载
   */
  void reStart() {
    cancel();
    start();
  }
}

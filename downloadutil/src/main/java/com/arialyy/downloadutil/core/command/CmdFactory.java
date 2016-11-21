/*
 * Copyright (C) 2016 AriaLyy(DownloadUtil)
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


package com.arialyy.downloadutil.core.command;

import com.arialyy.downloadutil.core.DownloadEntity;

/**
 * Created by Lyy on 2016/9/23.
 * 命令工厂
 */
public class CmdFactory {
  /**
   * 创建任务
   */
  public static final int TASK_CREATE = 0x122;
  /**
   * 启动任务
   */
  public static final int TASK_START  = 0x123;
  /**
   * 恢复任务
   */
  public static final int TASK_RESUME = 0x127;
  /**
   * 取消任务
   */
  public static final int TASK_CANCEL = 0x124;
  /**
   * 停止任务
   */
  public static final int TASK_STOP   = 0x125;

  private static final    Object     LOCK     = new Object();
  private static volatile CmdFactory INSTANCE = null;

  private CmdFactory() {

  }

  public static CmdFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (LOCK) {
        INSTANCE = new CmdFactory();
      }
    }
    return INSTANCE;
  }

  /**
   * @param entity 下载实体
   * @param type 命令类型{@link #TASK_CREATE}、{@link #TASK_START}、{@link #TASK_CANCEL}、{@link
   * #TASK_STOP}
   */
  public IDownloadCmd createCmd(DownloadEntity entity, int type) {
    switch (type) {
      case TASK_CREATE:
        return createAddCmd(entity);
      case TASK_RESUME:
      case TASK_START:
        return createStartCmd(entity);
      case TASK_CANCEL:
        return createCancelCmd(entity);
      case TASK_STOP:
        return createStopCmd(entity);
      default:
        return null;
    }
  }

  /**
   * 创建停止命令
   *
   * @return {@link StopCmd}
   */
  private StopCmd createStopCmd(DownloadEntity entity) {
    return new StopCmd(entity);
  }

  /**
   * 创建下载任务命令
   *
   * @return {@link AddCmd}
   */
  private AddCmd createAddCmd(DownloadEntity entity) {
    return new AddCmd(entity);
  }

  /**
   * 创建启动下载命令
   *
   * @return {@link StartCmd}
   */
  private StartCmd createStartCmd(DownloadEntity entity) {
    return new StartCmd(entity);
  }

  /**
   * 创建 取消下载的命令
   *
   * @return {@link CancelCmd}
   */
  private CancelCmd createCancelCmd(DownloadEntity entity) {
    return new CancelCmd(entity);
  }
}
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

import com.arialyy.aria.orm.Ignore;

/**
 * Created by Aria.Lao on 2017/2/23.
 */

public interface IEntity {
  /**
   * 其它状态
   */
  @Ignore public static final int STATE_OTHER = -1;
  /**
   * 失败状态
   */
  @Ignore public static final int STATE_FAIL = 0;
  /**
   * 完成状态
   */
  @Ignore public static final int STATE_COMPLETE = 1;
  /**
   * 停止状态
   */
  @Ignore public static final int STATE_STOP = 2;
  /**
   * 未开始状态
   */
  @Ignore public static final int STATE_WAIT = 3;
  /**
   * 下载中
   */
  @Ignore public static final int STATE_RUNNING = 4;
  /**
   * 预处理
   */
  @Ignore public static final int STATE_PRE = 5;
  /**
   * 预处理完成
   */
  @Ignore public static final int STATE_POST_PRE = 6;
  /**
   * 取消下载
   */
  @Ignore public static final int STATE_CANCEL = 7;

  public int getState();
}

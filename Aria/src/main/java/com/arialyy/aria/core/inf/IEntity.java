package com.arialyy.aria.core.inf;

import com.arialyy.aria.orm.Ignore;

/**
 * Created by Aria.Lao on 2017/2/23.
 */

public interface IEntity {
  /**
   * 其它状态
   */
  @Ignore public static final int STATE_OTHER        = -1;
  /**
   * 失败状态
   */
  @Ignore public static final int STATE_FAIL         = 0;
  /**
   * 完成状态
   */
  @Ignore public static final int STATE_COMPLETE     = 1;
  /**
   * 停止状态
   */
  @Ignore public static final int STATE_STOP         = 2;
  /**
   * 未开始状态
   */
  @Ignore public static final int STATE_WAIT         = 3;
  /**
   * 下载中
   */
  @Ignore public static final int STATE_RUNNING = 4;
  /**
   * 预处理
   */
  @Ignore public static final int STATE_PRE          = 5;
  /**
   * 预处理完成
   */
  @Ignore public static final int STATE_POST_PRE     = 6;
  /**
   * 取消下载
   */
  @Ignore public static final int STATE_CANCEL       = 7;

}

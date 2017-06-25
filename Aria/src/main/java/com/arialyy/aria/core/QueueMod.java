package com.arialyy.aria.core;

/**
 * Created by Aria.Lao on 2017/6/21.
 * 执行队列类型
 */
public enum QueueMod {
  /**
   * 等待模式，如果执行队列已经满了，再次使用start命令执行任务时，该任务会被添加到缓存队列中
   * 当执行队列的任务完成时，将自动执行缓存队列中的任务
   */
  WAIT("wait"),

  /**
   * 立刻执行模式
   * 如果执行队列已经满了，再次使用start命令执行任务时，该任务会添加到执行队列队尾，而原来执行队列的队首任务会停止
   */
  NOW("now");

  String tag;

  public String getTag() {
    return tag;
  }

  QueueMod(String tag) {
    this.tag = tag;
  }
}

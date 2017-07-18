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
package com.arialyy.aria.core.scheduler;

import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.queue.DownloadGroupTaskQueue;
import com.arialyy.aria.core.queue.DownloadTaskQueue;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Aria.Lao on 2017/7/13.
 * 下载任务和队列的映射表
 */
public class DQueueMapping {

  public static final int QUEUE_TYPE_DOWNLOAD = 0xa1;
  public static final int QUEUE_TYPE_DOWNLOAD_GROUP = 0xa2;
  public static final int QUEUE_NONE = 0xab2;
  private LinkedHashMap<String, Integer> types = new LinkedHashMap<>();

  private static volatile DQueueMapping instance = null;

  private DQueueMapping() {

  }

  public static DQueueMapping getInstance() {
    if (instance == null) {
      synchronized (AriaManager.LOCK) {
        instance = new DQueueMapping();
      }
    }
    return instance;
  }

  /**
   * map中增加类型
   *
   * @param key 任务的key
   * @param type {@link #QUEUE_TYPE_DOWNLOAD}、{@link #QUEUE_TYPE_DOWNLOAD}
   */
  public void addType(String key, int type) {
    types.put(key, type);
  }

  /**
   * @param key 任务的key
   */
  public void removeType(String key) {
    types.remove(key);
  }

  /**
   * 获取下一个任务类型
   *
   * @return {@link #QUEUE_TYPE_DOWNLOAD}、{@link #QUEUE_TYPE_DOWNLOAD}
   */
  public int nextType() {
    Iterator<Map.Entry<String, Integer>> iter = types.entrySet().iterator();
    if (iter.hasNext()) {
      Map.Entry<String, Integer> next = iter.next();
      int type = next.getValue();
      iter.remove();
      return type;
    }
    return QUEUE_NONE;
  }

  public boolean canStart() {
    return DownloadTaskQueue.getInstance().getCurrentExePoolNum()
        + DownloadGroupTaskQueue.getInstance().getCurrentExePoolNum() >= AriaManager.getInstance(
        AriaManager.APP).getDownloadConfig().getMaxTaskNum();
  }
}

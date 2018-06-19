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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程任务管理器
 */
class ThreadTaskManager {
  private static volatile ThreadTaskManager INSTANCE = null;
  private Map<String, List<AbsThreadTask>> mThreadTasks = new ConcurrentHashMap<>();

  private ThreadTaskManager() {

  }

  public static ThreadTaskManager getInstance() {
    if (INSTANCE == null) {
      synchronized (ThreadTaskManager.class) {
        INSTANCE = new ThreadTaskManager();
      }
    }
    return INSTANCE;
  }

  /**
   * 添加单条线程记录
   *
   * @param key 任务对应的key
   * @param threadTask 线程任务
   */
  public void addTask(String key, AbsThreadTask threadTask) {
    if (mThreadTasks.get(key) == null) {
      mThreadTasks.put(key, new ArrayList<AbsThreadTask>());
    }
    mThreadTasks.get(key).add(threadTask);
  }

  /**
   * 删除对应的任务的线程记录
   *
   * @param key 任务对应的key
   */
  public void removeTask(String key) {
    for (Iterator<Map.Entry<String, List<AbsThreadTask>>> iter = mThreadTasks.entrySet().iterator();
        iter.hasNext(); ) {
      Map.Entry<String, List<AbsThreadTask>> entry = iter.next();
      if (key.equals(entry.getKey())) {
        List<AbsThreadTask> list = mThreadTasks.get(key);
        if (list != null && !list.isEmpty()) {
          list.clear();
        }
        iter.remove();
      }
    }
  }



}

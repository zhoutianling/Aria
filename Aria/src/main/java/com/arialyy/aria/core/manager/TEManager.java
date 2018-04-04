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
package com.arialyy.aria.core.manager;

import android.support.v4.util.LruCache;
import com.arialyy.aria.core.download.DownloadGroupTaskEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.upload.UploadTaskEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Aria.Lao on 2017/11/1.
 * 任务实体管理器
 */
public class TEManager {
  private static final String TAG = "TaskManager";
  private static volatile TEManager INSTANCE = null;
  private LruCache<String, AbsTaskEntity> cache = new LruCache<>(1024);
  private Lock lock;

  public static TEManager getInstance() {
    if (INSTANCE == null) {
      synchronized (TEManager.class) {
        INSTANCE = new TEManager();
      }
    }
    return INSTANCE;
  }

  private TEManager() {
    lock = new ReentrantLock();
  }

  /**
   * 通过key创建任务，只适应于单任务，不能用于HTTP任务组，可用于Ftp文件夹
   * 如果是任务组，请使用{@link #createGTEntity(Class, List)}
   *
   * @return 如果任务实体创建失败，返回null
   */
  public <TE extends AbsTaskEntity> TE createTEntity(Class<TE> clazz, String key) {
    final Lock lock = this.lock;
    lock.lock();
    try {
      AbsTaskEntity tEntity = cache.get(convertKey(key));
      if (tEntity == null || tEntity.getClass() != clazz) {
        ITEntityFactory factory = chooseFactory(clazz);
        if (factory == null) {
          ALog.e(TAG, "任务实体创建失败");
          return null;
        }
        tEntity = factory.create(key);
        cache.put(convertKey(key), tEntity);
      }
      return (TE) tEntity;
    } finally {
      lock.unlock();
    }
  }

  /**
   * 创建任务组实体
   *
   * @return 如果任务实体创建失败，返回null
   */
  public <TE extends AbsTaskEntity> TE createGTEntity(Class<TE> clazz, List<String> urls) {
    final Lock lock = this.lock;
    lock.lock();
    try {
      String groupName = CommonUtil.getMd5Code(urls);
      AbsTaskEntity tEntity = cache.get(convertKey(groupName));
      if (tEntity == null || tEntity.getClass() != clazz) {
        IGTEntityFactory factory = chooseGroupFactory(clazz);
        if (factory == null) {
          ALog.e(TAG, "任务实体创建失败");
          return null;
        }
        tEntity = factory.create(groupName, urls);
        cache.put(convertKey(groupName), tEntity);
      }
      return (TE) tEntity;
    } finally {
      lock.unlock();
    }
  }

  private IGTEntityFactory chooseGroupFactory(Class clazz) {
    if (clazz == DownloadGroupTaskEntity.class) {
      return DGTEntityFactory.getInstance();
    }
    return null;
  }

  private ITEntityFactory chooseFactory(Class clazz) {
    if (clazz == DownloadTaskEntity.class) {
      return DTEntityFactory.getInstance();
    } else if (clazz == UploadTaskEntity.class) {
      return UTEntityFactory.getInstance();
    } else if (clazz == DownloadGroupTaskEntity.class) {
      return DGTEntityFactory.getInstance();
    }
    return null;
  }

  /**
   * 从缓存中获取任务实体，如果任务实体不存在，则创建任务实体
   *
   * @return 创建失败，返回null
   */
  public <TE extends AbsTaskEntity> TE getTEntity(Class<TE> clazz, String key) {
    final Lock lock = this.lock;
    lock.lock();
    try {
      AbsTaskEntity tEntity = cache.get(convertKey(key));
      if (tEntity == null) {
        return createTEntity(clazz, key);
      } else {
        return (TE) tEntity;
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * 从缓存中获取HTTP任务组的任务实体，如果任务实体不存在，则创建任务实体
   *
   * @param urls HTTP任务组的子任务下载地址列表
   * @return 地址列表为null或创建实体失败，返回null
   */
  public <TE extends AbsTaskEntity> TE getGTEntity(Class<TE> clazz, List<String> urls) {
    if (urls == null || urls.isEmpty()) {
      ALog.e(TAG, "获取HTTP任务组实体失败：任务组的子任务下载地址列表为null");
      return null;
    }
    final Lock lock = this.lock;
    lock.lock();
    try {
      String groupName = CommonUtil.getMd5Code(urls);
      AbsTaskEntity tEntity = cache.get(convertKey(groupName));
      if (tEntity == null) {
        return createGTEntity(clazz, urls);
      } else {
        return (TE) tEntity;
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * 更新任务实体
   */
  public void putTEntity(String key, AbsTaskEntity tEntity) {
    final Lock lock = this.lock;
    lock.lock();
    try {
      cache.put(convertKey(key), tEntity);
    } finally {
      lock.unlock();
    }
  }

  /**
   * 向管理器中增加任务实体
   *
   * @return {@code false} 实体为null，添加失败
   */
  public boolean addTEntity(AbsTaskEntity te) {
    if (te == null) {
      ALog.e(TAG, "任务实体添加失败");
      return false;
    }
    final Lock lock = this.lock;
    lock.lock();
    try {
      return cache.put(convertKey(te.getKey()), te) != null;
    } finally {
      lock.unlock();
    }
  }

  /**
   * 通过key删除任务实体
   * 当任务complete或删除记录时将删除缓存
   */
  public AbsTaskEntity removeTEntity(String key) {
    final Lock lock = this.lock;
    lock.lock();
    try {
      return cache.remove(convertKey(key));
    } finally {
      lock.unlock();
    }
  }

  private String convertKey(String key) {
    final Lock lock = this.lock;
    lock.lock();
    try {
      return CommonUtil.keyToHashKey(key);
    } finally {
      lock.unlock();
    }
  }
}

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
package com.arialyy.aria.core.download;

import com.arialyy.aria.core.inf.AbsGroupTarget;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.util.CheckUtil;
import java.io.File;
import java.util.List;

/**
 * Created by AriaL on 2017/6/29.
 */
public class DownloadGroupTarget
    extends AbsGroupTarget<DownloadGroupTarget, DownloadGroupEntity, DownloadGroupTaskEntity> {
  private List<String> mUrls;

  DownloadGroupTarget(DownloadGroupEntity entity, String targetName, List<String> urls) {
    this.mEntity = entity;
    this.mTargetName = targetName;
    this.mUrls = urls;
    mTaskEntity = new DownloadGroupTaskEntity();
    mTaskEntity.entity = entity;
  }

  /**
   * 设置保存路径组
   */
  public DownloadGroupTarget setDownloadPaths(List<String> paths) {
    CheckUtil.checkDownloadPaths(paths);
    if (mUrls.size() != paths.size()) {
      throw new IllegalArgumentException("下载链接数必须要和保存路径的数量一致");
    }
    for (int i = 0, len = mUrls.size(); i < len; i++) {
      mTaskEntity.getEntity().getChild().add(createDownloadEntity(mUrls.get(i), paths.get(i)));
    }
    return this;
  }

  /**
   * 创建子任务下载实体
   *
   * @param url 下载地址
   * @param path 保存路径
   */
  private DownloadEntity createDownloadEntity(String url, String path) {
    DownloadEntity entity = DownloadEntity.findData(DownloadEntity.class, "downloadUrl=?", url);
    if (entity == null) {
      entity = new DownloadEntity();
    }
    File file = new File(path);
    if (!file.exists()) {
      entity.setState(IEntity.STATE_WAIT);
    }
    entity.setDownloadPath(path);
    entity.setDownloadUrl(url);
    entity.setFileName(file.getName());
    return entity;
  }
}

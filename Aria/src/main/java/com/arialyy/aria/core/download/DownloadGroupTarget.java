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

import android.text.TextUtils;
import com.arialyy.aria.core.inf.AbsGroupTarget;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AriaL on 2017/6/29.
 */
public class DownloadGroupTarget
    extends AbsGroupTarget<DownloadGroupTarget, DownloadGroupEntity, DownloadGroupTaskEntity> {
  private List<String> mUrls = new ArrayList<>();
  /**
   * 子任务文件名
   */
  private List<String> mSubTaskFileName = new ArrayList<>();
  private String mGroupName;
  /**
   * 是否已经设置了文件路径
   */
  private boolean isSetDirPathed = false;

  DownloadGroupTarget(List<String> urls, String targetName) {
    this.mTargetName = targetName;
    this.mUrls = urls;
    mGroupName = CommonUtil.getMd5Code(urls);
    mTaskEntity = DbEntity.findFirst(DownloadGroupTaskEntity.class, "key=?", mGroupName);
    if (mTaskEntity == null) {
      mTaskEntity = new DownloadGroupTaskEntity();
      mTaskEntity.key = mGroupName;
      mTaskEntity.entity = getDownloadGroupEntity();
      mTaskEntity.insert();
    }
    if (mTaskEntity.entity == null) {
      mTaskEntity.entity = getDownloadGroupEntity();
    }
    mEntity = mTaskEntity.entity;
  }

  private DownloadGroupEntity getDownloadGroupEntity() {
    DownloadGroupEntity entity =
        DbEntity.findFirst(DownloadGroupEntity.class, "groupName=?", mGroupName);
    if (entity == null) {
      entity = new DownloadGroupEntity();
      entity.setGroupName(mGroupName);
      entity.setUrlmd5(mGroupName);
      entity.insert();
    }
    return entity;
  }

  /**
   * 设置任务组的文件夹路径，在Aria中，任务组的所有子任务都会下载到以任务组组名的文件夹中。
   * 如：groupDirPath = "/mnt/sdcard/download/group_test"
   * <pre>
   *   {@code
   *      + mnt
   *        + sdcard
   *          + download
   *            + group_test
   *              - task1.apk
   *              - task2.apk
   *              - task3.apk
   *              ....
   *
   *   }
   * </pre>
   *
   * @param groupDirPath 任务组保存文件夹路径
   */
  public DownloadGroupTarget setDownloadDirPath(String groupDirPath) {
    if (TextUtils.isEmpty(groupDirPath)) {
      throw new NullPointerException("任务组文件夹保存路径不能为null");
    }

    if (mEntity.getDirPath().equals(groupDirPath)) return this;

    File file = new File(groupDirPath);
    if (file.exists() && file.isFile()) {
      throw new IllegalArgumentException("路径不能为文件");
    }
    if (!file.exists()) {
      file.mkdirs();
    }

    mEntity.setDirPath(groupDirPath);
    if (!TextUtils.isEmpty(mEntity.getDirPath())) {
      reChangeDirPath(groupDirPath);
    } else {
      mEntity.setSubTasks(createSubTask());
    }
    mEntity.update();
    isSetDirPathed = true;
    return this;
  }

  /**
   * 改变任务组文件夹路径
   *
   * @param newDirPath 新的文件夹路径
   */
  private void reChangeDirPath(String newDirPath) {
    List<DownloadEntity> subTask = mEntity.getSubTask();
    if (subTask != null && !subTask.isEmpty()) {
      for (DownloadEntity entity : subTask) {
        File file = new File(entity.getDownloadPath());
        file.renameTo(new File(newDirPath, entity.getFileName()));
      }
    } else {
      mEntity.setSubTasks(createSubTask());
    }
  }

  /**
   * 设置子任务文件名，该方法如果在{@link #setDownloadDirPath(String)}之前调用，则不生效
   */
  public DownloadGroupTarget setSubtaskFileName(List<String> subTaskFileName) {
    if (subTaskFileName == null || subTaskFileName.isEmpty()) return this;
    mSubTaskFileName.addAll(subTaskFileName);
    if (mUrls.size() != subTaskFileName.size()) {
      throw new IllegalArgumentException("下载链接数必须要和保存路径的数量一致");
    }
    if (isSetDirPathed) {
      List<DownloadEntity> entities = mEntity.getSubTask();
      int i = 0;
      for (DownloadEntity entity : entities) {
        entity.setFileName(mSubTaskFileName.get(i));
        entity.update();
      }
    }
    return this;
  }

  /**
   * 创建子任务
   */
  private List<DownloadEntity> createSubTask() {
    List<DownloadEntity> list = new ArrayList<>();
    for (int i = 0, len = mUrls.size(); i < len; i++) {
      DownloadEntity entity = new DownloadEntity();
      entity.setDownloadUrl(mUrls.get(i));
      String fileName = mSubTaskFileName.isEmpty() ? CommonUtil.keyToHashKey(mUrls.get(i))
          : mSubTaskFileName.get(i);
      entity.setDownloadPath(mEntity.getDirPath() + "/" + fileName);
      entity.setGroupName(mGroupName);
      entity.setGroupChild(true);
      entity.setFileName(fileName);
      entity.insert();
      list.add(entity);
    }
    return list;
  }

  ///**
  // * 创建文件名，如果url链接有后缀名，则使用url中的后缀名
  // * @return url 的 hashKey
  // */
  //private String createFileName(String url){
  //  if (url.contains(".")){
  //    int s = url.lastIndexOf(".");
  //    String fileName = url.substring()
  //  }
  //}
}

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

import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.command.ICmd;
import com.arialyy.aria.core.command.normal.CancelAllCmd;
import com.arialyy.aria.core.command.normal.NormalCmdFactory;
import com.arialyy.aria.core.common.ProxyHelper;
import com.arialyy.aria.core.download.wrapper.DGEWrapper;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.AbsReceiver;
import com.arialyy.aria.core.inf.AbsTarget;
import com.arialyy.aria.core.manager.TEManager;
import com.arialyy.aria.core.scheduler.DownloadGroupSchedulers;
import com.arialyy.aria.core.scheduler.DownloadSchedulers;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by lyy on 2016/12/5.
 * 下载功能接收器
 */
public class DownloadReceiver extends AbsReceiver {
  private final String TAG = "DownloadReceiver";

  /**
   * 设置最大下载速度，单位：kb
   * 该方法为实验性功能，清不要轻易在生产环境中使用。
   *
   * @param maxSpeed 为0表示不限速
   */
  @Deprecated public void setMaxSpeed(int maxSpeed) {
    AriaManager.getInstance(AriaManager.APP).getDownloadConfig().setMsxSpeed(maxSpeed);
  }

  /**
   * 使用下载实体执行下载操作
   *
   * @param entity 下载实体
   */
  public DownloadTarget load(DownloadEntity entity) {
    return load(entity, false);
  }

  /**
   * 使用下载实体执行下载操作
   *
   * @param refreshInfo 是否刷新下载信息
   * @deprecated 请使用 {@link AbsTarget#resetState()}
   * <pre>
   *   <code>
   *   Aria.download(this)
   *       .load(URL)
   *       .setDownloadPath(PATH)
   *       .resetState()
   *       .start();
   *   </code>
   * </pre>
   */
  @Deprecated public DownloadTarget load(DownloadEntity entity, boolean refreshInfo) {
    CheckUtil.checkDownloadEntity(entity);
    return new DownloadTarget(entity, targetName, refreshInfo);
  }

  /**
   * 加载Http、https单任务下载地址
   *
   * @param url 下载地址
   */
  public DownloadTarget load(@NonNull String url) {
    return load(url, false);
  }

  /**
   * 加载Http、https单任务下载地址
   *
   * @param url 下载地址
   * @param refreshInfo 是否刷新下载信息，当下载地址改变而保存路径不变，则需要设置该参数为{@code true}
   * @deprecated 请使用 {@link AbsTarget#resetState()}
   * <pre>
   *   <code>
   *   Aria.download(this)
   *       .load(URL)
   *       .setDownloadPath(PATH)
   *       .resetState()
   *       .start();
   *   </code>
   * </pre>
   */
  @Deprecated public DownloadTarget load(@NonNull String url, boolean refreshInfo) {
    CheckUtil.checkUrlInvalidThrow(url);
    return new DownloadTarget(url, targetName, refreshInfo);
  }

  /**
   * 加载下载地址，如果任务组的中的下载地址改变了，则任务从新的一个任务组
   *
   * @param urls 任务组子任务下载地址列表
   * @deprecated {@link #loadGroup(DownloadGroupEntity)}
   */
  @Deprecated
  public DownloadGroupTarget load(List<String> urls) {
    return loadGroup(urls);
  }

  /**
   * 加载下载地址，如果任务组的中的下载地址改变了，则任务从新的一个任务组
   */
  public DownloadGroupTarget loadGroup(List<String> urls) {
    CheckUtil.checkDownloadUrls(urls);
    return new DownloadGroupTarget(urls, targetName);
  }

  /**
   * 使用下载实体执行FTP下载操作
   *
   * @param entity 下载实体
   */
  public FtpDownloadTarget loadFtp(DownloadEntity entity) {
    return loadFtp(entity, false);
  }

  /**
   * 使用下载实体执行下载操作
   *
   * @param refreshInfo 是否刷新下载信息，当下载地址改变而保存路径不变，则需要设置该参数为{@code true}
   * @deprecated 请使用 {@link AbsTarget#resetState()}
   * <pre>
   *   <code>
   *   Aria.download(this)
   *       .load(URL)
   *       .setDownloadPath(PATH)
   *       .resetState()
   *       .start();
   *   </code>
   * </pre>
   */
  @Deprecated public FtpDownloadTarget loadFtp(DownloadEntity entity, boolean refreshInfo) {
    CheckUtil.checkDownloadEntity(entity);
    if (!entity.getUrl().startsWith("ftp")) {
      throw new IllegalArgumentException("非FTP请求不能使用该方法");
    }
    return new FtpDownloadTarget(entity, targetName, refreshInfo);
  }

  /**
   * 加载ftp单任务下载地址
   */
  public FtpDownloadTarget loadFtp(@NonNull String url) {
    return loadFtp(url, false);
  }

  /**
   * 加载ftp单任务下载地址
   *
   * @param refreshInfo 是否刷新下载信息
   */
  public FtpDownloadTarget loadFtp(@NonNull String url, boolean refreshInfo) {
    CheckUtil.checkUrlInvalidThrow(url);
    return new FtpDownloadTarget(url, targetName, refreshInfo);
  }

  /**
   * 使用任务组实体执行任务组的实体执行任务组的下载操作，后续版本会删除该api
   *
   * @param groupEntity 如果加载的任务实体没有子项的下载地址，
   * 那么你需要使用{@link DownloadGroupTarget#setGroupUrl(List)}设置子项的下载地址
   * @deprecated 请使用 {@link #loadGroup(DownloadGroupEntity)}
   */
  @Deprecated
  public DownloadGroupTarget load(DownloadGroupEntity groupEntity) {
    return loadGroup(groupEntity);
  }

  /**
   * 使用任务组实体执行任务组的实体执行任务组的下载操作
   *
   * @param groupEntity 如果加载的任务实体没有子项的下载地址，
   * 那么你需要使用{@link DownloadGroupTarget#setGroupUrl(List)}设置子项的下载地址
   */
  public DownloadGroupTarget loadGroup(DownloadGroupEntity groupEntity) {
    return new DownloadGroupTarget(groupEntity, targetName);
  }

  /**
   * 加载ftp文件夹下载地址
   */
  public FtpDirDownloadTarget loadFtpDir(@NonNull String dirUrl) {
    CheckUtil.checkUrlInvalidThrow(dirUrl);
    return new FtpDirDownloadTarget(dirUrl, targetName);
  }

  /**
   * 将当前类注册到Aria
   */
  public DownloadReceiver register() {
    String className = obj.getClass().getName();
    Set<String> dCounter = ProxyHelper.getInstance().downloadCounter;
    Set<String> dgCounter = ProxyHelper.getInstance().downloadGroupCounter;
    Set<String> dgsCounter = ProxyHelper.getInstance().downloadGroupSubCounter;
    if (dCounter != null && dCounter.contains(className)) {
      DownloadSchedulers.getInstance().register(obj);
    }
    if ((dgCounter != null && dgCounter.contains(className)) || (dgsCounter != null
        && dgsCounter.contains(className))) {
      DownloadGroupSchedulers.getInstance().register(obj);
    }
    return this;
  }

  /**
   * 取消注册，如果是Activity或fragment，Aria会界面销毁时自动调用该方法。
   * 如果在activity中一定要调用该方法，那么请在onDestroy()中调用
   * 如果是Dialog或popupwindow，需要你在撤销界面时调用该方法
   */
  @Override public void unRegister() {
    if (needRmListener) {
      unRegisterListener();
    }
    AriaManager.getInstance(AriaManager.APP).removeReceiver(obj);
  }

  @Override public void unRegisterListener() {
    String className = obj.getClass().getName();
    Set<String> dCounter = ProxyHelper.getInstance().downloadCounter;
    Set<String> dgCounter = ProxyHelper.getInstance().downloadGroupCounter;
    Set<String> dgsCounter = ProxyHelper.getInstance().downloadGroupSubCounter;
    if (dCounter != null && dCounter.contains(className)) {
      DownloadSchedulers.getInstance().unRegister(obj);
    }
    if (dgCounter != null && dgCounter.contains(className) || (dgsCounter != null
        && dgsCounter.contains(className))) {
      DownloadGroupSchedulers.getInstance().unRegister(obj);
    }
  }

  @Override public void destroy() {
    targetName = null;
  }

  /**
   * 通过下载链接获取下载实体
   *
   * @return 如果url错误或查找不到数据，则返回null
   */
  public DownloadEntity getDownloadEntity(String downloadUrl) {
    if (CheckUtil.checkUrl(downloadUrl)) {
      return null;
    }
    return DbEntity.findFirst(DownloadEntity.class, "url=? and isGroupChild='false'", downloadUrl);
  }

  /**
   * 通过下载地址和文件保存路径获取下载任务实体
   *
   * @param downloadUrl 下载地址
   * @return 如果url错误或查找不到数据，则返回null
   */
  public DownloadTaskEntity getDownloadTask(String downloadUrl) {
    if (CheckUtil.checkUrl(downloadUrl)) {
      return null;
    }
    return TEManager.getInstance().getTEntity(DownloadTaskEntity.class, downloadUrl);
  }

  /**
   * 通过下载链接获取保存在数据库的下载任务组实体
   *
   * @param urls 任务组子任务下载地址列表
   * @return 返回对应的任务组实体；如果查找不到对应的数据或子任务列表为null，返回null
   */
  public DownloadGroupTaskEntity getGroupTask(List<String> urls) {
    if (urls == null || urls.isEmpty()) {
      ALog.e(TAG, "获取任务组实体失败：任务组子任务下载地址列表为null");
      return null;
    }
    return TEManager.getInstance().getGTEntity(DownloadGroupTaskEntity.class, urls);
  }

  /**
   * 获取FTP文件夹下载任务实体
   *
   * @param dirUrl FTP文件夹本地下载路径
   * @return 返回对应的任务组实体；如果查找不到对应的数据或路径为null，返回null
   */
  public DownloadGroupTaskEntity getFtpDirTask(String dirUrl) {
    if (TextUtils.isEmpty(dirUrl)) {
      ALog.e(TAG, "获取FTP文件夹实体失败：下载路径为null");
      return null;
    }
    return TEManager.getInstance().getFDTEntity(DownloadGroupTaskEntity.class, dirUrl);
  }

  /**
   * 下载任务是否存在
   */
  @Override public boolean taskExists(String downloadUrl) {
    return DownloadEntity.findFirst(DownloadEntity.class, "url=?", downloadUrl) != null;
  }

  /**
   * 获取所有普通下载任务
   * 获取未完成的普通任务列表{@link #getAllNotCompletTask()}
   * 获取已经完成的普通任务列表{@link #getAllCompleteTask()}
   */
  @Override public List<DownloadEntity> getTaskList() {
    return DownloadEntity.findDatas(DownloadEntity.class, "isGroupChild=? and downloadPath!=''",
        "false");
  }

  /**
   * 获取所有未完成的普通下载任务
   */
  public List<DownloadEntity> getAllNotCompletTask() {
    return DownloadEntity.findDatas(DownloadEntity.class,
        "isGroupChild=? and downloadPath!='' and isComplete=?", "false", "false");
  }

  /**
   * 获取所有已经完成的普通任务
   */
  public List<DownloadEntity> getAllCompleteTask() {
    return DownloadEntity.findDatas(DownloadEntity.class,
        "isGroupChild=? and downloadPath!='' and isComplete=?", "false", "true");
  }

  /**
   * 获取任务组列表
   *
   * @return 如果没有任务组列表，则返回null
   */
  public List<DownloadGroupEntity> getGroupTaskList() {
    List<DGEWrapper> wrappers = DbEntity.findRelationData(DGEWrapper.class);
    if (wrappers == null || wrappers.isEmpty()) {
      return null;
    }
    List<DownloadGroupEntity> entities = new ArrayList<>();
    for (DGEWrapper wrapper : wrappers) {
      entities.add(wrapper.groupEntity);
    }
    return entities;
  }

  /**
   * 获取普通任务和任务组的任务列表
   */
  public List<AbsEntity> getTotalTaskList() {
    List<AbsEntity> list = new ArrayList<>();
    List<DownloadEntity> simpleTask = getTaskList();
    List<DownloadGroupEntity> groupTask = getGroupTaskList();
    if (simpleTask != null && !simpleTask.isEmpty()) {
      list.addAll(simpleTask);
    }
    if (groupTask != null && !groupTask.isEmpty()) {
      list.addAll(groupTask);
    }
    return list;
  }

  /**
   * 停止所有正在下载的任务，并清空等待队列。
   */
  @Override public void stopAllTask() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(NormalCmdFactory.getInstance()
            .createCmd(targetName, new DownloadTaskEntity(), NormalCmdFactory.TASK_STOP_ALL,
                ICmd.TASK_TYPE_DOWNLOAD))
        .exe();
  }

  /**
   * 恢复所有正在下载的任务
   * 1.如果执行队列没有满，则开始下载任务，直到执行队列满
   * 2.如果队列执行队列已经满了，则将所有任务添加到等待队列中
   */
  public void resumeAllTask() {
    AriaManager.getInstance(AriaManager.APP)
        .setCmd(NormalCmdFactory.getInstance()
            .createCmd(targetName, new DownloadTaskEntity(), NormalCmdFactory.TASK_RESUME_ALL,
                ICmd.TASK_TYPE_DOWNLOAD))
        .exe();
  }

  /**
   * 删除所有任务
   *
   * @param removeFile {@code true} 删除已经下载完成的任务，不仅删除下载记录，还会删除已经下载完成的文件，{@code false}
   * 如果文件已经下载完成，只删除下载记录
   */
  @Override public void removeAllTask(boolean removeFile) {
    final AriaManager ariaManager = AriaManager.getInstance(AriaManager.APP);
    CancelAllCmd cancelCmd =
        (CancelAllCmd) CommonUtil.createNormalCmd(targetName, new DownloadTaskEntity(),
            NormalCmdFactory.TASK_CANCEL_ALL, ICmd.TASK_TYPE_DOWNLOAD);
    cancelCmd.removeFile = removeFile;
    ariaManager.setCmd(cancelCmd).exe();
    Set<String> keys = ariaManager.getReceiver().keySet();
    for (String key : keys) {
      ariaManager.getReceiver().remove(key);
    }
  }
}
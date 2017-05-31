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

package com.arialyy.simple.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.util.AndroidUtils;
import com.arialyy.frame.util.StringUtil;
import com.arialyy.frame.util.show.L;
import com.arialyy.simple.R;
import com.arialyy.simple.download.multi_download.FileListEntity;
import com.arialyy.simple.base.BaseModule;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Lyy on 2016/9/27.
 */
public class DownloadModule extends BaseModule {
  private List<String> mTestDownloadUrl = new ArrayList<>();

  public DownloadModule(Context context) {
    super(context);
    mTestDownloadUrl.add(
        "http://static.gaoshouyou.com/d/e6/f5/4de6329f9cf5dc3a1d1e6bbcca0d003c.apk");
    mTestDownloadUrl.add(
        "http://static.gaoshouyou.com/d/6e/e5/ff6ecaaf45e532e6d07747af82357472.apk");
    mTestDownloadUrl.add(
        "http://static.gaoshouyou.com/d/36/69/2d3699acfa69e9632262442c46516ad8.apk");
  }

  public String getRadomUrl() {
    Random random = new Random();
    int i = random.nextInt(2);
    return mTestDownloadUrl.get(i);
  }

  public DownloadEntity createRandomDownloadEntity() {
    return createDownloadEntity(getRadomUrl());
  }

  /**
   * 创建下载地址
   */
  public List<FileListEntity> createFileList() {
    String[] names = getContext().getResources().getStringArray(R.array.file_nams);
    String[] downloadUrl = getContext().getResources().getStringArray(R.array.download_url);
    List<FileListEntity> list = new ArrayList<>();
    int i = 0;
    for (String name : names) {
      FileListEntity entity = new FileListEntity();
      entity.name = name;
      entity.downloadUrl = downloadUrl[i];
      entity.downloadPath = Environment.getExternalStorageDirectory() + "/Download/" + name;
      list.add(entity);
      i++;
    }
    return list;
  }

  /**
   * 设置下载数据
   */
  public List<DownloadEntity> getDownloadData() {
    String[] urls = getContext().getResources().getStringArray(R.array.test_apk_download_url);
    List<DownloadEntity> list = new ArrayList<>();
    for (String url : urls) {
      DownloadEntity entity = Aria.download(getContext()).getDownloadEntity(url);
      if (entity == null) {
        entity = createDownloadEntity(url);
      }
      list.add(entity);
    }
    return list;
  }

  /**
   * 过滤任务
   *
   * @param sqlEntity 数据库的下载实体
   * @param createdEntity 通过下载链接生成的下载实体
   */
  private List<DownloadEntity> filter(List<DownloadEntity> sqlEntity,
      List<DownloadEntity> createdEntity) {
    List<DownloadEntity> list = new ArrayList<>();
    list.addAll(sqlEntity);
    for (DownloadEntity cEntity : createdEntity) {
      int count = 0;
      for (DownloadEntity sEntity : sqlEntity) {
        if (cEntity.getDownloadUrl().equals(sEntity.getDownloadUrl())) {
          break;
        }
        count++;
        if (count == createdEntity.size()) {
          list.add(cEntity);
        }
      }
    }
    return list;
  }

  private DownloadEntity createDownloadEntity(String url) {
    String fileName = CommonUtil.keyToHashCode(url) + ".apk";
    DownloadEntity entity = new DownloadEntity();
    entity.setDownloadUrl(url);
    entity.setDownloadPath(getDownloadPath(url));
    entity.setFileName(fileName);
    //entity.setFileName("taskName_________" + i);
    return entity;
  }

  /**
   * 创建下载列表
   */
  private List<DownloadEntity> createNewDownload() {
    List<DownloadEntity> list = new ArrayList<>();
    String[] urls = getContext().getResources().getStringArray(R.array.test_apk_download_url);
    int i = 0;
    for (String url : urls) {
      list.add(createDownloadEntity(url));
      i++;
    }
    return list;
  }

  /**
   * 下载广播过滤器
   */
  public IntentFilter getDownloadFilter() {
    IntentFilter filter = new IntentFilter();
    filter.addDataScheme(getContext().getPackageName());
    filter.addAction(Aria.ACTION_PRE);
    filter.addAction(Aria.ACTION_POST_PRE);
    filter.addAction(Aria.ACTION_RESUME);
    filter.addAction(Aria.ACTION_START);
    filter.addAction(Aria.ACTION_RUNNING);
    filter.addAction(Aria.ACTION_STOP);
    filter.addAction(Aria.ACTION_CANCEL);
    filter.addAction(Aria.ACTION_COMPLETE);
    filter.addAction(Aria.ACTION_FAIL);
    return filter;
  }

  /**
   * 创建Receiver
   */
  public BroadcastReceiver createReceiver(final Handler handler) {

    return new BroadcastReceiver() {
      long len = 0;

      @Override public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
          case Aria.ACTION_POST_PRE:
            DownloadEntity entity = intent.getParcelableExtra(Aria.DOWNLOAD_ENTITY);
            len = entity.getFileSize();
            L.d(TAG, "download pre");
            handler.obtainMessage(SingleTaskActivity.DOWNLOAD_PRE, len).sendToTarget();
            break;
          case Aria.ACTION_START:
            L.d(TAG, "download start");
            break;
          case Aria.ACTION_RESUME:
            L.d(TAG, "download resume");
            long location = intent.getLongExtra(Aria.CURRENT_LOCATION, 1);
            handler.obtainMessage(SingleTaskActivity.DOWNLOAD_RESUME, location).sendToTarget();
            break;
          case Aria.ACTION_RUNNING:
            long current = intent.getLongExtra(Aria.CURRENT_LOCATION, 0);
            int progress = len == 0 ? 0 : (int) ((current * 100) / len);
            handler.obtainMessage(SingleTaskActivity.DOWNLOAD_RUNNING, progress).sendToTarget();
            break;
          case Aria.ACTION_STOP:
            L.d(TAG, "download stop");
            handler.sendEmptyMessage(SingleTaskActivity.DOWNLOAD_STOP);
            break;
          case Aria.ACTION_COMPLETE:
            handler.sendEmptyMessage(SingleTaskActivity.DOWNLOAD_COMPLETE);
            break;
          case Aria.ACTION_CANCEL:
            handler.sendEmptyMessage(SingleTaskActivity.DOWNLOAD_CANCEL);
            break;
          case Aria.ACTION_FAIL:
            handler.sendEmptyMessage(SingleTaskActivity.DOWNLOAD_FAILE);
            break;
        }
      }
    };
  }

  /**
   * 设置下载队列
   */
  private String getDownloadPath(String url) {
    String path =
        Environment.getExternalStorageDirectory().getPath() + "/" + AndroidUtils.getAppName(
            getContext()) + "downloads/" + StringUtil.keyToHashKey(url) + ".apk";
    File file = new File(path);
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    return path;
  }
}
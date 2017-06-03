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
import android.content.res.Resources;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Lyy on 2016/9/27.
 */
public class DownloadModule extends BaseModule {

  public DownloadModule(Context context) {
    super(context);
  }

  /**
   * 最高优先级任务测试列表
   */
  public List<DownloadEntity> getHighestTestList() {
    List<DownloadEntity> list = new LinkedList<>();
    Resources res = getContext().getResources();
    String[] urls = res.getStringArray(R.array.highest_urls);
    String[] names = res.getStringArray(R.array.highest_names);
    for (int i = 0, len = urls.length; i < len; i++) {
      list.add(createDownloadEntity(urls[i], names[i]));
    }
    return list;
  }

  /**
   * 创建下载地址
   */
  public List<FileListEntity> createMultiTestList() {
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
   * 创建下载实体，Aria也可以通过下载实体启动下载
   */
  private DownloadEntity createDownloadEntity(String downloadUrl, String name) {
    String path = Environment.getExternalStorageDirectory() + "/download/" + name + ".apk";
    DownloadEntity entity = new DownloadEntity();
    entity.setFileName(name);
    entity.setDownloadUrl(downloadUrl);
    entity.setDownloadPath(path);
    return entity;
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
            L.d(TAG, "download onPre");
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
}
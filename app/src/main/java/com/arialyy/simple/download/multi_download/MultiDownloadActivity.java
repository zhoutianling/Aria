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

package com.arialyy.simple.download.multi_download;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import butterknife.Bind;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.frame.util.show.L;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityMultiDownloadBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AriaL on 2017/1/6.
 */
public class MultiDownloadActivity extends BaseActivity<ActivityMultiDownloadBinding> {
  @Bind(R.id.list) RecyclerView mList;
  private DownloadAdapter mAdapter;
  private List<DownloadEntity> mData = new ArrayList<>();

  String[] mFilterStr = new String[] {
      "https://g37.gdl.netease.com/onmyoji_netease_10_1.0.20.apk",
      "http://static.gaoshouyou.com/d/eb/f2/dfeba30541f209ab8a50d847fc1661ce.apk"
  };

  @Override protected int setLayoutId() {
    return R.layout.activity_multi_download;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.download(this).register();
    setTitle("下载列表");
    List<DownloadEntity> temps = Aria.download(this).getTaskList();
    if (temps != null && !temps.isEmpty()) {
      mData.addAll(temps);
    }
    mAdapter = new DownloadAdapter(this, mData);
    mList.setLayoutManager(new LinearLayoutManager(this));
    mList.setAdapter(mAdapter);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_mutil_task, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    Aria.download(this).resumeAllTask();
    return true;
  }

  @Download.onPre void onPre(DownloadTask task) {
    L.d(TAG, "download onPre");
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskStart void taskStart(DownloadTask task) {
    L.d(TAG, "download start");
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskResume void taskResume(DownloadTask task) {
    L.d(TAG, "download resume");
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskStop void taskStop(DownloadTask task) {
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskCancel void taskCancel(DownloadTask task) {
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskFail void taskFail(DownloadTask task) {
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskComplete void taskComplete(DownloadTask task) {
    mAdapter.updateState(task.getDownloadEntity());
  }

  @Download.onTaskRunning({
      "https://g37.gdl.netease.com/onmyoji_netease_10_1.0.20.apk",
      "http://static.gaoshouyou.com/d/eb/f2/dfeba30541f209ab8a50d847fc1661ce.apk"
  }) void taskRunning(DownloadTask task) {
    mAdapter.setProgress(task.getDownloadEntity());
  }
}

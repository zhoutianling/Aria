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
import butterknife.Bind;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.frame.util.show.L;
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

  @Override protected int setLayoutId() {
    return R.layout.activity_multi_download;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setTitle("下载列表");
    List<DownloadEntity> temps = Aria.download(this).getTaskList();
    if (temps != null && !temps.isEmpty()) {
      mData.addAll(temps);
    }
    mAdapter = new DownloadAdapter(this, mData);
    mList.setLayoutManager(new LinearLayoutManager(this));
    mList.setAdapter(mAdapter);
  }

  @Override protected void onResume() {
    super.onResume();
    Aria.download(this).addSchedulerListener(new MySchedulerListener());
  }

  private class MySchedulerListener extends Aria.DownloadSchedulerListener {
    @Override public void onTaskPre(DownloadTask task) {
      super.onTaskPre(task);
      L.d(TAG, "download onPre");
      mAdapter.updateState(task.getDownloadEntity());
    }

    @Override public void onTaskStart(DownloadTask task) {
      super.onTaskStart(task);
      L.d(TAG, "download start");
      mAdapter.updateState(task.getDownloadEntity());
    }

    @Override public void onTaskResume(DownloadTask task) {
      super.onTaskResume(task);
      L.d(TAG, "download resume");
      mAdapter.updateState(task.getDownloadEntity());
    }

    @Override public void onTaskRunning(DownloadTask task) {
      super.onTaskRunning(task);
      mAdapter.setProgress(task.getDownloadEntity());
    }

    @Override public void onTaskStop(DownloadTask task) {
      super.onTaskStop(task);
      mAdapter.updateState(task.getDownloadEntity());
    }

    @Override public void onTaskCancel(DownloadTask task) {
      super.onTaskCancel(task);
      mAdapter.updateState(task.getDownloadEntity());
    }

    @Override public void onTaskComplete(DownloadTask task) {
      super.onTaskComplete(task);
      mAdapter.updateState(task.getDownloadEntity());
    }

    @Override public void onTaskFail(DownloadTask task) {
      super.onTaskFail(task);
      L.d(TAG, "download fail");
    }
  }
}

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
package com.arialyy.simple.download.group;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import butterknife.Bind;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.scheduler.AbsSchedulerListener;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityDownloadGroupBinding;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;
import java.util.List;

/**
 * Created by Aria.Lao on 2017/7/6.
 */
public class DownloadGroupActivity extends BaseActivity<ActivityDownloadGroupBinding> {

  @Bind(R.id.progressBar) HorizontalProgressBarWithNumber mPb;
  @Bind(R.id.start) Button mStart;
  @Bind(R.id.stop) Button mStop;
  @Bind(R.id.cancel) Button mCancel;
  @Bind(R.id.size) TextView mSize;
  @Bind(R.id.speed) TextView mSpeed;
  List<String> mUrls;

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setTitle("任务组");
    mUrls = getModule(GroupModule.class).getUrls();
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_download_group;
  }

  @Override protected void onResume() {
    super.onResume();
    //Aria.download(this).addSchedulerListener(new AbsSchedulerListener<DownloadTask>() {
    //
    //});
  }

  //public void onClick(View view) {
  //  switch (view.getId()) {
  //    case R.id.start:
  //      String text = ((TextView) view).getText().toString();
  //      if (text.equals("重新开始？") || text.equals("开始")) {
  //        Aria.download(this)
  //            .load(mUrls)
  //            .setDownloadPaths()
  //            .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/test.apk")
  //            .start();
  //      } else if (text.equals("恢复")) {
  //        Aria.download(this).load(DOWNLOAD_URL).resume();
  //      }
  //      break;
  //    case R.id.stop:
  //      Aria.download(this).load(DOWNLOAD_URL).pause();
  //      break;
  //    case R.id.cancel:
  //      Aria.download(this).load(DOWNLOAD_URL).cancel();
  //      break;
  //  }
  //}
}

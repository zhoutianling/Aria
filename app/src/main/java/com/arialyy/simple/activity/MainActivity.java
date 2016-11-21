/*
 * Copyright (C) 2016 AriaLyy(DownloadUtil)
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


package com.arialyy.simple.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import butterknife.Bind;
import com.arialyy.frame.permission.OnPermissionCallback;
import com.arialyy.frame.permission.PermissionManager;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityMainBinding;

/**
 * Created by Lyy on 2016/10/13.
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {
  @Bind(R.id.toolbar) Toolbar mBar;
  @Bind(R.id.single_task) Button mSigleBt;
  @Bind(R.id.multi_task) Button mMultiBt;

  @Override protected int setLayoutId() {
    return R.layout.activity_main;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setSupportActionBar(mBar);
    mBar.setTitle("多线程多任务下载");
    boolean hasPermission = PermissionManager.getInstance()
        .checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    if (hasPermission) {
      setEnable(true);
    } else {
      setEnable(false);
      PermissionManager.getInstance().requestPermission(this, new OnPermissionCallback() {
        @Override public void onSuccess(String... permissions) {
          setEnable(true);
        }

        @Override public void onFail(String... permissions) {
          T.showShort(MainActivity.this, "没有文件读写权限");
          setEnable(false);
        }
      }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
  }

  private void setEnable(boolean enable) {
    mSigleBt.setEnabled(enable);
    mMultiBt.setEnabled(enable);
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.single_task:
        startActivity(new Intent(this, SingleTaskActivity.class));
        break;
      case R.id.multi_task:
        startActivity(new Intent(this, MultiTaskActivity.class));
        break;
    }
  }
}
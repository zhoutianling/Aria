package com.arialyy.simple.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import butterknife.Bind;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.core.DownloadManager;
import com.arialyy.frame.util.show.L;
import com.arialyy.simple.R;
import com.arialyy.simple.adapter.DownloadAdapter;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityMultiBinding;
import com.arialyy.simple.module.DownloadModule;

/**
 * Created by Lyy on 2016/9/27.
 */
public class MultiTaskActivity extends BaseActivity<ActivityMultiBinding> {
  @Bind(R.id.list)    RecyclerView mList;
  @Bind(R.id.toolbar) Toolbar      mBar;
  DownloadAdapter mAdapter;

  @Override protected int setLayoutId() {
    return R.layout.activity_multi;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setSupportActionBar(mBar);
    mBar.setTitle("多任务下载");
    mAdapter = new DownloadAdapter(this, getModule(DownloadModule.class).getDownloadData());
    mList.setLayoutManager(new LinearLayoutManager(this));
    mList.setAdapter(mAdapter);
  }

  private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    long len = 0;

    @Override public void onReceive(Context context, Intent intent) {
      String         action = intent.getAction();
      DownloadEntity entity = intent.getParcelableExtra(DownloadManager.ENTITY);
      switch (action) {
        case DownloadManager.ACTION_PRE:
          len = entity.getFileSize();
          L.d(TAG, "download pre");
          break;
        case DownloadManager.ACTION_START:
          L.d(TAG, "download start");
          break;
        case DownloadManager.ACTION_RESUME:
          L.d(TAG, "download resume");
          long location = intent.getLongExtra(DownloadManager.CURRENT_LOCATION, 1);
          mAdapter.updateState(entity);
          break;
        case DownloadManager.ACTION_RUNNING:
          long current = intent.getLongExtra(DownloadManager.CURRENT_LOCATION, 0);
          long speed = intent.getLongExtra(DownloadManager.CURRENT_SPEED, 0);
          //mAdapter.setProgress(entity.getDownloadUrl(), current, speed);
          mAdapter.setProgress(entity);
          break;
        case DownloadManager.ACTION_STOP:
          L.d(TAG, "download stop");
          mAdapter.updateState(entity);
          break;
        case DownloadManager.ACTION_COMPLETE:
          L.d(TAG, "download complete");
          mAdapter.updateState(entity);
          break;
        case DownloadManager.ACTION_CANCEL:
          L.d(TAG, "download cancel");
          mAdapter.updateState(entity);
          break;
        case DownloadManager.ACTION_FAIL:
          L.d(TAG, "download fail");
          break;
      }
    }
  };

  @Override protected void onResume() {
    super.onResume();
    registerReceiver(mReceiver, getModule(DownloadModule.class).getDownloadFilter());
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(mReceiver);
  }
}

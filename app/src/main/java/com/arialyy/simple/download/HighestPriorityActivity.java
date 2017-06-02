package com.arialyy.simple.download;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTarget;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.frame.util.show.L;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityHighestPriorityBinding;
import com.arialyy.simple.download.multi_download.DownloadAdapter;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;

/**
 * Created by lyy on 2017/6/2.
 * 最高优先级任务Demo
 */
public class HighestPriorityActivity extends BaseActivity<ActivityHighestPriorityBinding> {
  @Bind(R.id.progressBar) HorizontalProgressBarWithNumber mPb;
  @Bind(R.id.start) Button mStart;
  @Bind(R.id.stop) Button mStop;
  @Bind(R.id.cancel) Button mCancel;
  @Bind(R.id.size) TextView mSize;
  @Bind(R.id.toolbar) Toolbar toolbar;
  @Bind(R.id.speed) TextView mSpeed;
  @Bind(R.id.list) RecyclerView mList;

  private String mTaskName = "狂野飙车8";
  private static final String DOWNLOAD_URL =
      "http://static.gaoshouyou.com/d/82/ff/df82ed0af4ff4c1746cb191cf765aa8f.apk";
  private DownloadAdapter mAdapter;

  @Override protected int setLayoutId() {
    return R.layout.activity_highest_priority;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setSupportActionBar(toolbar);
    toolbar.setTitle("最高优先级任务演示");
    getBinding().setTaskName("任务名：" + mTaskName + " （该任务是最高优先级任务）");
    initWidget();
  }

  private void initWidget() {
    if (Aria.download(this).taskExists(DOWNLOAD_URL)) {
      DownloadTarget target = Aria.download(this).load(DOWNLOAD_URL);
      int p = (int) (target.getCurrentProgress() * 100 / target.getFileSize());
      mPb.setProgress(p);
    }
    mAdapter = new DownloadAdapter(this, getModule(DownloadModule.class).getDownloadTaskList());
    mList.setLayoutManager(new LinearLayoutManager(this));
    mList.setAdapter(mAdapter);
  }

  @Override protected void onResume() {
    super.onResume();
    Aria.download(this).addSchedulerListener(new MySchedulerListener());
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        String text = ((TextView) view).getText().toString();
        if (text.equals("重新开始？") || text.equals("开始")) {
          Aria.download(this)
              .load(DOWNLOAD_URL)
              .setDownloadPath(Environment.getExternalStorageDirectory().getPath()
                  + "/Download/"
                  + mTaskName
                  + ".apk")
              .setHighestPriority();
        } else if (text.equals("恢复")) {
          Aria.download(this).load(DOWNLOAD_URL).resume();
        }
        break;
      case R.id.stop:
        Aria.download(this).load(DOWNLOAD_URL).pause();
        break;
      case R.id.cancel:
        Aria.download(this).load(DOWNLOAD_URL).cancel();
        break;
    }
  }

  /**
   * 设置start 和 stop 按钮状态
   */
  private void setBtState(boolean state) {
    mStart.setEnabled(state);
    mStop.setEnabled(!state);
  }

  private class MySchedulerListener extends Aria.DownloadSchedulerListener {

    @Override public void onTaskPre(DownloadTask task) {
      super.onTaskPre(task);
      if (task.getKey().equals(DOWNLOAD_URL)) {
        mSize.setText(task.getConvertFileSize());
      } else {
        mAdapter.updateState(task.getDownloadEntity());
      }
    }

    @Override public void onTaskStart(DownloadTask task) {
      if (task.getKey().equals(DOWNLOAD_URL)) {
        setBtState(false);
      } else {
        mAdapter.updateState(task.getDownloadEntity());
      }
    }

    @Override public void onTaskResume(DownloadTask task) {
      if (task.getKey().equals(DOWNLOAD_URL)) {
        setBtState(false);
      } else {
        mAdapter.updateState(task.getDownloadEntity());
      }
    }

    @Override public void onTaskStop(DownloadTask task) {
      if (task.getKey().equals(DOWNLOAD_URL)) {
        setBtState(true);
      } else {
        mAdapter.updateState(task.getDownloadEntity());
      }
    }

    @Override public void onTaskCancel(DownloadTask task) {
      if (task.getKey().equals(DOWNLOAD_URL)) {
        setBtState(true);
      } else {
        mAdapter.updateState(task.getDownloadEntity());
      }
    }

    @Override public void onTaskFail(DownloadTask task) {
      if (task.getKey().equals(DOWNLOAD_URL)) {
        setBtState(true);
      } else {
        L.d(TAG, "download fail【" + task.getKey() + "】");
      }
    }

    @Override public void onTaskComplete(DownloadTask task) {
      if (task.getKey().equals(DOWNLOAD_URL)) {
        setBtState(true);
      }
    }

    @Override public void onTaskRunning(DownloadTask task) {
      if (task.getKey().equals(DOWNLOAD_URL)) {
        setBtState(true);
        mPb.setProgress(task.getPercent());
        mSpeed.setText(task.getConvertSpeed());
      } else {
        mAdapter.setProgress(task.getDownloadEntity());
      }
    }
  }
}

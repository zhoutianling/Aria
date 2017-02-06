package com.arialyy.simple.dialog_task;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.arialyy.aria.core.AMTarget;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.DownloadEntity;
import com.arialyy.aria.core.task.Task;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.core.AbsDialog;
import com.arialyy.simple.R;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;

/**
 * Created by AriaL on 2017/1/2.
 */
public class DownloadDialog extends AbsDialog {
  @Bind(R.id.progressBar) HorizontalProgressBarWithNumber mPb;
  @Bind(R.id.start)       Button                          mStart;
  @Bind(R.id.stop)        Button                          mStop;
  @Bind(R.id.cancel)      Button                          mCancel;
  @Bind(R.id.size)        TextView                        mSize;
  @Bind(R.id.speed)       TextView                        mSpeed;

  private static final String DOWNLOAD_URL =
      "http://static.gaoshouyou.com/d/3a/93/573ae1db9493a801c24bf66128b11e39.apk";

  public DownloadDialog(Context context) {
    super(context);
    init();
  }

  @Override protected int setLayoutId() {
    return R.layout.dialog_download;
  }

  private void init() {
    if (Aria.get(this).taskExists(DOWNLOAD_URL)) {
      AMTarget target = Aria.download(this).load(DOWNLOAD_URL);
      int      p      = (int) (target.getCurrentProgress() * 100 / target.getFileSize());
      mPb.setProgress(p);
    }
    Aria.download(this).addSchedulerListener(new MyDialogDownloadCallback());
    DownloadEntity entity = Aria.get(this).getDownloadEntity(DOWNLOAD_URL);
    if (entity != null) {
      mSize.setText(CommonUtil.formatFileSize(entity.getFileSize()));
      int state = entity.getState();
      setBtState(state != DownloadEntity.STATE_DOWNLOAD_ING);
    } else {
      setBtState(true);
    }
  }

  @OnClick({ R.id.start, R.id.stop, R.id.cancel }) public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        Aria.download(this)
            .load(DOWNLOAD_URL)
            .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/daialog.apk")
            .setDownloadName("daialog.apk")
            .start();
        break;
      case R.id.stop:
        Aria.download(this).load(DOWNLOAD_URL).stop();
        break;
      case R.id.cancel:
        Aria.download(this).load(DOWNLOAD_URL).cancel();
        break;
    }
  }

  @Override protected void dataCallback(int result, Object obj) {

  }

  private void setBtState(boolean startEnable) {
    mStart.setEnabled(startEnable);
    mCancel.setEnabled(!startEnable);
    mStop.setEnabled(!startEnable);
  }

  private class MyDialogDownloadCallback extends Aria.SimpleSchedulerListener {

    @Override public void onTaskPre(Task task) {
      super.onTaskPre(task);
      mSize.setText(CommonUtil.formatFileSize(task.getFileSize()));
      setBtState(false);
    }

    @Override public void onTaskStop(Task task) {
      super.onTaskStop(task);
      setBtState(true);
      mSpeed.setText("0.0kb/s");
    }

    @Override public void onTaskCancel(Task task) {
      super.onTaskCancel(task);
      setBtState(true);
      mPb.setProgress(0);
      mSpeed.setText("0.0kb/s");
    }

    @Override public void onTaskRunning(Task task) {
      super.onTaskRunning(task);
      long current = task.getCurrentProgress();
      long len     = task.getFileSize();
      if (len == 0) {
        mPb.setProgress(0);
      } else {
        mPb.setProgress((int) ((current * 100) / len));
      }
      mSpeed.setText(CommonUtil.formatFileSize(task.getSpeed()) + "/s");
    }
  }
}

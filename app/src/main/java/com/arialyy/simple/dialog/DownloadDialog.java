package com.arialyy.simple.dialog;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.arialyy.aria.core.AMTarget;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.task.Task;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.core.AbsDialog;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseDialog;
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
    return R.layout.content_single;
  }

  private void init() {
    if (Aria.get(getContext()).taskExists(DOWNLOAD_URL)) {
      AMTarget target = Aria.whit(getContext()).load(DOWNLOAD_URL);
      int      p      = (int) (target.getCurrentProgress() * 100 / target.getFileSize());
      mPb.setProgress(p);
    }
    Aria.whit(this).addSchedulerListener(new MyDialogDownloadCallback());
  }

  @OnClick({ R.id.start, R.id.stop, R.id.cancel }) public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        Aria.whit(getContext())
            .load(DOWNLOAD_URL)
            .setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/daialog.apk")
            .setDownloadName("daialog.apk")
            .start();
        break;
      case R.id.stop:
        Aria.whit(getContext()).load(DOWNLOAD_URL).stop();
        break;
      case R.id.cancel:
        Aria.whit(getContext()).load(DOWNLOAD_URL).cancel();
        break;
    }
  }

  @Override protected void dataCallback(int result, Object obj) {

  }

  private class MyDialogDownloadCallback extends Aria.SimpleSchedulerListener {
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

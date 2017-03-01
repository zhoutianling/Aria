package com.arialyy.simple;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import butterknife.Bind;
import butterknife.OnClick;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityMainBinding;
import com.arialyy.simple.download.DownloadActivity;
import com.arialyy.simple.upload.UploadActivity;

/**
 * Created by Aria.Lao on 2017/3/1.
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {

  @Bind(R.id.toolbar) Toolbar mBar;

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    mBar.setTitle("Aria  Demo");
  }

  @Override protected int setLayoutId() {
    return R.layout.activity_main;
  }

  @OnClick(R.id.download) public void downloadDemo() {
    startActivity(new Intent(this, DownloadActivity.class));
  }

  @OnClick(R.id.upload) public void uploadDemo() {
    startActivity(new Intent(this, UploadActivity.class));
  }
}

package com.arialyy.simple.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import butterknife.Bind;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityMainBinding;

/**
 * Created by Lyy on 2016/10/13.
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {
  @Bind(R.id.toolbar) Toolbar mBar;

  @Override protected int setLayoutId() {
    return R.layout.activity_main;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    setSupportActionBar(mBar);
    mBar.setTitle("多线程多任务下载");
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

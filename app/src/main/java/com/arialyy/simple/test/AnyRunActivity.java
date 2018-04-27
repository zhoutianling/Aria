package com.arialyy.simple.test;

import android.os.Bundle;
import android.view.View;
import com.arialyy.aria.core.Aria;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityTestBinding;

/**
 * Created by laoyuyu on 2018/4/13.
 */

public class AnyRunActivity extends BaseActivity<ActivityTestBinding> {
  AnyRunnModule module;
  String[] urls;
  int index = 0;

  @Override protected int setLayoutId() {
    return R.layout.activity_test;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    Aria.init(this);
    mBar.setVisibility(View.GONE);
    module = new AnyRunnModule(this);
    urls = getResources().getStringArray(R.array.group_urls);
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        //module.start();
        if (index < urls.length) {
          module.start(urls[index]);
          index++;
        }
        break;
      case R.id.stop:
        module.stop();
        break;
      case R.id.cancel:
        module.cancel();
        break;
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    module.unRegister();
  }
}

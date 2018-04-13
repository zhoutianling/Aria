package com.arialyy.simple.test;

import android.os.Bundle;
import android.view.View;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityTestBinding;

/**
 * Created by laoyuyu on 2018/4/13.
 */

public class AnyRunActivity extends BaseActivity<ActivityTestBinding> {
  AnyRunnModule module;

  @Override protected int setLayoutId() {
    return R.layout.activity_test;
  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
    mBar.setVisibility(View.GONE);
    module = new AnyRunnModule(this);
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start:
        module.start();
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
    module.unRegist();
  }
}

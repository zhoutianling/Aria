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

package com.arialyy.simple.base;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.os.PersistableBundle;
import com.arialyy.frame.core.AbsActivity;
import com.arialyy.frame.util.AndroidVersionUtil;
import com.arialyy.simple.R;

/**
 * Created by Lyy on 2016/9/27.
 */
public abstract class BaseActivity<VB extends ViewDataBinding> extends AbsActivity<VB> {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (AndroidVersionUtil.hasLollipop()) {
      getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
    }
  }

  @Override protected void dataCallback(int result, Object data) {

  }

  @Override protected void init(Bundle savedInstanceState) {
    super.init(savedInstanceState);
  }
}
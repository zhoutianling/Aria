package com.arialyy.simple.activity;

import android.support.v7.widget.RecyclerView;

import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityMainBinding;

import butterknife.Bind;

/**
 * Created by Lyy on 2016/9/27.
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {
    @Bind(R.id.list) RecyclerView mList;

    @Override protected int setLayoutId() {
        return R.layout.activity_main;
    }


}

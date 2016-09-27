package com.arialyy.simple.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.arialyy.simple.R;
import com.arialyy.simple.adapter.DownloadAdapter;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivityMainBinding;
import com.arialyy.simple.module.DownloadModule;

import butterknife.Bind;

/**
 * Created by Lyy on 2016/9/27.
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {
    @Bind(R.id.list) RecyclerView mList;
    DownloadAdapter mAdapter;

    @Override protected int setLayoutId() {
        return R.layout.activity_main;
    }

    @Override protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        mAdapter = new DownloadAdapter(this, getModule(DownloadModule.class).getDownloadData());
        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setAdapter(mAdapter);
    }
}

package com.arialyy.aria.window;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.AbsListView;
import android.widget.ListView;
import com.arialyy.aria.R;
import com.arialyy.aria.util.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Aria.Lao on 2017/3/21.
 * 文件选择
 */
public class AriaFileChangeActivity extends FragmentActivity {
  final String ROOT_PAT = Environment.getExternalStorageDirectory().getPath();
  ListView mList;
  FileChangeAdapter mAdapter;
  Map<String, List<FileEntity>> mData = new HashMap<>();
  private String mCurrentPath = ROOT_PAT;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_aria_file_shange);
    mList = (ListView) findViewById(R.id.list);
    mList.setOnScrollListener(new AbsListView.OnScrollListener() {
      int state;

      @Override public void onScrollStateChanged(AbsListView view, int scrollState) {
        state = scrollState;
      }

      @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
          int totalItemCount) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
            && firstVisibleItem + visibleItemCount == totalItemCount) {
          loadMore();
        }
      }
    });
  }

  private void loadMore() {

  }



}

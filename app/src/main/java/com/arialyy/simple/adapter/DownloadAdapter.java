package com.arialyy.simple.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import butterknife.Bind;
import com.arialyy.absadapter.common.AbsHolder;
import com.arialyy.absadapter.recycler_view.AbsRVAdapter;
import com.arialyy.downloadutil.entity.DownloadEntity;
import com.arialyy.simple.R;
import com.arialyy.simple.widget.HorizontalProgressBarWithNumber;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lyy on 2016/9/27.
 * 下载列表适配器
 */
public class DownloadAdapter extends AbsRVAdapter<DownloadEntity, DownloadAdapter.MyHolder> {
  private static final String TAG = "DownloadAdapter";
  private Map<String, Long> mProgress = new HashMap<>();
  private SparseArray<String> mPositions = new SparseArray<>();

  public DownloadAdapter(Context context, List<DownloadEntity> data) {
    super(context, data);
    int i = 0;
    for (DownloadEntity entity : data) {
      mProgress.put(entity.getDownloadUrl(), entity.getCurrentProgress());
      mPositions.append(i, entity.getDownloadUrl());
      i++;
    }
  }

  @Override protected MyHolder getViewHolder(View convertView, int viewType) {
    return new MyHolder(convertView);
  }

  @Override protected int setLayoutId(int type) {
    return R.layout.item_download;
  }

  public synchronized void setProgress(String url, long currentPosition) {
    mProgress.put(url, currentPosition);
    notifyItemChanged(indexItem(url));
  }

  private int indexItem(String url) {
    return mPositions.indexOfValue(url);
  }

  @Override protected void bindData(MyHolder holder, int position, DownloadEntity item) {
    //holder.progress.setProgress(item.getCurrentProgress());
    long size = item.getFileSize();
    int current = 0;
    if (size == 0){
      current = 0;
    }
    current = (int) (mProgress.get(item.getDownloadUrl()) * 100 / item.getFileSize());
    holder.progress.setProgress(current);
  }

  class MyHolder extends AbsHolder {
    @Bind(R.id.progressBar) HorizontalProgressBarWithNumber progress;
    @Bind(R.id.bt) Button bt;

    public MyHolder(View itemView) {
      super(itemView);
    }
  }
}

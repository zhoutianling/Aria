package com.arialyy.simple.module;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Environment;
import com.arialyy.downloadutil.core.DownloadManager;
import com.arialyy.downloadutil.entity.DownloadEntity;
import com.arialyy.downloadutil.util.Util;
import com.arialyy.frame.util.AndroidUtils;
import com.arialyy.frame.util.StringUtil;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseModule;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lyy on 2016/9/27.
 */
public class DownloadModule extends BaseModule {
  public DownloadModule(Context context) {
    super(context);
  }

  /**
   * 设置下载数据
   */
  public List<DownloadEntity> getDownloadData() {
    List<DownloadEntity> list = DownloadEntity.findAllData(DownloadEntity.class);
    List<DownloadEntity> newDownload = createNewDownload();
    if (list == null) {
      list = newDownload;
    } else {
      list = filter(list, newDownload);
    }
    return list;
  }

  /**
   * 过滤任务
   *
   * @param sqlEntity 数据库的下载实体
   * @param createdEntity 通过下载链接生成的下载实体
   */
  private List<DownloadEntity> filter(List<DownloadEntity> sqlEntity,
      List<DownloadEntity> createdEntity) {
    List<DownloadEntity> list = new ArrayList<>();
    list.addAll(sqlEntity);
    for (DownloadEntity cEntity : createdEntity) {
      int count = 0;
      for (DownloadEntity sEntity : sqlEntity) {
        if (cEntity.getDownloadUrl().equals(sEntity.getDownloadUrl())) {
          break;
        }
        count++;
        if (count == sqlEntity.size()) {
          list.add(cEntity);
        }
      }
    }
    return list;
  }

  /**
   * 创建下载列表
   */
  private List<DownloadEntity> createNewDownload() {
    List<DownloadEntity> list = new ArrayList<>();
    String[] urls = getContext().getResources().getStringArray(R.array.test_apk_download_url);
    for (String url : urls) {
      String fileName = Util.keyToHashCode(url) + ".apk";
      DownloadEntity entity = new DownloadEntity();
      entity.setDownloadUrl(url);
      entity.setDownloadPath(getDownloadPath(url));
      entity.setFileName(fileName);
      list.add(entity);
    }
    return list;
  }

  /**
   * 下载广播过滤器
   */
  public IntentFilter getDownloadFilter() {
    IntentFilter filter = new IntentFilter();
    filter.addDataScheme(getContext().getPackageName());
    filter.addAction(DownloadManager.ACTION_PRE);
    filter.addAction(DownloadManager.ACTION_RESUME);
    filter.addAction(DownloadManager.ACTION_START);
    filter.addAction(DownloadManager.ACTION_RUNNING);
    filter.addAction(DownloadManager.ACTION_STOP);
    filter.addAction(DownloadManager.ACTION_CANCEL);
    filter.addAction(DownloadManager.ACTION_COMPLETE);
    filter.addAction(DownloadManager.ACTION_FAIL);
    return filter;
  }

  /**
   * 设置下载队列
   */
  private String getDownloadPath(String url) {
    String path =
        Environment.getExternalStorageDirectory().getPath() + "/" + AndroidUtils.getAppName(
            getContext()) + "downloads/" + StringUtil.keyToHashKey(url) + ".apk";
    File file = new File(path);
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    return path;
  }
}

package com.arialyy.downloadutil.help;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import com.arialyy.downloadutil.R;
import com.arialyy.downloadutil.entity.DownloadEntity;
import java.io.File;

/**
 * Created by Lyy on 2016/9/23.
 * 检查帮助类
 */
public class CheckHelp {
  private static final String TAG = "CheckHelp";

  /**
   * 检测下载实体是否合法
   *
   * @param entity 下载实体
   * @return 合法(true)
   */
  public static boolean checkDownloadEntity(DownloadEntity entity) {
    if (entity == null) {
      Log.w(TAG, Resources.getSystem().getString(R.string.error_entity_null));
      return false;
    } else if (TextUtils.isEmpty(entity.getDownloadUrl())) {
      Log.w(TAG, Resources.getSystem().getString(R.string.error_download_url_null));
      return false;
    } else if (TextUtils.isEmpty(entity.getFileName())) {
      Log.w(TAG, Resources.getSystem().getString(R.string.error_file_name_null));
      return false;
    } else if (TextUtils.isEmpty(entity.getDownloadPath())) {
      Log.w(TAG, Resources.getSystem().getString(R.string.error_file_name_null));
      return false;
    }
    String fileName = entity.getFileName();
    if (fileName.contains(" ")) {
      fileName = fileName.replace(" ", "_");
    }
    String dPath = entity.getDownloadPath();
    File file = new File(dPath);
    if (file.isDirectory()) {
      dPath += fileName;
      entity.setDownloadPath(dPath);
    }
    return true;
  }
}

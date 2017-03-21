package com.arialyy.aria.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import com.arialyy.aria.window.FileEntity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aria.Lao on 2017/3/21.
 */

public class FileUtil {

  Context mContext;

  public FileUtil(Context context) {
    mContext = context;
  }

  /**
   * 文件列表
   */
  public List<FileEntity> loadFiles(String path) {
    File file = new File(path);
    File[] files = file.listFiles();
    List<FileEntity> list = new ArrayList<>();
    for (File f : files) {
      FileEntity entity = new FileEntity();
      entity.fileName = f.getName();
      //entity.fileInfo = getFileType(f.getPath());
      //entity.fileDrawable = getApkIcon(mContext, f.getPath());
      list.add(entity);
    }
    return list;
  }

  /**
   * 获取文件类型
   */
  public FileType getFileType(String path) {
    String exName = getExName(path);
    String type = "";
    FileType fType = null;
    if (exName.equalsIgnoreCase("apk")) {
      fType = new FileType("应用", getApkIcon(path));
    } else if (exName.equalsIgnoreCase("img")
        || exName.equalsIgnoreCase("png")
        || exName.equalsIgnoreCase("jpg")
        || exName.equalsIgnoreCase("jepg")) {
      //fType = new FileType("图片", )
    } else if (exName.equalsIgnoreCase("mp3") || exName.equalsIgnoreCase("wm")) {
      //fType = new FileType("音乐", );
    } else if (exName.equalsIgnoreCase("mp4")
        || exName.equalsIgnoreCase("rm")
        || exName.equalsIgnoreCase("rmvb")) {
      //fType = new FileType("视频", );
    } return fType;
  }

  /**
   * 获取扩展名
   */
  public String getExName(String path) {
    int separatorIndex = path.lastIndexOf(".");
    return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
  }

  /**
   * 获取apk文件的icon
   *
   * @param path apk文件路径
   */
  public Drawable getApkIcon(String path) {
    PackageManager pm = mContext.getPackageManager();
    PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
    if (info != null) {
      ApplicationInfo appInfo = info.applicationInfo;
      //android有bug，需要下面这两句话来修复才能获取apk图片
      appInfo.sourceDir = path;
      appInfo.publicSourceDir = path;
      //			    String packageName = appInfo.packageName;  //得到安装包名称
      //	            String version=info.versionName;       //得到版本信息
      return pm.getApplicationIcon(appInfo);
    }
    return null;
  }

  class FileType {
    String name;
    Drawable icon;

    public FileType(String name, Drawable icon) {
      this.name = name;
      this.icon = icon;
    }
  }
}

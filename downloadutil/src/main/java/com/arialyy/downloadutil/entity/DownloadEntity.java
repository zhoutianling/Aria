package com.arialyy.downloadutil.entity;

import android.os.Parcel;
import android.os.Parcelable;
import com.arialyy.downloadutil.orm.DbEntity;
import com.arialyy.downloadutil.orm.Ignore;

/**
 * Created by lyy on 2015/12/25.
 * 下载实体
 */
public class DownloadEntity extends DbEntity implements Parcelable, Cloneable {
  /**
   * 其它状态
   */
  @Ignore public static final int STATE_OTHER        = -1;
  /**
   * 失败状态
   */
  @Ignore public static final int STATE_FAIL         = 0;
  /**
   * 完成状态
   */
  @Ignore public static final int STATE_COMPLETE     = 1;
  /**
   * 停止状态
   */
  @Ignore public static final int STATE_STOP         = 2;
  /**
   * 未开始状态
   */
  @Ignore public static final int STATE_WAIT         = 3;
  /**
   * 下载中
   */
  @Ignore public static final int STATE_DOWNLOAD_ING = 4;
  /**
   * 取消下载
   */
  @Ignore public static final int STATE_CANCEL       = 5;

  private String downloadUrl  = ""; //下载路径
  private String downloadPath = "";    //保存路径
  private String fileName     = "";        //文件名
  private String str          = "";             //其它字段
  private long completeTime;  //完成时间
  private long    fileSize           = 1;
  private int     state              = STATE_WAIT;
  private boolean isDownloadComplete = false;   //是否下载完成
  private long    currentProgress    = 0;    //当前下载进度
  private int     failNum            = 0;

  public DownloadEntity() {
  }

  public String getStr() {
    return str;
  }

  public void setStr(String str) {
    this.str = str;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public int getFailNum() {
    return failNum;
  }

  public void setFailNum(int failNum) {
    this.failNum = failNum;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public long getCompleteTime() {
    return completeTime;
  }

  public void setCompleteTime(long completeTime) {
    this.completeTime = completeTime;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  public String getDownloadPath() {
    return downloadPath;
  }

  public void setDownloadPath(String downloadPath) {
    this.downloadPath = downloadPath;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public boolean isDownloadComplete() {
    return isDownloadComplete;
  }

  public void setDownloadComplete(boolean downloadComplete) {
    isDownloadComplete = downloadComplete;
  }

  public long getCurrentProgress() {
    return currentProgress;
  }

  public void setCurrentProgress(long currentProgress) {
    this.currentProgress = currentProgress;
  }

  @Override public DownloadEntity clone() throws CloneNotSupportedException {
    return (DownloadEntity) super.clone();
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.downloadUrl);
    dest.writeString(this.downloadPath);
    dest.writeLong(this.completeTime);
    dest.writeLong(this.fileSize);
    dest.writeInt(this.state);
    dest.writeByte(this.isDownloadComplete ? (byte) 1 : (byte) 0);
    dest.writeLong(this.currentProgress);
    dest.writeInt(this.failNum);
  }

  protected DownloadEntity(Parcel in) {
    this.downloadUrl = in.readString();
    this.downloadPath = in.readString();
    this.completeTime = in.readLong();
    this.fileSize = in.readLong();
    this.state = in.readInt();
    this.isDownloadComplete = in.readByte() != 0;
    this.currentProgress = in.readLong();
    this.failNum = in.readInt();
  }

  @Ignore public static final Creator<DownloadEntity> CREATOR = new Creator<DownloadEntity>() {
    @Override public DownloadEntity createFromParcel(Parcel source) {
      return new DownloadEntity(source);
    }

    @Override public DownloadEntity[] newArray(int size) {
      return new DownloadEntity[size];
    }
  };

  @Override public String toString() {
    return "DownloadEntity{" +
        "downloadUrl='" + downloadUrl + '\'' +
        ", downloadPath='" + downloadPath + '\'' +
        ", completeTime=" + completeTime +
        ", fileSize=" + fileSize +
        ", state=" + state +
        ", isDownloadComplete=" + isDownloadComplete +
        ", currentProgress=" + currentProgress +
        ", failNum=" + failNum +
        '}';
  }
}
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


package com.arialyy.aria.core.download;

import android.os.Parcel;
import android.os.Parcelable;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.ITaskEntity;
import com.arialyy.aria.orm.Ignore;
import com.arialyy.aria.orm.DbEntity;

/**
 * Created by lyy on 2015/12/25.
 * 下载实体
 * ！！！ 注意：CREATOR要进行@Ignore注解
 * ！！！并且需要Parcelable时需要手动填写rowID;
 */
public class DownloadEntity extends DbEntity implements Parcelable, IEntity{
  @Ignore public static final Creator<DownloadEntity> CREATOR = new Creator<DownloadEntity>() {
    @Override public DownloadEntity createFromParcel(Parcel source) {
      return new DownloadEntity(source);
    }

    @Override public DownloadEntity[] newArray(int size) {
      return new DownloadEntity[size];
    }
  };
  @Ignore private long    speed              = 0; //下载速度
  @Ignore private         int     failNum            = 0;
  private         String  downloadUrl        = ""; //下载路径
  private         String  downloadPath       = ""; //保存路径
  private         String  fileName           = ""; //文件名
  private         String  str                = ""; //其它字段
  private         long    fileSize           = 1;
  private         int     state              = STATE_WAIT;
  private         boolean isDownloadComplete = false;   //是否下载完成
  private         long    currentProgress    = 0;    //当前下载进度
  private long completeTime;  //完成时间

  public DownloadEntity() {
  }

  protected DownloadEntity(Parcel in) {
    this.downloadUrl = in.readString();
    this.downloadPath = in.readString();
    this.fileName = in.readString();
    this.str = in.readString();
    this.completeTime = in.readLong();
    this.fileSize = in.readLong();
    this.state = in.readInt();
    this.isDownloadComplete = in.readByte() != 0;
    this.currentProgress = in.readLong();
    this.failNum = in.readInt();
    this.speed = in.readLong();
    this.rowID = in.readInt();
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

  public DownloadEntity setFileName(String fileName) {
    this.fileName = fileName;
    return this;
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

  public DownloadEntity setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
    return this;
  }

  public long getCompleteTime() {
    return completeTime;
  }

  public void setCompleteTime(long completeTime) {
    this.completeTime = completeTime;
  }

  public String getDownloadPath() {
    return downloadPath;
  }

  public DownloadEntity setDownloadPath(String downloadPath) {
    this.downloadPath = downloadPath;
    return this;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  @Override
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

  public long getSpeed() {
    return speed;
  }

  public void setSpeed(long speed) {
    this.speed = speed;
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

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.downloadUrl);
    dest.writeString(this.downloadPath);
    dest.writeString(this.fileName);
    dest.writeString(this.str);
    dest.writeLong(this.completeTime);
    dest.writeLong(this.fileSize);
    dest.writeInt(this.state);
    dest.writeByte(this.isDownloadComplete ? (byte) 1 : (byte) 0);
    dest.writeLong(this.currentProgress);
    dest.writeInt(this.failNum);
    dest.writeLong(this.speed);
    dest.writeInt(this.rowID);
  }
}
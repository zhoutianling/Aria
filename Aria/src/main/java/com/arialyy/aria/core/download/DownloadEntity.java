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
import com.arialyy.aria.core.inf.AbsNormalEntity;
import com.arialyy.aria.orm.Primary;

/**
 * Created by lyy on 2015/12/25.
 * 下载实体
 */
public class DownloadEntity extends AbsNormalEntity implements Parcelable {
  @Primary private String downloadUrl = ""; //下载路径
  private String downloadPath = ""; //保存路径
  private boolean isDownloadComplete = false;   //是否下载完成
  private boolean isRedirect = false; //是否重定向
  private String redirectUrl = ""; //重定向链接

  /**
   * 所属任务组
   */
  private String groupName = "";

  public DownloadEntity() {
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public DownloadEntity setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
    return this;
  }

  public String getDownloadPath() {
    return downloadPath;
  }

  public DownloadEntity setDownloadPath(String downloadPath) {
    this.downloadPath = downloadPath;
    return this;
  }

  public boolean isDownloadComplete() {
    return isDownloadComplete;
  }

  public void setDownloadComplete(boolean downloadComplete) {
    isDownloadComplete = downloadComplete;
  }

  @Override public DownloadEntity clone() throws CloneNotSupportedException {
    return (DownloadEntity) super.clone();
  }

  public boolean isRedirect() {
    return isRedirect;
  }

  public void setRedirect(boolean redirect) {
    isRedirect = redirect;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeString(this.downloadUrl);
    dest.writeString(this.downloadPath);
    dest.writeByte(this.isDownloadComplete ? (byte) 1 : (byte) 0);
    dest.writeByte(this.isRedirect ? (byte) 1 : (byte) 0);
    dest.writeString(this.redirectUrl);
    dest.writeString(this.groupName);
  }

  protected DownloadEntity(Parcel in) {
    super(in);
    this.downloadUrl = in.readString();
    this.downloadPath = in.readString();
    this.isDownloadComplete = in.readByte() != 0;
    this.isRedirect = in.readByte() != 0;
    this.redirectUrl = in.readString();
    this.groupName = in.readString();
  }

  @Override public String toString() {
    return "DownloadEntity{"
        + "downloadUrl='"
        + downloadUrl
        + '\''
        + ", downloadPath='"
        + downloadPath
        + '\''
        + ", isDownloadComplete="
        + isDownloadComplete
        + ", isRedirect="
        + isRedirect
        + ", redirectUrl='"
        + redirectUrl
        + '\''
        + ", groupName='"
        + groupName
        + '\''
        + '}';
  }

  public static final Creator<DownloadEntity> CREATOR = new Creator<DownloadEntity>() {
    @Override public DownloadEntity createFromParcel(Parcel source) {
      return new DownloadEntity(source);
    }

    @Override public DownloadEntity[] newArray(int size) {
      return new DownloadEntity[size];
    }
  };
}
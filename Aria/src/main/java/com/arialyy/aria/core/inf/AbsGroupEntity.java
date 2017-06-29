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
package com.arialyy.aria.core.inf;

import android.os.Parcel;
import android.os.Parcelable;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.orm.Ignore;

/**
 * Created by AriaL on 2017/6/3.
 */
public abstract class AbsGroupEntity<CHILD_ENTITY extends AbsNormalEntity> extends DbEntity implements IEntity, Parcelable {
  /**
   * 速度
   */
  @Ignore private long speed = 0;
  /**
   * 单位转换后的速度
   */
  @Ignore private String convertSpeed = "0b/s";

  /**
   * 扩展字段
   */
  private String str = "";
  /**
   * 文件大小
   */
  private long fileSize = 1;
  private int state = STATE_WAIT;
  /**
   * 当前下载进度
   */
  private long currentProgress = 0;
  /**
   * 完成时间
   */
  private long completeTime;
  /**
   * 文件名
   */
  private String grooupName = "";

  public long getSpeed() {
    return speed;
  }

  public void setSpeed(long speed) {
    this.speed = speed;
  }

  public String getConvertSpeed() {
    return convertSpeed;
  }

  public void setConvertSpeed(String convertSpeed) {
    this.convertSpeed = convertSpeed;
  }

  public String getStr() {
    return str;
  }

  public void setStr(String str) {
    this.str = str;
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

  public long getCurrentProgress() {
    return currentProgress;
  }

  public void setCurrentProgress(long currentProgress) {
    this.currentProgress = currentProgress;
  }

  public long getCompleteTime() {
    return completeTime;
  }

  public void setCompleteTime(long completeTime) {
    this.completeTime = completeTime;
  }

  public AbsGroupEntity() {
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(this.speed);
    dest.writeString(this.convertSpeed);
    dest.writeString(this.str);
    dest.writeLong(this.fileSize);
    dest.writeInt(this.state);
    dest.writeLong(this.currentProgress);
    dest.writeLong(this.completeTime);
  }

  protected AbsGroupEntity(Parcel in) {
    this.speed = in.readLong();
    this.convertSpeed = in.readString();
    this.str = in.readString();
    this.fileSize = in.readLong();
    this.state = in.readInt();
    this.currentProgress = in.readLong();
    this.completeTime = in.readLong();
  }
}

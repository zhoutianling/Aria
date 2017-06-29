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
package com.arialyy.aria.core.upload;

import android.os.Parcel;
import android.os.Parcelable;
import com.arialyy.aria.core.inf.AbsNormalEntity;
import com.arialyy.aria.orm.Ignore;

/**
 * Created by lyy on 2017/2/9.
 * 上传文件实体
 */
public class UploadEntity extends AbsNormalEntity implements Parcelable {

  private String filePath;  //文件路径
  private boolean isComplete = false;

  public boolean isComplete() {
    return isComplete;
  }

  public void setComplete(boolean complete) {
    isComplete = complete;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public UploadEntity() {
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeString(this.filePath);
    dest.writeByte(this.isComplete ? (byte) 1 : (byte) 0);
  }

  protected UploadEntity(Parcel in) {
    super(in);
    this.filePath = in.readString();
    this.isComplete = in.readByte() != 0;
  }

  @Ignore public static final Creator<UploadEntity> CREATOR = new Creator<UploadEntity>() {
    @Override public UploadEntity createFromParcel(Parcel source) {
      return new UploadEntity(source);
    }

    @Override public UploadEntity[] newArray(int size) {
      return new UploadEntity[size];
    }
  };
}

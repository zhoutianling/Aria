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
public abstract class AbsGroupEntity extends AbsEntity implements Parcelable {
  /**
   * 组名
   */
  private String groupName = "";

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public AbsGroupEntity() {
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeString(this.groupName);
  }

  protected AbsGroupEntity(Parcel in) {
    super(in);
    this.groupName = in.readString();
  }
}

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

import android.support.annotation.NonNull;
import com.arialyy.aria.core.RequestEnum;
import java.util.Map;
import java.util.Set;

/**
 * Created by AriaL on 2017/6/29.
 * 任务组超类
 */
public abstract class AbsGroupTarget<TARGET extends AbsGroupTarget, TASK_ENTITY extends AbsTaskEntity>
    implements ITarget<TARGET> {

  protected TASK_ENTITY mTaskEntity;

  @Override public void resume() {

  }

  @Override public void start() {

  }

  @Override public void stop() {

  }

  @Override public void cancel() {

  }

  @Override public long getSize() {
    return 0;
  }

  @Override public String getConvertSize() {
    return null;
  }

  @Override public long getCurrentProgress() {
    return 0;
  }

  @Override public TARGET addHeader(@NonNull String key, @NonNull String header) {
    mTaskEntity.headers.put(key, header);
    return (TARGET) this;
  }

  @Override public TARGET addHeaders(Map<String, String> headers) {
    if (headers != null && headers.size() > 0) {
      Set<String> keys = headers.keySet();
      for (String key : keys) {
        mTaskEntity.headers.put(key, headers.get(key));
      }
    }
    return (TARGET) this;
  }

  @Override public TARGET setRequestMode(RequestEnum requestEnum) {
    mTaskEntity.requestEnum = requestEnum;
    return (TARGET) this;
  }
}

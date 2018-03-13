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
package com.arialyy.aria.core.delegate;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.inf.AbsEntity;
import com.arialyy.aria.core.inf.AbsTaskEntity;
import com.arialyy.aria.core.inf.IHttpHeaderTarget;
import com.arialyy.aria.core.inf.ITarget;
import com.arialyy.aria.util.ALog;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by laoyuyu on 2018/3/9.
 * HTTP header参数设置委托类
 */
public class HttpHeaderDelegate<TARGET extends ITarget, ENTITY extends AbsEntity, TASK_ENTITY extends AbsTaskEntity<ENTITY>>
    implements IHttpHeaderTarget<TARGET> {
  private static final String TAG = "HttpHeaderDelegate";
  private ENTITY mEntity;
  private TASK_ENTITY mTaskEntity;
  private TARGET mTarget;

  public HttpHeaderDelegate(TARGET target, TASK_ENTITY taskEntity) {
    mTarget = target;
    mTaskEntity = taskEntity;

    mEntity = mTaskEntity.getEntity();
  }

  /**
   * 给url请求添加Header数据
   * 如果新的header数据和数据保存的不一致，则更新数据库中对应的header数据
   *
   * @param key header对应的key
   * @param value header对应的value
   */
  @Override
  public TARGET addHeader(@NonNull String key, @NonNull String value) {
    if (TextUtils.isEmpty(key)) {
      ALog.w(TAG, "设置header失败，header对应的key不能为null");
      return mTarget;
    } else if (TextUtils.isEmpty(value)) {
      ALog.w(TAG, "设置header失败，header对应的value不能为null");
      return mTarget;
    }
    if (mTaskEntity.headers.get(key) == null) {
      mTaskEntity.headers.put(key, value);
      mTaskEntity.update();
    } else if (!mTaskEntity.headers.get(key).equals(value)) {
      mTaskEntity.headers.put(key, value);
      mTaskEntity.update();
    }
    return mTarget;
  }

  /**
   * 给url请求添加一组header数据
   * 如果新的header数据和数据保存的不一致，则更新数据库中对应的header数据
   *
   * @param headers 一组http header数据
   */
  @Override
  public TARGET addHeaders(@NonNull Map<String, String> headers) {
    if (headers.size() == 0) {
      ALog.w(TAG, "设置header失败，map没有header数据");
      return mTarget;
    }
    /*
      两个map比较逻辑
      1、比对key是否相同
      2、如果key相同，比对value是否相同
      3、只有当上面两个步骤中key 和 value都相同时才能任务两个map数据一致
     */
    boolean mapEquals = false;
    if (mTaskEntity.headers.size() == headers.size()) {
      int i = 0;
      Set<String> keys = mTaskEntity.headers.keySet();
      for (String key : keys) {
        if (headers.containsKey(key)) {
          i++;
        } else {
          break;
        }
      }
      if (i == mTaskEntity.headers.size()) {
        int j = 0;
        Collection<String> values = mTaskEntity.headers.values();
        for (String value : values) {
          if (headers.containsValue(value)) {
            j++;
          } else {
            break;
          }
        }
        if (j == mTaskEntity.headers.size()) {
          mapEquals = true;
        }
      }
    }

    if (!mapEquals) {
      mTaskEntity.headers.clear();
      Set<String> keys = headers.keySet();
      for (String key : keys) {
        mTaskEntity.headers.put(key, headers.get(key));
      }
      mTaskEntity.update();
    }

    return mTarget;
  }

  /**
   * 设置请求类型，POST或GET，默认为在GET
   * 只试用于HTTP请求
   *
   * @param requestEnum {@link RequestEnum}
   */
  @Override
  public TARGET setRequestMode(RequestEnum requestEnum) {
    mTaskEntity.requestEnum = requestEnum;
    return mTarget;
  }

  /**
   * 如果你的下载链接的header中含有md5码信息，那么你可以通过设置key，来获取从header获取该md5码信息。
   * key默认值为：Content-MD5
   * 获取md5信息：{@link DownloadEntity#getMd5Code()}
   */
  @Override
  public TARGET setHeaderMd5Key(String md5Key) {
    if (TextUtils.isEmpty(md5Key)) return mTarget;
    mTaskEntity.md5Key = md5Key;
    if (TextUtils.isEmpty(mTaskEntity.md5Key) || !mTaskEntity.md5Key.equals(md5Key)) {
      mTaskEntity.update();
    }
    return mTarget;
  }

  /**
   * 如果你的文件长度是放在header中，那么你需要配置key来让Aria知道正确的文件长度
   * key默认值为：Content-Length
   */
  @Override
  public TARGET setHeaderContentLengthKey(String contentLength) {
    if (TextUtils.isEmpty(contentLength)) return mTarget;
    mTaskEntity.contentLength = contentLength;
    if (TextUtils.isEmpty(mTaskEntity.contentLength) || !mTaskEntity.contentLength.equals(
        contentLength)) {
      mTaskEntity.update();
    }
    return mTarget;
  }

  /**
   * 如果你的下载链接的header中含有文件描述信息，那么你可以通过设置key，来获取从header获取该文件描述信息。
   * key默认值为：Content-Disposition
   * 获取文件描述信息：{@link DownloadEntity#getDisposition()}
   */
  @Override
  public TARGET setHeaderDispositionKey(String dispositionKey) {
    if (TextUtils.isEmpty(dispositionKey)) return mTarget;
    mTaskEntity.dispositionKey = dispositionKey;
    if (TextUtils.isEmpty(mTaskEntity.dispositionKey) || !mTaskEntity.dispositionKey.equals(
        dispositionKey)) {
      mTaskEntity.update();
    }
    return mTarget;
  }

  /**
   * 从文件描述信息{@link #setHeaderDispositionKey(String)}中含有文件名信息，你可以通过设置key来获取header中的文件名
   * key默认值为：attachment;filename
   * 获取文件名信息：{@link DownloadEntity#getServerFileName()}
   */
  @Override
  public TARGET setHeaderDispositionFileKey(String dispositionFileKey) {
    if (TextUtils.isEmpty(dispositionFileKey)) return mTarget;
    mTaskEntity.dispositionFileKey = dispositionFileKey;
    if (TextUtils.isEmpty(mTaskEntity.dispositionFileKey) || !mTaskEntity.dispositionFileKey.equals(
        dispositionFileKey)) {
      mTaskEntity.update();
    }
    return mTarget;
  }
}

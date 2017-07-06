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

import com.arialyy.aria.core.RequestEnum;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.orm.Ignore;
import com.arialyy.aria.orm.Primary;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lyy on 2017/2/23.
 */
public abstract class AbsTaskEntity<ENTITY extends AbsEntity> extends DbEntity {

  /**
   * Task实体对应的key
   */
  @Primary public String key = "";

  /**
   * http 请求头
   */
  public Map<String, String> headers = new HashMap<>();

  /**
   * 网络请求类型
   */
  public RequestEnum requestEnum = RequestEnum.GET;

  /**
   * 重定向后，从链接中获取新url所需要的key
   */
  public String redirectUrlKey = "location";

  /**
   * 用于判断删除任务时是否需要删除文件{@code true}删除
   */
  @Ignore public boolean removeFile = false;

  /**
   * 是否支持断点, {@code true} 为支持断点
   */
  public boolean isSupportBP = true;

  /**
   * 状态码
   */
  public int code;

  public abstract ENTITY getEntity();
}

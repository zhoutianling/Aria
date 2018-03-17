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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by AriaL on 2018/3/17.
 * 参数容器，用来临时存储链式命令的需要的参数。
 */
public class ParamContainer {

  public String url;
  public String filePath;
  public String charSet;
  public Map<String, String> header = new HashMap<>();

  // ftp 相关的
  public String userName, userPw, userAccount;
  public boolean needLogin;
}

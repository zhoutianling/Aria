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

import com.arialyy.aria.util.CAConfiguration;
import com.arialyy.aria.util.SSLContextUtil;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by lyy on 2017/1/18.
 * 链接帮助类
 */
class ConnectionHelp {
  /**
   * 处理链接
   *
   * @throws IOException
   */
  static HttpURLConnection handleConnection(URL url) throws IOException {
    HttpURLConnection conn;
    URLConnection urlConn = url.openConnection();
    if (urlConn instanceof HttpsURLConnection) {
      conn = (HttpsURLConnection) urlConn;
      SSLContext sslContext =
          SSLContextUtil.getSSLContext(CAConfiguration.CA_ALIAS, CAConfiguration.CA_ALIAS);
      if (sslContext == null) {
        sslContext = SSLContextUtil.getDefaultSLLContext();
      }
      SSLSocketFactory ssf = sslContext.getSocketFactory();
      ((HttpsURLConnection) conn).setSSLSocketFactory(ssf);
      ((HttpsURLConnection) conn).setHostnameVerifier(SSLContextUtil.HOSTNAME_VERIFIER);
    } else {
      conn = (HttpURLConnection) urlConn;
    }
    return conn;
  }

  /**
   * 设置头部参数
   *
   * @throws ProtocolException
   */
  static HttpURLConnection setConnectParam(DownloadTaskEntity entity, HttpURLConnection conn)
      throws ProtocolException {
    conn.setRequestMethod(entity.requestEnum.name);
    if (entity.headers != null && entity.headers.size() > 0) {
      Set<String> keys = entity.headers.keySet();
      for (String key : keys) {
        conn.setRequestProperty(key, entity.headers.get(key));
      }
    }
    conn.setRequestProperty("Charset", "UTF-8");
    conn.setRequestProperty("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
    conn.setRequestProperty("Accept",
        "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
    ////用于处理Disconnect 不起作用问题
    //conn.setRequestProperty("Connection", "close");
    conn.setRequestProperty("Connection", "Keep-Alive");
    return conn;
  }
}

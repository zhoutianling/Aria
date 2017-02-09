package com.arialyy.aria.core.upload;

import android.util.Log;
import com.arialyy.aria.util.CheckUtil;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Aria.Lao on 2017/2/9.
 * 上传工具
 */
public class UploadUtil implements Runnable {
  private static final String TAG = "UploadUtil";
  private UploadEntity mUploadEntity;
  private UploadTaskEntity mTaskEntity;
  private IUploadListener mListener;


  public UploadUtil(UploadTaskEntity taskEntity, IUploadListener listener) {
    mTaskEntity = taskEntity;
    CheckUtil.checkUploadEntity(taskEntity.uploadEntity);
    mUploadEntity = taskEntity.uploadEntity;
    mListener = listener;
  }

  public void start() {
    new Thread(this).start();
  }

  @Override public void run() {
    File file = new File(mUploadEntity.getFilePath());
    if (!file.exists()) {
      Log.e(TAG, "【" + mUploadEntity.getFilePath() + "】，文件不存在。");
      mListener.onFail();
      return;
    }
    String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
    String PREFIX = "--", LINE_END = "\r\n";
    String CONTENT_TYPE = "multipart/form-data"; // 内容类型
    try {
      HttpURLConnection conn = (HttpURLConnection) new URL(mTaskEntity.uploadUrl).openConnection();
      conn.setReadTimeout(5000);
      conn.setConnectTimeout(5000);
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setUseCaches(false);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Charset", "utf-8"); // 设置编码
      conn.setRequestProperty("connection", "keep-alive");
      //conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
      conn.setRequestProperty("Content-Type", mTaskEntity.contentType + ";boundary=" + BOUNDARY);

      Set<String> keys = mTaskEntity.headers.keySet();
      for (String key : keys) {
        conn.setRequestProperty(key, mTaskEntity.headers.get(key));
      }

      OutputStream outputSteam = conn.getOutputStream();
      DataOutputStream dos = new DataOutputStream(outputSteam);
      StringBuilder sb = new StringBuilder();
      sb.append(PREFIX);
      sb.append(BOUNDARY);
      sb.append(LINE_END);
      sb.append("Content-Disposition: form-data; name=\"")
          .append(mTaskEntity.uploadUrl)
          .append("\"; filename=\"")
          .append(file.getName())
          .append("\"")
          .append(LINE_END);
      sb.append("Content-Type:")
          .append(mTaskEntity.contentType)
          .append("; charset=utf-8")
          .append(LINE_END);
      sb.append(LINE_END);
      dos.write(sb.toString().getBytes());

      InputStream is = new FileInputStream(file);
      byte[] bytes = new byte[1024];
      int len = 0;
      while ((len = is.read(bytes)) != -1) {
        dos.write(bytes, 0, len);
      }
      is.close();
      dos.write(LINE_END.getBytes());
      byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
      dos.write(end_data);
      dos.flush();
      dos.close();

      int res = conn.getResponseCode();
      if (res == 200) {
        BufferedInputStream inputStream = new BufferedInputStream(conn.getInputStream());
        byte[] buf = new byte[1024];
        StringBuilder stringBuilder = new StringBuilder();
        while (inputStream.read(buf) > 0) {
          stringBuilder.append(new String(buf, 0, buf.length));
        }
        String data = stringBuilder.toString();
        Log.d(TAG, data);
        //L.j(data);
        //absResponse.onResponse(data);
      } else {
        //absResponse.onError("error");
      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

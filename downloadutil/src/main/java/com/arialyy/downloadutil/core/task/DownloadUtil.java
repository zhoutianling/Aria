package com.arialyy.downloadutil.core.task;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import com.arialyy.downloadutil.core.DownloadEntity;
import com.arialyy.downloadutil.util.Util;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lyy on 2015/8/25.
 * 下载工具类
 */
final class DownloadUtil implements IDownloadUtil {
  private static final String TAG  = "DownloadUtil";
  private static final Object LOCK = new Object();
  /**
   * 线程数
   */
  private final int               THREAD_NUM;
  //下载监听
  private       IDownloadListener mListener;
  private int     mConnectTimeOut    = 5000 * 4; //连接超时时间
  private int     mReadTimeOut       = 5000 * 20; //流读取的超时时间
  /**
   * 已经完成下载任务的线程数量
   */
  private int     mCompleteThreadNum = 0;
  private boolean isDownloading      = false;
  private boolean isStop             = false;
  private boolean isCancel           = false;
  private boolean isNewTask          = true;
  private int     mCancelNum         = 0;
  private long    mCurrentLocation   = 0;
  private int     mStopNum           = 0;
  private Context         mContext;
  private DownloadEntity  mDownloadEntity;
  private ExecutorService mFixedThreadPool;
  private SparseArray<Runnable> mTask = new SparseArray<>();

  DownloadUtil(Context context, DownloadEntity entity, IDownloadListener downloadListener) {
    this(context, entity, downloadListener, 3);
  }

  DownloadUtil(Context context, DownloadEntity entity, IDownloadListener downloadListener,
      int threadNum) {
    mContext = context.getApplicationContext();
    mDownloadEntity = entity;
    mListener = downloadListener;
    THREAD_NUM = threadNum;
    mFixedThreadPool = Executors.newFixedThreadPool(THREAD_NUM);
  }

  public IDownloadListener getListener() {
    return mListener;
  }

  /**
   * 设置连接超时时间
   */
  public void setConnectTimeOut(int timeOut) {
    mConnectTimeOut = timeOut;
  }

  /**
   * 设置流读取的超时时间
   */
  public void setReadTimeOut(int readTimeOut) {
    mReadTimeOut = readTimeOut;
  }

  /**
   * 获取当前下载位置
   */
  @Override public long getCurrentLocation() {
    return mCurrentLocation;
  }

  @Override public boolean isDownloading() {
    return isDownloading;
  }

  /**
   * 取消下载
   */
  @Override public void cancelDownload() {
    isCancel = true;
    isDownloading = false;
    mFixedThreadPool.shutdown();
    for (int i = 0; i < THREAD_NUM; i++) {
      DownLoadTask task = (DownLoadTask) mTask.get(i);
      if (task != null) {
        task.cancel();
      }
    }
  }

  /**
   * 停止下载
   */
  @Override public void stopDownload() {
    isStop = true;
    isDownloading = false;
    mFixedThreadPool.shutdown();
    for (int i = 0; i < THREAD_NUM; i++) {
      DownLoadTask task = (DownLoadTask) mTask.get(i);
      if (task != null) {
        task.stop();
      }
    }
  }

  /**
   * 删除下载记录文件
   */
  @Override public void delConfigFile() {
    if (mContext != null && mDownloadEntity != null) {
      File dFile = new File(mDownloadEntity.getDownloadPath());
      File config =
          new File(mContext.getFilesDir().getPath() + "/temp/" + dFile.getName() + ".properties");
      if (config.exists()) {
        config.delete();
      }
    }
  }

  /**
   * 删除temp文件
   */
  @Override public void delTempFile() {
    if (mContext != null && mDownloadEntity != null) {
      File dFile = new File(mDownloadEntity.getDownloadPath());
      if (dFile.exists()) {
        dFile.delete();
      }
    }
  }

  /**
   * 多线程断点续传下载文件，开始下载
   */
  @Override public void startDownload() {
    isDownloading = true;
    mCurrentLocation = 0;
    isStop = false;
    isCancel = false;
    mCancelNum = 0;
    mStopNum = 0;
    final String filePath    = mDownloadEntity.getDownloadPath();
    final String downloadUrl = mDownloadEntity.getDownloadUrl();
    final File   dFile       = new File(filePath);
    //读取已完成的线程数
    final File configFile =
        new File(mContext.getFilesDir().getPath() + "/temp/" + dFile.getName() + ".properties");
    try {
      if (!configFile.exists()) { //记录文件被删除，则重新下载
        isNewTask = true;
        Util.createFile(configFile.getPath());
      } else {
        isNewTask = !dFile.exists();
      }
    } catch (Exception e) {
      e.printStackTrace();
      failDownload("下载失败，记录文件被删除");
      return;
    }
    mListener.onPre();
    new Thread(new Runnable() {
      @Override public void run() {
        try {
          URL               url  = new URL(downloadUrl);
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          setConnectParam(conn);
          conn.setConnectTimeout(mConnectTimeOut * 4);
          conn.connect();
          int len = conn.getContentLength();
          if (len < 0) {  //网络被劫持时会出现这个问题
            failDownload("下载失败，网络被劫持");
            return;
          }
          int code = conn.getResponseCode();
          if (code == 200) {
            int fileLength = conn.getContentLength();
            //必须建一个文件
            Util.createFile(filePath);
            RandomAccessFile file = new RandomAccessFile(filePath, "rwd");
            //设置文件长度
            file.setLength(fileLength);
            mListener.onPostPre(fileLength);
            //分配每条线程的下载区间
            Properties pro = null;
            pro = Util.loadConfig(configFile);
            if (pro.isEmpty()) {
              isNewTask = true;
            } else {
              for (int i = 0; i < THREAD_NUM; i++) {
                if (pro.getProperty(dFile.getName() + "_record_" + i) == null) {
                  Object state = pro.getProperty(dFile.getName() + "_state_" + i);
                  if (state != null && Integer.parseInt(state + "") == 1) {
                    continue;
                  }
                  isNewTask = true;
                  break;
                }
              }
            }
            int   blockSize = fileLength / THREAD_NUM;
            int[] recordL   = new int[THREAD_NUM];
            int   rl        = 0;
            for (int i = 0; i < THREAD_NUM; i++) {
              recordL[i] = -1;
            }
            for (int i = 0; i < THREAD_NUM; i++) {
              long   startL = i * blockSize, endL = (i + 1) * blockSize;
              Object state  = pro.getProperty(dFile.getName() + "_state_" + i);
              if (state != null && Integer.parseInt(state + "") == 1) {  //该线程已经完成
                mCurrentLocation += endL - startL;
                Log.d(TAG, "++++++++++ 线程_" + i + "_已经下载完成 ++++++++++");
                mCompleteThreadNum++;
                mStopNum++;
                mCancelNum++;
                if (mCompleteThreadNum == THREAD_NUM) {
                  if (configFile.exists()) {
                    configFile.delete();
                  }
                  mListener.onComplete();
                  isDownloading = false;
                  return;
                }
                continue;
              }
              //分配下载位置
              Object record = pro.getProperty(dFile.getName() + "_record_" + i);
              if (!isNewTask
                  && record != null
                  && Long.parseLong(record + "") > 0) {       //如果有记录，则恢复下载
                Long r = Long.parseLong(record + "");
                mCurrentLocation += r - startL;
                Log.d(TAG, "++++++++++ 线程_" + i + "_恢复下载 ++++++++++");
                mListener.onChildResume(r);
                startL = r;
                recordL[rl] = i;
                rl++;
              } else {
                isNewTask = true;
              }
              if (isNewTask) {
                recordL[rl] = i;
                rl++;
              }
              if (i == (THREAD_NUM - 1)) {
                //如果整个文件的大小不为线程个数的整数倍，则最后一个线程的结束位置即为文件的总长度
                endL = fileLength;
              }
              ConfigEntity entity =
                  new ConfigEntity(mContext, fileLength, downloadUrl, dFile, i, startL, endL);
              DownLoadTask task = new DownLoadTask(entity);
              mTask.put(i, task);
            }
            if (mCurrentLocation > 0) {
              mListener.onResume(mCurrentLocation);
            } else {
              mListener.onStart(mCurrentLocation);
            }
            for (int l : recordL) {
              if (l == -1) continue;
              Runnable task = mTask.get(l);
              if (task != null) {
                mFixedThreadPool.execute(task);
              }
            }
          } else {
            failDownload("下载失败，返回码：" + code);
          }
        } catch (IOException e) {
          failDownload("下载失败【downloadUrl:"
              + downloadUrl
              + "】\n【filePath:"
              + filePath
              + "】"
              + Util.getPrintException(e));
        }
      }
    }).start();
  }

  @Override public void resumeDownload() {
    startDownload();
  }

  private void failDownload(String msg) {
    Log.e(TAG, msg);
    isDownloading = false;
    stopDownload();
    mListener.onFail();
  }

  private void setConnectParam(HttpURLConnection conn) throws ProtocolException {
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Charset", "UTF-8");
    conn.setRequestProperty("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
    conn.setRequestProperty("Accept",
        "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
  }

  /**
   * 子线程下载信息类
   */
  private static class ConfigEntity {
    //文件大小
    long    fileSize;
    String  downloadUrl;
    int     threadId;
    long    startLocation;
    long    endLocation;
    File    tempFile;
    Context context;

    private ConfigEntity(Context context, long fileSize, String downloadUrl, File file,
        int threadId, long startLocation, long endLocation) {
      this.fileSize = fileSize;
      this.downloadUrl = downloadUrl;
      this.tempFile = file;
      this.threadId = threadId;
      this.startLocation = startLocation;
      this.endLocation = endLocation;
      this.context = context;
    }
  }

  /**
   * 单个线程的下载任务
   */
  private class DownLoadTask implements Runnable {
    private static final String TAG = "DownLoadTask";
    private ConfigEntity dEntity;
    private String       configFPath;
    private long currentLocation = 0;

    private DownLoadTask(ConfigEntity downloadInfo) {
      this.dEntity = downloadInfo;
      configFPath = dEntity.context.getFilesDir().getPath()
          + "/temp/"
          + dEntity.tempFile.getName()
          + ".properties";
    }

    @Override public void run() {
      HttpURLConnection conn = null;
      InputStream       is   = null;
      try {
        Log.d(TAG, "线程_"
            + dEntity.threadId
            + "_正在下载【开始位置 : "
            + dEntity.startLocation
            + "，结束位置："
            + dEntity.endLocation
            + "】");
        URL url = new URL(dEntity.downloadUrl);
        conn = (HttpURLConnection) url.openConnection();
        //在头里面请求下载开始位置和结束位置
        conn.setRequestProperty("Range",
            "bytes=" + dEntity.startLocation + "-" + dEntity.endLocation);
        setConnectParam(conn);
        conn.setConnectTimeout(mConnectTimeOut);
        conn.setReadTimeout(mReadTimeOut);  //设置读取流的等待时间,必须设置该参数
        is = conn.getInputStream();
        //创建可设置位置的文件
        RandomAccessFile file = new RandomAccessFile(dEntity.tempFile, "rwd");
        //设置每条线程写入文件的位置
        file.seek(dEntity.startLocation);
        byte[] buffer = new byte[1024];
        int    len;
        //当前子线程的下载位置
        currentLocation = dEntity.startLocation;
        while ((len = is.read(buffer)) != -1) {
          if (isCancel) {
            Log.d(TAG, "++++++++++ thread_" + dEntity.threadId + "_cancel ++++++++++");
            break;
          }
          if (isStop) {
            break;
          }
          //把下载数据数据写入文件
          file.write(buffer, 0, len);
          progress(len);
          currentLocation += len;
        }
        file.close();
        //close 为阻塞的，需要使用线程池来处理
        is.close();
        conn.disconnect();

        if (isCancel) {
          cancel();
          return;
        }
        //停止状态不需要删除记录文件
        if (isStop) {
          //stop();
          return;
        }
        Log.i(TAG, "线程【" + dEntity.threadId + "】下载完毕");
        writeConfig(dEntity.tempFile.getName() + "_state_" + dEntity.threadId, 1 + "");
        mListener.onChildComplete(dEntity.endLocation);
        mCompleteThreadNum++;
        if (mCompleteThreadNum == THREAD_NUM) {
          File configFile = new File(configFPath);
          if (configFile.exists()) {
            configFile.delete();
          }
          isDownloading = false;
          mListener.onComplete();
        }
      } catch (MalformedURLException e) {
        failDownload(dEntity, currentLocation, "下载链接异常", e);
      } catch (IOException e) {
        failDownload(dEntity, currentLocation, "下载失败【" + dEntity.downloadUrl + "】", e);
      } catch (Exception e) {
        failDownload(dEntity, currentLocation, "获取流失败", e);
      }
    }

    /**
     * 停止下载
     */
    protected void stop() {
      synchronized (LOCK) {
        try {
          mStopNum++;
          String location = String.valueOf(currentLocation);
          Log.i(TAG, "thread_" + dEntity.threadId + "_stop, stop location ==> " + currentLocation);
          writeConfig(dEntity.tempFile.getName() + "_record_" + dEntity.threadId, location);
          if (mStopNum == THREAD_NUM) {
            Log.d(TAG, "++++++++++++++++ onStop +++++++++++++++++");
            isDownloading = false;
            mListener.onStop(mCurrentLocation);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    /**
     * 取消下载
     */
    private void cancel() {
      synchronized (LOCK) {
        mCancelNum++;
        if (mCancelNum == THREAD_NUM) {
          File configFile = new File(configFPath);
          if (configFile.exists()) {
            configFile.delete();
          }
          if (dEntity.tempFile.exists()) {
            dEntity.tempFile.delete();
          }
          Log.d(TAG, "++++++++++++++++ onCancel +++++++++++++++++");
          isDownloading = false;
          mListener.onCancel();
        }
      }
    }

    private void progress(long len) {
      synchronized (LOCK) {
        mCurrentLocation += len;
        mListener.onProgress(mCurrentLocation);
      }
    }

    /**
     * 将记录写入到配置文件
     */
    private void writeConfig(String key, String record) throws IOException {
      File       configFile = new File(configFPath);
      Properties pro        = Util.loadConfig(configFile);
      pro.setProperty(key, record);
      Util.saveConfig(configFile, pro);
    }

    /**
     * 下载失败
     */
    private void failDownload(ConfigEntity dEntity, long currentLocation, String msg,
        Exception ex) {
      synchronized (LOCK) {
        try {
          isDownloading = false;
          isStop = true;
          Log.e(TAG, msg);
          if (ex != null) {
            Log.e(TAG, Util.getPrintException(ex));
          }
          if (currentLocation != -1) {
            String location = String.valueOf(currentLocation);
            writeConfig(dEntity.tempFile.getName() + "_record_" + dEntity.threadId, location);
          }
          mListener.onFail();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
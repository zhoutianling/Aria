package com.arialyy.downloadutil.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import com.arialyy.downloadutil.entity.DownloadEntity;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by lyy on 2015/8/25.
 * 下载工具类
 */
final class DownLoadUtil {
  private static final String TAG  = "DownLoadUtil";
  private static final Object LOCK = new Object();
  //下载监听
  private IDownloadListener mListener;
  /**
   * 线程数
   */
  private static final int     THREAD_NUM         = 3;
  private static final int     TIME_OUT           = 5000; //超时时间
  /**
   * 已经完成下载任务的线程数量
   */
  private              int     mCompleteThreadNum = 0;
  private              boolean isDownloading      = false;
  private              boolean isStop             = false;
  private              boolean isCancel           = false;
  private long mCurrentLocation;
  boolean isNewTask = true;
  private int mCancelNum = 0;
  private int mStopNum   = 0;
  private Context        mContext;
  private DownloadEntity mDownloadEntity;

  public DownLoadUtil(Context context, DownloadEntity entity) {
    mContext = context.getApplicationContext();
    mDownloadEntity = entity;
  }

  public IDownloadListener getListener() {
    return mListener;
  }

  /**
   * 获取当前下载位置
   */
  public long getCurrentLocation() {
    return mCurrentLocation;
  }

  public boolean isDownloading() {
    return isDownloading;
  }

  /**
   * 取消下载
   */
  public void cancelDownload() {
    isCancel = true;
  }

  /**
   * 停止下载
   */
  public void stopDownload() {
    isStop = true;
  }

  /**
   * 删除下载记录文件
   */
  public void delConfigFile() {
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
  public void delTempFile() {
    if (mContext != null && mDownloadEntity != null) {
      File dFile = new File(mDownloadEntity.getDownloadPath());
      if (dFile.exists()) {
        dFile.delete();
      }
    }
  }

  /**
   * 多线程断点续传下载文件，暂停和继续
   *
   * @param downloadListener 下载进度监听 {@link DownloadListener}
   */
  public void start(@NonNull final IDownloadListener downloadListener) {
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
    new Thread(new Runnable() {
      @Override public void run() {
        try {
          mListener = downloadListener;
          URL               url  = new URL(downloadUrl);
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("GET");
          conn.setRequestProperty("Charset", "UTF-8");
          conn.setConnectTimeout(TIME_OUT * 4);
          conn.setRequestProperty("User-Agent",
              "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
          conn.setRequestProperty("Accept",
              "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
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
            mListener.onPreDownload(conn);
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
            SparseArray<Thread> tasks     = new SparseArray<>();
            int                 blockSize = fileLength / THREAD_NUM;
            int[]               recordL   = new int[THREAD_NUM];
            int                 rl        = 0;
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
              tasks.put(i, new Thread(task));
            }
            if (mCurrentLocation > 0) {
              mListener.onResume(mCurrentLocation);
            } else {
              mListener.onStart(mCurrentLocation);
            }
            for (int l : recordL) {
              if (l == -1) continue;
              Thread task = tasks.get(l);
              if (task != null) {
                task.start();
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

  private void failDownload(String msg) {
    Log.e(TAG, msg);
    isDownloading = false;
    stopDownload();
    mListener.onFail();
  }

  /**
   * 多线程下载任务类,不能使用AsyncTask来进行多线程下载，因为AsyncTask是串行执行的，这种方式下载速度太慢了
   */
  private class DownLoadTask implements Runnable {
    private static final String TAG = "DownLoadTask";
    private ConfigEntity dEntity;
    private String       configFPath;

    private DownLoadTask(ConfigEntity downloadInfo) {
      this.dEntity = downloadInfo;
      configFPath = dEntity.context.getFilesDir().getPath()
          + "/temp/"
          + dEntity.tempFile.getName()
          + ".properties";
    }

    @Override public void run() {
      long currentLocation = 0;
      try {
        Log.d(TAG, "线程_"
            + dEntity.threadId
            + "_正在下载【"
            + "开始位置 : "
            + dEntity.startLocation
            + "，结束位置："
            + dEntity.endLocation
            + "】");
        URL               url  = new URL(dEntity.downloadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //在头里面请求下载开始位置和结束位置
        conn.setRequestProperty("Range",
            "bytes=" + dEntity.startLocation + "-" + dEntity.endLocation);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("User-Agent",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
        conn.setRequestProperty("Accept",
            "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
        conn.setConnectTimeout(TIME_OUT * 4);
        conn.setReadTimeout(TIME_OUT * 24);  //设置读取流的等待时间,必须设置该参数
        InputStream is = conn.getInputStream();
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
        is.close();

        if (isCancel) {
          cancel();
          return;
        }
        //停止状态不需要删除记录文件
        if (isStop) {
          stop(currentLocation);
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
     *
     * @throws IOException
     */
    private void stop(long currentLocation) throws IOException {
      synchronized (LOCK) {
        mStopNum++;
        String location = String.valueOf(currentLocation);
        Log.i(TAG, "thread_" + dEntity.threadId + "_stop, stop location ==> " + currentLocation);
        writeConfig(dEntity.tempFile.getName() + "_record_" + dEntity.threadId, location);
        if (mStopNum == THREAD_NUM) {
          Log.d(TAG, "++++++++++++++++ onStop +++++++++++++++++");
          isDownloading = false;
          mListener.onStop(mCurrentLocation);
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

  /**
   * 子线程下载信息类
   */
  private class ConfigEntity {
    //文件大小
    long    fileSize;
    String  downloadUrl;
    int     threadId;
    long    startLocation;
    long    endLocation;
    File    tempFile;
    Context context;

    public ConfigEntity(Context context, long fileSize, String downloadUrl, File file, int threadId,
        long startLocation, long endLocation) {
      this.fileSize = fileSize;
      this.downloadUrl = downloadUrl;
      this.tempFile = file;
      this.threadId = threadId;
      this.startLocation = startLocation;
      this.endLocation = endLocation;
      this.context = context;
    }
  }

  public static class DownloadListener implements IDownloadListener {

    @Override public void onResume(long resumeLocation) {

    }

    @Override public void onCancel() {

    }

    @Override public void onFail() {

    }

    @Override public void onPreDownload(HttpURLConnection connection) {

    }

    @Override public void onProgress(long currentLocation) {

    }

    @Override public void onChildComplete(long finishLocation) {

    }

    @Override public void onStart(long startLocation) {

    }

    @Override public void onChildResume(long resumeLocation) {

    }

    @Override public void onStop(long stopLocation) {

    }

    @Override public void onComplete() {

    }
  }
}
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

package com.arialyy.simple.download;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import butterknife.Bind;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.download.DownloadTarget;
import com.arialyy.aria.core.download.DownloadTask;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import com.arialyy.frame.util.show.L;
import com.arialyy.frame.util.show.T;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseActivity;
import com.arialyy.simple.databinding.ActivitySingleBinding;

import java.io.File;
import java.util.Map;

public class SingleTaskActivity extends BaseActivity<ActivitySingleBinding> {

    private static final String DOWNLOAD_URL = "http://msoftdl.360.cn/mobilesafe/shouji360/360safe/500192/360MobileSafe.apk";
    @Bind(R.id.start)
    Button mStart;
    @Bind(R.id.stop)
    Button mStop;
    @Bind(R.id.cancel)
    Button mCancel;
    @Bind(R.id.speeds)
    RadioGroup mRg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Aria.download(this).register();
    }

    /**
     * 设置start 和 stop 按钮状态
     */
    private void setBtState(boolean state) {
        mStart.setEnabled(state);
        mStop.setEnabled(!state);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_single_task_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int speed = -1;
        String msg = "";
        switch (item.getItemId()) {
            case R.id.help:
                msg = "一些小知识点：\n"
                        + "1、你可以在注解中增加链接，用于指定被注解的方法只能被特定的下载任务回调，以防止progress乱跳\n"
                        + "2、当遇到网络慢的情况时，你可以先使用onPre()更新UI界面，待连接成功时，再在onTaskPre()获取完整的task数据，然后给UI界面设置正确的数据\n"
                        + "3、你可以在界面初始化时通过Aria.download(this).load(URL).getPercent()等方法快速获取相关任务的一些数据";
                showMsgDialog("tip", msg);
                break;
            case R.id.speed_0:
                speed = 0;
                break;
            case R.id.speed_128:
                speed = 128;
                break;
            case R.id.speed_256:
                speed = 256;
                break;
            case R.id.speed_512:
                speed = 512;
                break;
            case R.id.speed_1m:
                speed = 1024;
                break;
        }
        if (speed > -1) {
            msg = item.getTitle().toString();
            Aria.download(this).setMaxSpeed(speed);
            T.showShort(this, msg);
        }
        return true;
    }

    @Download.onWait
    void onWait(DownloadTask task) {
        if (task.getKey().equals(DOWNLOAD_URL)) {
            Log.d(TAG, "wait ==> " + task.getDownloadEntity().getFileName());
        }
    }

    @Download.onPre
    protected void onPre(DownloadTask task) {
        if (task.getKey().equals(DOWNLOAD_URL)) {
            setBtState(false);
        }
    }

    @Download.onTaskStart
    void taskStart(DownloadTask task) {
        if (task.getKey().equals(DOWNLOAD_URL)) {
            getBinding().setFileSize(task.getConvertFileSize());
        }
    }

    @Download.onTaskRunning
    protected void running(DownloadTask task) {
        ALog.d(TAG, String.format("%s_running_%s", getClass().getName(), hashCode()));
        if (task.getKey().equals(DOWNLOAD_URL)) {
            //Log.d(TAG, task.getKey());
            long len = task.getFileSize();
            if (len == 0) {
                getBinding().setProgress(0);
            } else {
                getBinding().setProgress(task.getPercent());
            }
            getBinding().setSpeed(task.getConvertSpeed());
        }
    }

    @Download.onTaskResume
    void taskResume(DownloadTask task) {
        if (task.getKey().equals(DOWNLOAD_URL)) {
            mStart.setText("暂停");
            setBtState(false);
        }
    }

    @Download.onTaskStop
    void taskStop(DownloadTask task) {
        if (task.getKey().equals(DOWNLOAD_URL)) {
            mStart.setText("恢复");
            setBtState(true);
            getBinding().setSpeed("");
        }
    }

    @Download.onTaskCancel
    void taskCancel(DownloadTask task) {
        if (task.getKey().equals(DOWNLOAD_URL)) {
            getBinding().setProgress(0);
            Toast.makeText(SingleTaskActivity.this, "取消下载", Toast.LENGTH_SHORT).show();
            mStart.setText("开始");
            setBtState(true);
            getBinding().setSpeed("");
            Log.d(TAG, "cancel");
        }
    }

    @Download.onTaskFail
    void taskFail(DownloadTask task) {
        if (task.getKey().equals(DOWNLOAD_URL)) {
            Toast.makeText(SingleTaskActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
            setBtState(true);
        }
    }

    @Download.onTaskComplete
    void taskComplete(DownloadTask task) {
        if (task.getKey().equals(DOWNLOAD_URL)) {
            getBinding().setProgress(100);
            Toast.makeText(SingleTaskActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
            mStart.setText("重新开始？");
            mCancel.setEnabled(false);
            setBtState(true);
            getBinding().setSpeed("");
            L.d(TAG, "path ==> " + task.getDownloadEntity().getDownloadPath());
            L.d(TAG, "md5Code ==> " + CommonUtil.getFileMD5(new File(task.getDownloadPath())));
            L.d(TAG, "data ==> " + Aria.download(this).getDownloadEntity(DOWNLOAD_URL));
            //Intent install = new Intent(Intent.ACTION_VIEW);
            //install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //File apkFile = new File(task.getDownloadPath());
            //install.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            //startActivity(install);
        }
    }

    @Download.onNoSupportBreakPoint
    public void onNoSupportBreakPoint(DownloadTask task) {
        if (task.getKey().equals(DOWNLOAD_URL)) {
            T.showShort(SingleTaskActivity.this, "该下载链接不支持断点");
        }
    }

    @Override
    protected int setLayoutId() {
        return R.layout.activity_single;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        setTitle("单任务下载");
        DownloadTarget target = Aria.download(this).load(DOWNLOAD_URL);
        getBinding().setProgress(target.getPercent());
        if (target.getTaskState() == IEntity.STATE_STOP) {
            mStart.setText("恢复");
            mStart.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
            setBtState(true);
        } else if (target.isRunning()) {
            setBtState(false);
        }
        getBinding().setFileSize(target.getConvertFileSize());
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                startD();
                break;
            case R.id.stop:
                Aria.download(this).load(DOWNLOAD_URL).stop();
                //startActivity(new Intent(this, SingleTaskActivity.class));
                //Aria.download(this).unRegister();
                //Aria.download(this).load(DOWNLOAD_URL).removeRecord();
                //Log.d(TAG, Aria.download(this).taskExists(DOWNLOAD_URL) + "");
                break;
            case R.id.cancel:
                Aria.download(this).load(DOWNLOAD_URL).cancel(true);
                //Aria.download(this).load(DOWNLOAD_URL).removeRecord();
                break;
        }
    }

    private void startD() {
        //Aria.get(this).setLogLevel(ALog.LOG_CLOSE);
        //Aria.download(this).load("aaaa.apk");
        String path = Environment.getExternalStorageDirectory().getPath() + "/360.apk";
        //File file = new File(path);
        //if (file.exists()){
        //  file.delete();
        //}

        Aria.download(SingleTaskActivity.this)
                .load(DOWNLOAD_URL)
                //.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                //.addHeader("Accept-Encoding", "gzip, deflate")
                //.addHeader("DNT", "1")
                //.addHeader("Cookie", "BAIDUID=648E5FF020CC69E8DD6F492D1068AAA9:FG=1; BIDUPSID=648E5FF020CC69E8DD6F492D1068AAA9; PSTM=1519099573; BD_UPN=12314753; locale=zh; BDSVRTM=0")
                //.useServerFileName(true)
                //.setRequestMode(RequestEnum.GET)
                .setFilePath(path, false)
                //.asPost().setParam("key", "value")
                //.setExtendField("{\n"
                //    + "\"id\":\"你的样子\"\n< > "
                //    + "}")
                //.resetState()
                .start();
        //.add();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Aria.download(this).unRegister();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Aria.download(this).unRegister();
    }
}
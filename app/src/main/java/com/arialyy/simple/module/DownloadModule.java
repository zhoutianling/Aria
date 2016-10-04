package com.arialyy.simple.module;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Environment;

import com.arialyy.downloadutil.core.DownloadManager;
import com.arialyy.downloadutil.entity.DownloadEntity;
import com.arialyy.frame.util.AndroidUtils;
import com.arialyy.frame.util.StringUtil;
import com.arialyy.simple.R;
import com.arialyy.simple.base.BaseModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lyy on 2016/9/27.
 */
public class DownloadModule extends BaseModule {
    public DownloadModule(Context context) {
        super(context);
    }

    /**
     * 设置下载数据
     * @return
     */
    public List<DownloadEntity> getDownloadData() {
        List<DownloadEntity> list = new ArrayList<>();
        String[]             urls = getContext().getResources()
                .getStringArray(R.array.test_apk_download_url);
        for (String url : urls) {
            String         fileName = StringUtil.keyToHashKey(url) + ".apk";
            DownloadEntity entity   = new DownloadEntity();
            entity.setDownloadUrl(url);
            entity.setDownloadPath(getDownloadPath(url));
            entity.setFileName(fileName);
            list.add(entity);
        }
        return list;
    }

    /**
     * 下载广播过滤器
     * @return
     */
    public IntentFilter getDownloadFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme(getContext().getPackageName());
        filter.addAction(DownloadManager.ACTION_PRE);
        filter.addAction(DownloadManager.ACTION_RESUME);
        filter.addAction(DownloadManager.ACTION_START);
        filter.addAction(DownloadManager.ACTION_RUNNING);
        filter.addAction(DownloadManager.ACTION_STOP);
        filter.addAction(DownloadManager.ACTION_CANCEL);
        filter.addAction(DownloadManager.ACTION_COMPLETE);
        filter.addAction(DownloadManager.ACTION_FAIL);
        return filter;
    }

    private String getDownloadPath(String url) {
        return Environment.getExternalStorageDirectory().getPath() + "/" + AndroidUtils.getAppName(
                getContext()) + "downloads/" + StringUtil.keyToHashKey(url) + ".apk";
    }
}

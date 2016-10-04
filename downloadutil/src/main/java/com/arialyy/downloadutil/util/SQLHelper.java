//package com.arialyy.downloadutil.util;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.support.annotation.NonNull;
//
//import com.arialyy.downloadutil.entity.DownloadEntity;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by lyy on 2016/8/11.
// * 数据库帮助类
// */
//public class SQLHelper extends SQLiteOpenHelper {
//    public static final String DB_NAME    = "ARIA_LYY_DB";
//    public static final String TABLE_NAME = "ARIA_LYY_DOWNLOAD";
//
//    public SQLHelper(Context context) {
//        this(context, DB_NAME, null, 1);
//    }
//
//    private SQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
//        super(context, name, factory, version);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        String sql = "create table " + TABLE_NAME + "(" +
//                "url varchar PRIMARY KEY, " +
//                "path varchar, " +
//                "completeTime interger, " +
//                "fileSize interger, " +
//                "state smallint , " +
//                "isDownloadComplete smallint, " +
//                "currentProgress interger" +
//                ")";
//        db.execSQL(sql);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//
//    }
//
//    /**
//     * 获取所有下载实体
//     */
//    public List<DownloadEntity> getAllEntity(@NonNull SQLiteDatabase db) {
//        List<DownloadEntity> list = new ArrayList<>();
//        Cursor               c    = db.query(TABLE_NAME, null, null, null, null, null, null);
//        if (c.moveToFirst()) {
//            while (c.moveToNext()) {
//                list.insert(cursor2Entity(c));
//            }
//        }
//        c.close();
//        return list;
//    }
//
//    /**
//     * 更新下载实体
//     */
//    public void updateEntity(@NonNull SQLiteDatabase db, DownloadEntity entity) {
//        String   whereClause = "url=?";
//        String[] whereArgs   = {entity.getDownloadUrl()};
//        db.update(TABLE_NAME, createCv(entity), whereClause, whereArgs);
//    }
//
//    /**
//     * 删除下载实体
//     */
//    public void delEntity(@NonNull SQLiteDatabase db, DownloadEntity entity) {
//        delEntity(db, entity.getDownloadUrl());
//    }
//
//    /**
//     * 通过下载链接删除下载实体
//     */
//    public void delEntity(@NonNull SQLiteDatabase db, String downloadUrl) {
//        String   whereClause = "url=?";
//        String[] whereArgs   = {downloadUrl};
//        db.delete(TABLE_NAME, whereClause, whereArgs);
//    }
//
//    /**
//     * 通过下载链接查找下载实体
//     *
//     * @param downloadUrl
//     * @return
//     */
//    public DownloadEntity findEntity(@NonNull SQLiteDatabase db, @NonNull String downloadUrl) {
//        DownloadEntity entity;
//        String         sql = "select * from " + TABLE_NAME + "where url=?";
//        Cursor         c   = db.rawQuery(sql, new String[]{downloadUrl});
//        if (c.getCount() <= 0) {
//            c.close();
//            return null;
//        } else {
//            c.moveToFirst();
//            entity = cursor2Entity(c);
//        }
//        c.close();
//        return entity;
//    }
//
//    /**
//     * 存储下载实体
//     *
//     * @param entity
//     */
//    public void savaEntity(@NonNull SQLiteDatabase db, @NonNull DownloadEntity entity) {
//        DownloadEntity temp = findEntity(db, entity.getDownloadUrl());
//        if (temp == null) {
//            db.insert(TABLE_NAME, null, createCv(entity));
//        } else {
//            updateEntity(db, entity);
//        }
//    }
//
//    /**
//     * 游标转实体
//     *
//     * @param c
//     * @return
//     */
//    private DownloadEntity cursor2Entity(Cursor c) {
//        DownloadEntity entity;
//        entity = new DownloadEntity();
//        entity.setDownloadUrl(c.getString(c.getColumnIndex("url")));
//        entity.setDownloadPath(c.getString(c.getColumnIndex("path")));
//        entity.setCompleteTime(c.getLong(c.getColumnIndex("completeTime")));
//        entity.setFileSize(c.getLong(c.getColumnIndex("fileSize")));
//        entity.setState(c.getInt(c.getColumnIndex("state")));
//        // 0 ==> false, 1 ==> true
//        entity.setDownloadComplete(c.getInt(c.getColumnIndex("isDownloadComplete")) == 0);
//        entity.setCurrentProgress(c.getLong(c.getColumnIndex("currentProgress")));
//        return entity;
//    }
//
//    /**
//     * 创建ContentValues
//     *
//     * @param entity
//     * @return
//     */
//    private ContentValues createCv(@NonNull DownloadEntity entity) {
//        ContentValues cv = new ContentValues();
//        cv.put("url", entity.getDownloadUrl());
//        cv.put("path", entity.getDownloadPath());
//        cv.put("completeTime", entity.getCompleteTime());
//        cv.put("fileSize", entity.getFileSize());
//        cv.put("state", entity.getState());
//        // 0 ==> false, 1 ==> true
//        cv.put("isDownloadComplete", entity.isDownloadComplete() ? 1 : 0);
//        cv.put("currentProgress", entity.getCurrentProgress());
//        return cv;
//    }
//}

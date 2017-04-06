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

package com.arialyy.aria.orm;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.util.CommonUtil;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * Created by lyy on 2015/11/2.
 * sql帮助类
 */
final class SqlHelper extends SQLiteOpenHelper {
  interface UpgradeListener {
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
  }

  private UpgradeListener mUpgradeListener;
  static String DB_NAME;
  static int VERSION = -1;

  static {
    if (TextUtils.isEmpty(DB_NAME)) {
      DB_NAME = "AriaLyyDb";
    }
    if (VERSION == -1) {
      VERSION = 1;
    }
  }

  //SqlHelper(Context context, UpgradeListener listener) {
  //  super(context, DB_NAME, null, VERSION);
  //  mUpgradeListener = listener;
  //}

  SqlHelper(Context context) {
    super(context, DB_NAME, null, VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {

  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    try {
      if (oldVersion < newVersion) {
        handleDbUpdate(db);
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * 处理数据库升级
   *
   * @throws ClassNotFoundException
   */
  private void handleDbUpdate(SQLiteDatabase db) throws ClassNotFoundException {
    if (db == null) {
      Log.d("SqlHelper", "db 为 null");
      return;
    } else if (!db.isOpen()) {
      Log.d("SqlHelper", "db已关闭");
      return;
    }
    Set<String> tables = DBMapping.mapping.keySet();
    for (String tableName : tables) {
      Class clazz = Class.forName(DBMapping.mapping.get(tableName));
      if (DbUtil.tableExists(db, clazz)) {
        String countColumnSql = "SELECT rowid FROM " + tableName;
        Cursor cursor = db.rawQuery(countColumnSql, null);
        int dbColumnNum = cursor.getColumnCount();
        int newEntityColumnNum = getEntityAttr(clazz);
        if (dbColumnNum != newEntityColumnNum) {
          back(db, clazz);
        }
      }
    }
  }

  /**
   * 备份
   */
  private void back(SQLiteDatabase db, Class clazz) {
    String oldTableName = CommonUtil.getClassName(clazz);
    //备份数据
    List<DbEntity> list = DbUtil.findAllData(db, clazz);
    //修改原来表名字
    String alertSql = "alter table " + oldTableName + " rename to " + oldTableName + "_temp";
    db.beginTransaction();
    db.execSQL(alertSql);
    //创建一个原来新表
    DbUtil.createTable(db, clazz, null);
    for (DbEntity entity : list) {
      DbUtil.insertData(db, entity);
    }
    //删除原来的表
    String deleteSQL = "drop table IF EXISTS " + oldTableName + "_temp";
    db.execSQL(deleteSQL);
    db.setTransactionSuccessful();
    db.endTransaction();
    db.close();
  }

  /**
   * 获取实体的字段数
   */
  private int getEntityAttr(Class clazz) {
    int count = 1;
    Field[] fields = CommonUtil.getFields(clazz);
    if (fields != null && fields.length > 0) {
      for (Field field : fields) {
        field.setAccessible(true);
        if (DbUtil.ignoreField(field)) {
          continue;
        }
        count++;
      }
    }
    return count;
  }
}
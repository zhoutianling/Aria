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
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by lyy on 2015/11/2.
 * sql帮助类
 */
final class SqlHelper extends SQLiteOpenHelper {
  private static final String TAG = "SqlHelper";
  private static final int CREATE_TABLE = 0;
  private static final int TABLE_EXISTS = 1;
  private static final int INSERT_DATA = 2;
  private static final int MODIFY_DATA = 3;
  private static final int FIND_DATA = 4;
  private static final int FIND_ALL_DATA = 5;
  private static final int DEL_DATA = 6;

  private static volatile SqlHelper INSTANCE = null;

  static SqlHelper init(Context context) {
    if (INSTANCE == null) {
      synchronized (AriaManager.LOCK) {
        INSTANCE = new SqlHelper(context.getApplicationContext());
        checkTable(INSTANCE.getWritableDatabase());
      }
    }
    return INSTANCE;
  }

  private SqlHelper(Context context) {
    super(context, DBConfig.DB_NAME, null, DBConfig.VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {

  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < newVersion) {
      handleDbUpdate(db);
    }
  }

  @Override public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //super.onDowngrade(db, oldVersion, newVersion);
    if (oldVersion > newVersion) {
      handleDbUpdate(db);
    }
  }

  /**
   * 处理数据库升级
   *
   * @throws ClassNotFoundException
   */
  private void handleDbUpdate(SQLiteDatabase db) {
    if (db == null) {
      Log.d("SqlHelper", "db 为 null");
      return;
    } else if (!db.isOpen()) {
      Log.d("SqlHelper", "db已关闭");
      return;
    }
    Set<String> tables = DBConfig.mapping.keySet();
    for (String tableName : tables) {
      Class clazz = DBConfig.mapping.get(tableName);
      if (tableExists(db, clazz)) {
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
    db = checkDb(db);
    String oldTableName = CommonUtil.getClassName(clazz);
    //备份数据
    List<DbEntity> list = findAllData(db, clazz);
    //修改原来表名字
    String alertSql = "alter table " + oldTableName + " rename to " + oldTableName + "_temp";
    db.beginTransaction();
    db.execSQL(alertSql);
    //创建一个原来新表
    createTable(db, clazz, null);
    if (list != null && list.size() > 0) {
      for (DbEntity entity : list) {
        insertData(db, entity);
      }
    }
    //删除原来的表
    String deleteSQL = "drop table IF EXISTS " + oldTableName + "_temp";
    db.execSQL(deleteSQL);
    db.setTransactionSuccessful();
    db.endTransaction();
    close(db);
  }

  /**
   * 获取实体的字段数
   */
  private int getEntityAttr(Class clazz) {
    int count = 1;
    List<Field> fields = CommonUtil.getAllFields(clazz);
    if (fields != null && fields.size() > 0) {
      for (Field field : fields) {
        field.setAccessible(true);
        if (ignoreField(field)) {
          continue;
        }
        count++;
      }
    }
    return count;
  }

  /**
   * 检查数据库表，如果配置的表不存在，则创建新表
   */
  static synchronized void checkTable(SQLiteDatabase db) {
    db = checkDb(db);
    Set<String> tables = DBConfig.mapping.keySet();
    for (String tableName : tables) {
      Class clazz = null;
      clazz = DBConfig.mapping.get(tableName);

      if (!tableExists(db, clazz)) {
        createTable(db, clazz, null);
      }
    }
  }

  /**
   * 条件查寻数据
   */
  static synchronized <T extends DbEntity> List<T> findData(SQLiteDatabase db, Class<T> clazz,
      String... expression) {
    db = checkDb(db);
    CheckUtil.checkSqlExpression(expression);
    String sql =
        "SELECT rowid, * FROM " + CommonUtil.getClassName(clazz) + " WHERE " + expression[0] + " ";
    sql = sql.replace("?", "%s");
    Object[] params = new String[expression.length - 1];
    for (int i = 0, len = params.length; i < len; i++) {
      params[i] = "'" + expression[i + 1] + "'";
    }
    sql = String.format(sql, params);
    print(FIND_DATA, sql);
    Cursor cursor = db.rawQuery(sql, null);
    List<T> data = cursor.getCount() > 0 ? newInstanceEntity(clazz, cursor) : null;
    cursor.close();
    close(db);
    return data;
  }

  /**
   * 条件查寻数据
   */
  @Deprecated static synchronized <T extends DbEntity> List<T> findData(SQLiteDatabase db,
      Class<T> clazz, @NonNull String[] wheres, @NonNull String[] values) {
    db = checkDb(db);
    if (wheres.length <= 0 || values.length <= 0) {
      Log.e(TAG, "请输入查询条件");
      return null;
    } else if (wheres.length != values.length) {
      Log.e(TAG, "key 和 vaule 长度不相等");
      return null;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT rowid, * FROM ").append(CommonUtil.getClassName(clazz)).append(" where ");
    int i = 0;
    for (Object where : wheres) {
      sb.append(where).append("=").append("'").append(values[i]).append("'");
      sb.append(i >= wheres.length - 1 ? "" : " AND ");
      i++;
    }
    print(FIND_DATA, sb.toString());
    Cursor cursor = db.rawQuery(sb.toString(), null);
    List<T> data = cursor.getCount() > 0 ? newInstanceEntity(clazz, cursor) : null;
    cursor.close();
    close(db);
    return data;
  }

  /**
   * 遍历所有数据
   */
  static synchronized <T extends DbEntity> List<T> findAllData(SQLiteDatabase db, Class<T> clazz) {
    db = checkDb(db);
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT rowid, * FROM ").append(CommonUtil.getClassName(clazz));
    print(FIND_ALL_DATA, sb.toString());
    Cursor cursor = db.rawQuery(sb.toString(), null);
    List<T> data = cursor.getCount() > 0 ? newInstanceEntity(clazz, cursor) : null;
    cursor.close();
    close(db);
    return data;
  }

  /**
   * 删除某条数据
   */
  static synchronized <T extends DbEntity> void delData(SQLiteDatabase db, Class<T> clazz,
      String... expression) {
    db = checkDb(db);
    CheckUtil.checkSqlExpression(expression);
    String sql = "DELETE FROM " + CommonUtil.getClassName(clazz) + " WHERE " + expression[0] + " ";
    sql = sql.replace("?", "%s");
    Object[] params = new String[expression.length - 1];
    for (int i = 0, len = params.length; i < len; i++) {
      params[i] = "'" + expression[i + 1] + "'";
    }
    sql = String.format(sql, params);
    SqlHelper.print(DEL_DATA, sql);
    db.execSQL(sql);
    close(db);
  }

  /**
   * 修改某行数据
   */
  static synchronized void modifyData(SQLiteDatabase db, DbEntity dbEntity) {
    db = checkDb(db);
    Class<?> clazz = dbEntity.getClass();
    List<Field> fields = CommonUtil.getAllFields(clazz);
    if (fields != null && fields.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append("UPDATE ").append(CommonUtil.getClassName(dbEntity)).append(" SET ");
      int i = 0;
      for (Field field : fields) {
        field.setAccessible(true);
        if (SqlHelper.ignoreField(field)) {
          continue;
        }
        sb.append(i > 0 ? ", " : "");
        try {
          Object value = field.get(dbEntity);
          sb.append(field.getName())
              .append("='")
              .append(value == null ? "" : value.toString())
              .append("'");
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
        i++;
      }
      sb.append(" where rowid=").append(dbEntity.rowID);
      print(MODIFY_DATA, sb.toString());
      db.execSQL(sb.toString());
    }
    close(db);
  }

  /**
   * 插入数据
   */
  static synchronized void insertData(SQLiteDatabase db, DbEntity dbEntity) {
    db = checkDb(db);
    Class<?> clazz = dbEntity.getClass();
    List<Field> fields = CommonUtil.getAllFields(clazz);
    if (fields != null && fields.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append("INSERT INTO ").append(CommonUtil.getClassName(dbEntity)).append("(");
      int i = 0;
      for (Field field : fields) {
        field.setAccessible(true);
        if (ignoreField(field)) {
          continue;
        }
        sb.append(i > 0 ? ", " : "");
        sb.append(field.getName());
        i++;
      }
      sb.append(") VALUES (");
      i = 0;
      for (Field field : fields) {
        field.setAccessible(true);
        if (ignoreField(field)) {
          continue;
        }
        sb.append(i > 0 ? ", " : "");
        sb.append("'");
        try {
          sb.append(field.get(dbEntity)).append("'");
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
        i++;
      }
      sb.append(")");
      print(INSERT_DATA, sb.toString());
      db.execSQL(sb.toString());
    }
    close(db);
  }

  /**
   * 查找表是否存在
   *
   * @param clazz 数据库实体
   * @return true，该数据库实体对应的表存在；false，不存在
   */
  static synchronized boolean tableExists(SQLiteDatabase db, Class clazz) {
    db = checkDb(db);
    Cursor cursor = null;
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("SELECT COUNT(*) AS c FROM sqlite_master WHERE type='table' AND name='");
      sb.append(CommonUtil.getClassName(clazz));
      sb.append("'");
      print(TABLE_EXISTS, sb.toString());
      cursor = db.rawQuery(sb.toString(), null);
      if (cursor != null && cursor.moveToNext()) {
        int count = cursor.getInt(0);
        if (count > 0) {
          return true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) cursor.close();
      close(db);
    }
    return false;
  }

  /**
   * 创建表
   *
   * @param clazz 数据库实体
   * @param tableName 数据库实体的类名
   */
  static synchronized void createTable(SQLiteDatabase db, Class clazz, String tableName) {
    db = checkDb(db);
    List<Field> fields = CommonUtil.getAllFields(clazz);
    if (fields != null && fields.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append("create table ")
          .append(TextUtils.isEmpty(tableName) ? CommonUtil.getClassName(clazz) : tableName)
          .append("(");
      for (Field field : fields) {
        field.setAccessible(true);
        if (ignoreField(field)) {
          continue;
        }
        sb.append(field.getName());
        Class<?> type = field.getType();
        if (type == String.class) {
          sb.append(" varchar");
        } else if (type == int.class || type == Integer.class) {
          sb.append(" interger");
        } else if (type == float.class || type == Float.class) {
          sb.append(" float");
        } else if (type == double.class || type == Double.class) {
          sb.append(" double");
        } else if (type == long.class || type == Long.class) {
          sb.append(" bigint");
        } else if (type == boolean.class || type == Boolean.class) {
          sb.append(" boolean");
        } else if (type == java.util.Date.class || type == java.sql.Date.class) {
          sb.append(" data");
        } else if (type == byte.class || type == Byte.class) {
          sb.append(" blob");
        } else {
          continue;
        }
        sb.append(",");
      }
      String str = sb.toString();
      str = str.substring(0, str.length() - 1) + ");";
      print(CREATE_TABLE, str);
      db.execSQL(str);
    }
    close(db);
  }

  /**
   * 打印数据库日志
   *
   * @param type {@link DbUtil}
   */
  static void print(int type, String sql) {
    if (true) {
      return;
    }
    String str = "";
    switch (type) {
      case CREATE_TABLE:
        str = "创建表 >>>> ";
        break;
      case TABLE_EXISTS:
        str = "表是否存在 >>>> ";
        break;
      case INSERT_DATA:
        str = "插入数据 >>>> ";
        break;
      case MODIFY_DATA:
        str = "修改数据 >>>> ";
        break;
      case FIND_DATA:
        str = "查询一行数据 >>>> ";
        break;
      case FIND_ALL_DATA:
        str = "遍历整个数据库 >>>> ";
        break;
    }
    Log.v(TAG, str + sql);
  }

  /**
   * 根据数据游标创建一个具体的对象
   */
  static synchronized <T extends DbEntity> List<T> newInstanceEntity(Class<T> clazz,
      Cursor cursor) {
    List<Field> fields = CommonUtil.getAllFields(clazz);
    List<T> entitys = new ArrayList<>();
    if (fields != null && fields.size() > 0) {
      try {
        while (cursor.moveToNext()) {
          T entity = clazz.newInstance();
          for (Field field : fields) {
            field.setAccessible(true);
            if (ignoreField(field)) {
              continue;
            }
            Class<?> type = field.getType();
            int column = cursor.getColumnIndex(field.getName());
            if (column == -1) continue;
            if (type == String.class) {
              field.set(entity, cursor.getString(column));
            } else if (type == int.class || type == Integer.class) {
              field.setInt(entity, cursor.getInt(column));
            } else if (type == float.class || type == Float.class) {
              field.setFloat(entity, cursor.getFloat(column));
            } else if (type == double.class || type == Double.class) {
              field.setDouble(entity, cursor.getDouble(column));
            } else if (type == long.class || type == Long.class) {
              field.setLong(entity, cursor.getLong(column));
            } else if (type == boolean.class || type == Boolean.class) {
              field.setBoolean(entity, !cursor.getString(column).equalsIgnoreCase("false"));
            } else if (type == java.util.Date.class || type == java.sql.Date.class) {
              field.set(entity, new Date(cursor.getString(column)));
            } else if (type == byte[].class) {
              field.set(entity, cursor.getBlob(column));
            } else {
              continue;
            }
          }
          entity.rowID = cursor.getInt(cursor.getColumnIndex("rowid"));
          entitys.add(entity);
        }
        cursor.close();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return entitys;
  }

  private static void close(SQLiteDatabase db) {
    //if (db != null && db.isOpen()) db.close();
  }

  private static SQLiteDatabase checkDb(SQLiteDatabase db) {
    if (db == null || !db.isOpen()) {
      db = INSTANCE.getWritableDatabase();
    }
    return db;
  }

  /**
   * @return true 忽略该字段
   */
  static boolean ignoreField(Field field) {
    // field.isSynthetic(), 使用as热启动App时，AS会自动给你的class添加change字段
    Ignore ignore = field.getAnnotation(Ignore.class);
    int modifiers = field.getModifiers();
    return (ignore != null && ignore.value())
        || field.getName().equals("rowID")
        || field.isSynthetic()
        || Modifier.isStatic(modifiers)
        || Modifier.isFinal(modifiers);
  }
}
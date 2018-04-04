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

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.arialyy.aria.orm.annotation.Primary;
import com.arialyy.aria.util.CheckUtil;
import com.arialyy.aria.util.CommonUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by laoyuyu on 2018/3/22.
 * 增加数据、更新数据
 */
class DelegateUpdate extends AbsDelegate {
  private DelegateUpdate() {
  }

  /**
   * 删除某条数据
   */
  <T extends DbEntity> void delData(SQLiteDatabase db, Class<T> clazz, String... expression) {
    db = checkDb(db);
    CheckUtil.checkSqlExpression(expression);

    String sql = "DELETE FROM " + CommonUtil.getClassName(clazz) + " WHERE " + expression[0] + " ";
    sql = sql.replace("?", "%s");
    Object[] params = new String[expression.length - 1];
    for (int i = 0, len = params.length; i < len; i++) {
      params[i] = "'" + expression[i + 1] + "'";
    }
    sql = String.format(sql, params);
    print(DEL_DATA, sql);
    db.execSQL(sql);
    close(db);
  }

  /**
   * 修改某行数据
   */
  void modifyData(SQLiteDatabase db, DbEntity dbEntity) {
    db = checkDb(db);
    Class<?> clazz = dbEntity.getClass();
    List<Field> fields = CommonUtil.getAllFields(clazz);
    DbEntity cacheEntity = mDataCache.get(getCacheKey(dbEntity));
    if (fields != null && fields.size() > 0) {
      StringBuilder sql = new StringBuilder();
      StringBuilder prams = new StringBuilder();
      sql.append("UPDATE ").append(CommonUtil.getClassName(dbEntity)).append(" SET ");
      int i = 0;
      try {
        for (Field field : fields) {
          field.setAccessible(true);
          if (isIgnore(dbEntity, field)) {
            continue;
          }
          if (cacheEntity != null
              && field.get(dbEntity) == field.get(cacheEntity)
              && !field.getName().equals("state")) {  //在LruCache中 state字段总是不能重新赋值...
            continue;
          }

          String value;
          prams.append(i > 0 ? ", " : "");
          prams.append(field.getName()).append("='");
          Type type = field.getType();
          if (type == Map.class && checkMap(field)) {
            value = SqlUtil.map2Str((Map<String, String>) field.get(dbEntity));
          } else if (type == List.class && checkList(field)) {
            value = SqlUtil.list2Str(dbEntity, field);
          } else {
            Object obj = field.get(dbEntity);
            value = obj == null ? "" : convertValue(obj.toString());
          }

          prams.append(TextUtils.isEmpty(value) ? "" : value);
          prams.append("'");
          i++;
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
      if (!TextUtils.isEmpty(prams.toString())) {
        sql.append(prams.toString());
        sql.append(" where rowid=").append(dbEntity.rowID);
        print(MODIFY_DATA, sql.toString());
        db.execSQL(sql.toString());
      }
    }
    mDataCache.put(getCacheKey(dbEntity), dbEntity);
    close(db);
  }

  /**
   * 插入数据
   */
  void insertData(SQLiteDatabase db, DbEntity dbEntity) {
    db = checkDb(db);
    Class<?> clazz = dbEntity.getClass();
    List<Field> fields = CommonUtil.getAllFields(clazz);
    if (fields != null && fields.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append("INSERT INTO ").append(CommonUtil.getClassName(dbEntity)).append("(");
      int i = 0;
      try {
        for (Field field : fields) {
          field.setAccessible(true);
          if (isIgnore(dbEntity, field)) {
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
          if (isIgnore(dbEntity, field)) {
            continue;
          }
          sb.append(i > 0 ? ", " : "");
          sb.append("'");
          Type type = field.getType();
          if (type == Map.class && checkMap(field)) {
            sb.append(SqlUtil.map2Str((Map<String, String>) field.get(dbEntity)));
          } else if (type == List.class && checkList(field)) {
            sb.append(SqlUtil.list2Str(dbEntity, field));
          } else {
            Object obj = field.get(dbEntity);
            if (obj != null) {
              sb.append(convertValue(field.get(dbEntity).toString()));
            }
          }
          sb.append("'");
          i++;
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
      sb.append(")");
      print(INSERT_DATA, sb.toString());
      db.execSQL(sb.toString());
    }
    close(db);
  }

  /**
   * {@code true}自动增长的主键和需要忽略的字段
   */
  private boolean isIgnore(Object obj, Field field) throws IllegalAccessException {
    if (SqlUtil.isIgnore(field)) {
      return true;
    }
    if (SqlUtil.isPrimary(field)) {   //忽略自动增长的主键
      Primary p = field.getAnnotation(Primary.class);
      if (p.autoincrement()) {
        return true;
      }
    }
    if (SqlUtil.isForeign(field)) {   //忽略外键为null的字段
      Object value = field.get(obj);
      if (value == null || (value instanceof String && TextUtils.isEmpty(String.valueOf(value)))) {
        return true;
      }
    }
    return false;
  }
}

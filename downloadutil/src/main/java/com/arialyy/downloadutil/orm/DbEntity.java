package com.arialyy.downloadutil.orm;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import com.arialyy.downloadutil.util.Util;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lyy on 2015/11/2.
 * 所有数据库实体父类
 */
public class DbEntity {
  private static final String TAG = "DbEntity";
  private volatile static SQLiteDatabase mDb = null;
  private volatile static DbUtil mUtil;
  private Context mContext;
  private static final Object LOCK = new Object();
  protected int rowID = -1;

  protected DbEntity() {

  }

  public DbEntity(Context context) {
    this(context, true);
  }

  public DbEntity(Context context, boolean newTable) {
    mContext = context;
    init(newTable);
  }

  private void init(boolean newTable) {
    if (mDb == null) {
      synchronized (LOCK) {
        if (mDb == null) {
          SqlHelper mHelper = new SqlHelper(mContext);
          mDb = mHelper.getWritableDatabase();
          mUtil = DbUtil.getInstance(mDb);
        }
      }
    }
    if (newTable && !mUtil.tableExists(this)) {
      mUtil.createTable(this);
    }
  }

  /**
   * 获取所有行的rowid
   */
  public int[] getRowId() {
    Cursor cursor = mUtil.getRowId(this);
    int[] ids = new int[cursor.getCount()];
    int i = 0;
    while (cursor.moveToNext()) {
      ids[i] = cursor.getInt(cursor.getColumnIndex("rowid"));
      i++;
    }
    return ids;
  }

  /**
   * 获取rowid
   */
  public int getRowId(@NonNull Object[] wheres, @NonNull Object[] values) {
    return mUtil.getRowId(this, wheres, values);
  }

  /**
   * 删除当前数据
   */
  public void deleteData() {
    mUtil.delData(this, new Object[] { "rowid" }, new Object[] { rowID });
  }

  /**
   * 根据条件删除数据
   */
  public void deleteData(@NonNull Object[] wheres, @NonNull Object[] values) {
    mUtil.delData(this, wheres, values);
  }

  /**
   * 修改数据
   */
  public void update() {
    mUtil.modifyData(this);
  }

  /**
   * 插入数据
   */
  public void save() {
    mUtil.insertData(this);
  }

  /**
   * 查询所有数据
   *
   * @return 没有数据返回null
   */
  public <T extends DbEntity> List<T> findAllData(Class<T> clazz) {
    Cursor cursor = mUtil.findAllData(this);
    return cursor.getCount() > 0 ? newInstanceEntity(clazz, cursor) : null;
  }

  /**
   * 查询一组数据
   *
   * @return 没有数据返回null
   */
  public <T extends DbEntity> List<T> findDatas(Class<T> clazz, @NonNull Object[] wheres,
      @NonNull Object[] values) {
    Cursor cursor = mUtil.findData(this, wheres, values);
    return cursor.getCount() > 0 ? newInstanceEntity(clazz, cursor) : null;
  }

  /**
   * 查询一行数据
   *
   * @return 没有数据返回null
   */
  public <T extends DbEntity> T findData(Class<T> clazz, @NonNull Object[] wheres,
      @NonNull Object[] values) {
    Cursor cursor = mUtil.findData(this, wheres, values);
    return cursor.getCount() > 0 ? newInstanceEntity(clazz, cursor).get(0) : null;
  }

  /**
   * 根据数据游标创建一个具体的对象
   */
  private <T extends DbEntity> List<T> newInstanceEntity(Class<T> clazz, Cursor cursor) {
    Field[] fields = Util.getFields(clazz);
    List<T> entitys = new ArrayList<>();
    if (fields != null && fields.length > 0) {
      try {
        while (cursor.moveToNext()) {
          Class[] paramTypes = { Context.class, boolean.class };
          Object[] params = { mContext, false };
          Constructor<T> con = clazz.getConstructor(paramTypes);
          T entity = con.newInstance(params);
          for (Field field : fields) {
            field.setAccessible(true);
            Ignore ignore = field.getAnnotation(Ignore.class);
            if (ignore != null && ignore.value()) {
              continue;
            }
            Class<?> type = field.getType();
            int column = cursor.getColumnIndex(field.getName());
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
            }
            //                        field.set(entity, cursor.getColumnIndex("entity_id"));
          }
          entity.rowID = cursor.getInt(cursor.getColumnIndex("rowid"));
          entitys.add(entity);
        }
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    cursor.close();
    return entitys;
  }
}

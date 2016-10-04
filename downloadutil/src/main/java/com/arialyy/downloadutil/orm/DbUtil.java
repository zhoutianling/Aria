package com.arialyy.downloadutil.orm;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.arialyy.downloadutil.util.Util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by AriaLyy on 2015/2/11.
 * 数据库操作工具
 */
public class DbUtil {
    private static final    String TAG           = "DbUtil";
    private volatile static DbUtil INSTANCE      = null;
    private static final    Object LOCK          = new Object();
    private                 int    CREATE_TABLE  = 0;
    private                 int    TABLE_EXISTS  = 1;
    private                 int    INSERT_DATA   = 2;
    private                 int    MODIFY_DATA   = 3;
    private                 int    FIND_DATA     = 4;
    private                 int    FIND_ALL_DATA = 5;
    private                 int    DEL_DATA      = 6;
    private                 int    ROW_ID        = 7;
    private SQLiteDatabase mDb;
    private SqlHelper      mHelper;

    private DbUtil() {

    }

    public static DbUtil init(Context context) {
        if (context instanceof Application) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new DbUtil(context);
                }
            }
        }
        return INSTANCE;
    }

    private DbUtil(Context context) {
        mHelper = new SqlHelper(context.getApplicationContext());
    }

    protected static DbUtil getInstance() {
        if (INSTANCE == null) {
            throw new NullPointerException("请在Application中调用init进行数据库工具注册注册");
        }
        return INSTANCE;
    }

    /**
     * 删除某条数据
     */
    protected void delData(DbEntity dbEntity, @NonNull Object[] wheres, @NonNull Object[] values) {
        mDb = mHelper.getWritableDatabase();
        if (wheres.length <= 0 || values.length <= 0) {
            Log.e(TAG, "输入删除条件");
            return;
        } else if (wheres.length != values.length) {
            Log.e(TAG, "key 和 vaule 长度不相等");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(Util.getClassName(dbEntity)).append(" WHERE ");
        int i = 0;
        for (Object where : wheres) {
            sb.append(where).append("=").append("'").append(values[i]).append("'");
            sb.append(i >= wheres.length - 1 ? "" : ",");
            i++;
        }
        print(DEL_DATA, sb.toString());
        mDb.execSQL(sb.toString());
        close();
    }

    /**
     * 修改某行数据
     */
    protected void modifyData(DbEntity dbEntity) {
        mDb = mHelper.getWritableDatabase();
        Class<?> clazz  = dbEntity.getClass();
        Field[]  fields = Util.getFields(clazz);
        if (fields != null && fields.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE ").append(Util.getClassName(dbEntity)).append(" SET ");
            int i = 0;
            for (Field field : fields) {
                field.setAccessible(true);
                Ignore ignore = field.getAnnotation(Ignore.class);
                if (ignore != null && ignore.value()) {
                    continue;
                }
                sb.append(i > 0 ? ", " : "");
                try {
                    sb.append(field.getName())
                      .append(" = '")
                      .append(field.get(dbEntity).toString())
                      .append("'");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                i++;
            }
            print(MODIFY_DATA, sb.toString());
            mDb.execSQL(sb.toString());
        }
        close();
    }

    /**
     * 遍历所有数据
     */
    protected <T extends DbEntity> List<T> findAllData(Class<T> clazz, DbEntity dbEntity) {
        mDb = mHelper.getReadableDatabase();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT rowid, * FROM ").append(Util.getClassName(dbEntity));
        print(FIND_ALL_DATA, sb.toString());
        Cursor cursor = mDb.rawQuery(sb.toString(), null);
        return cursor.getCount() > 0 ? newInstanceEntity(clazz, cursor) : null;
    }

    /**
     * 条件查寻数据
     */
    protected <T extends DbEntity> List<T> findData(Class<T> clazz, DbEntity dbEntity,
                                                    @NonNull String[] wheres,
                                                    @NonNull String[] values) {
        mDb = mHelper.getReadableDatabase();
        if (wheres.length <= 0 || values.length <= 0) {
            Log.e(TAG, "请输入查询条件");
            return null;
        } else if (wheres.length != values.length) {
            Log.e(TAG, "key 和 vaule 长度不相等");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT rowid, * FROM ").append(Util.getClassName(dbEntity)).append(" where ");
        int i = 0;
        for (Object where : wheres) {
            sb.append(where).append("=").append("'").append(values[i]).append("'");
            sb.append(i >= wheres.length - 1 ? "" : ", ");
            i++;
        }
        print(FIND_DATA, sb.toString());
        Cursor cursor = mDb.rawQuery(sb.toString(), null);
        return cursor.getCount() > 0 ? newInstanceEntity(clazz, cursor) : null;
    }

    /**
     * 插入数据
     */
    protected void insertData(DbEntity dbEntity) {
        mDb = mHelper.getWritableDatabase();
        if (!tableExists(dbEntity)) {
            createTable(dbEntity);
        }
        Class<?> clazz  = dbEntity.getClass();
        Field[]  fields = Util.getFields(clazz);
        if (fields != null && fields.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(Util.getClassName(dbEntity)).append("(");
            int i = 0;
            for (Field field : fields) {
                field.setAccessible(true);
                Ignore ignore = field.getAnnotation(Ignore.class);
                if (ignore != null && ignore.value()) {
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
                Ignore ignore = field.getAnnotation(Ignore.class);
                if (ignore != null && ignore.value()) {
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
            mDb.execSQL(sb.toString());
        }
        close();
    }

    /**
     * 查找某张表是否存在
     */
    public boolean tableExists(DbEntity dbEntity) {
        Cursor cursor = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='");
            sb.append(Util.getClassName(dbEntity));
            sb.append("'");
            print(TABLE_EXISTS, sb.toString());
            cursor = mDb.rawQuery(sb.toString(), null);
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
        }
        return false;
    }

    /**
     * 创建表
     */
    private void createTable(DbEntity dbEntity) {
        Field[] fields = Util.getFields(dbEntity.getClass());
        if (fields != null && fields.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("create table ").append(Util.getClassName(dbEntity)).append("(");
            for (Field field : fields) {
                field.setAccessible(true);
                Ignore ignore = field.getAnnotation(Ignore.class);
                if (ignore != null && ignore.value()) {
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
                } else {
                    sb.append(" blob");
                }
                sb.append(",");
            }
            String str = sb.toString();
            str = str.substring(0, str.length() - 1) + ");";
            print(CREATE_TABLE, str);
            mDb.execSQL(str);
        }
    }

    /**
     * 打印数据库日志
     *
     * @param type {@link DbUtil}
     */
    private void print(int type, String sql) {
        String str = "";
        switch (type) {
            case 0:
                str = "创建表 >>>> ";
                break;
            case 1:
                str = "表是否存在 >>>> ";
                break;
            case 2:
                str = "插入数据 >>>> ";
                break;
            case 3:
                str = "修改数据 >>>> ";
                break;
            case 4:
                str = "查询一行数据 >>>> ";
                break;
            case 5:
                str = "遍历整个数据库 >>>> ";
                break;
        }
        Log.v(TAG, str + sql);
    }

    /**
     * 关闭数据库
     */
    private void close() {
        if (mDb != null) {
            mDb.close();
        }
    }

    /**
     * 获取所在行Id
     */
    protected int[] getRowId(DbEntity dbEntity) {
        mDb = mHelper.getReadableDatabase();
        Cursor cursor = mDb.rawQuery("SELECT rowid, * FROM " + Util.getClassName(dbEntity), null);
        int[]  ids    = new int[cursor.getCount()];
        int    i      = 0;
        while (cursor.moveToNext()) {
            ids[i] = cursor.getInt(cursor.getColumnIndex("rowid"));
            i++;
        }
        cursor.close();
        close();
        return ids;
    }

    /**
     * 获取行Id
     */
    protected int getRowId(DbEntity dbEntity, Object[] wheres, Object[] values) {
        mDb = mHelper.getReadableDatabase();
        if (wheres.length <= 0 || values.length <= 0) {
            Log.e(TAG, "请输入删除条件");
            return -1;
        } else if (wheres.length != values.length) {
            Log.e(TAG, "key 和 vaule 长度不相等");
            return -1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT rowid FROM ").append(Util.getClassName(dbEntity)).append(" WHERE ");
        int i = 0;
        for (Object where : wheres) {
            sb.append(where).append("=").append("'").append(values[i]).append("'");
            sb.append(i >= wheres.length - 1 ? "" : ",");
            i++;
        }
        print(ROW_ID, sb.toString());
        Cursor c  = mDb.rawQuery(sb.toString(), null);
        int    id = c.getColumnIndex("rowid");
        c.close();
        close();
        return id;
    }

    /**
     * 根据数据游标创建一个具体的对象
     */
    private <T extends DbEntity> List<T> newInstanceEntity(Class<T> clazz, Cursor cursor) {
        Field[] fields  = Util.getFields(clazz);
        List<T> entitys = new ArrayList<>();
        if (fields != null && fields.length > 0) {
            try {
                while (cursor.moveToNext()) {
                    T entity = clazz.newInstance();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        Ignore ignore = field.getAnnotation(Ignore.class);
                        if (ignore != null && ignore.value()) {
                            continue;
                        }
                        Class<?> type   = field.getType();
                        int      column = cursor.getColumnIndex(field.getName());
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
                            field.setBoolean(entity,
                                             !cursor.getString(column).equalsIgnoreCase("false"));
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
            }
        }
        cursor.close();
        close();
        return entitys;
    }
}
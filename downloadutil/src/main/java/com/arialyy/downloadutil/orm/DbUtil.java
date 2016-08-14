package com.arialyy.downloadutil.orm;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.arialyy.downloadutil.util.Util;

import java.lang.reflect.Field;

/**
 * Created by AriaLyy on 2015/2/11.
 * 数据库操作工具
 */
public class DbUtil {
    private static final String TAG = "DbUtil";
    private volatile static DbUtil mDbUtil = null;
    private int CREATE_TABLE = 0;
    private int TABLE_EXISTS = 1;
    private int INSERT_DATA = 2;
    private int MODIFY_DATA = 3;
    private int FIND_DATA = 4;
    private int FIND_ALL_DATA = 5;
    private int DEL_DATA = 6;
    private int ROW_ID = 7;
    private static final Object LOCK = new Object();
    private SQLiteDatabase mDb;

    private DbUtil() {

    }

    private DbUtil(SQLiteDatabase db) {
        mDb = db;
    }

    protected static DbUtil getInstance(SQLiteDatabase db) {
        if (mDbUtil == null) {
            synchronized (LOCK) {
                if (mDbUtil == null) {
                    mDbUtil = new DbUtil(db);
                }
            }
        }
        return mDbUtil;
    }

    /**
     * 删除某条数据
     */
    protected void delData(DbEntity dbEntity, @NonNull Object[] wheres, @NonNull Object[] values) {
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

    }

    /**
     * 修改某行数据
     */
    protected void modifyData(DbEntity dbEntity) {
        Class<?> clazz = dbEntity.getClass();
        Field[] fields = Util.getFields(clazz);
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
                    sb.append(field.getName()).append(" = '").append(field.get(dbEntity).toString()).append("'");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                i++;
            }
            print(MODIFY_DATA, sb.toString());
            mDb.execSQL(sb.toString());
        }
    }

    /**
     * 遍历所有数据
     */
    protected Cursor findAllData(DbEntity dbEntity) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT rowid, * FROM ").append(Util.getClassName(dbEntity));
        print(FIND_ALL_DATA, sb.toString());
        return mDb.rawQuery(sb.toString(), null);
    }


    /**
     * 条件查寻数据
     */
    protected Cursor findData(DbEntity dbEntity, @NonNull Object[] wheres, @NonNull Object[] values) {
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
        return mDb.rawQuery(sb.toString(), null);
    }

    /**
     * 插入数据
     */
    protected void insertData(DbEntity dbEntity) {
        Class<?> clazz = dbEntity.getClass();
        Field[] fields = Util.getFields(clazz);
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
    }

    /**
     * 查找某张表是否存在
     */
    protected boolean tableExists(DbEntity dbEntity) {
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
            if (cursor != null)
                cursor.close();
        }
        return false;
    }

    /**
     * 创建表
     *
     * @param dbEntity
     */
    protected void createTable(DbEntity dbEntity) {
        Field[] fields = Util.getFields(dbEntity.getClass());
        if (fields != null && fields.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("create table ")
                    .append(Util.getClassName(dbEntity))
                    .append("(");
            int i = 0;
            int ignoreNum = 0;
            for (Field field : fields) {
                i++;
                field.setAccessible(true);
                Ignore ignore = field.getAnnotation(Ignore.class);
                if (ignore != null && ignore.value()) {
                    ignoreNum++;
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
                sb.append(i >= fields.length - ignoreNum - 1 ? "" : ", ");
            }
            sb.append(");");
            print(CREATE_TABLE, sb.toString());
            mDb.execSQL(sb.toString());
        }
    }

    /**
     * 打印数据库日志
     *
     * @param type {@link DbUtil}
     * @param sql
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
    protected void close() {
        if (mDb != null) {
            mDb.close();
        }
    }

    /**
     * 获取所有行Id
     */
    protected Cursor getRowId(DbEntity dbEntity) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT rowid, * FROM ").append(Util.getClassName(dbEntity));
        return mDb.rawQuery(sb.toString(), null);
    }

    /**
     * 获取行Id
     *
     * @return
     */
    protected int getRowId(DbEntity dbEntity, Object[] wheres, Object[] values) {
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
        Cursor c = mDb.rawQuery(sb.toString(), null);
        int id = c.getColumnIndex("rowid");
        c.close();
        return id;
    }
}
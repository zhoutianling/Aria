package com.arialyy.downloadutil.orm;

import android.support.annotation.NonNull;

import com.arialyy.downloadutil.util.Util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyy on 2015/11/2.
 * 所有数据库实体父类
 */
public class DbEntity {
    protected int    rowID = -1;
    private   DbUtil mUtil = DbUtil.getInstance();

    protected DbEntity() {

    }

    /**
     * 获取所有行的rowid
     */
    public int[] getRowId() {
        return mUtil.getRowId(getClass());
    }

    /**
     * 获取rowid
     */
    public int getRowId(@NonNull Object[] wheres, @NonNull Object[] values) {
        return mUtil.getRowId(getClass(), wheres, values);
    }

    /**
     * 删除当前数据
     */
    public void deleteData() {
        mUtil.delData(getClass(), new Object[]{"rowid"}, new Object[]{rowID});
    }

    /**
     * 根据条件删除数据
     */
    public void deleteData(@NonNull Object[] wheres, @NonNull Object[] values) {
        mUtil.delData(getClass(), wheres, values);
    }

    /**
     * 修改数据
     */
    public void update() {
        mUtil.modifyData(this);
    }


    /**
     * 保存自身，如果表中已经有数据，则更新数据，否则插入数据
     */
    public synchronized void save() {
        if (mUtil.tableExists(getClass()) && thisIsExist()) {
            update();
        } else {
            insert();
        }
    }

    /**
     * 查找数据在表中是否存在
     */
    private boolean thisIsExist() {
        try {
            Field[]      fields = Util.getFields(getClass());
            List<String> where  = new ArrayList<>();
            List<String> values = new ArrayList<>();
            for (Field field : fields) {
                field.setAccessible(true);
                Ignore ignore = field.getAnnotation(Ignore.class);
                if (ignore != null && ignore.value()) {
                    continue;
                }
                where.add(field.getName());
                values.add(field.get(this) + "");
            }
            return findData(getClass(), where.toArray(new String[where.size()]),
                            values.toArray(new String[values.size()])) != null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 插入数据
     */
    public void insert() {
        mUtil.insertData(this);
    }

    /**
     * 查询所有数据
     *
     * @return 没有数据返回null
     */
    public static <T extends DbEntity> List<T> findAllData(Class<T> clazz) {
        DbUtil util = DbUtil.getInstance();
        return util.findAllData(clazz);
    }

    /**
     * 查询一组数据
     *
     * @return 没有数据返回null
     */
    public static <T extends DbEntity> List<T> findDatas(Class<T> clazz, @NonNull String[] wheres,
            @NonNull String[] values) {
        DbUtil util = DbUtil.getInstance();
        return util.findData(clazz, wheres, values);
    }

    /**
     * 查询一行数据
     *
     * @return 没有数据返回null
     */
    public static <T extends DbEntity> T findData(Class<T> clazz, @NonNull String[] wheres,
            @NonNull String[] values) {
        DbUtil  util  = DbUtil.getInstance();
        List<T> datas = util.findData(clazz, wheres, values);
        return datas == null ? null : datas.size() > 0 ? datas.get(0) : null;
    }
}

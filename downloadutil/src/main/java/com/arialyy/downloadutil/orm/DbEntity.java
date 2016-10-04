package com.arialyy.downloadutil.orm;

import android.support.annotation.NonNull;

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
        return mUtil.getRowId(this);
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
        mUtil.delData(this, new Object[]{"rowid"}, new Object[]{rowID});
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
     * 保存自身，如果表中已经有数据，则更新数据，否则插入数据
     */
    public void save() {
        if (thisIsExist()) {
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
            Field[]      fields = getClass().getFields();
            List<String> where  = new ArrayList<>();
            List<String> values = new ArrayList<>();
            for (Field field : fields) {
                field.setAccessible(true);
                Ignore ignore = field.getAnnotation(Ignore.class);
                if (ignore != null && ignore.value()) {
                    continue;
                }
                where.add(field.getName());
                values.add((String) field.get(getClass()));
            }
            return findData(getClass(), (String[]) where.toArray(),
                            (String[]) values.toArray()) != null;
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
    public <T extends DbEntity> List<T> findAllData(Class<T> clazz) {
        return mUtil.findAllData(clazz, this);
    }

    /**
     * 查询一组数据
     *
     * @return 没有数据返回null
     */
    public <T extends DbEntity> List<T> findDatas(Class<T> clazz, @NonNull String[] wheres,
                                                  @NonNull String[] values) {
        return mUtil.findData(clazz, this, wheres, values);
    }

    /**
     * 查询一行数据
     *
     * @return 没有数据返回null
     */
    public <T extends DbEntity> T findData(Class<T> clazz, @NonNull String[] wheres,
                                           @NonNull String[] values) {
        List<T> datas = mUtil.findData(clazz, this, wheres, values);
        return datas.size() > 0 ? datas.get(0) : null;
    }
}

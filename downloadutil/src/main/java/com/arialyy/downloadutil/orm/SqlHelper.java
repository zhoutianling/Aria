package com.arialyy.downloadutil.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

/**
 * Created by lyy on 2015/11/2.
 * sql帮助类
 */
public class SqlHelper extends SQLiteOpenHelper {
  protected static String DB_NAME;
  protected static int VERSION = -1;

  static {
    if (TextUtils.isEmpty(DB_NAME)) {
      DB_NAME = "AriaLyyDb";
    }
    if (VERSION == -1) {
      VERSION = 1;
    }
  }

  public SqlHelper(Context context) {
    super(context, DB_NAME, null, VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {

  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }
}

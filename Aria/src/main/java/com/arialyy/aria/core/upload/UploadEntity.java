package com.arialyy.aria.core.upload;

import android.os.Parcel;
import android.os.Parcelable;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.orm.Ignore;

/**
 * Created by Aria.Lao on 2017/2/9.
 * 上传文件实体
 */
public class UploadEntity extends DbEntity implements IEntity, Parcelable {

  private String filePath;  //文件路径
  private String fileName;  //文件名
  private long fileSize;    //文件大小
  private int state = STATE_WAIT;
  private long currentProgress = 0;
  private boolean isComplete = false;
  @Ignore private long speed = 0; //下载速度
  @Ignore private int failNum = 0;

  public boolean isComplete() {
    return isComplete;
  }

  public void setComplete(boolean complete) {
    isComplete = complete;
  }

  public long getCurrentProgress() {
    return currentProgress;
  }

  public void setCurrentProgress(long currentProgress) {
    this.currentProgress = currentProgress;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public long getSpeed() {
    return speed;
  }

  public void setSpeed(long speed) {
    this.speed = speed;
  }

  public int getFailNum() {
    return failNum;
  }

  public void setFailNum(int failNum) {
    this.failNum = failNum;
  }

  @Override public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public UploadEntity() {
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.filePath);
    dest.writeString(this.fileName);
    dest.writeLong(this.fileSize);
    dest.writeInt(this.state);
    dest.writeLong(this.currentProgress);
    dest.writeByte(this.isComplete ? (byte) 1 : (byte) 0);
    dest.writeLong(this.speed);
    dest.writeInt(this.failNum);
  }

  protected UploadEntity(Parcel in) {
    this.filePath = in.readString();
    this.fileName = in.readString();
    this.fileSize = in.readLong();
    this.state = in.readInt();
    this.currentProgress = in.readLong();
    this.isComplete = in.readByte() != 0;
    this.speed = in.readLong();
    this.failNum = in.readInt();
  }

  @Ignore public static final Creator<UploadEntity> CREATOR = new Creator<UploadEntity>() {
    @Override public UploadEntity createFromParcel(Parcel source) {
      return new UploadEntity(source);
    }

    @Override public UploadEntity[] newArray(int size) {
      return new UploadEntity[size];
    }
  };
}

package com.zxk.appdial.model;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang3.builder.EqualsBuilder;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * 一个卑微的app
 *
 * @author zhangxinkun
 */
public class LocalApp implements Serializable, Comparable<LocalApp> {

  private static final long serialVersionUID = 1L;

  private String type = "launch"; // chuqq: 默认launch，还支持uri-scheme（packageName是uri-scheme）、shortcut
  private String appName;
  private String packageName;
  private String className;

  private HashMap<String, String> extras; // chuqq: 仅当type为shortcut时有效

  public Intent getIntent() {
    return intent;
  }

  public void setIntent(Intent intent) {
    this.intent = intent;
  }

  private Intent intent;// chuqq: 直接使用intent，其他信息都尽量保存在intent中，例如className、extras等

  private Drawable icon;
  private String simpleChn;
  private String pinyin;
  private int count = 0;
  private boolean inCount = true;

  @Override
  public String toString() {
    return appName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;

    if (o == null || getClass() != o.getClass())
      return false;

    LocalApp localApp = (LocalApp) o;

    return new EqualsBuilder().append(appName, localApp.appName)
        .append(packageName, localApp.packageName).isEquals();
  }

  @Override
  public int hashCode() {
    int result = appName != null ? appName.hashCode() : 0;
    result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
    return result;
  }

  @Override
  public int compareTo(LocalApp another) {
    if (another == null) {
      return -1;
    } else if (getCount() != another.getCount()) {
      return getCount() - another.getCount();// 常用的排前面
    } else if (getPinyin().length() != another.getPinyin().length()) {
      // 长度不同的 短的在前
      return another.getPinyin().length() - getPinyin().length();
    } else {
      return another.getPinyin().compareTo(getPinyin());// 字母序
    }
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }


  private String action; // chuqq: 仅当type为shortcut时有效

  public HashMap<String, String> getExtras() {
    return extras;
  }

  public void setExtras(HashMap<String, String> extras) {
    this.extras = extras;
  }


  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String name) {
    this.appName = name;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public Drawable getIcon() {
    return icon;
  }

  public void setIcon(Drawable icon) {
    this.icon = icon;
  }

  public String getSimpleChn() {
    return simpleChn;
  }

  public void setSimpleChn(String simpleChn) {
    this.simpleChn = simpleChn;
  }

  public String getPinyin() {
    return pinyin;
  }

  public void setPinyin(String pinyin) {
    this.pinyin = pinyin;
  }

  public boolean isInCount() {
    return inCount;
  }

  public void setInCount(boolean inCount) {
    this.inCount = inCount;
  }
}

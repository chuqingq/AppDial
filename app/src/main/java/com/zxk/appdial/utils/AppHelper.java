package com.zxk.appdial.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.zxk.appdial.MainActivity;
import com.zxk.appdial.model.LocalApp;

/**
 * 伟大的app管理君
 *
 * @author zhangxinkun
 */
public class AppHelper implements ThreadHelper.ThreadHeplerUser<PackageInfo> {

  private ConcurrentHashMap<LocalApp, Object> apps = null;

  private PackageManager packageManager;
  private CountHelper countHelper;

  public AppHelper(PackageManager packageManager, CountHelper countHelper) {
    this.packageManager = packageManager;
    this.countHelper = countHelper;
  }

  public List<LocalApp> scanLocalInstallAppList(boolean reload) {
    if (reload) {
      apps = null;
    }
    if (apps != null) {
      return new ArrayList<>(apps.keySet());
    }
    apps = new ConcurrentHashMap<>();
    try {
      List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
      new ThreadHelper<>(packageInfos, this, MainActivity.coutPerThread).exe();
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
    List<LocalApp> result = new ArrayList<>(apps.keySet());
    Collections.sort(result);
    Collections.reverse(result);
    return result;
  }

  @Override
  public void run(List<PackageInfo> list) {
    // Log.d(AppHelper.class.getName(), "遍历内容： " + list.toString());

    for (int i = 0; i < list.size(); i++) {
      PackageInfo packageInfo = list.get(i);
      if (countHelper.getNoMainActivityApps().contains(packageInfo.packageName)) {
        continue;
      } else {
        Intent intent = packageManager.getLaunchIntentForPackage(packageInfo.packageName);
        if (intent == null) {
          countHelper.getNoMainActivityApps().add(packageInfo.packageName);
          continue;
        }
      }
      LocalApp myAppInfo = new LocalApp();
      myAppInfo.setPackageName(packageInfo.packageName);
      myAppInfo.setAppName(packageInfo.packageName.replace(".", ""));
      String name = countHelper.getCachedPackageNameMap().optString(packageInfo.packageName, null);
      if (name == null) {
        name = packageInfo.applicationInfo.loadLabel(packageManager).toString();
        try {
          countHelper.getCachedPackageNameMap().put(packageInfo.packageName, name);
        } catch (Exception e) {
        }
      }
      myAppInfo.setAppName(name);
//      Log.d("chuqq", "appName: " + name);
      myAppInfo.setClassName(packageInfo.applicationInfo.className);
      myAppInfo.setPinyin(getPinyin(myAppInfo.getAppName(), myAppInfo.getPackageName()));
      myAppInfo.setCount(countHelper.getCount(myAppInfo.getPackageName()));
      myAppInfo.setInCount(!countHelper.isUnCount(myAppInfo.getPackageName()));
      int hashCode = myAppInfo.getPackageName().hashCode();
      File iconcFile = new File(countHelper.getCacheDir(), "/icons/" + hashCode);
      if (iconcFile.exists()) {
        myAppInfo.setIcon(Drawable.createFromPath(iconcFile.getAbsolutePath()));
      } else {
        Drawable appIcon = packageInfo.applicationInfo.loadIcon(packageManager);
        if (appIcon == null) {
          continue;
        }
        myAppInfo.setIcon(appIcon);
        try {
          iconcFile.getParentFile().mkdirs();
          iconcFile.createNewFile();
          FileOutputStream outputStream = new FileOutputStream(iconcFile);
          getBitmapFromDrawable(appIcon).compress(CompressFormat.PNG, 100, outputStream);
          outputStream.close();
        } catch (Exception e) {
        }
      }
      apps.put(myAppInfo, new Object());
    }

    // chuqq: 增加uri-scheme
    HashMap<String, String> uriSchemeInfos = new HashMap<String, String>();
    uriSchemeInfos.put("微信-扫一扫", "weixin://scanqrcode");
    uriSchemeInfos.put("支付宝-扫一扫", "alipayqr://platformapi/startapp?saId=10000007");
    uriSchemeInfos.put("支付宝-转账", "alipays://platformapi/startapp?appId=20000116");
    uriSchemeInfos.put("支付宝-蚂蚁森林", "alipays://platformapi/startapp?appId=60000002");
    uriSchemeInfos.put("支付宝-蚂蚁庄园", "alipays://platformapi/startapp?appId=66666674");
    uriSchemeInfos.put("支付宝-收款", "alipays://platformapi/startapp?appId=20000123");
    uriSchemeInfos.put("支付宝-付款", "alipayqr://platformapi/startapp?sald=20000056");
    uriSchemeInfos.put("支付宝-红包", "alipays://platformapi/startapp?appId=88886666");
    uriSchemeInfos.put("支付宝-AA收款", "alipays://platformapi/startapp?appId=20000263");
    uriSchemeInfos.put("支付宝-记账", "alipay://platformapi/startapp?appId=20000168");
    uriSchemeInfos.put("支付宝-还信用卡", "alipays://platformapi/startapp?appId=09999999");
    uriSchemeInfos.put("支付宝-滴滴出行", "alipays://platformapi/startapp?appId=20000778");
    uriSchemeInfos.put("支付宝-查快递", "alipays://platformapi/startapp?appId=20000754");
    uriSchemeInfos.put("支付宝-生活缴费", "alipays://platformapi/startapp?appId=20000193");
    uriSchemeInfos.put("支付宝-彩票", "alipays://platformapi/startapp?appId=10000011");
    uriSchemeInfos.put("支付宝-淘票票", "alipays://platformapi/startapp?appId=20000131");
    uriSchemeInfos.put("支付宝-股票", "alipays://platformapi/startapp?appId=20000134");
    uriSchemeInfos.put("支付宝-蚂蚁宝卡", "alipays://platformapi/startapp?appId=60000057");
    uriSchemeInfos.put("QQ音乐-最近播放", "qqmusic://today?mid=31&k1=2&k4=0");
    uriSchemeInfos.put("网易云音乐-听歌识曲", "orpheuswidget://recognize");
    uriSchemeInfos.put("网易云音乐-私人FM", "orpheuswidget://radio");

    for (String name: uriSchemeInfos.keySet()) {
      LocalApp myAppInfo = new LocalApp();
      myAppInfo.setType("uri-scheme");
      myAppInfo.setAppName(name);
      myAppInfo.setPackageName(uriSchemeInfos.get(name));

      myAppInfo.setPinyin(getPinyin(myAppInfo.getAppName(), ""));
      myAppInfo.setCount(countHelper.getCount(myAppInfo.getPackageName()));
      myAppInfo.setInCount(!countHelper.isUnCount(myAppInfo.getPackageName()));
      // myAppInfo.setIcon(

      apps.put(myAppInfo, new Object());
    }
    Log.d(AppHelper.class.getName(), Thread.currentThread().getName() + "结束");
  }

  private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
    final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
        drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    final Canvas canvas = new Canvas(bmp);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bmp;
  }

  private static String getPinyin(String appName, String defaultName) {
    try {
      HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
      format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
      format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
      StringBuilder sb = new StringBuilder();
      char[] chars = appName.toCharArray();
      for (char aChar : chars) {
        String[] strings = PinyinHelper.toHanyuPinyinStringArray(aChar, format);
        if (strings != null && strings.length > 0) {
          sb.append(strings[0]);
        } else {
          sb.append(aChar);
        }
      }
      return sb.toString();
    } catch (Exception e) {
      return defaultName;
    }
  }

  @Override
  public void afterRun() {
    new Thread(() -> {
      countHelper.savePackageNameMap();
      countHelper.saveNoMainActivityApps();
    }).start();
  }

}

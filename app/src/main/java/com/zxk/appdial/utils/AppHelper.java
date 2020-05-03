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

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

    // chuqq:
//    {
//      String name = "下载管理";
//
//      Intent intent = packageManager.getLaunchIntentForPackage("com.huawei.browser");
//      intent.setAction("com.huawei.browser.view.download");
//      intent.setComponent(new ComponentName("com.huawei.browser", "com.huawei.browser.Main2"));
//
//      LocalApp app = new LocalApp();
//      app.setAppName(name);
//      app.setIntent(intent);
//
//      addApp(app);
//    }
    {
      String name = "微信扫一扫";

      Intent intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
      intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
//    {
//      String name = "微信车来了精准实时公交";
//
//      Intent intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
//      intent.setAction("com.tencent.mm.action.WX_SHORTCUT");
//      intent.putExtra("ext_info", "shortcut_2b6ac38ac282c2b7c39c0c605a5cc3992764c3be2d16c390c38e");
//      intent.putExtra("digest", "61f1c6b4dbe1711e64ec02d0fa0e0222");
//      intent.putExtra("id", "shortcut_3b7ac2a2c280c3a6c28b506b0909c28c703fc2a32865c283c28f26");
//      intent.putExtra("type", "1");
//      intent.putExtra("token", "21ac2bcac55414ff4906aa31ed3857a2");
//      intent.putExtra("ext_info_1", "0");
//
//      LocalApp app = new LocalApp();
//      app.setAppName(name);
//      app.setIntent(intent);
//
//      addApp(app);
//    }
    {
      String name = "支付宝扫一扫";
      String uri = "alipays://platformapi/startapp?appId=10000007&source=nougat_shortcut&sourceId=nougat_shortcut_scan";

      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setData(Uri.parse(uri));

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
    {
      String name = "支付宝转账";
      String uri = "alipays://platformapi/startapp?appId=20000116";

      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setData(Uri.parse(uri));

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
    {
      String name = "支付宝蚂蚁森林";
      String uri = "alipays://platformapi/startapp?appId=60000002";

      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setData(Uri.parse(uri));

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
    {
      String name = "支付宝蚂蚁庄园";
      String uri = "alipays://platformapi/startapp?appId=66666674";

      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setData(Uri.parse(uri));

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
    {
      String name = "支付宝付钱";
      String uri = "alipayss://platformapi/startapp?appId=20000056&source=shortcut";

      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setData(Uri.parse(uri));

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
    {
      String name = "支付宝收钱";
      String uri = "alipays://platformapi/startapp?appId=20000123";

      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setData(Uri.parse(uri));

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
    {
      String name = "支付宝红包";
      String uri = "alipays://platformapi/startapp?appId=88886666";

      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setData(Uri.parse(uri));

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
//    {
//      String name = "支付宝AA收款";
//      String uri = "alipays://platformapi/startapp?appId=20000263";
//
//      Intent intent = new Intent();
//      intent.setAction("android.intent.action.VIEW");
//      intent.setData(Uri.parse(uri));
//
//      LocalApp app = new LocalApp();
//      app.setAppName(name);
//      app.setIntent(intent);
//
//      addApp(app);
//    }
//    {
//      String name = "支付宝记账";
//      String uri = "alipay://platformapi/startapp?appId=20000168";
//
//      Intent intent = new Intent();
//      intent.setAction("android.intent.action.VIEW");
//      intent.setData(Uri.parse(uri));
//
//      LocalApp app = new LocalApp();
//      app.setAppName(name);
//      app.setIntent(intent);
//
//      addApp(app);
//    }

//    uriSchemeInfos.put("支付宝-还信用卡", "alipays://platformapi/startapp?appId=09999999");
//    uriSchemeInfos.put("支付宝-滴滴出行", "alipays://platformapi/startapp?appId=20000778");
//    uriSchemeInfos.put("支付宝-查快递", "alipays://platformapi/startapp?appId=20000754");
//    uriSchemeInfos.put("支付宝-生活缴费", "alipays://platformapi/startapp?appId=20000193");
//    uriSchemeInfos.put("支付宝-彩票", "alipays://platformapi/startapp?appId=10000011");
//    uriSchemeInfos.put("支付宝-淘票票", "alipays://platformapi/startapp?appId=20000131");
//    uriSchemeInfos.put("支付宝-股票", "alipays://platformapi/startapp?appId=20000134");
//    uriSchemeInfos.put("支付宝-蚂蚁宝卡", "alipays://platformapi/startapp?appId=60000057");
    {
      String name = "QQ音乐听歌识曲";
      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setComponent(new ComponentName("com.tencent.qqmusic", "com.tencent.qqmusic.third.DispacherActivityForThird"));
      intent.putExtra("shortcutScheme", "recognize");

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
    {
      String name = "QQ音乐最近播放";
      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setComponent(new ComponentName("com.tencent.qqmusic", "com.tencent.qqmusic.third.DispacherActivityForThird"));
      intent.putExtra("shortcutScheme", "playRecent");

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
    {
      String name = "QQ音乐搜索歌曲";
      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setComponent(new ComponentName("com.tencent.qqmusic", "com.tencent.qqmusic.third.DispacherActivityForThird"));
      intent.putExtra("shortcutScheme", "searchSong");

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
    {
      String name = "网易云音乐听歌识曲";

      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setComponent(new ComponentName("com.netease.cloudmusic", "com.netease.cloudmusic.activity.RedirectActivity"));
      intent.putExtra("shortcutType", 1);

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
    {
      String name = "网易云音乐私人FM";

      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setComponent(new ComponentName("com.netease.cloudmusic", "com.netease.cloudmusic.activity.RedirectActivity"));
      intent.putExtra("shortcutType", 2);

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
//    {
//      String name = "斗鱼蛋塔秀Infi000的直播间";
//
//      Intent intent = new Intent();
//      intent.setAction("android.intent.action.VIEW");
//      intent.setComponent(new ComponentName("air.tv.douyu.android", "tv.douyu.view.activity.launcher.DYLauncherActivity"));
//      intent.putExtra("roomId", "11017");
//
//      LocalApp app = new LocalApp();
//      app.setAppName(name);
//      app.setIntent(intent);
//
//      addApp(app);
//    }
    {
      String name = "华为应用市场搜索";

      Intent intent = new Intent();
      intent.setAction("com.huawei.appmarket.appmarket.intent.action.SearchActivity");
      intent.setComponent(new ComponentName("com.huawei.appmarket", "com.huawei.appmarket.service.externalapi.view.ThirdApiActivity"));

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
//    {
//      String name = "汽车之家极速版";
//
//      Intent intent = new Intent();
//      intent.setAction("android.intent.action.MAIN");
//      intent.setComponent(new ComponentName("com.huawei.fastapp", "ccom.huawei.fastapp.app.processManager.RpkLoaderActivityEntry"));
//      intent.putExtra("rpk_load_hash", "7bc733da78c8da96f8865cb65639e790ddf0b985a9d21b3caaef8f42097d9ba8");
//      intent.putExtra("rpk_load_path", "http://appdlc.hicloud.com/dl/appdl/application/apk/a4/a47482325619436ab01cadf9190688cb/com.autohome.quickapp.1909261849.rpk?source=rpk&maple=0&trackId=0&distOpEntity=HWSW");
//      String type = null;
//      intent.putExtra("rpk_load_type", type);
//      intent.putExtra("rpk_load_app_id", "C100241877");
//      intent.putExtra("rpk_load_source", "shortcut_api|fastappList_other");
//      intent.putExtra("rpk_load_package", "com.autohome.quickapp");
//
//      LocalApp app = new LocalApp();
//      app.setAppName(name);
//      app.setIntent(intent);
//
//      addApp(app);
//    }
    {
      String name = "高德地图回家";
      String uri = "amapuri://commute?clearStack=1&dest=home&shortcutLabel=回家";

      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setData(Uri.parse(uri));

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
    {
      String name = "高德地图从这里出发";
      String uri = "amapuri://route/plan?shortcutLabel=从这里出发";

      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setData(Uri.parse(uri));

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }
    {
      String name = "高德地图从这里出发";
      String uri = "amapuri://commute?clearStack=1&dest=corp&shortcutLabel=去公司";

      Intent intent = new Intent();
      intent.setAction("android.intent.action.VIEW");
      intent.setData(Uri.parse(uri));

      LocalApp app = new LocalApp();
      app.setAppName(name);
      app.setIntent(intent);

      addApp(app);
    }

    Log.d(AppHelper.class.getName(), Thread.currentThread().getName() + "结束");
  }

  private void addApp(LocalApp app) {
    app.setPinyin(getPinyin(app.getAppName(), ""));
    app.setCount(countHelper.getCount(app.getPackageName()));
    app.setInCount(!countHelper.isUnCount(app.getPackageName()));
    apps.put(app, new Object());
  }

  private void addShortcut(HashMap<String, String> shortcut, HashMap<String, String> extras) {
    LocalApp app = new LocalApp();
    app.setType("shortcut");
    app.setPackageName(shortcut.get("packageName"));
    app.setAppName(shortcut.get("shortLabel"));
    app.setAction(shortcut.get("intents.act"));
    app.setClassName(shortcut.get("intents.cmp.cls"));
    app.setExtras(extras);

    app.setPinyin(getPinyin(app.getAppName(), ""));
    app.setCount(countHelper.getCount(app.getPackageName()));
    app.setInCount(!countHelper.isUnCount(app.getPackageName()));

    apps.put(app, new Object());
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

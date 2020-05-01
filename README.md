# AppDial
* Android上面好像没几个用的顺心的app搜索工具，唯一好用的meemo也不兼容Android 8.0
* 从这种工具类app入手，学习一下Android app开发

# 期望功能
1. ~~透明主题，没有割裂感~~
1. ~~不显示在最近使用的列表中，用完就消失~~
1. ~~T9搜索App，支持全拼和首字母~~
1. ~~更改键盘功能，显示T9字母~~
1. ~~加入Google分析，看看有多少人在用我的app~~
    * 这个会加大安装包体积，目前已经达到了1M
1. ~~完善排序方式，经常搜索打开的app排序显示在中央（离手指近）~~
    * 经常使用的app顺序排在前面，离手指近暂时没想好怎么做
1. 对搜索结果能做卸载

chuqq: 功能计划

- [x] 通过`getLaunchIntentForPackage`的方式启动所有应用
- [x] 通过URI-scheme方式启动常用的activity。例如支付宝-蚂蚁庄园， [webview方式跳转url-scheme](https://blog.csdn.net/LVXIANGAN/article/details/84552681)
- [ ] 其他无法用scheme方式打开activity。例如微信-扫一扫，[打开微信扫一扫activity](https://blog.csdn.net/bluezhangfun/article/details/77444053)
- [ ] 剩下的没有明确入口的activity。例如蚂蚁森林收能量，[模拟点击](https://github.com/sufadi/AccessibilityServiceMonitor)
- [ ] 分级显示：例如先输入支付宝，可以选择“支付宝-打开应用”或“支付宝-其他功能”，点击后者，会展示出“蚂蚁森林”等功能，仍然可以用T9方式搜索
- [ ] 更进一步：如果可以在其他应用上做遮罩，那可以做应用内T9搜索。




# 下载
* 没钱上架Google play，酷安基佬请[戳](https://www.coolapk.com/apk/169105)

#  License
* [GPL v3](http://www.gnu.org/licenses/gpl-3.0.html)


# flutter_weixin

微信分享到好友和朋友圈

## Getting Started

一.添加分享结果监听, WXEntryActivity只要继承FWXEntryActivity即可，无须在实现IWXAPIEventHandler。
<activity
    android:name="xxxx.wxapi.WXEntryActivity"
    android:theme="@android:style/Theme.Translucent.NoTitleBar"
    android:exported="true"
    android:taskAffinity="net.sourceforge.simcpux"
    android:launchMode="singleTask">
</activity>

二.使用

    1.初始化
        FlutterWeixin.init();
        或
        FlutterWeixin.init().then((result) {

        }).catchError((err){

        });

    2.分享文本给好友
        FlutterWeixin.shareToSession(description: "friend test desciption");

    3.分享文本到朋友圈
        FlutterWeixin.shareToTimeline(title : "pyq test title");

    4.分享图片到好友
        FlutterWeixin.shareToSession(imgUrl: "图片网络地址" (或imgPath: "图片FilePath"));

    5.分享图片到朋友圈
        FlutterWeixin.shareToTimeline(imgUrl: "图片网络地址" (或imgPath: "图片FilePath"));

     4.分享网页到好友
        FlutterWeixin.shareToSession(title : "test title", description: "test desciption", webUrl: "https://www.baidu.com",
            webImgUrl:  "图片网络地址" (或webImgPath: "图片FilePath"));

    5.分享网页到朋友圈
        FlutterWeixin.shareToTimeline(title : "test title", description: "test desciption", webUrl: "https://www.baidu.com",
                     webImgUrl:  "图片网络地址" (或webImgPath: "图片FilePath"));

    备注:所有方法都可以通过下面方式监听分享结果
        xxxxx.then((result) {

        }).catchError((err) {

        });

三.说明
    ios正在对接中。flutter插件qq群 176880648
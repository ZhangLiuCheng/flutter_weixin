import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_weixin/flutter_weixin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
    @override
    _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
    String _platformVersion = 'Unknown';

    @override
    void initState() {
        super.initState();
        initPlatformState();
    }

    // Platform messages are asynchronous, so we initialize in an async method.
    Future<void> initPlatformState() async {
        String platformVersion;
        // Platform messages may fail, so we use a try/catch PlatformException.
        try {
            platformVersion = await FlutterWeixin.platformVersion;
        } on PlatformException {
            platformVersion = 'Failed to get platform version.';
        }

        // If the widget was removed from the tree while the asynchronous platform
        // message was in flight, we want to discard the reply rather than calling
        // setState to update our non-existent appearance.
        if (!mounted) return;

        setState(() {
            _platformVersion = platformVersion;
        });
    }

    @override
    Widget build(BuildContext context) {
        String imgUrl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1556820992134&di=883e7fa3f97f991a314342b5d8ddb329&imgtype=0&src=http%3A%2F%2Fs15.sinaimg.cn%2Fmw690%2F0066UWNtgy6Viz3mEBoce%26690";
        return MaterialApp(
            home: Scaffold(
                appBar: AppBar(
                    title: const Text('Plugin example app'),
                ),
                body: Center(
                    child: Column(
                        children: <Widget>[
                            new FlatButton(
                                child: new Text('初始化'),
                                onPressed: () {
                                    FlutterWeixin.init();
                                },
                            ),
                            new FlatButton(
                                child: new Text('分享给好友'),
                                onPressed: () {
                                    FlutterWeixin.shareToSession(title : "friend test title", description: "friend test desciption", imgUrl: imgUrl).then((result) {
                                        print("微信分享成功");
                                    }).catchError((err) {
                                        print("微信分享失败 $err");
                                    });
                                },
                            ),
                            new FlatButton(
                                child: new Text('分享到朋友圈'),
                                onPressed: () {
                                    FlutterWeixin.shareToTimeline(title : "pyq test title", description: "pyq test desciption", imgUrl: null).then((result) {
                                        print("微信分享成功");
                                    }).catchError((err) {
                                        print("微信分享失败 $err");
                                    });
                                },
                            ),

                            new FlatButton(
                                child: new Text('分享网页'),
                                onPressed: () {
                                    FlutterWeixin.shareToSession(title : "test title", description: "test desciption", webUrl: "https://www.baidu.com", webImgUrl: imgUrl).then((result) {
                                        print("微信分享成功");
                                    }).catchError((err) {
                                        print("微信分享失败 $err");
                                    });
                                },
                            ),
                        ],
                    )
                ),
            ),
        );
    }
}

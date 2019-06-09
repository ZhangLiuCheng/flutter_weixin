import 'package:flutter/material.dart';

import 'package:flutter/services.dart';
import 'package:flutter_weixin/flutter_weixin.dart';
import 'dart:ui' as ui;
import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

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


    GlobalKey _globalKey = new GlobalKey();

    Future<Uint8List> _capturePng() async {
        try {
            RenderRepaintBoundary boundary = _globalKey.currentContext
                .findRenderObject();
            ui.Image image = await boundary.toImage(pixelRatio: 2.0);
            ByteData byteData = await image.toByteData(
                format: ui.ImageByteFormat.png);
            Uint8List pngBytes = byteData.buffer.asUint8List();
//            String bs64 = base64Encode(pngBytes);
            print(pngBytes);
//            print(bs64);
            return pngBytes;
        } catch (e) {
            print("error  $e");
        }
        return null;
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

        if (!mounted) return;

        setState(() {
            _platformVersion = platformVersion;
        });
    }

    _testShare() async {
        Uint8List imgData = await _capturePng();
        print("_testShare==========>  $imgData");
        FlutterWeixin.shareToSession(
            title: "friend test title",
            description: "friend test desciption",
            imgData: imgData).then((result) {
            print("微信分享成功");
        }).catchError((err) {
            print("微信分享失败 $err");
        });
    }


    @override
    Widget build(BuildContext context) {
        return MaterialApp(
            home: Scaffold(
                appBar: AppBar(
                    title: const Text('Plugin example app'),
                ),
                body: Stack(
                    children: <Widget>[
                        Positioned(
                            top: 0,
                            left: 0,
                            child: _buildTemp(),
                        ),
                        _buildContent(),

                    ],
                )
                ,

            ),
        );
    }

    _buildTemp() {
        return RepaintBoundary(
            key: _globalKey,
            child: Container(
                    alignment: Alignment.center,
                    width: 100,
                    height: 100,
                    color: Colors.yellow,
                    child: Text("temp"),
                )

        );
    }

    _buildContent() {
        String imgUrl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1556820992134&di=883e7fa3f97f991a314342b5d8ddb329&imgtype=0&src=http%3A%2F%2Fs15.sinaimg.cn%2Fmw690%2F0066UWNtgy6Viz3mEBoce%26690";
        return Container(
            width: double.infinity,
            color: Colors.white,
            child: Column(
                children: <Widget>[
                    new FlatButton(
                        child: new Text('初始化'),
                        onPressed: () {
                            FlutterWeixin.init("wx0a5d51592ca9e6dd").then((result) {
                                print("微信初始化成功: $result");
                            }).catchError((err) {
                                print("微信初始化失败: $err");
                            });
                        },
                    ),
                    new FlatButton(
                        child: new Text('分享给好友'),
                        onPressed: () {
                            FlutterWeixin.shareToSession(
                                title: "friend test title",
                                description: "friend test desciption",
                                imgUrl: null).then((result) {
                                print("微信分享成功");
                            }).catchError((err) {
                                print("微信分享失败 $err");
                            });
                        },
                    ),
                    new FlatButton(
                        child: new Text('分享到朋友圈'),
                        onPressed: () {
                            FlutterWeixin.shareToTimeline(
                                title: "pyq test title",
                                description: "pyq test desciption",
                                imgUrl: null).then((result) {
                                print("微信分享成功");
                            }).catchError((err) {
                                print("微信分享失败 $err");
                            });
                        },
                    ),

                    new FlatButton(
                        child: new Text('分享网页'),
                        onPressed: () {
                            FlutterWeixin.shareToSession(
                                title: "test title",
                                description: "test desciption",
                                webUrl: "https://www.baidu.com",
                                webImgUrl: imgUrl).then((result) {
                                print("微信分享成功");
                            }).catchError((err) {
                                print("微信分享失败 $err");
                            });
                        },
                    ),

                    new FlatButton(
                        child: new Text('分享截屏'),
                        onPressed: () {
                            _testShare();
                        },
                    ),
                ],
            )
        );
    }

}

import 'dart:async';

import 'package:flutter/services.dart';

class FlutterWeixin {
  static const MethodChannel _channel =
      const MethodChannel('flutter_weixin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future init() async {
    return await _channel.invokeMethod("init", {"wxAppId" : "wx0a5d51592ca9e6dd"});
  }

  static Future shareToSession({String title, String description, String imgPath, String imgUrl}) async {
    Map param = {"title" : title, "description" : description, "imgPath": imgPath, "imgUrl" : imgUrl};
    return await _channel.invokeMethod('shareToSession', param);
  }

  static Future shareToTimeline({String title, String description, String imgPath, String imgUrl}) async {
    Map param = {"title" : title, "description" : description, "imgPath": imgPath, "imgUrl" : imgUrl};
    return await _channel.invokeMethod('shareToTimeline', param);
  }
}

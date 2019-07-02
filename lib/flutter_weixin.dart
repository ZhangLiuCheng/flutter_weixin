import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class FlutterWeixin {
    static const MethodChannel _channel =
    const MethodChannel('flutter_weixin');

    static Future<String> get platformVersion async {
        final String version = await _channel.invokeMethod(
            'getPlatformVersion');
        return version;
    }

    static Future init(wxAppId) async {
        return await _channel.invokeMethod("init", {"wxAppId": wxAppId});
    }

    static Future shareToSession({String title, String description, String imgPath, String imgUrl, Uint8List imgData,
            String webUrl, String webImgUrl, String webImgPath}) async {
        Map param = {
            "title": title,
            "description": description,
            "imgPath": imgPath,
            "imgUrl": imgUrl,
            "imgData": imgData,
            "webUrl": webUrl,
            "webImgUrl": webImgUrl,
            "webImgPath": webImgPath
        };
        return await _channel.invokeMethod('shareToSession', param);
    }

    static Future shareToTimeline({String title, String description, String imgPath, String imgUrl, Uint8List imgData,
            String webUrl, String webImgUrl, String webImgPath}) async {
        Map param = {
            "title": title,
            "description": description,
            "imgPath": imgPath,
            "imgUrl": imgUrl,
            "imgData": imgData,
            "webUrl": webUrl,
            "webImgUrl": webImgUrl,
            "webImgPath": webImgPath
        };
        return await _channel.invokeMethod('shareToTimeline', param);
    }
}

import 'dart:async';

import 'package:flutter/services.dart';

class FlutterWeixin {
  static const MethodChannel _channel =
      const MethodChannel('flutter_weixin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}

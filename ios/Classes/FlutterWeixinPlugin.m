#import "FlutterWeixinPlugin.h"
#import "WXApi.h"

@interface FlutterWeixinPlugin()<WXApiDelegate>

@end

@implementation FlutterWeixinPlugin

static FlutterWeixinPlugin *_instance = nil;

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_weixin"
            binaryMessenger:[registrar messenger]];
  FlutterWeixinPlugin* instance = [[FlutterWeixinPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

+ (BOOL) handleOpenURL:(NSURL *)url {
    return [WXApi handleOpenURL:url delegate:_instance];
}

- (void)onReq:(BaseReq*)req {
    NSLog(@"onReq =========  %@", req);
}

- (void)onResp:(BaseResp*)resp {
    NSLog(@"onResp =========  %@", resp);
}

- (instancetype)init {
    NSLog(@"-------FlutterWeixinPlugin.h------");
    self = [super init];
    if (self) {
        _instance = self;
    }
    return self;
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else if ([@"init" isEqualToString:call.method]) {
      NSString *appId = [[call arguments] objectForKey:@"wxAppId"];
      [self regToWx:appId];
  } else if ([@"shareToSession" isEqualToString:call.method]) {
      [self shareToSession:[call arguments]];
  } else if ([@"shareToTimeline" isEqualToString:call.method]) {
      [self shareToTimeline: [call arguments]];
  } else {
    result(FlutterMethodNotImplemented);
  }
}

- (void)regToWx:(NSString *)appId {
    bool result = [WXApi registerApp:appId];
    NSLog(@"微信注册结果 %d", result);
}

- (void)shareToSession:(NSDictionary *)params {
    NSLog(@"shareToSession %@", params);
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = YES;
    req.text = [params objectForKey:@"title"];
    req.scene = WXSceneSession;
    bool result = [WXApi sendReq:req];
    NSLog(@"微信分享结果 %d", result);
}

- (void)shareToTimeline:(NSDictionary *)params {
    NSLog(@"shareToTimeline %@", params);
}

@end

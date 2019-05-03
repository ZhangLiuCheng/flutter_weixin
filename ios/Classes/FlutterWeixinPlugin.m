#import "FlutterWeixinPlugin.h"
#import "WXApi.h"

@interface FlutterWeixinPlugin()<WXApiDelegate>

@property(copy, nonatomic) FlutterResult flutterResult;

@end

@implementation FlutterWeixinPlugin

static FlutterWeixinPlugin *_instance = nil;
static bool isInited;

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_weixin"
            binaryMessenger:[registrar messenger]];
  FlutterWeixinPlugin* instance = [[FlutterWeixinPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

+ (BOOL) handleOpenURL:(NSURL *)url {
//    NSLog(@"handleOpenURL =========  %@", url);
    return [WXApi handleOpenURL:url delegate:_instance];
}

- (void)onReq:(BaseReq*)req {
}

- (void)onResp:(BaseResp*)resp {
    NSLog(@"onResp =========  %@ -- %d", resp, resp.errCode);
    if (self.flutterResult == nil) return;
    if (resp.errCode == WXSuccess) {
        self.flutterResult(@"微信分享成功");
    } else {
        self.flutterResult([FlutterError errorWithCode:@"-1" message:@"微信分享失败" details:resp.errStr]);
    }
    self.flutterResult = nil;
}

- (instancetype)init {
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
      [self regToWxWithResult:result appId:appId];
  } else if ([@"shareToSession" isEqualToString:call.method]) {
      [self shareToSession:result params:[call arguments]];
  } else if ([@"shareToTimeline" isEqualToString:call.method]) {
      [self shareToTimeline:result params:[call arguments]];
  } else {
    result(FlutterMethodNotImplemented);
  }
}

- (void)regToWxWithResult:(FlutterResult)result appId:(NSString *)appId {
    isInited = [WXApi registerApp:appId];
    NSLog(@"微信注册结果 %d", isInited);
    if (isInited) {
        result(@"微信注册成功");
    } else {
        result([FlutterError errorWithCode:@"-1" message:@"微信注册失败" details:@"wx.registerApp失败"]);
    }
}

- (void)shareToSession:(FlutterResult)result params:(NSDictionary *)params {
    [self share:result scene:WXSceneSession params:params];
}

- (void)shareToTimeline:(FlutterResult)result params:(NSDictionary *)params {
    [self share:result scene:WXSceneTimeline params:params];
}

- (void)share:(FlutterResult)result scene:(int)scene params:(NSDictionary *)params {
    if (!isInited) {
        result([FlutterError errorWithCode:@"-1" message:@"微信分享失败" details:@"FlutterWeixin没有初始化"]);
        return;
    }
    self.flutterResult = result;
    NSLog(@"shareToSession %@", params);
    NSString* webUrl = [params objectForKey:@"webUrl"];
    NSString* imgUrl = [params objectForKey:@"imgUrl"];
    NSString* imgPath = [params objectForKey:@"imgPath"];
    if ([self isNotEmpty:webUrl]) {
        [self sharePage: result scene:scene webUrl:webUrl title:[params objectForKey:@"title"] description:[params objectForKey:@"description"] webImgUrl:[params objectForKey:@"webImgUrl"] webImgPath:[params objectForKey:@"webImgPath"]];
    } else if ([self isNotEmpty:imgUrl] || [self isNotEmpty:imgPath]) {
        [self shareImage:result scene:scene imgUrl:imgUrl imgPath:imgPath];
    } else {
        [self shareText:result scene:scene title:[params objectForKey:@"title"] description:[params objectForKey:@"description"]];
    }
}

- (void)shareText:(FlutterResult)result scene:(int)scene title:(NSString *)title description:(NSString *)description {
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = YES;
    req.text = scene == WXSceneSession ? description : title;
    req.scene = scene;
    bool reqResult = [WXApi sendReq:req];
    if (reqResult != true) {
        result([FlutterError errorWithCode:@"-1" message:@"微信分享失败" details:@"sendReq为false"]);
    }
}

- (void)shareImage:(FlutterResult)result scene:(int)scene imgUrl:(NSString *)imgUrl imgPath:(NSString *)imgPath {
    WXImageObject *imageObject = [WXImageObject object];
    NSError *error = nil;
    NSData *imageData = nil;
    if ([self isNotEmpty:imgUrl]) {
       imageData = [NSData dataWithContentsOfURL:[NSURL URLWithString:imgUrl] options:NSDataReadingMappedIfSafe error:&error];
    } else if ([self isNotEmpty:imgPath]) {
        imageData = [NSData dataWithContentsOfFile:imgPath options:NSDataReadingMappedIfSafe error:&error];
    } else {
        result([FlutterError errorWithCode:@"-1" message:@"微信分享失败" details:@"获取图片失败"]);
        return;
    }
    if ([error code] == 0) {
        imageObject.imageData = imageData;
    } else {
        result([FlutterError errorWithCode:@"-1" message:@"微信分享失败" details:error.description]);
        return;
    }
    WXMediaMessage *message = [WXMediaMessage message];
    message.mediaObject = imageObject;
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = NO;
    req.message = message;
    req.scene = scene;
    bool reqResult = [WXApi sendReq:req];
    if (reqResult != true) {
        result([FlutterError errorWithCode:@"-1" message:@"微信分享失败" details:@"sendReq为false"]);
    }
}

- (void)sharePage:(FlutterResult)result scene:(int)scene webUrl:(NSString *)webUrl title:(NSString *)title description:(NSString *)description webImgUrl:(NSString *)webImgUrl webImgPath:(NSString *)webImgPath {
    WXWebpageObject *webpageObject = [WXWebpageObject object];
    webpageObject.webpageUrl = webUrl;
    WXMediaMessage *message = [WXMediaMessage message];
    message.title = title;
    message.description = description;
    
    NSError *error = nil;
    NSData *imageData = nil;
    if ([self isNotEmpty:webImgUrl]) {
        imageData = [NSData dataWithContentsOfURL:[NSURL URLWithString:webImgUrl] options:NSDataReadingMappedIfSafe error:&error];
    } else if ([self isNotEmpty:webImgPath]) {
        imageData = [NSData dataWithContentsOfFile:webImgPath options:NSDataReadingMappedIfSafe error:&error];
    }
    if (imageData != nil) {
        message.thumbData = imageData;
    }
    message.mediaObject = webpageObject;
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = NO;
    req.message = message;
    req.scene = WXSceneSession;
    bool reqResult = [WXApi sendReq:req];
    if (reqResult != true) {
        result([FlutterError errorWithCode:@"-1" message:@"微信分享失败" details:@"sendReq为false"]);
    }
}

- (BOOL)isNotEmpty:(NSString *)str {
    return (str != nil && ![str isKindOfClass:[NSNull class]] && str.length > 0);
}
@end

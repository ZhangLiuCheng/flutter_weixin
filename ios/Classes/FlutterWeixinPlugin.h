#import <Flutter/Flutter.h>

@interface FlutterWeixinPlugin : NSObject<FlutterPlugin>

+ (BOOL) handleOpenURL:(NSURL *)url;

@end

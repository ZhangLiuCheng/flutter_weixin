package com.boiling.point.flutter_weixin;

import android.app.Activity;
import android.util.Log;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class FlutterWeixinPlugin implements MethodCallHandler {

    public static String APP_ID = "wx0a5d51592ca9e6dd";

    private static Activity sActivity;
    private static Result sResult;

    private static IWXAPI sApi;

    private boolean isInited = false;

    public static void processShareResult(int errCode) {
        try {
            if (errCode == 0) {
                sResult.success("微信分享成功");
            } else {
                sResult.error("微信分享失败", "-1", "错误码" + errCode);
            }
        } catch (Exception ex) {
            sResult.error("微信分享异常", "-1", ex.toString());
        }
    }

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_weixin");
        channel.setMethodCallHandler(new FlutterWeixinPlugin());
        sActivity = registrar.activity();
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("init")) {
            APP_ID = call.argument("wxAppId");
            this.regToWx(result);
        } else if (call.method.equals("shareToSession")) {
            this.shareToSession(result);
        }  else if (call.method.equals("shareToTimeline")) {
            this.shareTimeline(result);
        } else {
            result.notImplemented();
        }
    }

    private void regToWx(Result result) {
        try {
            isInited = true;
            sApi = WXAPIFactory.createWXAPI(sActivity, APP_ID, true);
            boolean re =  sApi.registerApp(APP_ID);
            result.success("微信注册成功");
            Log.e("TAG", "微信初始化结果:" + re);
        } catch (Exception ex) {
            result.error("微信注册失败", "-1", ex.toString());
            isInited = false;
        }
    }

    private void shareToSession(Result result) {
        this.share(result, SendMessageToWX.Req.WXSceneSession);
    }

    private void shareTimeline(Result result) {
        this.share(result, SendMessageToWX.Req.WXSceneTimeline);
    }

    private void share(Result result, int scene) {
        if (!isInited) {
            result.error("微信分享失败", "-1", "FlutterWeixin没有初始化");
            return;
        }
        FlutterWeixinPlugin.sResult = result;
        WXTextObject textObj = new WXTextObject();
        textObj.text = "title1";

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.title = "Will be ignored";
        msg.description = "i am description";

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "buildTransaction ----  text";
        req.message = msg;
        req.scene = scene;
        boolean reqResult = sApi.sendReq(req);
//        Log.e("TAG", "微信分享结果:" + reqResult);

//        Bitmap bmp = BitmapFactory.decodeResource(sActivity.getResources(), R.drawable.send_img);
//
//        WXImageObject imgObj = new WXImageObject(bmp);
//        WXMediaMessage msg = new WXMediaMessage();
//        msg.mediaObject = imgObj;
//
//        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
//        bmp.recycle();
//        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("img");
//        req.message = msg;
//        req.scene = mTargetScene;
//        req.userOpenId = getOpenId();
//        sApi.sendReq(req);
    }
}

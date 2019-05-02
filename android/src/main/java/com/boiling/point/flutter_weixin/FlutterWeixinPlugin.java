package com.boiling.point.flutter_weixin;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

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
//            Map<String, String> parmas = call.arguments();
            this.shareToSession(result, (Map<String, String>)call.arguments());
        }  else if (call.method.equals("shareToTimeline")) {
            this.shareToTimeline(result, (Map<String, String>)call.arguments());
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

    private void shareToSession(Result result, Map<String, String> params) {
        this.share(result, SendMessageToWX.Req.WXSceneSession, params);
    }

    private void shareToTimeline(Result result, Map<String, String> params) {
        this.share(result, SendMessageToWX.Req.WXSceneTimeline, params);
    }

    private void share(final Result result, final int scene, final Map<String, String> params) {
        Log.e("TAG", "========>" + params);
        if (!isInited) {
            result.error("微信分享失败", "-1", "FlutterWeixin没有初始化");
            return;
        }
        FlutterWeixinPlugin.sResult = result;

        final String imgUrl = params.get("imgUrl");
        final String imgPath = params.get("imgPath");

        if (TextUtils.isEmpty(imgUrl) && TextUtils.isEmpty(imgPath)) {
            WXTextObject textObj = new WXTextObject();
            textObj.text = params.get("title");

            final WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = textObj;
            msg.description = params.get("description");

            final SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.message = msg;
            req.scene = scene;
            sApi.sendReq(req);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    try {
//                        Log.e("TAG", "微信分享 ----------1111111");
//
//                        WXImageObject imgObj = new WXImageObject();
//                        final WXMediaMessage msg = new WXMediaMessage();
//                        msg.mediaObject = imgObj;
//
//                        final SendMessageToWX.Req req = new SendMessageToWX.Req();
//                        req.message = msg;
//                        req.scene = scene;
//
//                        Bitmap thumb;
//                        if (!TextUtils.isEmpty(imgUrl)) {
//                            thumb =  BitmapFactory.decodeStream(new URL(imgUrl).openStream());
//                        } else if (!TextUtils.isEmpty(imgPath)) {
//                            thumb =  BitmapFactory.decodeStream(new FileInputStream(imgUrl));
//                        } else {
//                            return;
//                        }
//                        Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, 150, 150, true);
//                        thumb.recycle();
//                        msg.thumbData = bmpToByteArray(thumbBmp, true);
//                        Log.e("TAG", "微信分享 ----------2222222 " + msg.thumbData);
//
//                        boolean reqResult = sApi.sendReq(req);
//                        Log.e("TAG", "微信分享结果:" + reqResult);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        result.error("微信分享失败", "-1", e.toString());
//                    }

                    try {
                        WXImageObject imgObj = new WXImageObject();
                        final WXMediaMessage msg = new WXMediaMessage();
                        msg.mediaObject = imgObj;

                        final SendMessageToWX.Req req = new SendMessageToWX.Req();
                        req.message = msg;
                        req.scene = scene;

                        Bitmap thumb;
                        if (!TextUtils.isEmpty(imgUrl)) {
                            thumb =  BitmapFactory.decodeStream(new URL(imgUrl).openStream());
                        } else if (!TextUtils.isEmpty(imgPath)) {
                            thumb =  BitmapFactory.decodeStream(new FileInputStream(imgUrl));
                        } else {
                            return;
                        }
//                        Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, 150, 150, true);
//                        thumb.recycle();
//                        imgObj.imageData = bmpToByteArray(thumbBmp, true);

                        imgObj.imageData = compressImage(thumb, 1024);

                        boolean reqResult = sApi.sendReq(req);
                        if (reqResult != true) {
                            result.error("微信分享失败", "-1", "sendReq为false");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        result.error("微信分享失败", "-1", e.toString());
                    }
                }
            }).start();
        }
    }

    private byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 10, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        output.close();
        return result;
    }

    public static byte[] compressImage(Bitmap image, int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 90;
        while (baos.toByteArray().length / 1024 > size) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
//        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
//        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
//        return bitmap;
        return baos.toByteArray();
    }
}

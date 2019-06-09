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
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
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

    public static String APP_ID;

    private static Activity sActivity;
    private static Result sResult;

    private static IWXAPI sApi;

    private boolean isInited = false;

    public static void processShareResult(int errCode, String errStr) {
        if (sResult == null) return;
        if (errCode == 0) {
            sResult.success("微信分享成功");
        } else {
            sResult.error("微信分享失败", errCode + "", errStr);
        }
        sResult = null;
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
            this.shareToSession(result, (Map<String, Object>) call.arguments());
        } else if (call.method.equals("shareToTimeline")) {
            this.shareToTimeline(result, (Map<String, Object>) call.arguments());
        } else {
            result.notImplemented();
        }
    }

    private void regToWx(Result result) {
        sApi = WXAPIFactory.createWXAPI(sActivity, APP_ID, true);
        isInited = sApi.registerApp(APP_ID);
        Log.e("TAG", "微信初始化结果:" + isInited);
        if (isInited) {
            result.success("微信注册成功");
        } else {
            result.error("微信注册失败", "-1", "wx.registerApp失败");
        }
    }

    private void shareToSession(Result result, Map<String, Object> params) {
        this.share(result, SendMessageToWX.Req.WXSceneSession, params);
    }

    private void shareToTimeline(Result result, Map<String, Object> params) {
        this.share(result, SendMessageToWX.Req.WXSceneTimeline, params);
    }

    private void share(final Result result, final int scene, final Map<String, Object> params) {
        Log.e("TAG", "========>" + params);
        if (!isInited) {
            result.error("微信分享失败", "-1", "FlutterWeixin没有初始化");
            return;
        }
        FlutterWeixinPlugin.sResult = result;
        String title = (String)params.get("title");
        String description = (String)params.get("description");
        String webImgUrl = (String)params.get("webImgUrl");
        String webImgPath = (String)params.get("webImgPath");

        String webUrl = (String) params.get("webUrl");
        String imgUrl = (String) params.get("imgUrl");
        String imgPath = (String) params.get("imgPath");
        byte[] imgData = null;
        if (params.containsKey("imgData")) {
            imgData = (byte[]) params.get("imgData");
        }

        if (!TextUtils.isEmpty(webUrl)) {
            sharePage(result, scene, webUrl, title, description, webImgUrl, webImgPath);
        } else if (!TextUtils.isEmpty(imgUrl) || !TextUtils.isEmpty(imgPath) || null != imgData) {
            shareImage(result, scene, imgUrl, imgPath, imgData);
        } else {
            shareText(result, scene, title, description);
        }
    }

    private void shareText(Result result, int scene, String title, String description) {
        WXTextObject textObj = new WXTextObject();
        textObj.text = title;

        final WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = description;

        final SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = msg;
        req.scene = scene;
        boolean reqResult = sApi.sendReq(req);
        if (reqResult != true) {
            result.error("微信分享失败", "-1", "sendReq为false");
        }
    }

    private void shareImage(final Result result, final int scene, final String imgUrl,
                            final String imgPath, final byte[] imgData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WXImageObject imgObj = new WXImageObject();
                    final WXMediaMessage msg = new WXMediaMessage();
                    msg.mediaObject = imgObj;

                    final SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.message = msg;
                    req.scene = scene;

                    Bitmap thumb;
                    if (!TextUtils.isEmpty(imgUrl)) {
                        thumb = BitmapFactory.decodeStream(new URL(imgUrl).openStream());
                        imgObj.imageData = compressImage(thumb, 1024, true);
                    } else if (!TextUtils.isEmpty(imgPath)) {
                        thumb = BitmapFactory.decodeStream(new FileInputStream(imgUrl));
                        imgObj.imageData = compressImage(thumb, 1024, true);
                    } else if (null != imgData) {
                        imgObj.imageData = imgData;
                    } else {
                        result.error("微信分享失败", "-1", "图片不能为空");
                        return;
                    }
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

    private void sharePage(final Result result, final int scene, final String webUrl, final String title,
                           final String description, final String webImgUrl, final String webImgPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WXWebpageObject webpage = new WXWebpageObject();
                    webpage.webpageUrl = webUrl;
                    final WXMediaMessage msg = new WXMediaMessage();
                    msg.title = title;
                    msg.description = description;
                    msg.mediaObject = webpage;
                    final SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.message = msg;
                    req.scene = scene;

                    Bitmap thumb;
                    if (!TextUtils.isEmpty(webImgUrl)) {
                        thumb = BitmapFactory.decodeStream(new URL(webImgUrl).openStream());
                    } else if (!TextUtils.isEmpty(webImgPath)) {
                        thumb = BitmapFactory.decodeStream(new FileInputStream(webImgPath));
                    } else {
                        return;
                    }
                    Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, 150, 150, true);
                    thumb.recycle();
                    msg.thumbData = bmpToByteArray(thumbBmp, true);
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

    public static byte[] compressImage(Bitmap image, int size,  boolean needRecycle) {
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
        if (needRecycle) {
            image.recycle();
        }
        return baos.toByteArray();
    }
}

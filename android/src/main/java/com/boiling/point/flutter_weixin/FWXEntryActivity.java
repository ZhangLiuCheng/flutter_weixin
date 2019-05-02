package com.boiling.point.flutter_weixin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.boiling.point.flutter_weixin.FlutterWeixinPlugin;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class FWXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, FlutterWeixinPlugin.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        api.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp baseResp) {
        /*
        Log.e("TAG", "FWXEntryActivity --------- onResp  " + baseResp.errCode + " ==  " + baseResp.getType() + " == " + baseResp.transaction);
        finish();
        if (baseResp.errCode == BaseResp.ErrCode.ERR_OK) {
            Log.e("TAG", "FWXEntryActivity --------- onResp  成功");
        } else {
            Log.e("TAG", "FWXEntryActivity --------- onResp  失败");
        }
        */
        finish();
        FlutterWeixinPlugin.processShareResult(baseResp.errCode);
    }
}

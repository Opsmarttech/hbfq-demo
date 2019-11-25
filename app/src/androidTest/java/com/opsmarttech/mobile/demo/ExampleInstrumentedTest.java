package com.opsmarttech.mobile.demo;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.opsmarttech.mobile.api.core.HbfqTradePayPreCreate;
import com.opsmarttech.mobile.api.core.constant.Constants;
import com.opsmarttech.mobile.api.core.http.DefaultHbfqApi;
import com.opsmarttech.mobile.api.core.http.TradeParam;
import com.opsmarttech.mobile.service.Hbfq;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.opsmarttech.mobile.demo", appContext.getPackageName());
    }

    /**
     * 订单预创建
     * 接口返回二维码链接，由POS机生成后提供买家扫码
     * 设备已绑定
     */
    @Test
    public void testPosDevPreCreate() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, Context.MODE_PRIVATE).edit().putString(Constants.PRE_CREATE_ROUTE, "https://136.25.18.11").commit();
        context.getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, Context.MODE_PRIVATE).edit().putString(Constants.DEVICE_MEID, "A000009B09D368"/**已绑定的设备编号*/).commit();
        TradeParam tradeParam = new TradeParam();
        tradeParam.put("totalMount", "100");/**交易金额*/
        tradeParam.put("hbfqSellerPercent", "100");/**付息方式：100商家贴息，0买家按期付息*/
        tradeParam.put("hbfqPhaseNum", "12");/**分期数*/
        String qrUrl = "";
        try {
            Hbfq.preCreateToPay(context, tradeParam).getString("qr_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals(true, !"".equals(qrUrl));
    }

    /**
     * 扫码支付
     * POS机扫取买家手机二位码，提供参数传递给接口，完成交易
     * 设备已绑定
     */
    @Test
    public void testPosDevScan2() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, Context.MODE_PRIVATE).edit().putString(Constants.PRE_CREATE_ROUTE, "https://136.25.18.11").commit();
        context.getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, Context.MODE_PRIVATE).edit().putString(Constants.DEVICE_MEID, "A000009B09D368").commit();
        TradeParam tradeParam = new TradeParam();
        tradeParam.put("totalMount", "0.2");
        tradeParam.put("hbfqSellerPercent", "100");
        tradeParam.put("hbfqPhaseNum", "12");
        tradeParam.put("authCode", "285476910192082196");/**authCode 为买家手机付款码数字，买家被扫模式下该参数必选*/
        JSONObject jsonObject = Hbfq.scanToPay(context, tradeParam);
        String code = "";
        try {
            code = jsonObject.getString("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals(true, "10003".equals(code));
    }

    @Test
    public void testPosDevPreCreate2() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, Context.MODE_PRIVATE).edit().putString(Constants.PRE_CREATE_ROUTE, "https://136.25.18.11").commit();
        context.getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, Context.MODE_PRIVATE).edit().putString(Constants.DEVICE_MEID, "666554423"/**未绑定的设备*/).commit();
        TradeParam tradeParam = new TradeParam();
        tradeParam.put("totalMount", "100");
        tradeParam.put("hbfqSellerPercent", "100");
        tradeParam.put("hbfqPhaseNum", "12");
        JSONObject jsonObject = new DefaultHbfqApi(context).doPay(tradeParam, HbfqTradePayPreCreate.class.getName());
        String resp = "";
        try {
            resp = Hbfq.preCreateToPay(context, tradeParam).getString("qr_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals(true, "".equals(resp));/**未绑定的设备，调用接口返回二维码链接为空串*/
    }


    @Test
    public void testPosDevScan() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, Context.MODE_PRIVATE).edit().putString(Constants.PRE_CREATE_ROUTE, "https://136.25.18.11").commit();
        context.getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, Context.MODE_PRIVATE).edit().putString(Constants.DEVICE_MEID, "666554423").commit();
        TradeParam tradeParam = new TradeParam();
        tradeParam.put("totalMount", "0.1");
        tradeParam.put("hbfqSellerPercent", "100");
        tradeParam.put("hbfqPhaseNum", "12");
        tradeParam.put("authCode", "285476910192082196");
        JSONObject jsonObject = Hbfq.scanToPay(context, tradeParam);
        String resp = "";
        try {
            resp = jsonObject.getString("status");/**未绑定的设备，调用接口返回device unbind提示*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals(true, "device unbind".equals(resp));
    }

    @Test
    public void testQuery() {
        JSONObject jsonObject = Hbfq.query("5510b6db_0326_4e2b_b009_fe5e81516720", "2088901200045560");
        String tradeResult = "";
        try {
            JSONObject queryJson = jsonObject.getJSONObject("alipay_trade_query_response");
            tradeResult = queryJson.getString("trade_status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals(true, "TRADE_SUCCESS".equals(tradeResult));
    }

}

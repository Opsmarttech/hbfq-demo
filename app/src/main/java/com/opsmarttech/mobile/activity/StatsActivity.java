package com.opsmarttech.mobile.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.opsmarttech.mobile.MyApplication;
import com.opsmarttech.mobile.R;
import com.opsmarttech.mobile.api.core.constant.Constants;
import com.opsmarttech.mobile.api.core.http.TradeParam;
import com.opsmarttech.mobile.api.core.utils.QRCodeUtil;
import com.opsmarttech.mobile.service.Hbfq;
import com.opsmarttech.mobile.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class StatsActivity extends AppCompatActivity {

    private static final String TAG = StatsActivity.class.getName();

    private static final int START_SCAN = 0x131;
    private static final int SCAN_TOPAY = 0x11;
    private static final int QR_TOPAY = 0x12;

    private String mTotalAmount;
    private String mTradeType;
    private String mPayType;
    private int mHbfqSellerPercent;
    private int mInsNum;
    private float mPerInSer;
    private float mPerInsVal;

    private TextView mMainHint;
    private TextView mTotalAmountTxv;
    private TextView mOrderNoTxv;
    private TextView mPerValTxv;
    private TextView mInsNumTxv;
    private ImageView mSateImgv;
    private ProgressBar mResultWaitingProv;
    private TextView mBuyerLogonIdTxv;
    private TextView mBuyerUserIdTxv;
    private ImageView qrCodeImgv;
    private View qrCodeConv;
    private View stateImgConv;
    private View mCloseBtnv;

    private SharedPreferences mSharedPreferences;
    private UIHandler mUIHandler;
    private Timer mTimer;
    private long queryStartTimestamp = 0l;
    private String mAutoCode;
    private int currentPayMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    public void initView() {
        setContentView(R.layout.activity_stats);
        mMainHint = findViewById(R.id.stats_main_hint);
        mResultWaitingProv = findViewById(R.id.result_waiting);
        mTotalAmountTxv = findViewById(R.id.state_payfor);
        mOrderNoTxv = findViewById(R.id.state_res_order);
        mPerValTxv = findViewById(R.id.state_per_serv);
        mInsNumTxv = findViewById(R.id.state_ins_num);
        mSateImgv = findViewById(R.id.state_img);
        mBuyerLogonIdTxv = findViewById(R.id.state_user_account);
        mBuyerUserIdTxv = findViewById(R.id.state_user_id);
        qrCodeConv = findViewById(R.id.qr_code_con);
        qrCodeImgv = findViewById(R.id.qr_code);
        stateImgConv = findViewById(R.id.stats_img_con);
        mCloseBtnv = findViewById(R.id.close_btn);
    }

    public void initData() {

        mSharedPreferences = getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE);
        mUIHandler = new UIHandler();

        try {
            mTotalAmount = getIntent().getExtras().getString("totalPay");
            mTradeType = getIntent().getExtras().getString("tradType");
            mPayType = getIntent().getExtras().getString("payType");
            mPerInSer = getIntent().getExtras().getFloat("perInsSer");
            mPerInsVal = getIntent().getExtras().getFloat("perInsVal");
            mHbfqSellerPercent = getIntent().getExtras().getBoolean("sellerPercent") ? 100 : 0;
            mInsNum = getIntent().getExtras().getInt("insNum");
            mMainHint.setText(R.string.state_hint_wait_user_pay);
            JSONObject merChant = new JSONObject(mSharedPreferences.getString(mTradeType, ""));
            mTotalAmountTxv.setText(getResources().getString(R.string.state_payfor, mTotalAmount));
            mPerValTxv.setText(getResources().getString(R.string.state_per_ser, mPerInsVal, mPerInSer));
            mInsNumTxv.setText(getResources().getString(R.string.state_ins_num, mInsNum));
            mCloseBtnv.setOnClickListener(mVClk);

            if(Constants.TRADE_TYPE_HB.equals(mTradeType)) {
                //mHbfqSellerPercent = merChant.getInt("hbfqSellerPercent");
                if("scan".equals(mPayType)) {
                    Intent intent = new Intent("com.summi.scan");
                    intent.setPackage("com.sunmi.sunmiqrcodescanner");
                    intent.putExtra("CURRENT_PPI", 0X0003);
                    intent.putExtra("PLAY_SOUND", true);
                    intent.putExtra("PLAY_VIBRATE", false);
                    intent.putExtra("IDENTIFY_INVERSE_QR_CODE", true);// 识别反色二维码，默认true
                    intent.putExtra("IDENTIFY_MORE_CODE", false);// 识别画面中多个二维码，默认false
                    intent.putExtra("IS_SHOW_SETTING", true);// 是否显示右上角设置按钮，默认true
                    intent.putExtra("IS_SHOW_ALBUM", true);// 是否显示从相册选择图片按钮，默认true
                    startActivityForResult(intent, START_SCAN);
                } else if("qrCode".equals(mPayType)) {
                    stateImgConv.setVisibility(View.GONE);
                    qrCodeConv.setVisibility(View.VISIBLE);
                    mMainHint.setText(R.string.state_hint_wait_qr_loading);
                    new Thread(() -> {
                            TradeParam tradeParam = new TradeParam();
                            tradeParam.put("totalMount", mTotalAmount);
                            tradeParam.put("hbfqSellerPercent", String.valueOf(mHbfqSellerPercent));
                            tradeParam.put("hbfqPhaseNum", String.valueOf(mInsNum));
                            tradeParam.put("merchantType", Constants.TRADE_TYPE_HB);
                            try {
                                JSONObject jsonObject = Hbfq.preCreateToPay(MyApplication.getContext(), tradeParam);
                                Message msg = mUIHandler.obtainMessage();
                                msg.what = 0x1;
                                msg.obj = jsonObject;
                                msg.sendToTarget();
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                                e.printStackTrace();
                            }
                        }).start();
                }
            } else if(Constants.TRADE_TYPE_LBF.equals(mTradeType)) {
                if("swape".equals(mPayType)) {

                } else if("qrCode".equals(mPayType)) {
                    stateImgConv.setVisibility(View.GONE);
                    qrCodeConv.setVisibility(View.VISIBLE);

                    mTotalAmountTxv.setGravity(Gravity.CENTER);
                    mTotalAmountTxv.setText(R.string.swape_hint_a);
                    mPerValTxv.setGravity(Gravity.CENTER);
                    mPerValTxv.setTextSize(16.0f);
                    mPerValTxv.setText(R.string.swape_hint_b);
                    mInsNumTxv.setVisibility(View.GONE);

                    mMainHint.setText(R.string.state_hint_wait_qr_loading);
                    new Thread(() -> {
                        try {
                            TradeParam tradeParam = new TradeParam();
                            tradeParam.put("totalAmount", mTotalAmount);
                            tradeParam.put("insNum", String.valueOf(mInsNum));
                            tradeParam.put("lbfStoreId", merChant.getString("merId"));
                            JSONObject jsonObject = Hbfq.formToPay(MyApplication.getContext(), tradeParam);
                            Message msg = mUIHandler.obtainMessage();
                            msg.what = 0x2;
                            msg.obj = jsonObject;
                            msg.sendToTarget();
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                            e.printStackTrace();
                        }
                    }).start();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, e.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if(Constants.TRADE_TYPE_HB.equals(mTradeType) && "scan".equals(mPayType) && TextUtils.isEmpty(mAutoCode)) finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == START_SCAN && data != null) {
            String autoCode = "";
            Bundle bundle = data.getExtras();
            ArrayList result = (ArrayList) bundle .getSerializable("data");
            Iterator it = result.iterator();
            while (it.hasNext()) {
                HashMap hashMap = (HashMap) it.next();
                Log.i("扫码的类型", hashMap.get("TYPE").toString());
                Log.i("扫码的结果", hashMap.get("VALUE").toString());
                autoCode = hashMap.get("VALUE").toString();
            }

            final String _authCode = autoCode;
            mAutoCode = _authCode;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject merchant = new JSONObject(mSharedPreferences.getString(mTradeType, ""));
                        TradeParam tradeParam = new TradeParam();
                        tradeParam.put("totalMount", mTotalAmount);
                        tradeParam.put("hbfqSellerPercent", String.valueOf(mHbfqSellerPercent));
                        tradeParam.put("hbfqPhaseNum", String.valueOf(mInsNum));
                        tradeParam.put("authCode", _authCode);/**authCode 为买家手机付款码数字，买家被扫模式下该参数必选*/
                        tradeParam.put("merchantType", mTradeType);
                        JSONObject jsonObject = Hbfq.scanToPay(MyApplication.getContext(), tradeParam);
                        Message message = mUIHandler.obtainMessage();
                        message.what = 0x5;
                        message.obj = jsonObject;
                        message.sendToTarget();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            JSONObject jsonObject = (JSONObject) msg.obj;

            switch (msg.what) {
                case 0x1:
                    String qrUrl = "";
                    String outTradeCode = "";

                    try {
                        qrUrl = jsonObject.getString("qr_url");
                        outTradeCode = jsonObject.getJSONObject("raw").getString("out_trade_no");
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }

                    if(TextUtils.isEmpty(qrUrl)) return;
                    Bitmap qrBitmap = QRCodeUtil.createQRCodeBitmap(qrUrl, 200, 200);
                    qrCodeImgv.setImageBitmap(qrBitmap);
                    qrCodeImgv.setVisibility(View.VISIBLE);

                    if(mTimer != null) {
                        mTimer.purge();
                        mTimer.cancel();
                        mTimer = null;
                    }

                    mTimer = new Timer();
                    mTimer.schedule(new StatsActivity.MyTask(outTradeCode), 0, 2000);
                    queryStartTimestamp = new Date().getTime();
                    mOrderNoTxv.setVisibility(View.VISIBLE);
                    mOrderNoTxv.setText(getResources().getString(R.string.state_res_order, outTradeCode));
                    mMainHint.setText(R.string.state_hint_scan_qr);
                    break;
                case 0x2:
                    String formUrl = "";

                    try {
                        formUrl = jsonObject.getString("fromUrl");
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }

                    if(TextUtils.isEmpty(formUrl)) return;
                    Bitmap formBitmap = QRCodeUtil.createQRCodeBitmap(formUrl, 200, 200);
                    qrCodeImgv.setImageBitmap(formBitmap);
                    qrCodeImgv.setVisibility(View.VISIBLE);

                    queryStartTimestamp = new Date().getTime();
                    mMainHint.setText(R.string.state_hint_scan_qr_2);
                    break;
                case 0x3:
                    Log.i(TAG, "------------------- execute query ---------------------");
                    JSONObject jsonObj = (JSONObject) msg.obj;
                    String tradeResult = "WAIT_BUYER_PAY";
                    String totalAmount = "0.00";
                    JSONObject tradeQueryResp = null;
                    try {
                        tradeQueryResp = jsonObj.getJSONObject("alipay_trade_query_response");
                        String code = tradeQueryResp.getString("code");
                        if(tradeQueryResp.has("total_amount")) totalAmount = tradeQueryResp.getString("total_amount");
                        if(Integer.valueOf(code) == 10000) {
                            tradeResult = tradeQueryResp.getString("trade_status");
                        } else {
                            tradeResult = tradeQueryResp.getString("sub_code");
                        }
                        Log.i(TAG, "--------------------------[" + tradeResult + "]----------------------------");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Message message = null;
                    switch(tradeResult) {

                        case "ACQ.TRADE_NOT_EXIST":
                            long currentTimestamp = new Date().getTime();
                            mMainHint.setText(R.string.state_hint_scan_qr);

                            if(currentTimestamp - queryStartTimestamp > 60000) {
                                mMainHint.setText(R.string.state_hint_failed);
                                mResultWaitingProv.setVisibility(View.INVISIBLE);
                                qrCodeConv.setVisibility(View.GONE);
                                stateImgConv.setVisibility(View.VISIBLE);
                                mSateImgv.setVisibility(View.VISIBLE);
                                mSateImgv.setImageResource(R.drawable.w);
                                message = mUIHandler.obtainMessage();
                                message.what = 0x4;
                                message.sendToTarget();
                            }

                            break;
                        case "WAIT_BUYER_PAY":
                            mMainHint.setText(R.string.state_hint_wait_user_pay);
                            break;
                        case "TRADE_FINISHED":
                            mMainHint.setText("交易已结束，不可退款");
                            mResultWaitingProv.setVisibility(View.INVISIBLE);
                            mSateImgv.setVisibility(View.VISIBLE);
                            mSateImgv.setImageResource(R.drawable.rt);
                            message = mUIHandler.obtainMessage();
                            message.what = 0x4;
                            message.sendToTarget();
                            break;
                        case "TRADE_SUCCESS":
                            mMainHint.setText(R.string.state_hint_success);
                            mResultWaitingProv.setVisibility(View.INVISIBLE);
                            qrCodeConv.setVisibility(View.GONE);
                            stateImgConv.setVisibility(View.VISIBLE);
                            mSateImgv.setVisibility(View.VISIBLE);
                            mSateImgv.setImageResource(R.drawable.rt);
                            try {
                                String buyerLogonId = tradeQueryResp.getString("buyer_logon_id");
                                String buyerUserId = tradeQueryResp.getString("buyer_user_id");
                                mBuyerUserIdTxv.setVisibility(View.VISIBLE);
                                mBuyerLogonIdTxv.setVisibility(View.VISIBLE);
                                mBuyerLogonIdTxv.setText(getResources().getString(R.string.state_user_account, buyerLogonId));
                                mBuyerUserIdTxv.setText(getResources().getString(R.string.state_user_id, buyerUserId));
                            } catch (Exception e) {
                                e.printStackTrace();
                                LogUtil.e(TAG, e.toString());
                            }
                            message = mUIHandler.obtainMessage();
                            message.what = 0x4;
                            message.sendToTarget();
                            break;
                        case "TRADE_CLOSED":
                        default:
                            mMainHint.setText(R.string.state_hint_failed);
                            mResultWaitingProv.setVisibility(View.INVISIBLE);
                            qrCodeConv.setVisibility(View.GONE);
                            stateImgConv.setVisibility(View.VISIBLE);
                            mSateImgv.setVisibility(View.VISIBLE);
                            mSateImgv.setImageResource(R.drawable.w);
                            message = mUIHandler.obtainMessage();
                            message.what = 0x4;
                            message.sendToTarget();
                            break;
                    }
                    break;
                case 0x4:
                    if(mTimer != null) {
                        mTimer.purge();
                        mTimer.cancel();
                        mTimer = null;
                    }
                    break;
                case 0x5:
                    JSONObject scanResObj = (JSONObject) msg.obj;

                    if(scanResObj != null) {
                        double resultTotalPay = 0.00;
                        String resultCode = "";
                        String resultMsg = "";
                        String resultOutTradeCode = "";
                        String buyerLogonId = "";
                        String buyerUserId = "";
                        try {

                            resultTotalPay = scanResObj.getDouble("total_amount");
                            resultCode = scanResObj.getString("code");
                            resultOutTradeCode = scanResObj.getString("out_trade_no");
                            buyerLogonId = scanResObj.getString("buyer_logon_id");
                            buyerUserId = scanResObj.getString("buyer_user_id");

                            mBuyerUserIdTxv.setVisibility(View.VISIBLE);
                            mBuyerLogonIdTxv.setVisibility(View.VISIBLE);
                            mBuyerLogonIdTxv.setText(getResources().getString(R.string.state_user_account, buyerLogonId));
                            mBuyerUserIdTxv.setText(getResources().getString(R.string.state_user_id, buyerUserId));

                            mOrderNoTxv.setVisibility(View.VISIBLE);
                            mOrderNoTxv.setText(getResources().getString(R.string.state_res_order, resultOutTradeCode));

                            if("10000".equals(resultCode) || "10003".equals(resultCode)) {
                                if(mTimer != null) {
                                    mTimer.purge();
                                    mTimer.cancel();
                                    mTimer = null;
                                }
                                mTimer = new Timer();
                                mTimer.schedule(new StatsActivity.MyTask(resultOutTradeCode), 0, 2000);
                                queryStartTimestamp = new Date().getTime();
                            } else {
                                resultCode = "收款失败，交易已关闭[" + scanResObj.getString("msg") + "]";
                                mMainHint.setText(resultCode);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                            e.printStackTrace();
                        }

                    }

                    break;
            }
        }
    }

    private class MyTask extends TimerTask {

        private String outTradeNo;

        public MyTask(String outTradeNo) {
            this.outTradeNo = outTradeNo;
        }

        @Override
        public void run() {
            Message msg = mUIHandler.obtainMessage();
            try {
                JSONObject jsonObject = Hbfq.query(MyApplication.getContext(), outTradeNo, mTradeType);
                msg.what = 0x3;
                msg.obj = jsonObject;
                msg.sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e(TAG, e.toString());
                msg.what = 0x4;
                msg.sendToTarget();
            }

        }

    }

    View.OnClickListener mVClk = v -> {
        switch (v.getId()) {
            case R.id.close_btn:
                Message message = mUIHandler.obtainMessage();
                message.what = 0x4;
                message.sendToTarget();
                finish();
                break;
        }
    };

}

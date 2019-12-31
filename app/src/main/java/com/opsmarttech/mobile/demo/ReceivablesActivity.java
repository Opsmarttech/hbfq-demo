package com.opsmarttech.mobile.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.opsmarttech.mobile.api.core.constant.Constants;
import com.opsmarttech.mobile.api.core.http.TradeParam;
import com.opsmarttech.mobile.api.core.utils.QRCodeUtil;
import com.opsmarttech.mobile.demo.adapter.HbfqListAdapter;
import com.opsmarttech.mobile.service.Hbfq;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class ReceivablesActivity extends AppCompatActivity {

    private static final String TAG = ReceivablesActivity.class.getName();
    private static final int START_SCAN = 0x131;
    private static final int SCAN_TOPAY = 0x11;
    private static final int QR_TOPAY = 0x12;


    private UIHandler mUIHandler = new UIHandler();

    private TextView clientTitleTxv;
    private TextView hbfqPerTxv;
    private SharedPreferences sharedPreferences;
    private ListView listView;
    private HbfqListAdapter adapter;
    private View zfbBtnView;
    private ImageView zfbChkImgv;
    private EditText amountEdtv;
    private View clearBtnView;
    private View qrRecBtnView;
    private View scanRecBtnVew;
    private View qrCodeLayer;
    private ImageView qrCodeImgv;
    private ProgressBar qrCodeLoading;
    private ProgressBar qrCodeWaiting;
    private ProgressBar resultInfoWaiting;
    private TextView qrCodePayHintTxv;
    private View insertLineView;
    private View resultAlertView;
    private TextView resultAlertAmoutTxv;
    private TextView resultAlertClientNameTxv;
    private TextView resultAlertStatusTxv;
    private TextView resultAlertOrderCodeTxv;
    private ImageView rtImgv;
    private ImageView wImgv;
    private View resultAlertClosev;
    private View backBtnv;

    private int hbfqPer = 0;
    private int hbfqNum = 0;
    private Bitmap qrBitmap;
    private Timer mTimer;
    private long queryStartTimestamp = 0l;
    private int currentPayMode = -1;
    private String deviceSN;
    private String clientTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#000000"));
        window.setNavigationBarColor(Color.parseColor("#000000"));
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.receivable);
        Toolbar toolbar = findViewById(R.id.receivable_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        sharedPreferences = getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE);

        deviceSN = sharedPreferences.getString(Constants.DEVICE_MEID, null);
        clientTitle = sharedPreferences.getString(Constants.CLIENT_TITLE, null);

        clientTitleTxv = findViewById(R.id.client_title);
        clientTitleTxv.setText(clientTitle);

        hbfqPerTxv = findViewById(R.id.hbfq_per);
        hbfqPer = sharedPreferences.getInt(Constants.CLIENT_HBFQ_PER, -1);
        if(hbfqPer == 100) hbfqPerTxv.setVisibility(View.VISIBLE);
        listView = findViewById(R.id.hbfq_list);
        String nums = sharedPreferences.getString(Constants.CLIENT_HBFQ_NUM, null);
        String[] sep = nums.split(",");
        ArrayList<Integer> list = new ArrayList<>();
        for(String item : sep) {
            list.add(Integer.valueOf(item));
        }
        adapter = new HbfqListAdapter(this, 0.0f, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listItemClk);
        hbfqNum = adapter.currentCheckIndex;

        zfbBtnView = findViewById(R.id.zfb_btn);
        zfbBtnView.setOnClickListener(onClk);

        zfbChkImgv = findViewById(R.id.zfb_check);
        amountEdtv = findViewById(R.id.amount_input);

        amountEdtv.addTextChangedListener(textWatcher);
        amountEdtv.setOnFocusChangeListener(onFocusChangeListener);

        clearBtnView = findViewById(R.id.clear_btn);
        clearBtnView.setOnClickListener(onClk);

        qrRecBtnView = findViewById(R.id.qr_rec_btn);
        qrRecBtnView.setOnClickListener(onClk);
        scanRecBtnVew = findViewById(R.id.scan_rec_btn);
        scanRecBtnVew.setOnClickListener(onClk);

        qrCodeLayer = findViewById(R.id.qr_code_layer);
        qrCodeImgv = findViewById(R.id.qr_code_bed);

        qrCodeImgv = findViewById(R.id.qr_code_bed);

        qrCodeLoading = findViewById(R.id.qr_code_loading);
        Sprite doubleBounce = new DoubleBounce();
        qrCodeLoading.setIndeterminateDrawable(doubleBounce);

        qrCodePayHintTxv = findViewById(R.id.qr_code_pay_hint);

        qrCodeWaiting = findViewById(R.id.qr_code_pay_waiting);
        insertLineView = findViewById(R.id.insert_line);

        resultAlertView = findViewById(R.id.result_alert_layer);

        resultAlertAmoutTxv = findViewById(R.id.result_alert_amount);
        resultAlertStatusTxv = findViewById(R.id.result_alert_status);
        resultAlertClientNameTxv = findViewById(R.id.result_alert_client_name);
        resultAlertView.setOnClickListener(onClk);

        resultInfoWaiting = findViewById(R.id.result_alert_waiting);

        rtImgv = findViewById(R.id.rt_status);
        wImgv = findViewById(R.id.w_status);
        resultAlertOrderCodeTxv = findViewById(R.id.out_order_number);
        resultAlertClosev = findViewById(R.id.result_alert_close);
        resultAlertClosev.setOnClickListener(onClk);

        backBtnv = findViewById(R.id.back_btn);
        backBtnv.setOnClickListener(onClk);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public AdapterView.OnItemClickListener listItemClk = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HbfqListAdapter.CellHolder cellHolder = (HbfqListAdapter.CellHolder) view.getTag();
            adapter.currentCheckIndex = cellHolder.times.intValue();
            adapter.notifyDataSetChanged();
            zfbChkImgv.setImageResource(R.drawable.unchecked);
            hbfqNum = cellHolder.times;
        }
    };

    public View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            insertLineView.setBackgroundColor(hasFocus ? Color.parseColor("#00A0E9") : Color.parseColor("#E9E9E9"));
        }
    };

    public View.OnClickListener onClk = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.zfb_btn:
                    hbfqNum = 0;
                    adapter.currentCheckIndex = -1;
                    adapter.notifyDataSetChanged();
                    zfbChkImgv.setImageResource(R.drawable.checked);
                    break;
                case R.id.clear_btn:
                    amountEdtv.setText("");
                    adapter.computSep(0f, true);
                    adapter.notifyDataSetChanged();
                    break;
                case R.id.qr_rec_btn:

                    if(TextUtils.isEmpty(amountEdtv.getText())) {
                        Toast.makeText(ReceivablesActivity.this, "请输入金额", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentPayMode = QR_TOPAY;

                    qrCodeLayer.setVisibility(qrCodeLayer.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            TradeParam tradeParam = new TradeParam();
                            tradeParam.put("totalMount", amountEdtv.getText().toString());
                            tradeParam.put("hbfqSellerPercent", String.valueOf(hbfqPer));
                            tradeParam.put("hbfqPhaseNum", String.valueOf(hbfqNum));
                            try {
                                JSONObject jsonObject = Hbfq.preCreateToPay(ReceivablesActivity.this, tradeParam);
                                Message msg = mUIHandler.obtainMessage();
                                msg.what = 0x1;
                                msg.obj = jsonObject;
                                msg.sendToTarget();
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    break;
                case R.id.scan_rec_btn:

                    if(TextUtils.isEmpty(amountEdtv.getText())) {
                        Toast.makeText(ReceivablesActivity.this, "请输入金额", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentPayMode = SCAN_TOPAY;

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
                    break;
                case R.id.result_alert_layer:
                    Log.i(TAG, "nothing todo ");
                    break;
                case R.id.result_alert_close:
                    if(resultAlertView.getVisibility() == View.VISIBLE) {
                        resultAlertView.setVisibility(View.GONE);
                        if(mTimer != null) {
                            mTimer.purge();
                            mTimer.cancel();
                            mTimer = null;
                        }
                    }
                    break;
                case R.id.back_btn:
                    onBackPressed();
                    break;
            }
        }
    };

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

            new Thread(new Runnable() {
                @Override
                public void run() {
                    TradeParam tradeParam = new TradeParam();
                    tradeParam.put("totalMount", amountEdtv.getText().toString());
                    tradeParam.put("hbfqSellerPercent", String.valueOf(hbfqPer));
                    tradeParam.put("hbfqPhaseNum", String.valueOf(hbfqNum));
                    tradeParam.put("authCode", _authCode);/**authCode 为买家手机付款码数字，买家被扫模式下该参数必选*/
                    JSONObject jsonObject = Hbfq.scanToPay(ReceivablesActivity.this, tradeParam);
                    Message message = mUIHandler.obtainMessage();
                    message.what = 0x5;
                    message.obj = jsonObject;
                    message.sendToTarget();
                }
            }).start();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String amountStr = s.toString();
            if(!TextUtils.isEmpty(amountStr)) {
                if(amountStr.contains(".")) {
                    int dotIndex = amountStr.indexOf(".");
                    if(dotIndex == 0) {
                        amountStr = "0" + amountStr;
                    }
                }
                float totalPayfor = Float.valueOf(amountStr);
                adapter.computSep(totalPayfor, hbfqPer == 100 ? true : false);
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onBackPressed() {

        if(qrCodeLayer.getVisibility() == View.VISIBLE) {
            qrCodeLayer.setVisibility(View.GONE);
            qrCodeImgv.setImageBitmap(null);
            if(qrBitmap != null) qrBitmap.recycle();
            qrBitmap = null;
            qrCodeLoading.setVisibility(View.VISIBLE);
            qrCodeWaiting.setVisibility(View.INVISIBLE);
            if(mTimer != null) {
                mTimer.purge();
                mTimer.cancel();
                mTimer = null;
            }
            return;
        }

        if(resultAlertView.getVisibility() == View.VISIBLE) {
            return;
        }

        super.onBackPressed();
    }

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x1:
                    JSONObject jsonObject = (JSONObject) msg.obj;

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
                    qrBitmap = QRCodeUtil.createQRCodeBitmap(qrUrl, 200, 200);
                    qrCodeImgv.setImageBitmap(qrBitmap);
                    qrCodeLoading.setVisibility(View.INVISIBLE);
                    qrCodeWaiting.setVisibility(View.VISIBLE);

                    if(mTimer != null) {
                        mTimer.purge();
                        mTimer.cancel();
                        mTimer = null;
                    }

                    mTimer = new Timer();
                    mTimer.schedule(new MyTask(outTradeCode), 0, 2000);
                    queryStartTimestamp = new Date().getTime();

                    resultAlertAmoutTxv.setText(amountEdtv.getText());
                    resultAlertOrderCodeTxv.setText(outTradeCode);
                    resultAlertClientNameTxv.setText(clientTitle);

                    break;
                case 0x3:

                    Log.i(TAG, "------------------- execute query ---------------------");
                    JSONObject jsonObj = (JSONObject) msg.obj;
                    String tradeResult = "WAIT_BUYER_PAY";
                    String totalAmount = "0.00";

                    try {
                        JSONObject tradeQueryResp = jsonObj.getJSONObject("alipay_trade_query_response");
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
                            if(currentPayMode == QR_TOPAY) {
                                qrCodePayHintTxv.setTextColor(Color.parseColor("#FF6347"));
                                qrCodePayHintTxv.setText("等待用户扫取识别二维码");
                            }
                            resultAlertStatusTxv.setTextColor(Color.parseColor("#FF6347"));
                            resultAlertStatusTxv.setText("交易超时，收款失败");

                            if(currentTimestamp - queryStartTimestamp > 60000) {
                                if(currentPayMode == QR_TOPAY) {
                                    qrCodePayHintTxv.setTextColor(Color.parseColor("#000000"));
                                    qrCodePayHintTxv.setText("未付款交易超时已关闭，请刷新二维码重新支付");
                                }

                                rtImgv.setVisibility(View.INVISIBLE);
                                wImgv.setVisibility(View.VISIBLE);
                                resultAlertStatusTxv.setTextColor(Color.parseColor("#FF6347"));
                                resultAlertStatusTxv.setText("交易超时，收款失败");

                                message = mUIHandler.obtainMessage();
                                message.what = 0x4;
                                message.sendToTarget();
                            }

                            break;
                        case "WAIT_BUYER_PAY":

                            if(currentPayMode == QR_TOPAY) {
                                qrCodePayHintTxv.setTextColor(Color.parseColor("#FF6347"));
                                qrCodePayHintTxv.setText("等待用户付款中...");
                            }

                            qrCodeLayer.setVisibility(View.GONE);
                            resultInfoWaiting.setVisibility(View.VISIBLE);
                            resultAlertView.setVisibility(View.VISIBLE);
                            rtImgv.setVisibility(View.INVISIBLE);
                            wImgv.setVisibility(View.INVISIBLE);
                            resultAlertStatusTxv.setTextColor(Color.parseColor("#FF6347"));
                            resultAlertStatusTxv.setText("等待用户付款中...");

                            break;
                        case "TRADE_FINISHED":
                            if(currentPayMode == QR_TOPAY) {
                                qrCodePayHintTxv.setTextColor(Color.parseColor("#000000"));
                                qrCodePayHintTxv.setText("交易已结束，不可退款");
                            }

                            rtImgv.setVisibility(View.INVISIBLE);
                            wImgv.setVisibility(View.VISIBLE);
                            resultAlertStatusTxv.setTextColor(Color.parseColor("#000000"));
                            resultAlertStatusTxv.setText("交易已结束，不可退款");

                            message = mUIHandler.obtainMessage();
                            message.what = 0x4;
                            message.sendToTarget();
                            break;
                        case "TRADE_SUCCESS":
                            if(currentPayMode == QR_TOPAY) {
                                qrCodePayHintTxv.setTextColor(Color.parseColor("#008B45"));
                                qrCodePayHintTxv.setText("交易支付成功");
                            }

                            rtImgv.setVisibility(View.VISIBLE);
                            wImgv.setVisibility(View.INVISIBLE);
                            resultAlertStatusTxv.setTextColor(Color.parseColor("#008B45"));
                            resultAlertStatusTxv.setText("收款成功");
                            resultAlertAmoutTxv.setText(totalAmount);

                            message = mUIHandler.obtainMessage();
                            message.what = 0x4;
                            message.sendToTarget();
                            break;
                        case "TRADE_CLOSED":
                        default:
                            if(currentPayMode == QR_TOPAY) {
                                qrCodePayHintTxv.setTextColor(Color.parseColor("#000000"));
                                qrCodePayHintTxv.setText("未付款交易超时已关闭，请刷新二维码重新支付");
                            }

                            rtImgv.setVisibility(View.INVISIBLE);
                            wImgv.setVisibility(View.VISIBLE);
                            resultAlertStatusTxv.setTextColor(Color.parseColor("#000000"));
                            resultAlertStatusTxv.setText("收款失败，交易超时已关闭");

                            message = mUIHandler.obtainMessage();
                            message.what = 0x4;
                            message.sendToTarget();
                            break;
                    }

                    break;
                case 0x4:

                    qrCodeWaiting.setVisibility(View.INVISIBLE);
                    resultInfoWaiting.setVisibility(View.INVISIBLE);
                    if(mTimer != null) {
                        mTimer.purge();
                        mTimer.cancel();
                        mTimer = null;
                    }
                    resultAlertClosev.setVisibility(View.VISIBLE);

                    break;
                case 0x5:

                    JSONObject scanResObj = (JSONObject) msg.obj;

                    if(scanResObj != null) {
                        rtImgv.setVisibility(View.INVISIBLE);
                        wImgv.setVisibility(View.INVISIBLE);
                        resultInfoWaiting.setVisibility(View.VISIBLE);
                        resultAlertView.setVisibility(View.VISIBLE);
                        resultAlertClientNameTxv.setText(clientTitle);
                        double resultTotalPay = 0.00;
                        String resultCode = "";
                        String resultMsg = "";
                        String resultOutTradeCode = "";
                        try {

                            resultTotalPay = scanResObj.getDouble("total_amount");
                            resultCode = scanResObj.getString("code");
                            resultOutTradeCode = scanResObj.getString("out_trade_no");

                            resultAlertAmoutTxv.setText(String.valueOf(resultTotalPay));
                            resultAlertOrderCodeTxv.setText(resultOutTradeCode);

                            if("10000".equals(resultCode) || "10003".equals(resultCode)) {
                                if(mTimer != null) {
                                    mTimer.purge();
                                    mTimer.cancel();
                                    mTimer = null;
                                }
                                mTimer = new Timer();
                                mTimer.schedule(new MyTask(resultOutTradeCode), 0, 2000);
                                queryStartTimestamp = new Date().getTime();
                            } else {
                                resultCode = "收款失败[" + scanResObj.getString("msg") + "]";
                                resultAlertStatusTxv.setText(resultCode);
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

            JSONObject jsonObject = Hbfq.query(ReceivablesActivity.this, outTradeNo);
            Message msg = mUIHandler.obtainMessage();
            msg.what = 0x3;
            msg.obj = jsonObject;
            msg.sendToTarget();

        }

    }

}

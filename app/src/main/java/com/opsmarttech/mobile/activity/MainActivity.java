package com.opsmarttech.mobile.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.opsmarttech.mobile.api.core.constant.Constants;
import com.opsmarttech.mobile.api.core.http.TradeParam;
import com.opsmarttech.mobile.api.core.utils.QRCodeUtil;
import com.opsmarttech.mobile.R;
import com.opsmarttech.mobile.service.Hbfq;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int CALL_READ_PHONE_STATE = 0xA;
    private String deviceSN;
    private TextView respCont;
    private ProgressBar wait;
    private Button scanBtn;
    private Button qrBtn;
    private ImageView imgv;

    private Timer timer;
    private MyHandelr myHandelr = new MyHandelr();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initConfigInfo();
        setContentView(R.layout.activity_main);
        respCont = (TextView) findViewById(R.id.respCont);
        wait = (ProgressBar) findViewById(R.id.wait);
        scanBtn = (Button) findViewById(R.id.scanBtn);
        qrBtn = (Button) findViewById(R.id.qrBtn);
        imgv = (ImageView) findViewById(R.id.qrImg);
        scanBtn.setOnClickListener(mClk);
        qrBtn.setOnClickListener(mClk);
    }

    private void initConfigInfo() {
        String[] mPermissionList = new String[]{Manifest.permission.READ_PHONE_STATE};
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            deviceSN = Build.SERIAL;
            getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE).edit().putString(Constants.DEVICE_MEID, deviceSN).commit();
            String readIt = getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE).getString(Constants.DEVICE_MEID, "--");
            System.out.println("-------->" + readIt);
            Toast.makeText(this, deviceSN, Toast.LENGTH_LONG).show();
        } else {
            EasyPermissions.requestPermissions(MainActivity.this, "need to bind device to complete payment!", CALL_READ_PHONE_STATE, mPermissionList);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            deviceSN = Build.SERIAL;
            getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE).edit().putString(Constants.DEVICE_MEID, deviceSN).commit();
        }
        String readIt = getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE).getString(Constants.DEVICE_MEID, "--");
        Toast.makeText(this, readIt, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    View.OnClickListener mClk = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.scanBtn:

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            TradeParam tradeParam = new TradeParam();
                            tradeParam.put("totalMount", "0.1");
                            tradeParam.put("hbfqSellerPercent", "100");
                            tradeParam.put("hbfqPhaseNum", "12");
                            tradeParam.put("authCode", "285476910192082196");
                            JSONObject jsonObject = Hbfq.scanToPay(MainActivity.this, tradeParam);
                            Message msg = myHandelr.obtainMessage();
                            msg.obj = jsonObject.toString();
                            msg.what = 0x1;
                            myHandelr.sendMessage(msg);
                        }
                    }).start();
                    wait.setVisibility(View.VISIBLE);

                    break;
                case R.id.qrBtn:

                    if(timer != null) {
                        Toast.makeText(MainActivity.this, "there is a trade continuing..., please wait a moment.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            TradeParam tradeParam = new TradeParam();
                            tradeParam.put("totalMount", "0.1");/**交易金额*/
                            tradeParam.put("hbfqSellerPercent", "100");/**付息方式：100商家贴息，0买家按期付息*/
                            tradeParam.put("hbfqPhaseNum", "12");/**分期数*/
                            JSONObject jsonObject = Hbfq.preCreateToPay(MainActivity.this, tradeParam);//.fetchQrCode(MainActivity.this, tradeParam, 200, 200);
                            Message msg = myHandelr.obtainMessage();
                            msg.obj = jsonObject;
                            msg.what = 0x2;
                            myHandelr.sendMessage(msg);
                        }
                    }).start();
                    wait.setVisibility(View.VISIBLE);

                    break;
            }
        }
    };

    private class MyHandelr extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {

            switch(msg.what) {
                case 0x1:
                    String respStr = (String) msg.obj;
                    respCont.setText(respStr);
                    wait.setVisibility(View.INVISIBLE);
                    break;
                case 0x2:
                    JSONObject jsonObject = (JSONObject) msg.obj;

                    respCont.setText(jsonObject.toString());
                    String qrUrl = "";
                    String storeId = "";
                    String outTradeNo = "";

                    try {
                        qrUrl = jsonObject.getString("qr_url");
                        Bitmap bitmap = QRCodeUtil.createQRCodeBitmap(qrUrl, 200, 200);
                        storeId = jsonObject.getString("storeId");
                        outTradeNo = jsonObject.getJSONObject("raw").getString("out_trade_no");
                        imgv.setImageBitmap(bitmap);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    timer = new Timer();
                    timer.schedule(new MyTask(outTradeNo), 0, 3000);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Message message = myHandelr.obtainMessage();
                            message.what = 0x4;
                            message.sendToTarget();
                        }
                    }, 60000);

                    wait.setVisibility(View.INVISIBLE);
                    break;
                case 0x3:

                    JSONObject jsonObj = (JSONObject) msg.obj;
                    respCont.setText(jsonObj.toString());

                    String tradeResult = "WAIT_BUYER_PAY";
                    try {
                        tradeResult = jsonObj.getJSONObject("alipay_trade_query_response").getString("trade_status");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(MainActivity.this, tradeResult, Toast.LENGTH_SHORT).show();

                    if("TRADE_SUCCESS".equals(tradeResult)) {
                        Message message = myHandelr.obtainMessage();
                        message.what = 0x4;
                        message.sendToTarget();
                    }

                    break;

                case 0x4:

                    if(timer != null) {
                        timer.purge();
                        timer.cancel();
                        timer = null;
                    }

                    imgv.setImageBitmap(null);

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

            JSONObject jsonObject = Hbfq.query(MainActivity.this, outTradeNo, "001");

            Message msg = myHandelr.obtainMessage();
            msg.what = 0x3;
            msg.obj = jsonObject;
            msg.sendToTarget();

        }

    }

}

package com.opsmarttech.mobile.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.opsmarttech.mobile.api.core.constant.Constants;
import com.opsmarttech.mobile.R;
import com.opsmarttech.mobile.service.Hbfq;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SplashActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = SplashActivity.class.getName();
    private static final int CALL_PERMISSION = 0xA;
    private static final String[] READ_PHONE_STATE = {Manifest.permission.READ_PHONE_STATE};
    private SharedPreferences mSharedPreferences;
    private SplashUIHandler mUIHandler = new SplashUIHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.splash);
        mSharedPreferences = getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String deviceSN = checkDeviceSN();
                Message msg = mUIHandler.obtainMessage();
                JSONObject jsonObject = Hbfq.fetchClientInfo(null, deviceSN);
                try {
                    boolean status = jsonObject.getBoolean("status");
                    if(status) {
                        String clientTitle = jsonObject.getString("clientTitle");
                        mSharedPreferences.edit().putString(Constants.CLIENT_TITLE, clientTitle).apply();
                        Set<String> typeSet = new HashSet<>();
                        if(jsonObject.has("001")) {
                            typeSet.add("001");
                            JSONObject antCreditMerchant = jsonObject.getJSONObject("001");
                            antCreditMerchant.put("tradeType", "001");
                            mSharedPreferences.edit().putString("001", antCreditMerchant.toString()).apply();
                        }
                        if(jsonObject.has("002")) {
                            typeSet.add("002");
                            JSONObject fundshareMerChant = jsonObject.getJSONObject("002");
                            fundshareMerChant.put("tradeType", "002");
                            mSharedPreferences.edit().putString("002", fundshareMerChant.toString()).apply();
                        }
                        mSharedPreferences.edit().putStringSet("tradeTypes", typeSet).apply();
                        msg.what = 0x1;
                    } else {
                        String errorInfo = jsonObject.getString("message");
                        msg.what = 0x2;
                        msg.obj = errorInfo;
                    }
                    msg.sendToTarget();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                    msg.what = 0x2;
                    msg.obj = e.toString();
                }

            }
        }).start();

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

    private String checkDeviceSN() throws SecurityException {
        String deviceSN = mSharedPreferences.getString(Constants.DEVICE_MEID, null);
        if(TextUtils.isEmpty(deviceSN)) {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE)) {
                deviceSN = getDeviceSN();
            } else {
                EasyPermissions.requestPermissions(SplashActivity.this, "need to bind device to complete payment!", CALL_PERMISSION, READ_PHONE_STATE);
            }
        }
        return deviceSN;
    }

    private String checkClientInfo() {
        return mSharedPreferences.getString(Constants.CLIENT_TITLE, null);
    }

    @AfterPermissionGranted(CALL_PERMISSION)
    private String getDeviceSN() throws SecurityException {
        String deviceSN = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            deviceSN = Build.getSerial();
        } else {
            deviceSN = Build.SERIAL;
        }
        if(!TextUtils.isEmpty(deviceSN)) {
            getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE).edit().putString(Constants.DEVICE_MEID, deviceSN).apply();
        }
        return deviceSN;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    private class SplashUIHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x1:
                    startActivity(new Intent("com.opsmarttech.mobile.activity.IndexActivity"));
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SplashActivity.this.finish();
                        }
                    }, 2000);
                    break;
                case 0x2:
                    Toast.makeText(SplashActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SplashActivity.this.finish();
                        }
                    }, 10000);
                    break;
            }
        }

    }


}

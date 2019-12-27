package com.opsmarttech.mobile.demo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import androidx.appcompat.app.AppCompatActivity;

import com.opsmarttech.mobile.api.core.constant.Constants;
import com.opsmarttech.mobile.service.Hbfq;

import org.json.JSONObject;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SplashActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = SplashActivity.class.getName();
    private static final int CALL_PERMISSION = 0xA;
    private static final String[] READ_PHONE_STATE = {Manifest.permission.READ_PHONE_STATE};
    private SharedPreferences mSharedPreferences;
    private SplashUIHandler mUIHandler = new SplashUIHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        setContentView(R.layout.splash);
        mSharedPreferences = getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String deviceSN = checkDeviceSN();
                String clientTitle = null;//checkClientInfo();
                Message msg = mUIHandler.obtainMessage();
                if(TextUtils.isEmpty(clientTitle)) {
                    JSONObject jsonObject = Hbfq.fetchClientInfo(null, deviceSN);
                    try {
                        clientTitle = jsonObject.getString("clientTitle");
                        if(TextUtils.isEmpty(clientTitle) || "[error : device unbind]".equals(clientTitle)) {
                            String errorInfo = clientTitle;
                            msg.what = 0x2;
                            msg.obj = errorInfo;
                        } else {
                            String hbfqNum = jsonObject.getString("hbfqNum");
                            int hbfqSellerPercent = jsonObject.getInt("hbfqSellerPercent");
                            mSharedPreferences.edit().putString(Constants.CLIENT_TITLE, clientTitle).commit();
                            mSharedPreferences.edit().putString(Constants.CLIENT_HBFQ_NUM, hbfqNum).commit();
                            mSharedPreferences.edit().putInt(Constants.CLIENT_HBFQ_PER, hbfqSellerPercent).commit();
                            msg.what = 0x1;
                        }
                        msg.sendToTarget();
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }
                } else {
                    msg.what = 0x1;
                    msg.sendToTarget();
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
            getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE).edit().putString(Constants.DEVICE_MEID, deviceSN).commit();
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
                    startActivity(new Intent("com.opsmarttech.mobile.demo.ReceivablesActivity"));
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

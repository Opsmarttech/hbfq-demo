package com.opsmarttech.mobile;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.DisplayMetrics;

import com.opsmarttech.mobile.util.LogUtil;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2;
import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.SunmiPrinterService;
import com.sunmi.scanner.IScanInterface;


public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getName();

    public static Context context;
    public static BasicOptV2 mBasicOptV2;           // 获取基础操作模块
    public static ReadCardOptV2 mReadCardOptV2;     // 获取读卡模块
    public static PinPadOptV2 mPinPadOptV2;         // 获取PinPad操作模块
    public static SecurityOptV2 mSecurityOptV2;     // 获取安全操作模块
    public static EMVOptV2 mEMVOptV2;               // 获取EMV操作模块

    public static SunmiPrinterService sunmiPrinterService;
    public static IScanInterface scanInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        initLocaleLanguage();

        bindPrintService();

        bindScannerService();
    }

    public static void initLocaleLanguage() {
        Resources resources = MyApplication.getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        resources.updateConfiguration(config, dm);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.e(TAG, "onConfigurationChanged");
    }

    public static Context getContext() {
        return context;
    }

    private void bindPrintService() {
        try {
            InnerPrinterManager.getInstance().bindService(this, new InnerPrinterCallback() {
                @Override
                protected void onConnected(SunmiPrinterService service) {
                    MyApplication.sunmiPrinterService = service;
                }
                @Override
                protected void onDisconnected() {
                    MyApplication.sunmiPrinterService = null;
                }
            });
        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
    }


    public void bindScannerService() {
        Intent intent = new Intent();
        intent.setPackage("com.sunmi.scanner");
        intent.setAction("com.sunmi.scanner.IScanInterface");
        bindService(intent, scanConn, Service.BIND_AUTO_CREATE);
    }

    private static ServiceConnection scanConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            scanInterface = IScanInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scanInterface = null;
        }
    };
}

package com.opsmarttech.mobile.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class PosInstallmentBaseActivity<layoutResourceId extends Integer> extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(0);
        initContent();
        initData();
    }

    public abstract void initContent();

    public abstract  void initData();

}

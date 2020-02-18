package com.opsmarttech.mobile.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.opsmarttech.mobile.MyApplication;
import com.opsmarttech.mobile.R;
import com.opsmarttech.mobile.adapter.TradeTypeAdapter;
import com.opsmarttech.mobile.api.core.constant.Constants;
import com.opsmarttech.mobile.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

public class IndexActivity  extends AppCompatActivity {

    private String TAG = IndexActivity.class.getName();
    private ListView mListView;
    private TradeTypeAdapter mAdapter;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        initViews();
        try {
            initData();
        } catch (JSONException e) {
            LogUtil.e(TAG, e.toString());
        }
    }

    private void initViews() {
        mListView = findViewById(R.id.trade_types);
    }

    private void initData() throws JSONException {
        mSharedPreferences = getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE);
        Set<String> tradeType = mSharedPreferences.getStringSet("tradeTypes", null);
        ArrayList<JSONObject> tradeTypeDataList = new ArrayList<>();
        for(String type : tradeType) {
            String jsonString = mSharedPreferences.getString(type, "");
            JSONObject jsonObject = new JSONObject(jsonString);
            tradeTypeDataList.add(jsonObject);
        }
        mAdapter = new TradeTypeAdapter(MyApplication.context, tradeTypeDataList);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

}

package com.opsmarttech.mobile.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.opsmarttech.mobile.MyApplication;
import com.opsmarttech.mobile.R;
import com.opsmarttech.mobile.adapter.InsNumAdapter;
import com.opsmarttech.mobile.api.core.constant.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class CounterActivity extends AppCompatActivity {

    private static final int START_SCAN = 0x131;
    private static final int SCAN_TOPAY = 0x11;
    private static final int QR_TOPAY = 0x12;

    private ListView mInsNumListView;
    private InsNumAdapter mInsNumAdapter;
    private SharedPreferences mSharedPreferences;
    private View mInsertLineV;
    private EditText mEditText;
    private ImageView mPayTypeImgv;
    private TextView mPayTypeTxv;
    private TextView mPayHintATxv;
    private TextView mPayHintBTxv;
    private View mPayBtnV;
    private View mClearAmountBtnV;
    private View mToPayBtnV;

    private String mCurrentTradeType;
    private String mCurrentPayType;
    private int mCheckedInsNum;//分期数
    private float mPerInsVal;//每期金额
    private float mPerInsSer;//每期手续费
    private Boolean isSellerPercnet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initData();
    }

    private void initViews() {
        setContentView(R.layout.activity_counter);
        mInsNumListView = findViewById(R.id.pay_num_list);
        mInsertLineV = findViewById(R.id.insert_line);
        mEditText = findViewById(R.id.amount_input);
        mPayBtnV = findViewById(R.id.pay_btn);
        mPayTypeTxv = findViewById(R.id.pay_type_txt);
        mPayTypeImgv = findViewById(R.id.pay_type_img);
        mPayHintATxv = findViewById(R.id.pay_hint_a);
        mPayHintBTxv = findViewById(R.id.pay_hint_b);
        mClearAmountBtnV = findViewById(R.id.clear_btn);
        mToPayBtnV = findViewById(R.id.pay_btn);
    }

    private void initData() {
        mSharedPreferences = getSharedPreferences(Constants.SHAREDPREFERENCES_FILE, MODE_PRIVATE);
        String tradeType = getIntent().getStringExtra("tradeType");
        String payType = getIntent().getStringExtra("payType");
        mCurrentTradeType = tradeType;
        mCurrentPayType = payType;
        try {
            switch (tradeType) {
                case Constants.TRADE_TYPE_HB:
                    if("scan".equals(payType)) {
                        mPayTypeImgv.setImageResource(R.drawable.scan_to_pay);
                        mPayTypeTxv.setText(R.string.scan_to_pay);
                        mPayHintATxv.setText(R.string.scan_hint);
                        mPayHintBTxv.setVisibility(View.GONE);
                    } else if("qrCode".equals(payType)) {
                        mPayTypeImgv.setImageResource(R.drawable.qrcode_to_pay);
                        mPayTypeTxv.setText(R.string.qrcode_to_pay);
                        mPayHintATxv.setText(R.string.qrcode_hint);
                        mPayHintBTxv.setVisibility(View.GONE);
                    }
                    break;
                case Constants.TRADE_TYPE_LBF:
                    if("swape".equals(payType)) {
                        mPayTypeImgv.setImageResource(R.drawable.swape_to_pay);
                        mPayTypeTxv.setText(R.string.swape_to_pay);
                        mPayHintATxv.setText(R.string.swape_hint_a);
                        mPayHintBTxv.setText(R.string.swape_hint_b);
                    } else if("qrCode".equals(payType)) {
                        mPayTypeImgv.setImageResource(R.drawable.qrcode_to_pay);
                        mPayTypeTxv.setText(R.string.qrcode_to_pay);
                        mPayHintATxv.setText(R.string.qrcode_hint);
                        mPayHintBTxv.setVisibility(View.GONE);
                    }
                    break;
            }
            JSONObject merChant = new JSONObject(mSharedPreferences.getString(tradeType, ""));
            mInsNumAdapter = new InsNumAdapter(0.0f, merChant);
            mInsNumListView.setAdapter(mInsNumAdapter);
            mInsNumAdapter.notifyDataSetChanged();
            mInsNumListView.setOnItemClickListener(listItemClk);
            mEditText.addTextChangedListener(textWatcher);
            mEditText.setOnFocusChangeListener(onFocusChangeListener);
            mClearAmountBtnV.setOnClickListener(mViewClick);
            mToPayBtnV.setOnClickListener(mViewClick);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public AdapterView.OnItemClickListener listItemClk = (AdapterView<?> parent, View view, int position, long id) -> {
            InsNumAdapter.CellHolder cellHolder = (InsNumAdapter.CellHolder) view.getTag();
            mInsNumAdapter.mCurrentCheckIndex = cellHolder.times.intValue();
            mInsNumAdapter.notifyDataSetChanged();
            mCheckedInsNum = cellHolder.times;
        };

    public View.OnFocusChangeListener onFocusChangeListener = (View v, boolean hasFocus) -> {
            mInsertLineV.setBackgroundColor(hasFocus ? Color.parseColor("#00A0E9") : Color.parseColor("#E9E9E9"));
        };

    public View.OnClickListener mViewClick = v -> {
        switch (v.getId()) {
            case R.id.clear_btn:
                mCheckedInsNum = 0;
                mEditText.setText("");
                mInsNumAdapter.setTotalPayfor(0f);
                mInsNumAdapter.notifyDataSetChanged();
                break;
            case R.id.pay_btn:
                if(TextUtils.isEmpty(mEditText.getText())) {
                    Toast.makeText(MyApplication.getContext(), "请输入金额", Toast.LENGTH_SHORT).show();
                    return;
                }

                mPerInsSer = mInsNumAdapter.mCurrentSv;
                mPerInsVal = mInsNumAdapter.mCurrentMy;
                mCheckedInsNum = mInsNumAdapter.mCurrentCheckIndex;
                isSellerPercnet = mInsNumAdapter.mCurrentPercent.get(mCheckedInsNum);

                Intent intent = new Intent(MyApplication.getContext(), StatsActivity.class);
                switch (mCurrentTradeType) {
                    case Constants.TRADE_TYPE_HB:
                        intent.putExtra("totalPay", mEditText.getText().toString());
                        intent.putExtra("tradType", mCurrentTradeType);
                        intent.putExtra("payType", mCurrentPayType);
                        intent.putExtra("insNum", mCheckedInsNum);
                        intent.putExtra("perInsSer", mPerInsSer);
                        intent.putExtra("perInsVal", mPerInsVal);
                        intent.putExtra("sellerPercent", isSellerPercnet);
                        break;
                    case Constants.TRADE_TYPE_LBF:
                        intent.putExtra("totalPay", mEditText.getText().toString());
                        intent.putExtra("tradType", mCurrentTradeType);
                        intent.putExtra("payType", mCurrentPayType);
                        intent.putExtra("insNum", mCheckedInsNum);
                        intent.putExtra("sellerPercent", isSellerPercnet);
                        break;
                }
                startActivity(intent);
                break;
        }
    };

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
                mInsNumAdapter.setTotalPayfor(totalPayfor);
                mInsNumAdapter.notifyDataSetChanged();
            }
        }
    };

}

package com.opsmarttech.mobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.opsmarttech.mobile.MyApplication;
import com.opsmarttech.mobile.R;
import com.opsmarttech.mobile.activity.CounterActivity;
import com.opsmarttech.mobile.api.core.constant.Constants;
import com.opsmarttech.mobile.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TradeTypeAdapter extends BaseAdapter {

    private static final String TAG = TradeTypeAdapter.class.getName();

    private Context context;

    private ArrayList<JSONObject> tradeTypeDataList;

    public TradeTypeAdapter(Context ctx, ArrayList<JSONObject> tradeData) {
        context = ctx;
        tradeTypeDataList = tradeData;
    }

    public void setDataList(ArrayList<JSONObject> tradeData) {
        tradeTypeDataList = tradeData;
    }

    @Override
    public int getCount() {
        return tradeTypeDataList == null ? 0 : tradeTypeDataList.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return tradeTypeDataList == null ? null : tradeTypeDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        long id = -1l;
        if(tradeTypeDataList != null) {
            try {
                id = tradeTypeDataList.get(position).getInt("merId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        JSONObject item = tradeTypeDataList.get(position);
        TradeTypeAdapter.CellHolder cellHolder = null;

        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.trade_type, null);
            cellHolder = new CellHolder(convertView);
            convertView.setTag(cellHolder);
        } else {
            cellHolder = (TradeTypeAdapter.CellHolder) convertView.getTag();
        }

        try {

            switch (item.getString("tradeType")) {
                case "001":
                    cellHolder.tradeImg.setImageResource(R.drawable.hb_3);
                    cellHolder.tradeTitleTxv.setText("支付宝花呗");
                    cellHolder.tradeTypeATxv.setText("扫码收款");
                    cellHolder.tradeTypeAHintTxv.setText("扫描用户支付宝收款码收款");
                    cellHolder.tradeTypeAImgv.setImageResource(R.drawable.trade_type_scan);
                    cellHolder.tradeTypeBTxv.setText("出示收款码");
                    cellHolder.tradeTypeBHintTxv.setText("展示收款二维码，请用户使用支付宝扫描收款");
                    cellHolder.tradeTypeBImgv.setImageResource(R.drawable.trade_type_qr);
                    break;
                case "002":
                    cellHolder.tradeImg.setImageResource(R.drawable.yl_3);
                    cellHolder.tradeTitleTxv.setText("银联信用卡");
                    cellHolder.tradeSubHintTxv.setVisibility(View.VISIBLE);
                    cellHolder.tradeSubHintTxv.setText("不支持招商银行、浦发银行、交通银行");
                    cellHolder.tradeTypeATxv.setText("刷卡收款");
                    cellHolder.tradeTypeAHintTxv.setText("刷取用户银联信用卡收款");
                    cellHolder.tradeTypeAImgv.setImageResource(R.drawable.trade_type_swape);
                    cellHolder.tradeTypeBTxv.setText("出示收款码");
                    cellHolder.tradeTypeBHintTxv.setText("展示收款二维码，请用户支付宝或微信扫描，填写信用卡信息支付");
                    cellHolder.tradeTypeBImgv.setImageResource(R.drawable.trade_type_qr);
                    break;
            }

            cellHolder.tradeTypeA.setTag(item);
            cellHolder.tradeTypeB.setTag(item);

        } catch (Exception e) {
            LogUtil.e(TAG, e.toString());
        }

        return convertView;
    }

    public class CellHolder {

        public CardView tradeTypeA;
        public CardView tradeTypeB;

        public TextView tradeTitleTxv;
        public TextView tradeSubHintTxv;
        public ImageView tradeImg;

        public TextView tradeTypeATxv;
        public TextView tradeTypeAHintTxv;
        public ImageView tradeTypeAImgv;
        public TextView tradeTypeBTxv;
        public TextView tradeTypeBHintTxv;
        public ImageView tradeTypeBImgv;


        public CellHolder(View cellview) {

            tradeTypeA = cellview.findViewById(R.id.trade_type_a);
            tradeTypeB = cellview.findViewById(R.id.trade_type_b);

            tradeTitleTxv = cellview.findViewById(R.id.trade_title);
            tradeSubHintTxv = cellview.findViewById(R.id.trade_sub_hint);
            tradeImg = cellview.findViewById(R.id.trade_img);

            tradeTypeATxv = cellview.findViewById(R.id.trade_type_a_title);
            tradeTypeAHintTxv = cellview.findViewById(R.id.trade_type_a_hint);
            tradeTypeAImgv = cellview.findViewById(R.id.trade_type_a_img);

            tradeTypeBTxv = cellview.findViewById(R.id.trade_type_b_title);
            tradeTypeBHintTxv = cellview.findViewById(R.id.trade_type_b_hint);
            tradeTypeBImgv = cellview.findViewById(R.id.trade_type_b_img);

            tradeTypeA.setOnClickListener(v -> {
                JSONObject tradeObject = (JSONObject) v.getTag();
                try {
                    String tradeType = tradeObject.getString("tradeType");
                    Intent intent = new Intent(MyApplication.getContext(), CounterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("tradeType", tradeType);
                    if(Constants.TRADE_TYPE_HB.equals(tradeType)) {
                        intent.putExtra("payType", "scan");
                    } else if(Constants.TRADE_TYPE_LBF.equals(tradeType)) {
                        intent.putExtra("payType", "swape");
                    }
                    context.startActivity(intent);
                } catch (Exception e) {
                    LogUtil.e(TAG, e.toString());
                }
            });

            tradeTypeB.setOnClickListener(v -> {
                JSONObject tradeObject = (JSONObject) v.getTag();
                try {
                    String tradeType = tradeObject.getString("tradeType");
                    Intent intent = new Intent(MyApplication.getContext(), CounterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("tradeType", tradeType);
                    intent.putExtra("payType", "qrCode");
                    context.startActivity(intent);
                } catch (Exception e) {
                    LogUtil.e(TAG, e.toString());
                }
            });

        }

    }

}

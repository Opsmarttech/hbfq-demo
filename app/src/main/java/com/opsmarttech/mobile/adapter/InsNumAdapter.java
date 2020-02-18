package com.opsmarttech.mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.opsmarttech.mobile.MyApplication;
import com.opsmarttech.mobile.R;
import com.opsmarttech.mobile.api.core.constant.Constants;
import com.opsmarttech.mobile.util.LogUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InsNumAdapter extends BaseAdapter {

    private static final String TAG = InsNumAdapter.class.getName();

    private List<Integer> mNumList;
    private Context mContext;
    private JSONObject mMerChantType;
    private String mCurrentTradeType;
    private float mTotalPayfor;
    private HashMap<Integer, Float> mCurrentPhase;

    public int mCurrentCheckIndex = 6;
    public float mCurrentMy;
    public float mCurrentSv;


    public InsNumAdapter(float totalPayfor, JSONObject merChantType) {

        mContext = MyApplication.getContext();
        mNumList = new ArrayList<>();
        mCurrentPhase = new HashMap<>();
        mMerChantType = merChantType;
        mTotalPayfor = totalPayfor;

        try {
            mCurrentTradeType = merChantType.getString("tradeType");
            switch (mCurrentTradeType) {
                case Constants.TRADE_TYPE_HB:
                    boolean exempt = merChantType.getInt("hbfqSellerPercent") == 100 ? true : false;
                    for(String num : merChantType.getString("hbfqNum").split(",")) {
                        mNumList.add(Integer.parseInt(num));
                        switch (num) {
                            case "3":
                                mCurrentPhase.put(Integer.parseInt(num), exempt ? 0.00f : Constants.HB_PHASE_3_PERCENT);
                                break;
                            case "6":
                                mCurrentPhase.put(Integer.parseInt(num), exempt ? 0.00f : Constants.HB_PHASE_6_PERCENT);
                                break;
                            case "12":
                                mCurrentPhase.put(Integer.parseInt(num), exempt ? 0.00f : Constants.HB_PHASE_12_PERCENT);
                                break;
                        }
                    }
                    break;
                case Constants.TRADE_TYPE_LBF:
                    for(String num : merChantType.getString("lbffqNum").split(",")) {
                        mNumList.add(Integer.parseInt(num));
                        mCurrentPhase.put(Integer.parseInt(num), 0.00f);
                    }
                    break;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e.toString());
        }

    }

    public void setTotalPayfor(float totalPayfor) {
        mTotalPayfor = totalPayfor;
    }

    public void setDataList(List<Integer> dataList) {
        if(mNumList != null) mNumList.clear();
        else mNumList = new ArrayList<>();
        mNumList.addAll(dataList);
    }

    @Override
    public int getCount() {
        return mNumList == null ? 0 : mNumList.size();
    }

    @Override
    public Integer getItem(int position) {
        return mNumList == null ? null : mNumList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Integer sep = mNumList.get(position);
        CellHolder cellHolder = null;

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.num_item, null);
            cellHolder = new CellHolder(convertView);
            convertView.setTag(cellHolder);
        } else {
            cellHolder = (CellHolder) convertView.getTag();
        }

        float sv = mTotalPayfor * mCurrentPhase.get(sep) / 3.0f;
        float my = mTotalPayfor / sep;

        mCurrentMy = my;
        mCurrentSv = sv;

        cellHolder.times = sep;
        cellHolder.timesTxv.setText(sep.intValue() + " 期");
        cellHolder.moneyTxv.setText(String.format("%.2f", my));
        cellHolder.servTxv.setText(" " + String.format("%.2f", sv) + " 元");

        if(sep == mCurrentCheckIndex) {
            cellHolder.checkImgv.setImageResource(R.drawable.checked);
        } else {
            cellHolder.checkImgv.setImageResource(R.drawable.unchecked);
        }

        return convertView;
    }

    public class CellHolder {

        public Integer times;
        public TextView timesTxv;
        public TextView moneyTxv;
        public TextView servTxv;
        public ImageView checkImgv;


        public CellHolder(View cellview) {
            timesTxv = cellview.findViewById(R.id.item_sep);
            moneyTxv = cellview.findViewById(R.id.item_money);
            servTxv = cellview.findViewById(R.id.item_serv);
            checkImgv = cellview.findViewById(R.id.item_check);
        }

    }


}

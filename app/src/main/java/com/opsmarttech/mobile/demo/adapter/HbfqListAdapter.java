package com.opsmarttech.mobile.demo.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.opsmarttech.mobile.demo.R;

import java.util.ArrayList;
import java.util.List;

public class HbfqListAdapter extends BaseAdapter {

    private List<Integer> numList;
    private Activity context;

    private float my3 = 0.0f;
    private float my6 = 0.0f;
    private float my12 = 0.0f;
    private float my24 = 0.0f;

    private float sv3 = 0.0f;
    private float sv6 = 0.0f;
    private float sv12 = 0.0f;
    private float sv24 = 0.0f;

    private float phase3percent = 0.023f;
    private float phase6percent = 0.045f;
    private float phase12percent = 0.075f;
    private float phase24percent = 0.00f;

    public int currentCheckIndex = 6;

    public HbfqListAdapter(Activity context, float totalPayfor, List<Integer> hbfqNumList) {

        this.context = context;
        numList = hbfqNumList;
        computSep(totalPayfor, false);

    }

    public void computSep(float totalPayfor, boolean noInter) {

        //手续费
        sv3 = noInter ? 0.0f : totalPayfor * phase3percent / 3;
        sv6 = noInter ? 0.0f :  totalPayfor * phase6percent / 6;
        sv12 = noInter ? 0.0f :  totalPayfor * phase12percent / 12;
        sv24 = noInter ? 0.0f :  totalPayfor * phase24percent / 24;

        //每期金额
        my3 = totalPayfor / 3.0f;
        my6 = totalPayfor / 6.0f;
        my12 = totalPayfor / 12.0f;
        my24 = totalPayfor / 24.0f;
    }

    public void setDataList(List<Integer> dataList) {

        if(numList != null) numList.clear();
        else numList = new ArrayList<>();
        numList.addAll(dataList);

    }

    @Override
    public int getCount() {
        return numList == null ? 0 : numList.size();
    }

    @Override
    public Integer getItem(int position) {
        return numList == null ? null : numList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Integer sep = numList.get(position);
        CellHolder cellHolder = null;

        if(convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.hbfq_num_item, null);
            cellHolder = new CellHolder(convertView);
            convertView.setTag(cellHolder);
        } else {
            cellHolder = (CellHolder) convertView.getTag();
        }

        String sv = "";
        String my = "";

        switch(sep.intValue()) {
            case 3:
                my = String.format("%.2f", my3);
                sv = String.format("%.2f", sv3);
                break;
            case 6:
                my = String.format("%.2f", my6);
                sv = String.format("%.2f", sv6);
                break;
            case 12:
                my = String.format("%.2f", my12);
                sv = String.format("%.2f", sv12);
                break;
            case 24:
                my = String.format("%.2f", my24);
                sv = String.format("%.2f", sv24);
                break;
        }

        cellHolder.times = sep;
        cellHolder.timesTxv.setText(sep.intValue() + " 期");
        cellHolder.moneyTxv.setText(my);
        cellHolder.servTxv.setText(sv + " 元");

        if(sep == currentCheckIndex) {
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
            timesTxv = (TextView) cellview.findViewById(R.id.hbfq_item_sep);
            moneyTxv = (TextView) cellview.findViewById(R.id.hbfq_item_money);
            servTxv = (TextView) cellview.findViewById(R.id.hbfq_item_serv);
            checkImgv = (ImageView) cellview.findViewById(R.id.hbfq_item_check);
        }

    }


}

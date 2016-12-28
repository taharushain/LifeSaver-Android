/*******************************************************************************
 * Copyright (c) 2016. Golemtron.com
 * Designed and Developed by Taha Rushain
 ******************************************************************************/

package com.golemtron.lifesaver.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.golemtron.lifesaver.R;
import com.golemtron.lifesaver.model.HospitalItem;

import java.util.List;

/**
 * Created by trushain on 10/3/16.
 */

public class HospitalsListAdapter extends BaseAdapter{

    private Activity activity;
    private LayoutInflater inflater;
    private List<HospitalItem> hospitalItemItems;

    public HospitalsListAdapter(Activity activity, List<HospitalItem> hospitalItemItems) {
        this.activity = activity;
        this.hospitalItemItems = hospitalItemItems;
    }
    @Override
    public int getCount() {
        return hospitalItemItems.size();
    }

    @Override
    public Object getItem(int i) {
        return hospitalItemItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.hospital_item, null);

        TextView name = (TextView) convertView.findViewById(R.id.tv_hospital);

        HospitalItem m = hospitalItemItems.get(i);

        name.setText(m.getName());

        return convertView;
    }
}

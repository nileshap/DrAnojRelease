package com.endo.intellih.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.endo.intellih.R;
import com.endo.intellih.models.NavigationModle;

import java.util.ArrayList;

/**
 * Created by admin on 10/18/2016.
 */
public class NavigationListAdapter extends ArrayAdapter<NavigationModle> {

    private ArrayList<NavigationModle> data;
    private Context mContext;
    public NavigationListAdapter(Context mContext,ArrayList<NavigationModle> data){
        super(mContext, 0, data);
        this.mContext = mContext;
        this.data = data;

    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public NavigationModle getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        NavigationModle modle = data.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_navigation, parent, false);
        }
// Lookup view for data population
        TextView txtName = (TextView) convertView.findViewById(R.id.txt_name);
        txtName.setText(modle.getMenuName());

        if (modle.isSelected()){
            txtName.setTextColor(mContext.getResources().getColor(R.color.navigation_selected));
        }else{
            txtName.setTextColor(mContext.getResources().getColor(R.color.navigation_not_selected));
        }

        return convertView;
    }
}

package com.endo.intellih.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.endo.intellih.R;
import com.endo.intellih.webservice.services.response.SearchFoodResponse;

import java.util.ArrayList;

/**
 * Purpose: it is adapter for Diet Search Food List
 * IDM 1583
 */
public class DietFoodSearchAdapter extends ArrayAdapter<SearchFoodResponse> {

    private static String TAG = DietFoodSearchAdapter.class.getSimpleName();
    private ArrayList<SearchFoodResponse> mData;

    public DietFoodSearchAdapter(Context context, ArrayList<SearchFoodResponse> mData) {
        super(context, 0, mData);
        this.mData = mData;


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SearchFoodResponse item = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            //init views from xml
            convertView = inflater.inflate(R.layout.adapter_diet_search_result, parent, false);
            viewHolder.foodName = (TextView) convertView.findViewById(R.id.txtDesc_Add_Search);
            viewHolder.servingDesc = (TextView) convertView.findViewById(R.id.txt_servingDesc);
            viewHolder.cals = (TextView) convertView.findViewById(R.id.txt_cals);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //setting data into UI
        viewHolder.foodName.setText(item.getFoodName());
        if ((item.getServingDescription() == null)) {
            viewHolder.servingDesc.setText("");
        } else if (item.getServingDescription().length() == 0) {
            viewHolder.servingDesc.setText("");
        } else {
            viewHolder.servingDesc.setText(item.getServingDescription());
        }

        if (item.getCalorie() == null || (item.getCalorie() == 0)) {
            viewHolder.cals.setText("");
        } else {
            viewHolder.cals.setText("" + item.getCalorie() + " cals");
        }


        return convertView;
    }

    private static class ViewHolder {
        TextView foodName;
        TextView cals;
        TextView servingDesc;

    }

    public ArrayList<SearchFoodResponse> getData() {
        return mData;
    }

}

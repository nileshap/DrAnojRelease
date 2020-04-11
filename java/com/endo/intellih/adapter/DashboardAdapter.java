package com.endo.intellih.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.endo.intellih.R;
import com.endo.intellih.models.DashboardItem;

import java.util.ArrayList;

/**
 * Created by admin on 9/22/2016.
 */
public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.RecyclerViewHolders> {

    public static final String TAG = DashboardAdapter.class.getSimpleName();
    private ArrayList<DashboardItem> itemList;


    public DashboardAdapter(ArrayList<DashboardItem> itemList) {
        this.itemList = itemList;

    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_dashboard, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }


    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        DashboardItem item = itemList.get(position);
       // AppLog.showD(TAG,item.getTitle());
        holder.txtTitle.setText(item.getTitle());
        holder.imgIcon.setImageResource(item.getImageIcon());
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }


    public class RecyclerViewHolders extends RecyclerView.ViewHolder {

        public TextView txtTitle;
        public ImageView imgIcon;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            txtTitle = (TextView) itemView.findViewById(R.id.txt_title);
            imgIcon = (ImageView) itemView.findViewById(R.id.img_module);
        }
    }
}

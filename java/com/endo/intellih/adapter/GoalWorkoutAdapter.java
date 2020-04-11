package com.endo.intellih.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.endo.intellih.R;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.webservice.services.response.GetPatientDataView;

import java.util.ArrayList;

/**
 * Created by admin on 10/4/2016.
 */
public class GoalWorkoutAdapter extends RecyclerView.Adapter<GoalWorkoutAdapter.DataObjectHolder> {

    private static String TAG = GoalWorkoutAdapter.class.getSimpleName();

    private ArrayList<GetPatientDataView.Goal> mDataset;

    public GoalWorkoutAdapter(ArrayList<GetPatientDataView.Goal> myDataset) {
        mDataset = myDataset;

    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder {
        TextView txtFactorName, txtFactorUnit;
        EditText edtMin, edtMax;

        public DataObjectHolder(View itemView) {
            super(itemView);
            txtFactorName = (TextView) itemView.findViewById(R.id.txt_factorname);
            txtFactorUnit = (TextView) itemView.findViewById(R.id.txt_factorunit);
            edtMin = (EditText) itemView.findViewById(R.id.txt_min);
            edtMax = (EditText) itemView.findViewById(R.id.txt_max);
        }
    }


    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_goal_self_measure, parent, false);
        return new DataObjectHolder(view);
    }


    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {

        final GetPatientDataView.Goal obj = mDataset.get(position);
        holder.txtFactorName.setText(obj.getFactorName());
//        if (!obj.getUnit().equals(null)) {
            holder.txtFactorUnit.setVisibility(View.VISIBLE);
            holder.txtFactorUnit.setText(String.valueOf(obj.getUnit()));
//        } else {
//            holder.txtFactorUnit.setVisibility(View.GONE);
//        }

        //holder.edtMin.setText("" + obj.getGoal1());
        if(obj.getGoal1()==0){
            holder.edtMin.setText("");
        }else{
            holder.edtMin.setText("" + obj.getGoal1());
        }
        holder.edtMin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 ) {
                    obj.setGoal1(Float.parseFloat("" + s));
                    AppLog.showD(TAG, "goal1=" + obj.getGoal1());
                }
            }
        });
        if (obj.getGoalType().equals("Range")) {
//            holder.edtMin.setHint("From");
//            holder.edtMax.setHint("To");
            holder.edtMax.setVisibility(View.VISIBLE);
           // holder.edtMax.setText("" + obj.getGoal2());
            if( obj.getGoal2()==0){
                holder.edtMax.setText("" );
            }else{
                holder.edtMax.setText("" + obj.getGoal2());
            }
            holder.edtMax.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0 ) {
                        obj.setGoal2(Float.parseFloat("" + s));
                        AppLog.showD(TAG, "goal2=" + obj.getGoal2());
                    }
                }
            });
        } else {
            holder.edtMax.setVisibility(View.GONE);

        }

    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public ArrayList<GetPatientDataView.Goal> getData() {
        return mDataset;
    }
}

package com.endo.intellih.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.endo.intellih.AppConstants;
import com.endo.intellih.R;
import com.endo.intellih.models.AssociateHFGroup;
import com.endo.intellih.utils.AppLog;

public class VitalSixMWTFragment extends Fragment {

    public static final String TAG = VitalSixMWTFragment.class.getSimpleName();
    private AssociateHFGroup moduleData;
    private Gson gson;
    private LinearLayout llRoot;
    private boolean showTiming = false;

    public static Fragment newInstance(Context context) {
        VitalSixMWTFragment f = new VitalSixMWTFragment();

        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        gson = new Gson();
        moduleData = gson.fromJson(getArguments().getString(AppConstants.INTENT_KEY.VITAL_FACTORS), AssociateHFGroup.class);
        showTiming = moduleData.getIsSeperateDate();
        AppLog.showD(TAG, "showTiming=" + showTiming);
        AppLog.showD(TAG, moduleData.getTenantHFGroupName());
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_vital_glucose, null);
//        TextView txtTitle = (TextView) root.findViewById(R.id.txt_title);
//        txtTitle.setText(moduleData.getTenantHFGroupName());
//        llRoot = (LinearLayout) root.findViewById(R.id.ll_root);
//
//        for (AssociateVital item : moduleData.getAssociateVitals()) {
//            createCustomViews(item);
//        }


        return root;
    }


//    public void createCustomViews(AssociateVital item) {
//        AppLog.showD(TAG, "createCustomViews");
//        LayoutInflater factory = LayoutInflater.from(getActivity());
//        View view = factory.inflate(R.layout.layout_vital_with_time, null);
//        TextView txtLable = (TextView) view.findViewById(R.id.txt_lable);
//        txtLable.setText(item.getVitalName());
//        TextView txtUnit = (TextView) view.findViewById(R.id.txt_unit);
//        txtUnit.setText(item.getVitalUnitName());
//        EditText edtValue = (EditText) view.findViewById(R.id.edt_value);
//        final EditText edtTime = (EditText) view.findViewById(R.id.edt_time);
//        edtTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                Calendar mcurrentTime = Calendar.getInstance();
//                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
//                int minute = mcurrentTime.get(Calendar.MINUTE);
//                TimePickerDialog mTimePicker;
//                mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
//                    @Override
//                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
//                        ((EditText) v).setText(selectedHour + ":" + selectedMinute);
//                    }
//                }, hour, minute, true);//Yes 24 hour time
//               // mTimePicker.setTitle("Select Time");
//                mTimePicker.show();
//            }
//        });
//        if (showTiming) {
//            edtTime.setVisibility(View.VISIBLE);
//        } else {
//            edtTime.setVisibility(View.GONE);
//        }
//
//        llRoot.addView(view);
//    }


}
package com.endo.intellih.activities;

import android.app.Activity;
import android.os.Bundle;

import com.google.gson.Gson;
import com.endo.intellih.AppConstants;
import com.endo.intellih.R;
import com.endo.intellih.models.VitalsMeasurementList;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.webservice.services.response.VitalRes;

/**
 * Created by admin on 12/30/2016.
 */
public class VitalUpdateActivity extends Activity {

    public static final String TAG = VitalUpdateActivity.class.getSimpleName();
    private VitalsMeasurementList item;
    VitalRes groupItem = null;
    private Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vital_update);
        gson = new Gson();
        Bundle extra = getIntent().getExtras();
        if(extra!=null){
            groupItem =  gson.fromJson(extra.getString(AppConstants.INTENT_KEY.EDIT_VITAL),VitalRes.class);

            AppLog.showE(TAG,"size ="+groupItem.getVitalsMeasurementList().size());

        }


    }
}

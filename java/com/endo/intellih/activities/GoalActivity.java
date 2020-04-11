package com.endo.intellih.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.endo.intellih.AppConstants;
import com.endo.intellih.LoginActivity;
import com.endo.intellih.R;
import com.endo.intellih.application.AppController;
import com.endo.intellih.models.TenantGoalList;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.response.UserResponse;

import java.util.ArrayList;

/**
 * Created by admin on 9/30/2016.
 */
public class GoalActivity extends Activity {

    public static final String TAG = GoalActivity.class.getSimpleName();
    private UserResponse userDetails;
    private ArrayList<TenantGoalList> tenantGoalList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_goal_home);
        userDetails = new Gson().fromJson(getIntent().getStringExtra(AppConstants.INTENT_KEY.USER_DETAILS), UserResponse.class);
        AppLog.showD(TAG, userDetails.toString());
        tenantGoalList = userDetails.tenantGoalList;



    }

    protected void onResume() {
        super.onResume();
        Log.e("Token_ForeGround", WebserviceConstants.AUTHORIZATION_TOKEN);
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        //WebserviceConstants.tempVariable="NO";
        SharedPreferences settins = PreferenceManager
                .getDefaultSharedPreferences(this);

        String N = settins.getString("TimeOver", "");
        if (N.equals("YES")) {
            SharedPreferences.Editor edit = settins.edit();
            edit.putString("TimeOver", "NO");
            edit.commit();

            if(!WebserviceConstants.logout) {
                Intent intent = new Intent(AppController.getContext(), LoginActivity.class);
                //FLAG_ACTIVITY_NEW_TASK
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AppController.getContext().startActivity(intent);
            }
        }
    }
}

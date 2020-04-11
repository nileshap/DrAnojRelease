package com.endo.intellih.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.endo.intellih.R;
import com.endo.intellih.customviews.CustomTextView;
import com.endo.intellih.reports.MWT_Graph_Trend_Activity;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.webservice.Constant;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.workout.WorkoutHistoryActivity;
import com.endo.intellih.workout.Workout_Line_Chart_UserActivity_Activity;


import java.util.Calendar;


/**
 * Created by admin on 12/13/2016.
 */

/**
 * Created by admin on 12/13/2016.
 */
public class UserActivity extends FragmentActivity implements FragmentTabHost.OnTabChangeListener {

    public static final String TAG = UserActivity.class.getSimpleName();
    private GoogleApiClient mClient = null;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private FragmentTabHost mTabHost;

    private Button btnback;
    private ImageButton imgHistory, imgGraph;
    private String tabId = "";
    boolean check = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_activity);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);

        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.addTab(
                mTabHost.newTabSpec("Activity Data").
                        setIndicator(createTabIndicator("Activity Data")),
                UserActivityFragment.class, null);


        if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk")) {
            mTabHost.addTab(
                    mTabHost.newTabSpec("6 Min Walk").
                            setIndicator(createTabIndicator("6 Min Walk")),
                    SixMinWalkFragment.class, null);
        }
        else{
            mTabHost.addTab(
                    mTabHost.newTabSpec("3 Min Walk").
                            setIndicator(createTabIndicator("3 Min Walk")),
                    SixMinWalkFragment.class, null);
        }
        mTabHost.setOnTabChangedListener(this);
        tabId = "Activity Data";
        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        if (!checkPermissions()) {
            requestPermissions();
        }

        btnback = (Button) findViewById(R.id.btn_back);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        imgHistory = (ImageButton) findViewById(R.id.imgBtn_workout_home_History);
        imgHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoWorkoutHistory();
            }
        });
        imgGraph = (ImageButton) findViewById(R.id.imgBtn_workout_6mwt_chart);
        imgGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotochart();
            }
        });
//        if (tabId.equals("Activity Data")) {
//            imgGraph.setVisibility(View.GONE);
//        }
//        else{
//            imgGraph.setVisibility(View.VISIBLE);
//        }

    }


    public void gotoWorkoutHistory() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 00);
//           String select_date= Workout_Constants.webServiceDateFormat.format(calendar.getTime());
        String select_date = WebserviceConstants.dateFormatter.format(calendar.getTime());
        Intent intent = null;
        if (tabId.equals("Activity Data")) {
            intent = new Intent(this, WorkoutHistoryActivity.class);
        } else {
            intent = new Intent(this, SixMinWalkHistory.class);
        }

        Bundle bundle = new Bundle();
        bundle.putString("currentDate", select_date);
        Log.e("currentDate", select_date);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void gotochart()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 00);
//           String select_date= Workout_Constants.webServiceDateFormat.format(calendar.getTime());
        String select_date = WebserviceConstants.dateFormatter.format(calendar.getTime());
        Intent intent = null;
        if (tabId.equals("Activity Data")) {
            intent = new Intent(this, Workout_Line_Chart_UserActivity_Activity.class);
        } else {
            intent = new Intent(this, MWT_Graph_Trend_Activity.class);
        }
        WebserviceConstants.BtnLeft_Label_Title="";
        WebserviceConstants.BtnRight_Label_Title="";
        WebserviceConstants.Chart_Label_Title="";
        Bundle bundle = new Bundle();
        bundle.putString("currentDate", select_date);
        Log.e("currentDate", select_date);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    private View createTabIndicator(String label) {
        View tabIndicator = View.inflate(this, R.layout.tab_indicator_goal, null);
        tabIndicator.setBackgroundResource(R.drawable.selector_tab_goal);

        TextView tv = (TextView) tabIndicator.findViewById(R.id.label);
        tv.setText(label);

        ImageView iv = (ImageView) tabIndicator.findViewById(R.id.tab_icon);

        if (label.equals("Activity Data")) {
            iv.setImageResource(R.drawable.ac_new);
        }
        if (label.equals("6 Min Walk")) {
            iv.setImageResource(R.drawable.ac_new);
        }
        if (label.equals("3 Min Walk")) {
            iv.setImageResource(R.drawable.ac_new);
        }

        return tabIndicator;
    }

    @Override
    protected void onResume() {
        super.onResume();
        buildFitnessClient();
    }

    public void buildFitnessClient()
    {
        if (mClient == null && checkPermissions())
        {
            Log.e("BuildFitnessClient", " " + "BuildFitnessClient");

            mClient = new GoogleApiClient.Builder(this)
                    .addApi(Fitness.SESSIONS_API)
                    .addApi(Fitness.HISTORY_API)
                    .addApi(Fitness.SENSORS_API)
                    .addApi(Fitness.RECORDING_API)
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                    .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                    .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks()
                            {
                                @Override
                                public void onConnected(Bundle bundle)
                                {

                                    Log.i(TAG, "Connected!!!");
                                    // Now you can make calls to the Fitness APIs.
                                    // getDailyHistory();
                                    Log.e("Connected","Connected");

                                    addSubcribtion();
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    Log.e("Connected","Suspended");

                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.e("Connected","Suspended1");
                                        Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.e("Connected","Suspended2");
                                        Log.i(TAG,
                                                "Connection lost.  Reason: Service Disconnected");
                                    }
                                }
                            }
                    )
                    .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result)
                        {


                            Log.e("Connected","Failed");
                            Log.i(TAG, "Google Play services connection failed. Cause: " +
                                    result.toString() +"---"+result.getErrorMessage());

                            showDialog(getResources().getString(R.string.connectionfailed), 1);


                        }
                    })
                    .build();

        } else
        {
            // getDailyHistory();
        }
    }

    public void addSubcribtion()
    {

        Log.d(TAG, "addSubcribtion");
        Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_ACTIVITY_SEGMENT)
                .setResultCallback(new ResultCallback<Status>()
                {
                    @Override
                    public void onResult(Status status) {

                        Log.e("StatusCode", " " + status.isSuccess());

                        if (status.isSuccess())
                        {


                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED)
                            {
                                check = true;
                                Log.e("Check_S_1", " " + check);
                                Log.i(TAG, "Existing subscription for activity detected.");
                                //getDailyHistory();
                            } else {

                                check = true;
                                Log.e("Check_S_2", " " + check);
                                Log.i(TAG, "Successfully subscribed!");
                                // getDailyHistory();
                            }
                        } else {

                            check = false;

                            Log.e("Check_F", " " + check);

                            showDialog(getResources().getString(R.string.connectionfailed), 1);

                            Log.e("Connected","problem subscribing");
                            Log.i(TAG, "There was a problem subscribing." + status.getResolution());
                        }
                    }
                });

           Log.e("Check", " " + check);



    }


    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(UserActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    public GoogleApiClient getGoogleClient() {

        return mClient;
    }

    @Override
    public void onTabChanged(String tabId) {
        AppLog.showE(TAG, "tabId=" + tabId);
        if (tabId.equals("Activity Data")) {
            imgGraph.setVisibility(View.VISIBLE);
        }
        else{
            imgGraph.setVisibility(View.VISIBLE);
        }
        this.tabId = tabId;
    }

    public void showDialog(String message, final int no)
    {
        final Dialog dialog = new Dialog(UserActivity.this);
        LayoutInflater layoutInflater = LayoutInflater.from(UserActivity.this);
        View dialogView = layoutInflater.inflate(R.layout.fragment_dialog_rss, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        ((CustomTextView) dialogView.findViewById(R.id.txtMessage)).setText(message);
        Button btnOK = (Button) dialogView.findViewById(R.id.btnok);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Yes
                if (no == 1)
                {

                    if (dialog != null) {
                        dialog.dismiss();
                    }



                }
            }
        });

        dialog.show();
    }
}


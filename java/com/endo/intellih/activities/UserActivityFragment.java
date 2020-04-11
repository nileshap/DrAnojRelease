package com.endo.intellih.activities;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.endo.intellih.R;
import com.endo.intellih.adapter.UserRecentActivityAdapter;
import com.endo.intellih.common.CommonMethod;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.models.TodaysAeobicsList;
import com.endo.intellih.models.TodaysAerobicsSummary;
import com.endo.intellih.models.UserWorkout;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.webservice.RemoteMethods3;
import com.endo.intellih.webservice.services.request.UserActivityRequest;
import com.endo.intellih.webservice.services.response.UserActivityResponse;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class  UserActivityFragment extends Fragment {

    public static final String TAG = UserActivityFragment.class.getSimpleName();
    private ArrayList<UserWorkout> mainActivityList;
    private SimpleDateFormat dateFormatGTM = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat dateFormatSelected = new SimpleDateFormat("yyyy-MM-dd");
    private UserSharedPreferences userPrerence;
    private String lastSync = "";
    public RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private UserRecentActivityAdapter mAdapter;
    private TextView txtCalories, totalDistance, totalSteps, totalDuration;
    private View rootView;
    private GoogleApiClient mClient = null;
    CommonMethod common;
    LinearLayout layout_sync;

    public UserActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_user_activity, container, false);
        userPrerence = UserSharedPreferences.getInstance(getActivity());

        dateFormatGTM.setTimeZone(TimeZone.getTimeZone("GMT"));

        txtCalories = (TextView) rootView.findViewById(R.id.total_calories);
        totalDistance = (TextView) rootView.findViewById(R.id.total_distance);
        totalSteps = (TextView) rootView.findViewById(R.id.total_steps);
        totalDuration = (TextView) rootView.findViewById(R.id.total_duration);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.useractivity_rc);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        layout_sync = (LinearLayout)rootView.findViewById(R.id.layout_sync);

        common = new CommonMethod(getActivity());
        mainActivityList = new ArrayList<>();

        getDailyHistory();

        layout_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mainActivityList.clear();
                getDailyHistory();
            }
        });

        return rootView;
    }

    public void getDailyHistory() {

        try {
            Log.e(TAG, "getDailyHistory");
            new GoogleFitHistoryAsync().execute("");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    class GoogleFitHistoryAsync extends AsyncTask<String, Void, String> {


        public GoogleFitHistoryAsync()
        {
            mClient = ((UserActivity) getActivity()).getGoogleClient();
            CustomProgressbar.showProgressBar(getActivity(), false);

            if (!mClient.isConnected()) {
                CustomProgressbar.hideProgressBar();
                return;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            getDetailsGooglefit();
            return null;
        }


        public void getDetailsGooglefit()
        {
            try {
                Log.e(TAG, "getDetailsGooglefit");
                lastSync = userPrerence.getString(UserSharedPreferences.LAST_SYNC_ACTIVITY);

                Calendar cal = Calendar.getInstance();
                if (lastSync.equals("")) {
                    //start of the day
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                } else {
                    //last sync time
                    cal.setTimeInMillis(Long.parseLong(lastSync));
                    AppLog.showD(TAG, "old one=" + lastSync);
                }
                DataReadRequest readRequest = new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                        .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY)
                        .bucketByActivitySegment(1, TimeUnit.MILLISECONDS)
                        //.enableServerQueries()
                        .setTimeRange(cal.getTimeInMillis(), System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .build();

                lastSync = "" + System.currentTimeMillis();
                AppLog.showD(TAG, "lastSync=" + lastSync);
                userPrerence.putString(UserSharedPreferences.LAST_SYNC_ACTIVITY, lastSync);
                DataReadResult dataReadResult =
                        Fitness.HistoryApi.readData(mClient, readRequest).await(30, TimeUnit.MINUTES);
                ArrayList<UserWorkout> arrayList = new ArrayList<>();

                if (dataReadResult.getBuckets().size() > 0) {

                    for (Bucket bucket : dataReadResult.getBuckets()) {
                        Log.d(TAG, "bucket");
                        UserWorkout item = new UserWorkout();
                        List<DataSet> dataSets = bucket.getDataSets();

                        for (DataSet dataSet : dataSets) {

                            //dumpDataSet(dataSet);
                            for (DataPoint dp : dataSet.getDataPoints()) {

                                if (dp.getDataType().getName().equals("com.google.calories.expended")) {
                                    for (Field field : dp.getDataType().getFields()) {
                                        item.setCaloriesBurned(dp.getValue(field).asFloat());
                                    }
                                }
                                if (dp.getDataType().getName().equals("com.google.step_count.delta")) {
                                    for (Field field : dp.getDataType().getFields()) {
                                        item.setStepsCount(dp.getValue(field).asInt());
                                    }
                                }
                                if (dp.getDataType().getName().equals("com.google.activity.summary")) {
                                    Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                                    Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                                    //item.setStartTime(dateFormatGTM.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                                    //item.setEndTime(dateFormatGTM.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

                                    item.setStartTime(dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                                    item.setEndTime(dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

                                    Log.e("St", "star time=" + (item.getStartTime()) + ":" + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                                    Log.e("Et", "total endtime=" + (item.getEndTime()) + ":" + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

                                    for (Field field : dp.getDataType().getFields()) {
                                        if (field.getName().equals("activity")) {
                                            item.setActivityCode(dp.getValue(field).asInt());
                                        } else if (field.getName().equals("duration")) {
                                            item.setDuration(dp.getValue(field).asInt());
                                        }
                                    }
                                }
                                if (dp.getDataType().getName().equals("com.google.distance.delta")) {

                                    for (Field field : dp.getDataType().getFields()) {
                                        Log.e("Distance**", " " + dp.getValue(field).asFloat());

                                        item.setDistance(dp.getValue(field).asFloat() / 1000);
                                    }
                                }
                            }
                        }
                        Log.e(TAG, "Activity=" + item.getActivityCode());
                        Log.e(TAG, "total calories=" + item.getCaloriesBurned());
                        Log.e(TAG, "total steps=" + item.getStepsCount());
                        Log.e(TAG, "total duration=" + (item.getDuration() / 60000));
                        Log.e(TAG, "total distance=" + (item.getDistance()));
                        Log.e(TAG, "star time=" + (item.getStartTime()));
                        Log.e(TAG, "total endtime=" + (item.getEndTime()));

                        arrayList.add(item);
                    }
                }
                Log.e("SizeOfGData", "" + arrayList.size());
                for (UserWorkout obj : arrayList) {
                    Log.e("SizeOfGData-1", "" + obj.getActivityCode());
                    if ((obj.getActivityCode() != 3) && (obj.getActivityCode() != 4) && (obj.getActivityCode() != 0)) {

                    /*if(obj.getDistance() > 0.5)
                    {*/
                        Log.e("InsertDistance", " " + obj.getDistance());
                        mainActivityList.add(obj);
                   /* }
                    else
                    {
                        Log.e("InsertDistance___Not", " "  + obj.getDistance());
                    }*/
                    }
                }
                //Log.d(TAG, "" + mainActivityList.size());

                Log.e("SizeOfGData-3", "" + mainActivityList.size());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        @Override
        protected void onPostExecute(String s)
        {
            try
            {
                super.onPostExecute(s);
                Log.d(TAG, "post excute");
                CustomProgressbar.hideProgressBar();
                // if (mainActivityList.size() > 0) {
                callAPI();
                //} else {
                //   CustomProgressbar.hideProgressBar();
                // }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }

    }

    public void callAPI() {

        try {
            Log.d(TAG, "callAPI");
            CustomProgressbar.hideProgressBar();


            UserActivityRequest request = new UserActivityRequest();
            // request.noOfRecord = mainActivityList.size();
            request.patientUserId = userPrerence.getString(UserSharedPreferences.KEY_USER_ID);


            //String startDate = common.getUTCTime_1(common.dateToString(new SimpleDateFormat("yyyy-MM-dd hh:mm a"),common.getCurrentDate_2()+" "+"12:00 AM"));

            // String endDate = common.getUTCTime_1(common.dateToString(new SimpleDateFormat("yyyy-MM-dd hh:mm a"),common.getTomorrowDate()+" "+"12:00 PM"));


     /*   String startDate = common.getCurrentDate_2()+" "+"12:00 AM";

        String endDate = common.getTomorrowDate()+" "+"12:00 PM";
*/

            String startDate = common.getCurrentDate_2();

            String endDate = common.getCurrentDate_2();


            Log.e("StartDate", " " + startDate + ":" + endDate);

            request.startDateTime = startDate;
            request.endDateTime = endDate;


            //request.startDateTime
            // request.selectedDate = "13-12-2016";
            request.lstGoogleActivities = mainActivityList;

            request.utcOffsetMinutes = 0;
            CustomProgressbar.showProgressBar(getActivity(), false);
            RemoteMethods3.getWorkOutService().createGoogleFitEntry(request, new Callback<UserActivityResponse>() {
                @Override
                public void success(UserActivityResponse baseResponse, Response response) {
                    AppLog.showD(TAG, "success");

                    List<TodaysAeobicsList> malActivityList = baseResponse.getTodaysAeobicsList();

                    Collections.sort(malActivityList, new Comparator<TodaysAeobicsList>() {
                        @Override
                        public int compare(TodaysAeobicsList entry1, TodaysAeobicsList entry2) {

                            // Sort by date
                            //Log.e("Date@@", " " +entry2.getAerobicsActivityDateTimeLocal() + ":" +  entry1.getAerobicsActivityDateTimeLocal());
                            return entry2.getAerobicsActivityDateTimeLocal().compareTo(entry1.getAerobicsActivityDateTimeLocal());
                        }
                    });

                    mAdapter = new UserRecentActivityAdapter(malActivityList);
                    if (baseResponse.getTodaysAerobicsSummary() != null) {
                        setSummary(baseResponse.getTodaysAerobicsSummary());
                    }
                    mRecyclerView.setAdapter(mAdapter);
                    CustomProgressbar.hideProgressBar();
                }

                @Override
                public void failure(RetrofitError error) {
                    AppLog.showD(TAG, "failure=" + error.getMessage());
                    CustomProgressbar.hideProgressBar();
                }
            });

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    public void setSummary(TodaysAerobicsSummary todaysAerobicsSummary)
    {

        Log.e("Total Distance", " " + todaysAerobicsSummary.totalDistance);


        double distance = 0;


        if(todaysAerobicsSummary.totalDistance >0)
        {
            distance = todaysAerobicsSummary.totalDistance;

            Log.e("Total Distance-1", " " + distance );
        }
        else
        {
            distance= 0;
            Log.e("Total Distance-2", " " + distance );
        }

        //txtCalories.setText("" + String.format("%.2f", todaysAerobicsSummary.TotalCaloriesBurnt));

        txtCalories.setText("" + String.valueOf(Math.round(todaysAerobicsSummary.TotalCaloriesBurnt)));

        totalDistance.setText("" + String.format("%.2f", distance));
        //totalDuration.setText("" + todaysAerobicsSummary.totalDuration);

        totalDuration.setText(String.valueOf(Math.round(todaysAerobicsSummary.totalDuration)));

        totalSteps.setText("" + todaysAerobicsSummary.totalSteps);
    }


    private void dumpDataSet(DataSet dataSet)
    {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        for (DataPoint dp : dataSet.getDataPoints()) {

            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));

            }
        }
    }

}

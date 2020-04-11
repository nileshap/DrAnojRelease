package com.endo.intellih.activities;


import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.endo.intellih.AppFunctions;
import com.endo.intellih.R;
import com.endo.intellih.common.CommonMethod;
import com.endo.intellih.common.DecimalDigitsInputFilter;
import com.endo.intellih.customviews.CircleCountDownView;
import com.endo.intellih.customviews.CircleCountDownViewInner;
import com.endo.intellih.customviews.CustomButton;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.customviews.CustomTextView;
import com.endo.intellih.models.ActivityDetail;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.validation.AllValidation;
import com.endo.intellih.webservice.Constant;
import com.endo.intellih.webservice.RemoteMethods3;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.request.SixMinWalkCreateRequest;
import com.endo.intellih.webservice.services.response.BaseResponse;


import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SixMinWalkFragment extends Fragment implements OnDataPointListener {

    public static final String TAG = SixMinWalkFragment.class.getSimpleName();
    protected CircleCountDownView countDownView;
    MediaPlayer mp = null;
    CountDownTimer countDownTimer;
    CircleCountDownViewInner countDownTimerInner;
    CountDownTimer countDownTimer1min;
    int progress, progressInner;
    boolean isPlayed = false;
    int endTime;
    boolean isRunning = false;
    SixMinWalkCreateRequest request;
    ArrayList<ActivityDetail> activityDetails;
    CommonMethod common;
    PopupWindow popupAddExercise;
    EditText edtTime;
    EditText edtEndHR;
    EditText edt1Mins;
    EditText edtFirstHR;
    int totalHrField = 0;
    LinearLayout layout_bottom;
    private View rootView;
    private Button btnStart;
    private TextView txtDistance;
    private GoogleApiClient mClient = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private float distanceCovere = 0;
    private int stepsCount = 0;
    private TextView txtFirstHR, txtFirstHRLabel;
    private float stepLength = 0;
    private long startTime;
    private int oneMinEndtime = 60;
    private int oneMinProgress = 0;
    int final_hrr1 = 0;

    public SixMinWalkFragment() {
        // Required empty public constructor
    }

    public static void setPopupWindowTouchModal(PopupWindow popupWindow, boolean touchModal) {
        if (null == popupWindow) {
            return;
        }
        Method method;
        try {
            method = PopupWindow.class.getDeclaredMethod("setTouchModal", boolean.class);
            method.setAccessible(true);
            method.invoke(popupWindow, touchModal);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void onDestroy() {

        super.onDestroy();
        if (progress > 1) {
            countDownView.setProgress(0, endTime);
            countDownTimer.cancel();
            countDownTimerInner.setProgress(0, 60);
            progress = 1;
        }

    }

    public void onResume() {

//            countDownView.setProgress(0, endTime);
//            countDownTimer.cancel();
//            progress=1;

        super.onResume();
        if (isPlayed) {
            isPlayed = false;
            mp.stop();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_six_min_walk, container, false);
        countDownView = (CircleCountDownView) rootView.findViewById(R.id.circle_count_down_view);
        countDownTimerInner = (CircleCountDownViewInner) rootView.findViewById(R.id.circle_count_down_view_inner);
        common = new CommonMethod(getActivity());
        btnStart = (Button) rootView.findViewById(R.id.btn_start);
        txtDistance = (TextView) rootView.findViewById(R.id.txt_distance);
        txtFirstHR = (TextView) rootView.findViewById(R.id.edt_first_hr);
        txtFirstHRLabel = (TextView) rootView.findViewById(R.id.first_hr);
        if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk")) {
            countDownView.setProgress(0, 360);
            btnStart.setText("Start 6 Min Walk");
            txtFirstHRLabel.setText("Heart Rate before 6 Min walk");
        } else {
            countDownView.setProgress(0, 180);
            btnStart.setText("Start 3 Min Walk");
            txtFirstHRLabel.setText("Heart Rate before 3 Min walk");
        }
        countDownTimerInner.setProgress(0, 60);


//        mp = MediaPlayer.create(R.raw.threemin,R.raw.sixmin,R.raw.sixminfinish);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (isRunning == false) {


                    checkForStartTimer();
                } else {

                    stopTimer();
                    if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk")) {
                        btnStart.setText("Start 6 Min Walk");
                    } else {
                        btnStart.setText("Start 3 Min Walk");
                    }
                }
            }
        });

        mClient = ((UserActivity) getActivity()).getGoogleClient();

        if (!mClient.isConnected()) {
            AppLog.showD(TAG, "not connected");
        }

        getStepLength();


        return rootView;
    }

    public void getStepLength() {

        stepLength = (float) (61 * 0.413);
    }

    public void checkForStartTimer()
    {

//        if (txtFirstHR.getText().toString() != null && txtFirstHR.getText().toString().length() > 1) {
        startTimer();
        isRunning = true;
        if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk")) {
            btnStart.setText("Stop 6 Min Walk");
        } else {
            btnStart.setText("Stop 3 Min Walk");
        }
//        } else {
//            showDialog("Please enter Heart Rate before 6 Min Walk");
//        }
    }

    public void stopTimer() {
        AppLog.showD(TAG, "stop timer");
        isRunning = false;
        String currentPro = countDownView.getProgress();
        String distance = txtDistance.getText().toString();
        String firstHR = txtFirstHR.getText().toString();
        countDownView.setProgress(0, endTime);
        countDownTimerInner.setProgress(0, 60);
        countDownTimer.cancel();
        txtDistance.setText("00.00");
        txtFirstHR.setText("");
        removeListener();
        AppLog.showD(TAG, "currentPro=" + currentPro);
        long min = Integer.parseInt(currentPro.substring(0, 2));
        long sec = Integer.parseInt(currentPro.substring(3));

        long t = (min * 60) + sec;

        long result = t;

        request.setDistance(Double.parseDouble(distance));
        request.setDuration("" + result);
        request.setDistanceUnitId("20");

        activityDetails = new ArrayList<>();
        if (!firstHR.isEmpty()) {
            ActivityDetail HrFirst = new ActivityDetail();


            HrFirst.setHeartRate(firstHR);
            HrFirst.setHeartRateMinuteIndex(1);
            HrFirst.setReadingDateTime("" + request.getStartDateTime());
            activityDetails.add(HrFirst);
        }
        showSixMinwalkSummary(currentPro, distance, firstHR, "" + getCurrentTimeUTC());
    }

    public void removeListener()
    {
        if (mClient != null && mClient.isConnected())
        {
            AppLog.showD(TAG, "removeListener");
            Fitness.SensorsApi.remove(
                    mClient,
                    this)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "Listener was removed!");
                            } else {
                                Log.i(TAG, "Listener was not removed.");
                            }
                        }
                    });
        } else {
            AppLog.showE(TAG, "removeListener mclient null and not connected");
            showDialog("Google Fit not connected");
        }
    }


//    private void registerFitnessDataListener() {
//
//        AppLog.showD(TAG, "registerFitnessDataListener");
//        SensorRequest request = new SensorRequest.Builder()
//                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
//                .setSamplingRate(3, TimeUnit.SECONDS)
//                .setAccuracyMode(SensorRequest.ACCURACY_MODE_HIGH)
//                .setTimeout(7, TimeUnit.MINUTES)
//                .build();
//
//        Fitness.SensorsApi.add(mClient, request, this)
//                .setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(Status status) {
//                        if (status.isSuccess()) {
//                            Log.e("GoogleFit", "SensorApi successfully added");
//                        }
//                    }
//                });
//    }

    public void startTimer()
    {

        if (!mClient.isConnected()) {
            AppLog.showE(TAG, "not connected");
            showDialog("Google Fit not connected please try after sometime.");
        }

        AppLog.showD(TAG, "startTimer");
        progress = 1;
        progressInner = 0;
        if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk"))
            endTime = 360;
        else
            endTime = 180;
        distanceCovere = 0;
        stepsCount = 0;
        txtDistance.setText("00.00");
        countDownTimer = new CountDownTimer(endTime * 1000 /*finishTime**/, 1000 /*interval**/) {
            @Override
            public void onTick(long millisUntilFinished)
            {
                countDownView.setProgress(progress, endTime);
                progress = progress + 1;
                progressInner = progressInner + 1;
                if (progressInner > 59) {
                    progressInner = 0;
                }
                countDownTimerInner.setProgress(progressInner, 60);
                Log.e("Progress", "" + progress);
                if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk"))
                {
                    if (progress == 150)
                    {
                        isPlayed = true;
                        mp = MediaPlayer.create(getActivity(), R.raw.threemin);
                        mp.start();
                    }
                    if (progress == 160) {
                        isPlayed = false;
                        mp.stop();
                    }
                    if (progress == 330) {
                        isPlayed = true;
                        mp = MediaPlayer.create(getActivity(), R.raw.sixmin);
                        mp.start();
                    }
                    if (progress == 340) {
                        isPlayed = false;
                        mp.stop();
                    }
                    if (progress == 356) {
                        isPlayed = true;
                        mp = MediaPlayer.create(getActivity(), R.raw.sixminfinish);
                        mp.start();
                    }
                } else {
                    if (progress == 150) {
                        isPlayed = true;
                        mp = MediaPlayer.create(getActivity(), R.raw.threemin);
                        mp.start();
                    }
                }

//                if (progress==360){
//                    mp.stop();
//                }
            }

            @Override
            public void onFinish() {
                if (isPlayed) {
                    isPlayed = false;

                    mp.stop();
                }
                countDownView.setProgress(progress, endTime);
                // AppLog.showD(TAG, "onFinish");
                stopTimer();
                //view.setVisibility(View.VISIBLE);
                //cancelTimerBt.setVisibility(View.GONE);
            }
        };
        Log.e("StartSensorData", " " + "StartSensorData-0");
        countDownTimer.start(); // start timer
        startTime = System.currentTimeMillis();
        startSensorData();
        request = new SixMinWalkCreateRequest();
        request.setUserId(UserSharedPreferences.getInstance(getActivity()).getString(UserSharedPreferences.KEY_USER_ID));
        request.setStartDateTime(getCurrentTimeUTC());

        //  startSession();
    }

    public void startSensorData() {
        AppLog.showD(TAG, "startSensorData");

        Log.e("StartSensorData", " " + "StartSensorData-1");

        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                Log.e("StartSensorData", " " + "StartSensorData-2");
                AppLog.showD(TAG, "datasource result =onResult");
                AppLog.showE(TAG, "data source size=" + dataSourcesResult.getDataSources().size());
                for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                    //  AppLog.showE(TAG, dataSource.getName());
                    Log.e("StartSensorData", " " + "StartSensorData-3");
                    AppLog.showE(TAG, " data source type=" + dataSource.getDataType());
                    if (DataType.TYPE_STEP_COUNT_DELTA.equals(dataSource.getDataType())) {
                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_DELTA);
                        Log.e("StartSensorData", " " + "StartSensorData-4");


                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(mClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);

        // registerFitnessDataListener();
    }

    //
    private void registerFitnessDataListener(DataSource mDataSource, DataType mDataType) {

        AppLog.showD(TAG, "registerFitnessDataListener");
        SensorRequest request = new SensorRequest.Builder()
                // .setAccuracyMode(SensorRequest.ACCURACY_MODE_HIGH)
                .setDataSource(mDataSource)
                .setDataType(mDataType)
                .setSamplingRate(10, TimeUnit.SECONDS)
                .setFastestRate(3, TimeUnit.MILLISECONDS)
                .build();

        Fitness.SensorsApi.add(mClient, request, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.e("GoogleFit", "SensorApi successfully added");
                        } else {
                            Log.e("GoogleFit", "SensorApi not connected -" + status.getStatusMessage());
                            Log.e("GoogleFit", "SensorApi not connected -" + status.getResolution());
                        }
                    }
                });
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {

        Log.e("StartSensorData", " " + "StartSensorData-5");
        AppLog.showD(TAG, "onDataPoint");
        AppLog.showD(TAG, "starttime walking=" + startTime);
        AppLog.showD(TAG, "starttime=" + dataPoint.getStartTime(TimeUnit.MILLISECONDS));
        AppLog.showD(TAG, "endtime=" + dataPoint.getEndTime(TimeUnit.MILLISECONDS));

        if (startTime > dataPoint.getStartTime(TimeUnit.MILLISECONDS)) {
            AppLog.showD(TAG, "wrong data");
            return;
        }
        for (final Field field : dataPoint.getDataType().getFields()) {

            final Value value = dataPoint.getValue(field);
            AppLog.showD(TAG, "onDataPoint-" + value);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stepsCount = stepsCount + value.asInt();
                    AppLog.showD(TAG, "Steps=" + stepsCount);
                    AppLog.showD(TAG, "stesps lenght=" + stepLength);
                    //distanceCovere = distanceCovere + value.asFloat();
                    distanceCovere = (float) ((stepsCount * stepLength) * 0.0254);
                    AppLog.showD(TAG, "distanceCovere=" + distanceCovere);
                    // distanceCovere = value.asFloat();
                    //  txtDistance.setText("" +stepsCount );
                    txtDistance.setText("" + String.format("%.2f", distanceCovere));
                    // Toast.makeText(getActivity().getApplication(), "Field: " + field.getName() + " Value: " + value, Toast.LENGTH_SHORT).show();
                    // AppLog.showE(TAG, "Field: " + field.getName() + " Value: " + value);
                }
            });
        }
    }

    private void dumpDataSet(DataSet dataSet) {
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

    public void showDialog(String message) {

        final Dialog dialog = new Dialog(getActivity());
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View dialogView = layoutInflater.inflate(R.layout.fragment_dialog_rss, null);
        dialog.setContentView(dialogView);

        ((CustomTextView) dialogView.findViewById(R.id.txtMessage)).setText(message);
        Button btnOK = (Button) dialogView.findViewById(R.id.btnok);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Yes
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showDialog1(String message) {

        final Dialog dialog = new Dialog(getActivity());
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View dialogView = layoutInflater.inflate(R.layout.fragment_dialog_rss, null);
        dialog.setContentView(dialogView);

        ((CustomTextView) dialogView.findViewById(R.id.txtMessage)).setText(message);
        Button btnOK = (Button) dialogView.findViewById(R.id.btnok);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Yes
                dialog.dismiss();


            }
        });

        dialog.show();
    }

    public String showSixMinwalkSummary(String time, String distance, String firstHR, final String CurrentTimeUTC) {
        if (isPlayed) {
            isPlayed = false;
            mp.stop();
        }
        AppLog.showE(TAG, "showStartExcercisePopup");
        int popupWidth = 460;
        int screenWidth;
        int pointX;
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;

        popupWidth = screenWidth - 20;
        pointX = (screenWidth - popupWidth) / 2;
        View view = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.pop_up_end_six_min, null);

        popupAddExercise = new PopupWindow(getActivity());
        popupAddExercise.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupAddExercise.setContentView(view);

        popupAddExercise.setWidth(popupWidth);
        popupAddExercise.setHeight(-2);

//        popupAddExercise.setOutsideTouchable(true);
//        popupAddExercise.setFocusable(true);
        setPopupWindowTouchModal(popupAddExercise, false);
        popupAddExercise.setOutsideTouchable(false);
        popupAddExercise.setFocusable(true);
        popupAddExercise.setAnimationStyle(R.style.workout_AddExerciseAnimation);
        popupAddExercise.showAtLocation(view, 0, pointX, 80);

        Button btnOk = (Button) view.findViewById(R.id.btnOK_workout_add_exercise);
        CustomButton btnClose = (CustomButton) view.findViewById(R.id.btnOK_close_exercise);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddExercise.dismiss();
            }
        });
        CustomTextView txtTitleresult = (CustomTextView) view.findViewById(R.id.txtTitle_result);
        CustomTextView txtHr1 = (CustomTextView) view.findViewById(R.id.txt_hr1);
        CustomTextView txtHR2 = (CustomTextView) view.findViewById(R.id.txt_hr2);
        CustomTextView txtHR3 = (CustomTextView) view.findViewById(R.id.txt_hr3);

        layout_bottom = (LinearLayout) view.findViewById(R.id.layout_bottom_pop_up_end_six_min);

        if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk")) {
            txtTitleresult.setText("6 Min Walk Result");
            txtHr1.setText("HR before 6 Min Walk");
            txtHR2.setText("HR after 6 Min Walk");
            txtHR3.setText("HR after 1 min of 6 Min Walk");
        } else {
            txtTitleresult.setText("3 Min Walk Result");
            txtHr1.setText("HR before 3 Min Walk");
            txtHR2.setText("HR after 3 Min Walk");
            txtHR3.setText("HR after 1 min of  Min Walk");
        }
        edtTime = (EditText) view.findViewById(R.id.edt_time);
        edtTime.setText("" + time);
        edtTime.setAlpha((float) 0.5);
        edtTime.setEnabled(false);

        final EditText edtDistance = (EditText) view.findViewById(R.id.edt_distance);

        edtDistance.setText("" + distance);
        edtDistance.setAlpha((float) 0.5);
        edtDistance.setEnabled(false);


        edtFirstHR = (EditText) view.findViewById(R.id.edt_first_hr);
        edtFirstHR.setText("" + firstHR);
        // edtFirstHR.setAlpha((float) 0.5);
        edtFirstHR.setEnabled(true);


        final TextView txtWait = (TextView) view.findViewById(R.id.txt_wait);

        edtEndHR = (EditText) view.findViewById(R.id.edt_end_hr); // second hr
        edtEndHR.setText("");

        final LinearLayout thirdHRln = (LinearLayout) view.findViewById(R.id.ll_third_hr);
        final LinearLayout layout_hhr1 = (LinearLayout) view.findViewById(R.id.layout_hhr1_pop_up_end_six_min);

        final TextView textView_hhr1 = (TextView) view.findViewById(R.id.textView_hhr1_pop_up_end_six_min);

        start1MinTimer(txtWait, thirdHRln, layout_hhr1);

        edt1Mins = (EditText) view.findViewById(R.id.edt_1min_hr); // third hr
        edt1Mins.setText("");

        setValidation(edtFirstHR, 27);
        setValidation(edtEndHR, 27);
        setValidation(edt1Mins, 27);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


//                if (edtEndHR.getText().toString().trim().length() == 0) {
//                    showDialog("Please enter Heart rate after 6 Min Walk.");
//                }
//                else if (edt1Mins.getText().toString().trim().length() == 0) {
//                    showDialog("Please enter Heart rate after 1 Min.");
//                } else {


                String value1 = edtFirstHR.getText().toString();
                String value2 = edtEndHR.getText().toString();
                String value3 = edt1Mins.getText().toString();
                String value4 = textView_hhr1.getText().toString();

                if(value2.isEmpty())
                {
                     showDialog1_one(2,getResources().getString(R.string.hrsecondshouldnotbeblank));
                     edtEndHR.requestFocus();
                }
                else if(value3.isEmpty())
                {
                    showDialog1_one(2,getResources().getString(R.string.hrthirdshouldnotbeblank));
                    edt1Mins.requestFocus();
                }
                else if(final_hrr1 < 0)
                {
                    showDialog1_one(2, getResources().getString(R.string.hrrshouldnotnegative));

                }
                else
                {
                    FirstField(CurrentTimeUTC);
                }
            }
        });


        edtEndHR.addTextChangedListener(new TextWatcher()
        {


            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {

                if (s.length() != 0)
                {
                    setHHR1(s.toString(),edt1Mins.getText().toString(),textView_hhr1);
                    layout_hhr1.setVisibility(View.VISIBLE);
                }
                else
                {
                    setHHR1("0",edt1Mins.getText().toString(),textView_hhr1);
                    layout_hhr1.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });

        edt1Mins.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {

                if (s.length() != 0)
                {
                    setHHR1(edtEndHR.getText().toString(),s.toString(),textView_hhr1);
                    layout_hhr1.setVisibility(View.VISIBLE);
                }
                else
                {
                    setHHR1(edtEndHR.getText().toString(),s.toString(),textView_hhr1);
                    layout_hhr1.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        return null;
    }


    public void setHHR1(String secondhr, String thirdhr,TextView ed_hhr1)
    {
        int second = 0;
        int third = 0;
        int hhr1 = 0;

        if (secondhr.length() != 0)
        {
            ed_hhr1.setVisibility(View.VISIBLE);
            second = Integer.parseInt(secondhr.toString());
            if(!thirdhr.equalsIgnoreCase(""))
            {
                third = Integer.parseInt(thirdhr);

                hhr1 = second - third;
                final_hrr1 = hhr1;
            }
            ed_hhr1.setText("Your HRR1 value is "+String.valueOf(hhr1)+" BPM");
        }
        else if(secondhr.length() == 0 && thirdhr.length() == 0)
        {
            ed_hhr1.setText("Your HRR1 value is "+ 0 +" BPM");
        }
    }


    public void callAPI(String time) {

        totalHrField = 0;
        if (!edtFirstHR.getText().toString().isEmpty()) {
            ArrayList<ActivityDetail> activityDetails_final = new ArrayList<ActivityDetail>();
            if (!edtFirstHR.getText().toString().isEmpty()) {

                ActivityDetail HrSecond = new ActivityDetail();

                HrSecond.setHeartRateMinuteIndex(1);
                HrSecond.setReadingDateTime(time);
                HrSecond.setHeartRate(edtFirstHR.getText().toString());
                activityDetails_final.add(HrSecond);


            }

            if (!edtEndHR.getText().toString().isEmpty()) {
                ActivityDetail HrSecond = new ActivityDetail();
                HrSecond.setHeartRateMinuteIndex(2);
                HrSecond.setReadingDateTime(time);
                HrSecond.setHeartRate(edtEndHR.getText().toString());
                activityDetails_final.add(HrSecond);
            }

            if (!edt1Mins.getText().toString().isEmpty()) {
                ActivityDetail HrThird = new ActivityDetail();
                HrThird.setHeartRate(edt1Mins.getText().toString());
                HrThird.setReadingDateTime("" + getCurrentTimeUTC());
                HrThird.setHeartRateMinuteIndex(3);
                activityDetails_final.add(HrThird);
            }
            request.setActivityDetails(activityDetails_final);

        } else {

            if (!edtEndHR.getText().toString().isEmpty()) {
                ActivityDetail HrSecond = new ActivityDetail();
                HrSecond.setHeartRateMinuteIndex(2);
                HrSecond.setReadingDateTime(time);
                HrSecond.setHeartRate(edtEndHR.getText().toString());
                activityDetails.add(HrSecond);
            }

            if (!edt1Mins.getText().toString().isEmpty()) {
                ActivityDetail HrThird = new ActivityDetail();
                HrThird.setHeartRate(edt1Mins.getText().toString());
                HrThird.setReadingDateTime("" + getCurrentTimeUTC());
                HrThird.setHeartRateMinuteIndex(3);
                activityDetails.add(HrThird);
            }
            request.setActivityDetails(activityDetails);
        }


        AppLog.showD(TAG, "callAPi");
        Log.e("CallActivity", " " + "CreateActivity 6 minute");
        request.setUtcOffsetMinutes(common.getUtcOffsetMinutes());
        CustomProgressbar.showProgressBar(

                getActivity(), false);
        if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk")) {
            RemoteMethods3.getWorkOutService().sixMinWalkEntry(request, new Callback<BaseResponse>() {
                @Override
                public void success(BaseResponse baseResponse, Response response) {
                    AppLog.showD(TAG, "success");


                   // Constant.APPLICATION_SESSION = false;
                    AppFunctions.showMessageDialog(getActivity(), baseResponse.Message);
                    CustomProgressbar.hideProgressBar();
                }

                @Override
                public void failure(RetrofitError error) {
                    AppLog.showD(TAG, "fail");
                    CustomProgressbar.hideProgressBar();
                }
            });
        } else {
            RemoteMethods3.getWorkOutService().threeMinWalkEntry(request, new Callback<BaseResponse>() {
                @Override
                public void success(BaseResponse baseResponse, Response response) {
                    AppLog.showD(TAG, "success");
                    AppFunctions.showMessageDialog(getActivity(), baseResponse.Message);
                    CustomProgressbar.hideProgressBar();
                }

                @Override
                public void failure(RetrofitError error) {
                    AppLog.showD(TAG, "fail");
                    CustomProgressbar.hideProgressBar();
                }
            });
        }

        common.setFalseValidation();


    }

    public void start1MinTimer(final TextView view, final LinearLayout linearLayout, final LinearLayout layout_hhr1) {
        AppLog.showD(TAG, " start1MinTimer");
        oneMinProgress = 0;
        countDownTimer1min = new CountDownTimer(oneMinEndtime * 1000 /*finishTime**/, 1000 /*interval**/) {
            @Override
            public void onTick(long millisUntilFinished) {
                AppLog.showD(TAG, "on tick");
                view.setText("Wait for " + oneMinProgress + "/60 (secs) to enter third HR");
                oneMinProgress = oneMinProgress + 1;
            }

            @Override
            public void onFinish() {
                AppLog.showD(TAG, "1 min stop timer");
                stop1MinTimer(view, linearLayout, layout_hhr1);
            }
        };
        countDownTimer1min.start();
    }

    public void stop1MinTimer(final TextView view, LinearLayout linearLayout, LinearLayout layout_hhr1) {
        AppLog.showD(TAG, "stop1MinTimer");
        view.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);
        layout_hhr1.setVisibility(View.VISIBLE);


      /*  LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        params.topMargin = 40;
        layout_bottom.setLayoutParams(params);*/


    }

    public String getCurrentTimeUTC() {
        Date myDate = new Date();
        Calendar calendar = Calendar.getInstance();
        // calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(myDate);
        Date time = calendar.getTime();
        String dateAsString = WebserviceConstants.dateFormatter.format(time);
        System.out.println(dateAsString);
        return dateAsString;

    }

    public void showDialogOKCancel(final String time, final int no, final String type, final int mpos, final int pos, String message) {

        final Dialog dialog = new Dialog(getActivity());
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View dialogView = layoutInflater.inflate(R.layout.dialog_error, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        ((CustomTextView) dialogView.findViewById(R.id.message)).setText(message);
        Button btnOK = (Button) dialogView.findViewById(R.id.dialog_ok);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Yes

                dialog.dismiss();
                if (type.equalsIgnoreCase("R")) {
                    WebserviceConstants.allvalidation.get(mpos).getValidationRangeList().get(pos).setShown(true);
                } else if (type.equalsIgnoreCase("V")) {
                    WebserviceConstants.allvalidation.get(mpos).getValidationValueToCompareList().get(pos).setShown(true);
                }

                Log.e("TotalHrField", " " + totalHrField);

                if (totalHrField == 2) {
                    callAPI(time);


                    popupAddExercise.dismiss();
                } else {

                    totalHrField = totalHrField + 1;
                    FirstField(time);

                }


            }
        });
        Button btnCabcel = (Button) dialogView.findViewById(R.id.dialog_cancel);
        btnCabcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Yes

                dialog.dismiss();


            }
        });

        dialog.show();
    }

    public void showDialog1_one(final int no, String message) {

        final Dialog dialog = new Dialog(getActivity());
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View dialogView = layoutInflater.inflate(R.layout.fragment_dialog_rss, null);
        dialog.setContentView(dialogView);

        ((CustomTextView) dialogView.findViewById(R.id.txtMessage)).setText(message);
        Button btnOK = (Button) dialogView.findViewById(R.id.btnok);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Yes


                dialog.dismiss();


            }
        });

        dialog.show();
    }

    public void setValidation(EditText edtValue, int factorid) {
        AppLog.showE(TAG, "setValidation");


        for (int v = 0; v < WebserviceConstants.allvalidation.size(); v++) {
            boolean check = false;
            if (factorid == WebserviceConstants.allvalidation.get(v).getFactorId()) {

                for (int r = 0; r < WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().size(); r++) {


                    String dataType = WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getDataType();
                    Log.e("SetDataType", "  " + factorid + ":" + dataType + ":" + WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength());
                    if (dataType.equals("Integer")) {
                        Log.e("SetDataType-iNTEGER", "  " + factorid + ":" + dataType);
                        edtValue.setRawInputType(Configuration.KEYBOARD_12KEY);
                        edtValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength())});
                    } else if (dataType.equals("Decimal")) {
                        Log.e("SetDataType-dECIMAL", "  " + factorid + ":" + dataType);
                        edtValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        edtValue.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength(), WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getDecimalPlaceLength())});
                    }
                    //setting length
                    check = true;


                    Log.e("SetValidation###", " " + factorid);

                }
            }

            Log.e("Check", " " + check);
            if (check) {
                break;
            }
        }

    }

    public void FirstField(String CurrentTimeUTC) {

        Log.e("TotalHrM", " " + totalHrField);

        String value1 = "";
        for (int m = totalHrField; m < 3; m++) {

            if (m == 0) {
                value1 = edtFirstHR.getText().toString();
                edtFirstHR.requestFocus();
            } else if (m == 1) {
                value1 = edtEndHR.getText().toString();
                edtEndHR.requestFocus();
            } else if (m == 2) {
                value1 = edt1Mins.getText().toString();
                edt1Mins.requestFocus();
            }

            if (!value1.equals("")) {
                Log.e("Call-1", "Call" + ":" + value1);

                HashMap<String, Integer> returnMap = AllValidation.rangeValidation(27, value1, WebserviceConstants.allvalidation);
                AppLog.showD(TAG, "returnMap status=" + returnMap.get(AllValidation.STATUS) + ":" + returnMap.get(AllValidation.POSITION) + ":" + returnMap.get(AllValidation.MPOSITION));

                if (returnMap.get(AllValidation.STATUS) == 0 && Float.parseFloat(value1) != 0) {
                    Log.e("RangePosiiton-Shown", "mPOSITION=" + WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isShown());
                    Log.e("Call-2", "Call");
                    if (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isIsWarning()
                            && (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isShown() == false)) {
                        Log.e("Call-3", "Call");
                        totalHrField = m;
                        showDialogOKCancel(CurrentTimeUTC, 1, "R", returnMap.get(AllValidation.MPOSITION), returnMap.get(AllValidation.POSITION), WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).getCustomMessage());
                        return;

                    } else if (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isIsWarning() == false) {
                        Log.e("Call-4", "Call");

                        showDialog1_one(1, WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).getCustomMessage());
                        return;

                    } else {
                        Log.e("Call-5", "Call");
                        AppLog.showD(TAG, "warning removed range");

                        if (m == 2) {

                            callAPI(CurrentTimeUTC);


                            popupAddExercise.dismiss();
                            break;
                        }
                    }
                }


                HashMap<String, Integer> returnValueToComp = AllValidation.valueToCompareValidation(27, value1, WebserviceConstants.allvalidation);

                Log.e("ValueCompare-*", " " + returnValueToComp.get(AllValidation.STATUS) + ":" + returnValueToComp.get(AllValidation.MPOSITION) + ":" + returnValueToComp.get(AllValidation.POSITION) + ":" + returnValueToComp.get(AllValidation.MPOSITION));


                if (returnValueToComp.get(AllValidation.STATUS) == 0)
                {
                    Log.e("Call-6", "Call");
                    Log.e("ValueCompare-1", " " + WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isIsWarning() + ":" + WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isShown() + ":" + WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isIsWarning());
                    // (WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isShown() == false
                    if (WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isIsWarning() &&
                            (WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isShown() == false)) {
                        // WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).setShown(true);
                        Log.e("Call-6", "Call");
                        totalHrField = m;
                        showDialogOKCancel(CurrentTimeUTC, 1, "V", returnValueToComp.get(AllValidation.MPOSITION), returnValueToComp.get(AllValidation.POSITION), WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).getCustomMessage());
                        return;
                    } else if (WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isIsWarning() == false) {
                        Log.e("Call-8", "Call");
                        showDialog1_one(1, WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).getCustomMessage());
                        return;
                    } else {
                        AppLog.showD(TAG, "warning removed");
                        Log.e("Call-9", "Call");

                        if (m == 2) {

                            callAPI(CurrentTimeUTC);
                            popupAddExercise.dismiss();
                            break;
                        }

                    }
                } else {
                    if (m == 2) {

                        callAPI(CurrentTimeUTC);


                        popupAddExercise.dismiss();
                        break;
                    }
                }


            } else {
                totalHrField = m + 1;
                if (m == 2) {
                    Log.e("Call-10", "Call");
                    callAPI(CurrentTimeUTC);


                    popupAddExercise.dismiss();

                    break;
                }


            }

            Log.e("CountM", " " + m);
        }

    }


}



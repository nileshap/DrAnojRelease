package com.endo.intellih.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.endo.intellih.R;
import com.endo.intellih.adapter.SixMinListAdapter;
import com.endo.intellih.common.BaseActivity;
import com.endo.intellih.common.CommonMethod;
import com.endo.intellih.common.DecimalDigitsInputFilter;
import com.endo.intellih.customviews.CustomButton;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.customviews.CustomTextView;
import com.endo.intellih.listener.OnItemClickListener;
import com.endo.intellih.medicine.data.Medicine_Constants;
import com.endo.intellih.models.SixMinWalkList;
import com.endo.intellih.models.SixMinWalkUpdate;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.validation.AllValidation;
import com.endo.intellih.webservice.RemoteMethods;
import com.endo.intellih.webservice.RemoteMethods3;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.request.UpdateSixMinuteActivity;
import com.endo.intellih.webservice.services.response.BaseResponse;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by admin on 2/10/2017.
 */
public class SixMinWalkHistory extends BaseActivity {

    public static final String TAG = SixMinWalkHistory.class.getSimpleName();
    private Gson gson;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private Button btnBack;
    private TextView txtTitle, txtNodata;
    private TAB selectedTab = TAB.TODAY;
    private SimpleDateFormat sdf;
    private ArrayList<SixMinWalkList> minWalkListArrayList;
    private SixMinListAdapter mAdapter;

     PopupWindow popupAddExercise;
     EditText edtFirstHR;
    EditText edtEndHR;
 EditText edt1Mins;
    SixMinWalkList sixMinWalkList;
    int totalHrField = 0;
    CommonMethod common;
    int final_hrr1 = 0;
    LinearLayout layout_header;

    private enum TAB {
        TODAY(1), WEEKLY(2), MONTHLY(3);

        final int code;

        TAB(int id) {
            code = id;
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vital_list);
        common = new CommonMethod(SixMinWalkHistory.this);

        layout_header = (LinearLayout)findViewById(R.id.layout_header) ;
        layout_header.setVisibility(View.VISIBLE);

        btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        txtNodata = (TextView) findViewById(R.id.txt_nodata);
        txtTitle = (TextView) findViewById(R.id.txt_title);
        if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk")) {
            txtTitle.setText("6 Min Walk History");
        }
        else{
            txtTitle.setText("3 Min Walk History");
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        minWalkListArrayList = new ArrayList<>();
        callVitalListAPI(0);
    }


    public void onSelectToday(View view) {
        AppLog.showD(TAG, "onSelectToday");
        selectedTab = TAB.TODAY;
        callVitalListAPI(0);
    }

    public void onSelectWeekly(View view) {
        AppLog.showD(TAG, "onSelectWeekly");
        selectedTab = TAB.WEEKLY;
        callVitalListAPI(6);
    }


    public void onSelectMonthly(View view) {
        AppLog.showD(TAG, "onSelectMonthly");
        selectedTab = TAB.MONTHLY;
        callVitalListAPI(30);
    }

    public void callVitalListAPI(int days) {
        AppLog.showD(TAG, "callVitalListAPI");
        CustomProgressbar.showProgressBar(this, false);
        Calendar calendarToDate = Calendar.getInstance();

        calendarToDate.set(Calendar.HOUR_OF_DAY, 00);
        calendarToDate.set(Calendar.MINUTE, 00);
        calendarToDate.set(Calendar.SECOND, 00);

        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        int minuteToAdd = (int) TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS);
        // Log.e("minutes", "" + TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS));

        Calendar calendarGMT = Calendar.getInstance();
        calendarGMT.setTime(calendarToDate.getTime());
        calendarGMT.add(Calendar.MINUTE, (-minuteToAdd));

        calendarToDate.set(Calendar.HOUR_OF_DAY, 23);
        calendarToDate.set(Calendar.MINUTE, 59);

        String date = common.getCurrentDate_4();

        String userId = UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID);
        if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk")) {
            RemoteMethods3.getWorkOutService().getActivity(userId, date,
                    days,  new Callback<List<SixMinWalkList>>() {
                        @Override
                        public void success(List<SixMinWalkList> sixMinWalkLists, Response response) {
                            AppLog.showD(TAG, "success count=" + sixMinWalkLists.size());

                            if (sixMinWalkLists.size() > 0) {
                                txtNodata.setVisibility(View.GONE);
                                mAdapter = new SixMinListAdapter((ArrayList<SixMinWalkList>) sixMinWalkLists, SixMinWalkHistory.this);
                                mAdapter.setOnItemClickListener(new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        AppLog.showD(TAG, "ontap=" + position);
                                        //showDeleteConfirmationDialog(position);
                                        showDeleteUpdateDialog(position);
                                    }
                                });
                                mRecyclerView.setVisibility(View.VISIBLE);
                                mRecyclerView.setAdapter(mAdapter);
                            } else {
                                mRecyclerView.setVisibility(View.INVISIBLE);
                                txtNodata.setVisibility(View.VISIBLE);
                            }

                            CustomProgressbar.hideProgressBar();

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            AppLog.showD(TAG, "fail=" + error.getMessage());
                            CustomProgressbar.hideProgressBar();
                        }
                    });
        }
        else{
            RemoteMethods.getWorkOutService().getthreeMinHistory(userId, Medicine_Constants.webServiceDateFormat.format(calendarGMT.getTime()),
                    days, minuteToAdd, new Callback<List<SixMinWalkList>>() {
                        @Override
                        public void success(List<SixMinWalkList> sixMinWalkLists, Response response) {
                            AppLog.showD(TAG, "success count=" + sixMinWalkLists.size());

                            if (sixMinWalkLists.size() > 0) {
                                txtNodata.setVisibility(View.GONE);
                                mAdapter = new SixMinListAdapter((ArrayList<SixMinWalkList>) sixMinWalkLists, SixMinWalkHistory.this);
                                mAdapter.setOnItemClickListener(new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        AppLog.showD(TAG, "ontap=" + position);
                                        //showDeleteConfirmationDialog(position);
                                        showDeleteUpdateDialog(position);
                                    }
                                });
                                mRecyclerView.setVisibility(View.VISIBLE);
                                mRecyclerView.setAdapter(mAdapter);
                            } else {
                                mRecyclerView.setVisibility(View.INVISIBLE);
                                txtNodata.setVisibility(View.VISIBLE);
                            }

                            CustomProgressbar.hideProgressBar();

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            AppLog.showD(TAG, "fail=" + error.getMessage());
                            CustomProgressbar.hideProgressBar();
                        }
                    });
        }
    }


    public void showDeleteUpdateDialog(final int position)
    {
        final Dialog dialog = new Dialog(SixMinWalkHistory.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Include dialog.xml file
        dialog.setContentView(R.layout.alert_dialog_cutome_layout);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        // Set dialog title
        // set values for custom dialog components - text, image and button

        dialog.getWindow().setAttributes(lp);

        CustomTextView txt_title = (CustomTextView) dialog.findViewById(R.id.textView_title_alert_dialog_custome);

        LinearLayout layout_cancel = (LinearLayout) dialog.findViewById(R.id.layout_cancel_alert_dialog_custome);

        RadioGroup radioGroup = (RadioGroup)dialog. findViewById(R.id.radiogroup_alert_dialog_custome);

        RadioButton radio_one = (RadioButton) dialog.findViewById(R.id.radio_one_alert_dialog_custome);

        RadioButton radio_three = (RadioButton) dialog.findViewById(R.id.radio_three_alert_dialog_custome);

        radio_one.setVisibility(View.GONE);
        radio_three.setVisibility(View.GONE);

        txt_title.setText(getResources().getString(R.string.action));

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
               if(checkedId == R.id.radio_two_alert_dialog_custome)
               {
                   if (dialog != null) {
                       dialog.dismiss();
                   }
                   showDeleteConfirmationDialog(position);
               }
               else if(checkedId == R.id.radio_three_alert_dialog_custome)
               {
                   if (dialog != null) {
                       dialog.dismiss();
                   }
                   openDialogForUpdate(position);
               }

            }
        });

        // if decline button is clicked, close the custom dialog
        layout_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();


        /*AppLog.showD(TAG, "showDeleteUpdateDialog");
        final ArrayAdapter<String> arrayAdapter;
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                this);

        builderSingle.setView(R.layout.alert_dialog_cutome_layout);

       builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Action");


        arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("Delete");
        arrayAdapter.add("Update");

        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        if (strName.equals("Delete")) {
                            showDeleteConfirmationDialog(position);
                        } else if (strName.equals("Update")) {
                            openDialogForUpdate(position);

                        } else {

                        }

                    }
                });
        builderSingle.show();*/
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_one_alert_dialog_custome:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.radio_two_alert_dialog_custome:
                if (checked)
                    // Ninjas rule
                    break;
            case R.id.radio_three_alert_dialog_custome:
                if (checked)
                    // Ninjas rule
                    break;
        }
    }

    public void openDialogForUpdate(final int position)
    {
        AppLog.showE(TAG, "showStartExcercisePopup");

         sixMinWalkList = mAdapter.getData().get(position);

        int popupWidth = 460;
        int screenWidth;
        int pointX;
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;

        popupWidth = screenWidth - 20;
        pointX = (screenWidth - popupWidth) / 2;
        View view = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.pop_up_edit_sixminwalk, null);

         popupAddExercise = new PopupWindow(this);
        popupAddExercise.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupAddExercise.setContentView(view);

        popupAddExercise.setWidth(popupWidth);
        popupAddExercise.setHeight(-2);

        popupAddExercise.setOutsideTouchable(true);
        popupAddExercise.setFocusable(true);

        popupAddExercise.setAnimationStyle(R.style.workout_AddExerciseAnimation);
        popupAddExercise.showAtLocation(view, 0, pointX, 80);

        Button btnOk = (Button) view.findViewById(R.id.btnOK_workout_add_exercise);
        CustomButton btnClose = (CustomButton) view.findViewById(R.id.btnOK_close_add_exercise);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddExercise.dismiss();
            }
        });
        TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle_workout_add_exercise);
        CustomTextView txtHr1=(CustomTextView)view.findViewById(R.id.txt_hr1);
        CustomTextView txtHR2=(CustomTextView)view.findViewById(R.id.txt_hr2);
        CustomTextView txtHR3=(CustomTextView)view.findViewById(R.id.txt_hr3);

        final LinearLayout layout_hhr1 = (LinearLayout) view.findViewById(R.id.layout_hhr1_pop_up_edit_six_min);

        final EditText textView_hhr1 = (EditText) view.findViewById(R.id.editText_hhr1_pop_up_edit_six_min);


        if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk")) {
            txtTitle.setText("6 Min Walk Result");
            txtHr1.setText("HR before 6 Min Walk");
            txtHR2.setText("HR after 6 Min Walk");
            txtHR3.setText("HR after 1 min of 6 Min Walk");
        }
        else{
            txtTitle.setText("3 Min Walk Result");
            txtHr1.setText("HR before 3 Min Walk");
            txtHR2.setText("HR after 3 Min Walk");
            txtHR3 .setText("HR after 1 min of  Min Walk");
        }
        final EditText edtTime = (EditText) view.findViewById(R.id.edt_time);
        edtTime.setText("" + String.format("%.2f", sixMinWalkList.getDuration()));
        edtTime.setAlpha((float) 0.5);
        edtTime.setEnabled(false);

        final EditText edtDistance = (EditText) view.findViewById(R.id.edt_distance);

        edtDistance.setText("" + String.format("%.2f", sixMinWalkList.getDistance()));
        edtDistance.setAlpha((float) 0.5);
        edtDistance.setEnabled(false);
        if (sixMinWalkList.getActivityDetails().size() > 0) {
            Collections.sort(sixMinWalkList.getActivityDetails(), new Comparator<SixMinWalkList.ActivityDetail>() {
                @Override
                public int compare(SixMinWalkList.ActivityDetail o1, SixMinWalkList.ActivityDetail o2) {
                    return o1.getHeartRateMinuteIndex().compareTo(o2.getHeartRateMinuteIndex());
                }
            });
        }


         edtFirstHR = (EditText) view.findViewById(R.id.edt_first_hr);
        if (sixMinWalkList.getActivityDetails().get(0).getHeartRate()!=null) {
            edtFirstHR.setText("" + sixMinWalkList.getActivityDetails().get(0).getHeartRate());
            if (edtFirstHR.getText().toString().equalsIgnoreCase("0"))
                edtFirstHR.setText("");
        }
        else
            edtFirstHR.setText("");

         edtEndHR = (EditText) view.findViewById(R.id.edt_end_hr);
        if (sixMinWalkList.getActivityDetails().get(1).getHeartRate()!=null) {
            edtEndHR.setText("" + sixMinWalkList.getActivityDetails().get(1).getHeartRate());
            if (edtEndHR.getText().toString().equalsIgnoreCase("0"))
                edtEndHR.setText("");
        }
        else
            edtEndHR.setText("");

        edt1Mins = (EditText) view.findViewById(R.id.edt_1min_hr);
        if (sixMinWalkList.getActivityDetails().get(2).getHeartRate()!=null) {
            edt1Mins.setText("" + sixMinWalkList.getActivityDetails().get(2).getHeartRate());
            if (edt1Mins.getText().toString().equalsIgnoreCase("0"))
                edt1Mins.setText("");
        }
        else
            edt1Mins.setText("");

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AppLog.showD(TAG, "update ");

                String value1 = edtFirstHR.getText().toString();
                String value2 = edtEndHR.getText().toString();
                String value3 = edt1Mins.getText().toString();
                String value4 = textView_hhr1.getText().toString();

                setValidation(edtFirstHR, 27);
                setValidation(edtEndHR, 27);
                setValidation(edt1Mins, 27);

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
                    FirstField();
                }



                           /* callUpdateAPI(sixMinWalkList,
                                    edtFirstHR.getText().toString(),
                                    edtEndHR.getText().toString(),
                                    edt1Mins.getText().toString());
                            popupAddExercise.dismiss();*/

            }
        });

        setHHR1(edtEndHR.getText().toString(), edt1Mins.getText().toString(), textView_hhr1);

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

                }
                else
                {
                    setHHR1("0",edt1Mins.getText().toString(),textView_hhr1);
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
                }
                else
                {
                    setHHR1(edtEndHR.getText().toString(),s.toString(),textView_hhr1);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void setHHR1(String secondhr, String thirdhr,EditText ed_hhr1)
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



    public void callUpdateAPI(SixMinWalkList sixMinWalkList, String hr1, String hr2, String hr3) {

        totalHrField = 0;
        UpdateSixMinuteActivity request = new UpdateSixMinuteActivity();

        CustomProgressbar.showProgressBar(this, false);
        Calendar calendarToDate = Calendar.getInstance();

        calendarToDate.set(Calendar.HOUR_OF_DAY, 00);
        calendarToDate.set(Calendar.MINUTE, 00);
        calendarToDate.set(Calendar.SECOND, 00);

        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        int minuteToAdd = (int) TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS);
        // Log.e("minutes", "" + TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS));

        Calendar calendarGMT = Calendar.getInstance();
        calendarGMT.setTime(calendarToDate.getTime());
        calendarGMT.add(Calendar.MINUTE, (-minuteToAdd));

        calendarToDate.set(Calendar.HOUR_OF_DAY, 23);
        calendarToDate.set(Calendar.MINUTE, 59);

        String userId = UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID);

        request.patientUserId = userId;
        request.minutesToAdd = "" + minuteToAdd;
        request.noOfDays = "0";
        request.selectedDate = Medicine_Constants.webServiceDateFormat.format(calendarGMT.getTime());

        ArrayList<SixMinWalkUpdate> minWalkUpdateArrayList = new ArrayList<>();


        if (!hr1.isEmpty() ) {
            SixMinWalkUpdate firstHR = new SixMinWalkUpdate();
            firstHR.HeartRate = Integer.parseInt(hr1);
            firstHR.PatientActivityDetailsId = sixMinWalkList.getActivityDetails().get(0).getPatientActivityDetailsId();
            if (firstHR.PatientActivityDetailsId.equalsIgnoreCase("00000000-0000-0000-0000-000000000000"))
                firstHR.ReadingDateTime = Medicine_Constants.webServiceDateFormat.format(calendarGMT.getTime());
            firstHR.HeartRateMinuteIndex="1";
            firstHR.PatientActivityId=sixMinWalkList.getPatientActivityId();
            minWalkUpdateArrayList.add(firstHR);
        }
        else if(!sixMinWalkList.getActivityDetails().get(0).getPatientActivityDetailsId().equalsIgnoreCase("00000000-0000-0000-0000-000000000000")){
            SixMinWalkUpdate firstHR = new SixMinWalkUpdate();
            firstHR.PatientActivityDetailsId = sixMinWalkList.getActivityDetails().get(0).getPatientActivityDetailsId();
            firstHR.HeartRateMinuteIndex="1";
            firstHR.PatientActivityId=sixMinWalkList.getPatientActivityId();
            minWalkUpdateArrayList.add(firstHR);
        }
        if (!hr2.isEmpty()) {
            SixMinWalkUpdate secondHR = new SixMinWalkUpdate();
            secondHR.HeartRate = Integer.parseInt(hr2);
            secondHR.PatientActivityDetailsId = sixMinWalkList.getActivityDetails().get(1).getPatientActivityDetailsId();
            if (secondHR.PatientActivityDetailsId.equalsIgnoreCase("00000000-0000-0000-0000-000000000000"))
                secondHR.ReadingDateTime = Medicine_Constants.webServiceDateFormat.format(calendarGMT.getTime());
            secondHR.HeartRateMinuteIndex="2";
            secondHR.PatientActivityId=sixMinWalkList.getPatientActivityId();
            minWalkUpdateArrayList.add(secondHR);
        }
        else if(!sixMinWalkList.getActivityDetails().get(1).getPatientActivityDetailsId().equalsIgnoreCase("00000000-0000-0000-0000-000000000000")){
            SixMinWalkUpdate secondHR = new SixMinWalkUpdate();
            secondHR.PatientActivityDetailsId = sixMinWalkList.getActivityDetails().get(1).getPatientActivityDetailsId();
            secondHR.HeartRateMinuteIndex="2";
            secondHR.PatientActivityId=sixMinWalkList.getPatientActivityId();
            minWalkUpdateArrayList.add(secondHR);
        }
        if (!hr3.isEmpty()) {
            SixMinWalkUpdate thirdHR = new SixMinWalkUpdate();
            thirdHR.HeartRate = Integer.parseInt(hr3);
            thirdHR.PatientActivityDetailsId = sixMinWalkList.getActivityDetails().get(2).getPatientActivityDetailsId();
            if (thirdHR.PatientActivityDetailsId.equalsIgnoreCase("00000000-0000-0000-0000-000000000000"))
                thirdHR.ReadingDateTime = Medicine_Constants.webServiceDateFormat.format(calendarGMT.getTime());
            thirdHR.HeartRateMinuteIndex="3";
            thirdHR.PatientActivityId=sixMinWalkList.getPatientActivityId();
            minWalkUpdateArrayList.add(thirdHR);
        }
        else if(!sixMinWalkList.getActivityDetails().get(2).getPatientActivityDetailsId().equalsIgnoreCase("00000000-0000-0000-0000-000000000000")){
            SixMinWalkUpdate thirdHR = new SixMinWalkUpdate();
            thirdHR.PatientActivityDetailsId = sixMinWalkList.getActivityDetails().get(2).getPatientActivityDetailsId();
            thirdHR.HeartRateMinuteIndex="2";
            thirdHR.PatientActivityId=sixMinWalkList.getPatientActivityId();
            minWalkUpdateArrayList.add(thirdHR);
        }
        request.updateHeartRateDetails = minWalkUpdateArrayList;
        if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk")) {
            RemoteMethods3.getWorkOutService().updateActivity(request, new Callback<BaseResponse>() {
                @Override
                public void success(BaseResponse baseResponse, Response response) {
                    AppLog.showD(TAG, "sucess");
                    CustomProgressbar.hideProgressBar();
                    switch (selectedTab) {
                        case TODAY:
                            callVitalListAPI(0);
                            break;
                        case WEEKLY:
                            callVitalListAPI(6);
                            break;
                        case MONTHLY:
                            callVitalListAPI(30);
                            break;

                    }
                    common.setFalseValidation();
                }

                @Override
                public void failure(RetrofitError error) {
                    AppLog.showD(TAG, "failure");
                    CustomProgressbar.hideProgressBar();
                }
            });
        }
        else{
            RemoteMethods.getWorkOutService().threeMinUpdate(request, new Callback<BaseResponse>() {
                @Override
                public void success(BaseResponse baseResponse, Response response) {
                    AppLog.showD(TAG, "sucess");
                    CustomProgressbar.hideProgressBar();
                    switch (selectedTab)
                    {
                        case TODAY:
                            callVitalListAPI(0);
                            break;
                        case WEEKLY:
                            callVitalListAPI(6);
                            break;
                        case MONTHLY:
                            callVitalListAPI(30);
                            break;

                    }
                    common.setFalseValidation();
                }

                @Override
                public void failure(RetrofitError error) {
                    AppLog.showD(TAG, "failure");
                    CustomProgressbar.hideProgressBar();
                }
            });
        }



    }

    private void showDeleteConfirmationDialog(final int position) {

        final Dialog dialog = new Dialog(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View dialogView = layoutInflater.inflate(R.layout.fragment_dialog_delete, null);
        dialog.setContentView(dialogView);

        ((CustomTextView) dialogView.findViewById(R.id.txtMessageDialog)).setText(("Are you sure you want to delete this entry"));
        Button btnOK = (Button) dialogView.findViewById(R.id.btnOkDialog);
        Button btnCancel = (Button) dialogView.findViewById(R.id.btnCancalDialog);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppLog.showD(TAG, "ok delete");
                deleteRecord(position);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void deleteRecord(int position) {
        AppLog.showD(TAG, "deleteRecord=" + position);
        String userId = UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID);
        String activityId = mAdapter.getData().get(position).getPatientActivityId();
        if (WebserviceConstants.FactorCode.equalsIgnoreCase("6minwalk"))
        {
            RemoteMethods3.getWorkOutService().deleteActivity(userId, activityId, new Callback<BaseResponse>() {
                @Override
                public void success(BaseResponse baseResponse, Response response) {
                    AppLog.showD(TAG, "success");

                    switch (selectedTab) {
                        case TODAY:
                            callVitalListAPI(0);
                            break;
                        case WEEKLY:
                            callVitalListAPI(6);
                            break;
                        case MONTHLY:
                            callVitalListAPI(30);
                            break;

                    }

                }

                @Override
                public void failure(RetrofitError error) {
                    AppLog.showD(TAG, "fail");
                }
            });
        }
        else{
            RemoteMethods.getWorkOutService().threeMinDelete(userId, activityId, new Callback<BaseResponse>() {
                @Override
                public void success(BaseResponse baseResponse, Response response) {
                    AppLog.showD(TAG, "success");

                    switch (selectedTab) {
                        case TODAY:
                            callVitalListAPI(0);
                            break;
                        case WEEKLY:
                            callVitalListAPI(6);
                            break;
                        case MONTHLY:
                            callVitalListAPI(30);
                            break;

                    }

                }

                @Override
                public void failure(RetrofitError error) {
                    AppLog.showD(TAG, "fail");
                }
            });
        }


    }

    /****
     * for only ok button
     **/
    public void showDialog(String message) {
        final Dialog dialog = new Dialog(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
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




    public void showDialog1_one(final int no,String message) {

        final Dialog dialog = new Dialog(SixMinWalkHistory.this);
        LayoutInflater layoutInflater = LayoutInflater.from(SixMinWalkHistory.this);
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

    public void setValidation(EditText edtValue, int factorid)
    {
        AppLog.showE(TAG, "setValidation");





        for(int v=0 ; v < WebserviceConstants.allvalidation.size(); v++)
        {
            boolean check = false;
            if(factorid == WebserviceConstants.allvalidation.get(v).getFactorId())
            {

                for (int r = 0; r < WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().size(); r++)
                {



                    String dataType = WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getDataType();
                    Log.e("SetDataType", "  " + factorid + ":" + dataType + ":" +WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength());
                    if (dataType.equals("Integer")) {
                        Log.e("SetDataType-iNTEGER", "  " + factorid + ":" + dataType);
                        edtValue.setRawInputType(Configuration.KEYBOARD_12KEY);
                        edtValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength())});
                    } else if (dataType.equals("Decimal")) {
                        Log.e("SetDataType-dECIMAL", "  " + factorid+ ":" + dataType);
                        edtValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        edtValue.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength(),WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getDecimalPlaceLength())});
                    }
                    //setting length
                    check = true;


                    Log.e("SetValidation###", " " + factorid);

                }
            }

            Log.e("Check", " " + check);
            if(check)
            {
                break;
            }
        }

    }



    public void FirstField()
    {

        Log.e("TotalHrM", " " + totalHrField);

        String value1 = "";
        for(int m=totalHrField;m<3 ; m++)
        {
            if(m==0) {
                value1 = edtFirstHR.getText().toString();
                edtFirstHR.requestFocus();
            }
            else if(m==1) {
                value1 = edtEndHR.getText().toString();
                edtEndHR.requestFocus();
            }
            else if(m==2) {
                value1 = edt1Mins.getText().toString();
                edt1Mins.requestFocus();

            }

            if (!value1.equals(""))
            {
                Log.e("Call-1", "Call");

                HashMap<String, Integer> returnMap = AllValidation.rangeValidation(27, value1, WebserviceConstants.allvalidation);
                AppLog.showD(TAG, "returnMap status=" + returnMap.get(AllValidation.STATUS) + ":" + returnMap.get(AllValidation.POSITION) + ":" + returnMap.get(AllValidation.MPOSITION));

                if (returnMap.get(AllValidation.STATUS) == 0 && Float.parseFloat(value1) != 0)
                {
                    Log.e("RangePosiiton-Shown", "mPOSITION=" + WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isShown());
                    Log.e("Call-2", "Call");
                    if (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isIsWarning()
                            && (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isShown() == false)) {
                        Log.e("Call-3", "Call");
                        totalHrField = m;
                        showDialogOKCancel(1, "R", returnMap.get(AllValidation.MPOSITION), returnMap.get(AllValidation.POSITION), WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).getCustomMessage());
                        return;

                    } else if (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isIsWarning() == false) {
                        Log.e("Call-4", "Call");

                        showDialog1_one(1, WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).getCustomMessage());
                        return;

                    } else {
                        Log.e("Call-5", "Call");
                        AppLog.showD(TAG, "warning removed range");

                        if(m==2) {

                            callUpdateAPI(sixMinWalkList,
                                    edtFirstHR.getText().toString(),
                                    edtEndHR.getText().toString(),
                                    edt1Mins.getText().toString());
                            popupAddExercise.dismiss();

                           break;


                        }
                    }
                }


                HashMap<String, Integer> returnValueToComp = AllValidation.valueToCompareValidation(27, value1, WebserviceConstants.allvalidation);

                Log.e("ValueCompare-*", " " + returnValueToComp.get(AllValidation.STATUS) + ":" + returnValueToComp.get(AllValidation.MPOSITION) + ":" + returnValueToComp.get(AllValidation.POSITION) + ":"+returnValueToComp.get(AllValidation.MPOSITION));


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
                        showDialogOKCancel(1, "V", returnValueToComp.get(AllValidation.MPOSITION), returnValueToComp.get(AllValidation.POSITION), WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).getCustomMessage());
                        return;

                    } else if (WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isIsWarning() == false) {
                        Log.e("Call-8", "Call");

                        showDialog1_one(1, WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).getCustomMessage());

                        return;
                    } else {
                        AppLog.showD(TAG, "warning removed");
                        Log.e("Call-9", "Call");

                        if(m==2) {

                            callUpdateAPI(sixMinWalkList,
                                    edtFirstHR.getText().toString(),
                                    edtEndHR.getText().toString(),
                                    edt1Mins.getText().toString());
                            popupAddExercise.dismiss();

                            break;



                        }

                    }
                }
                else
                {
                    if(m==2) {

                        callUpdateAPI(sixMinWalkList,
                                edtFirstHR.getText().toString(),
                                edtEndHR.getText().toString(),
                                edt1Mins.getText().toString());
                        popupAddExercise.dismiss();
                        popupAddExercise.dismiss();
                        break;
                    }
                }

            }
            else
            {
                totalHrField = m + 1;
                if(m==2)
                {
                    Log.e("Call-10", "Call");
                     callUpdateAPI(sixMinWalkList,
                                    edtFirstHR.getText().toString(),
                                    edtEndHR.getText().toString(),
                                    edt1Mins.getText().toString());
                            popupAddExercise.dismiss();

                    break;
                }


            }

            Log.e("CountM", " " + m);
        }

    }

    public void showDialogOKCancel( final int no, final String type, final int mpos, final int pos, String message) {

        final Dialog dialog = new Dialog(SixMinWalkHistory.this);
        LayoutInflater layoutInflater = LayoutInflater.from(SixMinWalkHistory.this);
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

                if(totalHrField==2)
                {
                    callUpdateAPI(sixMinWalkList,
                            edtFirstHR.getText().toString(),
                            edtEndHR.getText().toString(),
                            edt1Mins.getText().toString());




                    popupAddExercise.dismiss();
                }
                else {

                    totalHrField = totalHrField + 1;
                    FirstField();

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

}


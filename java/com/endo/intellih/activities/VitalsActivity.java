package com.endo.intellih.activities;


import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.endo.intellih.AppConstants;
import com.endo.intellih.AppFunctions;
import com.endo.intellih.R;
import com.endo.intellih.common.CommonMethod;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.models.VitalsInput;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.webservice.Constant;
import com.endo.intellih.webservice.RemoteMethods3;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.request.VitalSaveRequest;
import com.endo.intellih.webservice.services.response.GetPatientDataView;
import com.endo.intellih.webservice.services.response.VsaveResponse;

import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class VitalsActivity extends FragmentActivity implements FragmentTabHost.OnTabChangeListener
{
    public static final String TAG = VitalsActivity.class.getSimpleName();
    Calendar cal;
    private Button btnSave, btnHome;
    private ArrayList<GetPatientDataView.AssociatedHFGroup> vitalArray;
    private Gson gson;
    private FragmentTabHost mTabHost;
    public String selectedTab;
    private TextView txtTitle;
    VitalGlucoseFragment fragment = null;
    HorizontalScrollView hs;
    //private ArrayList<VitalValidation> vitalValidation;
    private List<ImageView> dots;
    private ImageButton btnPrev, btnNext;
    private DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
    private MutableDateTime dateTime;
    int hour, minute;
    private TextView txtDate;
    private String todayDate = "";
    CommonMethod common;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cal = Calendar.getInstance();
        common = new CommonMethod(VitalsActivity.this);
        WebserviceConstants.Cal = cal;
        setContentView(R.layout.activity_vitals);
        gson = new Gson();
        Bundle extra = getIntent().getExtras();
        vitalArray = gson.fromJson(extra.getString(AppConstants.INTENT_KEY.VITAL_ARRAY), new TypeToken<List<GetPatientDataView.AssociatedHFGroup>>() {
        }.getType());
       /* vitalValidation = gson.fromJson(extra.getString(AppConstants.INTENT_KEY.VITAL_VALIDATION), new TypeToken<List<VitalValidation>>() {
        }.getType());*/
        //AppLog.showE(TAG, "vital array=" + vitalValidation.size());


        Log.e("VitalArraySize", " " + vitalArray.size());


        dateTime = new MutableDateTime();
        hour = dateTime.getHourOfDay();
        minute = dateTime.getMinuteOfHour();


        txtDate = (TextView) findViewById(R.id.txt_date);
        btnPrev = (ImageButton) findViewById(R.id.btn_prev);
        btnNext = (ImageButton) findViewById(R.id.btn_next);

        btnPrev.setOnClickListener(onClickPrev);
        btnNext.setOnClickListener(onClickNext);

        btnHome = (Button) findViewById(R.id.btn_home);
        txtTitle = (TextView) findViewById(R.id.txt_title);

        txtDate.setText("Today");
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempDate = txtDate.getText().toString();
                SimpleDateFormat dayDateFormat = new SimpleDateFormat("MM/dd/yyyy");
                tempDate = dayDateFormat.format(cal.getTime());
                String[] separated = tempDate.split("/");
                //cal.set(Integer.parseInt(separated[2]), Integer.parseInt(separated[0])-1, Integer.parseInt(separated[1]));
                openDateDialog(Integer.parseInt(separated[2]), Integer.parseInt(separated[0]) - 1, Integer.parseInt(separated[1]));

            }
        });
        todayDate = dtf.print(dateTime);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(onClickSave);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);


        for (GetPatientDataView.AssociatedHFGroup item : vitalArray)
        {
            AppLog.showD(TAG, item.getTenantHFGroupName());

             /*for (GetPatientDataView.AssociatedVitals vital : item.getAssociateVitals()) {

                int vitalId = vital.getVitalID();
               *//* for (VitalValidation vitalValidationItem : vitalValidation) {

                    if (vitalId == vitalValidationItem.getVitalId()) {
                        //vital.setVitalValidation(vitalValidationItem);
                    }
                }*//*

            }*/

            Log.e("HHH-1", " " + gson.toJson(item));
            Bundle args = new Bundle();
            args.putString(AppConstants.INTENT_KEY.VITAL_FACTORS, gson.toJson(item));

            mTabHost.addTab(
                    mTabHost.newTabSpec(item.getTenantHFGroupName()).
                            setIndicator(createTabIndicator(item.getTenantHFGroupName())),
                    VitalGlucoseFragment.class, args);
        }

        if(Constant.selected_tab !=-1) {
            mTabHost.setCurrentTab(Constant.selected_tab);
        }
        else
        {
            mTabHost.setCurrentTab(0);
        }
        addDots();
        if (vitalArray.size() > 0)
            selectedTab = vitalArray.get(0).getTenantHFGroupName();
        mTabHost.setOnTabChangedListener(this);
        Log.e("HHH-2", " " + selectedTab);
        selectDot(0);

    }

    public void openDateDialog(int year, int month, int day) {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(year, month, day);
        DatePickerDialog dobDialog = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            dobDialog = new DatePickerDialog(this,R.style.datepicker_dialog, new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    if (view.isShown()) {

                        view.clearFocus();
                        view.updateDate(view.getYear(), view.getMonth(), view.getDayOfMonth());
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(view.getYear(), view.getMonth(), view.getDayOfMonth());
                        Calendar today = Calendar.getInstance();
                        //                if ((today.get(Calendar.YEAR) <= newDate.get(Calendar.YEAR)) &&
                        //                        (today.get(Calendar.MONTH) <= newDate.get(Calendar.MONTH)) && (today.get(Calendar.DAY_OF_MONTH) <= newDate.get(Calendar.DAY_OF_MONTH))) {
                        //AppFunctions.showMessageDialog(getActivity(), getString(R.string.medicine_addMedicine_birthDateShouldBeLower));
                        if (today.compareTo(newDate) < 0) {
                            AppFunctions.showMessageDialog(VitalsActivity.this, "Selected date can not be greater than current date.");
                        } else {
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

                            cal.setTime(newDate.getTime());
                            WebserviceConstants.Cal = cal;
                            dateTime = new MutableDateTime();
                            long msDiff = Calendar.getInstance().getTimeInMillis() - cal.getTimeInMillis();
                            long daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff);
                            Log.e("Here", daysDiff + "");
                            if (getComparisionWithCurrentDate(cal) <= 0) {

                                txtDate.setText("Today");
                                cal = Calendar.getInstance();
                                WebserviceConstants.Cal = cal;
                                dateTime = new MutableDateTime();
//                cal.set(Calendar.HOUR_OF_DAY, 23);
//                cal.set(Calendar.MINUTE, 59);
                            } else {
                                dateTime.addDays(-(int) daysDiff);
                                txtDate.setText(DateFormat.getMediumDateFormat(VitalsActivity.this).format(cal.getTime()));
                            }
                            Log.e("Days", today.compareTo(WebserviceConstants.Cal) + "");
                        }
                    }
                }

            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        } else {
            dobDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    if (view.isShown()) {
                        view.clearFocus();
                        view.updateDate(view.getYear(), view.getMonth(), view.getDayOfMonth());
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(view.getYear(), view.getMonth(), view.getDayOfMonth());
                        Calendar today = Calendar.getInstance();
                        //                if ((today.get(Calendar.YEAR) <= newDate.get(Calendar.YEAR)) &&
                        //                        (today.get(Calendar.MONTH) <= newDate.get(Calendar.MONTH)) && (today.get(Calendar.DAY_OF_MONTH) <= newDate.get(Calendar.DAY_OF_MONTH))) {
                        //AppFunctions.showMessageDialog(getActivity(), getString(R.string.medicine_addMedicine_birthDateShouldBeLower));


                        if (today.compareTo(newDate) < 0) {
                            AppFunctions.showMessageDialog(VitalsActivity.this, "Selected date can not be greater than current date.");

                        } else {
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

                            cal.setTime(newDate.getTime());
                            WebserviceConstants.Cal = cal;
                            dateTime = new MutableDateTime();
                            long msDiff = Calendar.getInstance().getTimeInMillis() - cal.getTimeInMillis();
                            long daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff);
                            Log.e("Here", daysDiff + "");
                            if (getComparisionWithCurrentDate(cal) <= 0) {

                                txtDate.setText("Today");
                                cal = Calendar.getInstance();
                                WebserviceConstants.Cal = cal;
                                dateTime = new MutableDateTime();
//                cal.set(Calendar.HOUR_OF_DAY, 23);
//                cal.set(Calendar.MINUTE, 59);
                            } else {
                                dateTime.addDays((int) daysDiff);
                                txtDate.setText(DateFormat.getMediumDateFormat(VitalsActivity.this).format(cal.getTime()));
                            }
                            Log.e("Days", today.compareTo(WebserviceConstants.Cal) + "");
                        }
                    }
                }

            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        }
        dobDialog.show();

    }

    public int getComparisionWithCurrentDate(Calendar calendar) {
        Calendar cal1, currentCal;
        cal1 = Calendar.getInstance();
        cal1.setTime(calendar.getTime());
        cal1.set(Calendar.HOUR_OF_DAY, 23);
        cal1.set(Calendar.MINUTE, 59);
        cal1.set(Calendar.SECOND, 59);
        cal1.set(Calendar.MILLISECOND, 0);

        currentCal = Calendar.getInstance();
        currentCal.set(Calendar.HOUR_OF_DAY, 23);
        currentCal.set(Calendar.MINUTE, 59);
        currentCal.set(Calendar.SECOND, 59);
        currentCal.set(Calendar.MILLISECOND, 0);

        return currentCal.compareTo(cal1);
    }

    public MutableDateTime getDateTime() {

        return dateTime;
    }

    View.OnClickListener onClickPrev = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppLog.showD(TAG, "onClickPrev");
            dateTime.addDays(-1);
            cal.add(Calendar.DATE, -1);
            WebserviceConstants.Cal = cal;
            AppLog.showD(TAG, dtf.print(dateTime));
            txtDate.setText("" + dtf.print(dateTime));

        }
    };

    View.OnClickListener onClickNext = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppLog.showD(TAG, "onClickNext");

            if (txtDate.getText().toString().equals("Today")) {
                return;
            }

            dateTime.addDays(1);
            String tempDate = dtf.print(dateTime);
            if (todayDate.equals(tempDate)) {
                txtDate.setText("Today");
            } else {
                txtDate.setText(tempDate);

            }
            if (getComparisionWithCurrentDate(cal) > 0) {
                cal.add(Calendar.DATE, 1);
                WebserviceConstants.Cal = cal;
            }
            AppLog.showD(TAG, dtf.print(dateTime));
        }
    };

    public void addDots() {
        dots = new ArrayList<>();
        LinearLayout dotsLayout = (LinearLayout) findViewById(R.id.dots);

        for (int i = 0; i < vitalArray.size(); i++) {
            ImageView dot = new ImageView(this);
            dot.setImageDrawable(getResources().getDrawable(R.drawable.unselected_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            dotsLayout.addView(dot, params);

            dots.add(dot);
        }
    }

    public void selectDot(int idx) {
        Resources res = getResources();
        for (int i = 0; i < vitalArray.size(); i++) {
            int drawableId = (i == idx) ? (R.drawable.selected_dot) : (R.drawable.unselected_dot);
            Drawable drawable = res.getDrawable(drawableId);
            dots.get(i).setImageDrawable(drawable);
        }
    }

    private View createTabIndicator(String label) {
        View tabIndicator = View.inflate(this, R.layout.tab_indicator_goal, null);
        tabIndicator.setBackgroundResource(R.drawable.selector_tab_goal);

        TextView tv = (TextView) tabIndicator.findViewById(R.id.label);
        tv.setText(label);

        ImageView iv = (ImageView) tabIndicator.findViewById(R.id.tab_icon);

        if (label.equals("Glucose")) {
            iv.setImageResource(R.drawable.ic_tab_blood_glucose);
        } else if (label.equals("Blood Pressure")) {
            iv.setImageResource(R.drawable.ic_tab_blood_pressure);
        } else if (label.equals("Weight")) {
            iv.setImageResource(R.drawable.ic_tab_weight);
        } else {
            iv.setImageResource(R.drawable.ic_self_measurement);
        }

        return tabIndicator;
    }

    View.OnClickListener onClickSave = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AppLog.showD(TAG, "onClickSave");
            callApi();
        }
    };

    public void callApi()
    {
        AppLog.showD(TAG, "callApi");
        fragment = (VitalGlucoseFragment) getSupportFragmentManager().findFragmentByTag(selectedTab);
        ArrayList<VitalsInput> returnArray = fragment.saveVital();

        if (returnArray == null) {
            AppLog.showD(TAG, "there is some issue in validation");
            return;
        }
        CustomProgressbar.showProgressBar(VitalsActivity.this, false);
        VitalSaveRequest request = new VitalSaveRequest();
        request.setUserId(UserSharedPreferences.getInstance(VitalsActivity.this).getString(UserSharedPreferences.KEY_USER_ID));


        request.setVitalsInputs(returnArray);
        for (VitalsInput item : returnArray) {
            Log.e("VitalID", item.getFactorid() + "");
            Log.e("VitalDate", item.getMeasurementDateTime() + "");
            Log.e("VitalValue", item.getVitalValue() + "");

        }


        RemoteMethods3.getVitalService().saveVital(request, new Callback<VsaveResponse>() {

            @Override
            public void success(VsaveResponse response, Response response2) {

                Log.e("Call Vital Save", " " + "success");
//                Log.e("Result",response.toString());
//                Log.e("Result123",response.getBody().toString());
//                Log.e("Result123",response.getHeaders().toString());
                // Log.e("CreateEmtryResponse", " " + response.Message);

                CustomProgressbar.hideProgressBar();

                if (response.getStatus())
                {
                    WebserviceConstants.appConfing = response.getLatestVitalValues();
                    // fragment.resetValues(getResources().getString(R.string.record_save));

                    common.setFalseValidation();

                    fragment.resetValues(response.getMessage());
                    CustomProgressbar.hideProgressBar();
                } else {
                    AppFunctions.showMessageDialog(VitalsActivity.this, response.getMessage());
                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Call Vital Save", " " + "Failer" + ";" + error);
                CustomProgressbar.hideProgressBar();
                // {"Status":false,"Message":"Glucose reading for the chosen time already exists","latestVitalValues":null}


            }


        });

    }


    @Override
    public void onTabChanged(String tabId) {
        AppLog.showD(TAG, "fragment change listener=" + tabId);
        int count = 0;
        for (GetPatientDataView.AssociatedHFGroup item : vitalArray) {
            if (item.getTenantHFGroupName().equals(tabId)) {
                break;
            }
            count++;
        }
        AppLog.showD(TAG, "selected dot" + count);
        selectDot(count);
        selectedTab = tabId;
    }
}
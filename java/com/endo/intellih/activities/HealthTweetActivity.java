package com.endo.intellih.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.endo.intellih.AppFunctions;
import com.endo.intellih.Fragments.SymptomsDetailsFragment;
import com.endo.intellih.LoginActivity;
import com.endo.intellih.R;
import com.endo.intellih.ViewListActivity;
import com.endo.intellih.application.AppController;
import com.endo.intellih.common.BaseActivity;
import com.endo.intellih.customviews.CustomButton;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.webservice.MyCallback;
import com.endo.intellih.webservice.RemoteMethods3;
import com.endo.intellih.webservice.SendRequestClass.CreateTweetRequest;
import com.endo.intellih.webservice.WebserviceConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;
import retrofit.client.Response;


public class HealthTweetActivity extends BaseActivity {
    EditText symtomdetailtype;
    TextView txtCounter,txtTime,labledate;
    private String symptom = "";
    private String time = "";
    ImageButton btnPrev,btnNext;
    SymptomsDetailsFragment mFragment;

    private int hourOfTime = -1;
    private int minuteOfTime = -1;
    Calendar cal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_healthtweet);
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        symtomdetailtype=(EditText)findViewById(R.id.edt_symptoms);
        txtCounter=(TextView)findViewById(R.id.textcounter);
        txtTime=(TextView)findViewById(R.id.text_timeValue);
        ImageButton imgBack=(ImageButton)findViewById(R.id.imgBtnBack_home);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView txtEdit= (TextView)findViewById(R.id.txtEdit);
        txtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                symptom=symtomdetailtype.getText().toString().trim();
                if (symptom.isEmpty()) {
                    AppFunctions.showMessageDialog(HealthTweetActivity.this,"Please enter text");
                }
                 else{
                    Calendar getCal = Calendar.getInstance();
                    getCal.setTime(cal.getTime());
                    getCal.set(Calendar.HOUR_OF_DAY, getHourOfTime());
                    getCal.set(Calendar.MINUTE, getMinuteOfTime());


                    CreateTweetRequest request = new CreateTweetRequest();
                    request.setHealthTweet(symptom);
                    request.setHealthTweetDateTime(WebserviceConstants.dateFormatter.format(getCal.getTime()));
                    request.setPatientUserId(UserSharedPreferences.getInstance(HealthTweetActivity.this).getString(UserSharedPreferences.KEY_USER_ID));
                    request.setUtcOffsetMinutes(getUtcOffminute());

                    CustomProgressbar.showProgressBar(HealthTweetActivity.this, false);
                    RemoteMethods3.getSelfMeasurementService().symptomsEntry(request, new MyCallback<CreateTweetRequest>() {
                        @Override
                        public void success(CreateTweetRequest createTweetRequestResponse, Response response) {
                            CustomProgressbar.hideProgressBar();
                            AppFunctions.showMessageDialog(HealthTweetActivity.this,"Record Saved Successfully");
                            symtomdetailtype.setText("");
                        }
                        @Override
                        public void failure(RetrofitError error) {
                            CustomProgressbar.hideProgressBar();
                            AppFunctions.showMessageDialog(HealthTweetActivity.this,error.getMessage());
                        }
                    });
                }

            }
        });



        CustomButton txtsave= (CustomButton)findViewById(R.id.txtsave);
        txtsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Called","List");
                Intent intent = new Intent(HealthTweetActivity.this, ViewListActivity.class);
                AppController.getInstance().setpassedDate(cal);
                intent.putExtra("title", HealthTweetActivity.this.getString(R.string.tweet_list));
                intent.putExtra("type", 4);
                cal.set(Calendar.HOUR_OF_DAY,getHourOfTime());
                cal.set(Calendar.MINUTE,getMinuteOfTime());
                intent.putExtra("date", getDateStringForGettingData());
                startActivity(intent);
            }
        });
        btnPrev = (ImageButton) findViewById(R.id.prev);
        btnNext = (ImageButton)findViewById(R.id.next);
        labledate = (TextView) findViewById(R.id.labledate);
        labledate.setText("Today");
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.add(Calendar.DATE, -1);
                labledate.setText(DateFormat.getMediumDateFormat(HealthTweetActivity.this).format(cal.getTime()));
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getComparisionWithCurrentDate(cal) > 0){

                    cal.add(Calendar.DATE, 1);
//        AppFunctions.showMessageDialog(mFragment.getActivity() , cal.getTime()+"");

            /*Calendar cal1 ,currentCal;
            cal1 = Calendar.getInstance();
            cal1.setTime(cal.getTime());
            cal1.set(Calendar.HOUR_OF_DAY, 23);
            cal1.set(Calendar.MINUTE , 59);

            currentCal = Calendar.getInstance();
            currentCal.set(Calendar.HOUR_OF_DAY, 23);
            currentCal.set(Calendar.MINUTE , 59);*/

                    if(getComparisionWithCurrentDate(cal) <= 0){
                        labledate.setText("Today");
                        cal = Calendar.getInstance();

//                cal.set(Calendar.HOUR_OF_DAY, 23);
//                cal.set(Calendar.MINUTE , 59);
                    }
                    else{
                        labledate.setText(DateFormat.getMediumDateFormat(HealthTweetActivity.this).format(cal.getTime()));
                    }
                }
            }
        });
        setTimeFrom(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE));
        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hourOfTime < 0) {
                    hourOfTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    minuteOfTime = Calendar.getInstance().get(Calendar.MINUTE);
                }

                TimePickerDialog timePickerDialog = new TimePickerDialog(HealthTweetActivity.this,R.style.datepicker_dialog, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        view.clearFocus();
                        hourOfDay=view.getCurrentHour();
                        minute=view.getCurrentMinute();
                        setTimeFrom(hourOfDay, minute);

                    }
                },  Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), false);

                timePickerDialog.show();

            }
        });
        symtomdetailtype.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("Called1", "Called");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.e("Called2", "Called");
            }

            @Override
            public void afterTextChanged(final Editable s) {

                int remianing=200-s.toString().length();
                txtCounter.setText(""+remianing+"/200");


            }
        });

       /* labledate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempDate = labledate.getText().toString();
                SimpleDateFormat dayDateFormat = new SimpleDateFormat("MM/dd/yyyy");
                tempDate=dayDateFormat.format(cal.getTime());
                String[] separated = tempDate.split("/");
                //cal.set(Integer.parseInt(separated[2]), Integer.parseInt(separated[0])-1, Integer.parseInt(separated[1]));
                openDateDialog(Integer.parseInt(separated[2]), Integer.parseInt(separated[0]) - 1, Integer.parseInt(separated[1]));

            }
        });*/
    }
    public void openDateDialog(int year, int month, int day) {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(year, month, day);
        DatePickerDialog dobDialog = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            dobDialog = new DatePickerDialog(this, R.style.datepicker_dialog, new DatePickerDialog.OnDateSetListener() {

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
                            AppFunctions.showMessageDialog(HealthTweetActivity.this, "Selected date can not be greater than current date.");
                        } else {
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

                            cal.setTime(newDate.getTime());
                            WebserviceConstants.Cal=cal;

                            if (getComparisionWithCurrentDate(cal) <= 0) {
                                labledate.setText("Today");
                                cal = Calendar.getInstance();
                                WebserviceConstants.Cal=cal;
//                cal.set(Calendar.HOUR_OF_DAY, 23);
//                cal.set(Calendar.MINUTE, 59);
                            } else {
                                labledate.setText(DateFormat.getMediumDateFormat(HealthTweetActivity.this).format(cal.getTime()));
                            }
                        }
                    }
                }

            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        }
        else{
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
                            AppFunctions.showMessageDialog(HealthTweetActivity.this, "Selected date can not be greater than current date.");

                        } else {
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

                            cal.setTime(newDate.getTime());
                            WebserviceConstants.Cal=cal;

                            if (getComparisionWithCurrentDate(cal) <= 0) {
                                labledate.setText("Today");
                                cal = Calendar.getInstance();
                                WebserviceConstants.Cal=cal;
//                cal.set(Calendar.HOUR_OF_DAY, 23);
//                cal.set(Calendar.MINUTE, 59);
                            } else {
                                labledate.setText(DateFormat.getMediumDateFormat(HealthTweetActivity.this).format(cal.getTime()));
                            }
                        }
                    }
                }

            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        }
        dobDialog.show();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
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
    private void setTimeFrom(int hourOfDay, int minute) {
        hourOfTime = hourOfDay;
        minuteOfTime = minute;

        String am_pm = "";
        if (hourOfTime >= 12 && hourOfTime != 24) {
            am_pm = "PM";
        } else {
            am_pm = "AM";
        }
        setHourOfTime(hourOfDay);
        setMinuteOfTime(minute);
        String hourToPass = (hourOfTime % 12) < 10 ? "0" + (hourOfTime % 12) : (hourOfTime % 12) + "";
        String minutesToPass = minuteOfTime < 10 ? "0" + (minuteOfTime) : minuteOfTime + "";
        if (hourToPass.equals("00")){
            hourToPass="12";
        }
        txtTime.setText(hourToPass + " : " + minutesToPass + " " + am_pm);
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getHourOfTime() {
        return hourOfTime;
    }

    public void setHourOfTime(int hourOfTime) {
        this.hourOfTime = hourOfTime;
    }

    public int getMinuteOfTime() {
        return minuteOfTime;
    }

    public void setMinuteOfTime(int minuteOfTime) {
        this.minuteOfTime = minuteOfTime;
    }
    public int getComparisionWithCurrentDate(Calendar calendar){
        Calendar cal1 ,currentCal;
        cal1 = Calendar.getInstance();
        cal1.setTime(calendar.getTime());
        cal1.set(Calendar.HOUR_OF_DAY, 23);
        cal1.set(Calendar.MINUTE , 59);
        cal1.set(Calendar.SECOND , 59);
        cal1.set(Calendar.MILLISECOND , 0);

        currentCal = Calendar.getInstance();
        currentCal.set(Calendar.HOUR_OF_DAY, 23);
        currentCal.set(Calendar.MINUTE , 59);
        currentCal.set(Calendar.SECOND , 59);
        currentCal.set(Calendar.MILLISECOND , 0);

        return currentCal.compareTo(cal1);
    }
    public String getDateStringForGettingData() {
        Calendar getCal = Calendar.getInstance();
        getCal.setTime(cal.getTime());
        getCal.set(Calendar.HOUR_OF_DAY, 00);
        getCal.set(Calendar.MINUTE, 00);
        return WebserviceConstants.dateFormatter.format(getCal.getTime());
    }

    public int getUtcOffminute()
    {
        String minuteToAdd = "0";
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        minuteToAdd = String.valueOf((int) TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS));

        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sd.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date remoteDate = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat();
            df.applyPattern("yyyy-MM-dd HH:mm:ss");
            String d = df.format(Calendar.getInstance().getTime());
            remoteDate = sd.parse(d); // your utc date
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date localDate = Calendar.getInstance().getTime();
        long diff = remoteDate.getTime() - localDate.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        if (minutes > 0)
            minuteToAdd = "" + (minutes + 1);
        else
            minuteToAdd = "" + minutes;


        return  Integer.parseInt(minuteToAdd);
    }
}

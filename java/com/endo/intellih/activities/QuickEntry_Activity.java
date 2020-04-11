package com.endo.intellih.activities;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.endo.intellih.AppConstants;
import com.endo.intellih.AppFunctions;
import com.endo.intellih.Fragments.dietnewfoodentry.ImageLoader;
import com.endo.intellih.R;
import com.endo.intellih.common.BaseActivity;
import com.endo.intellih.customviews.CustomButton;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.models.AssociateVitalForSave;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.webservice.RemoteMethods;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.request.QuickEntrySelectionParams;
import com.endo.intellih.webservice.services.response.GetPatientDataView;
import com.endo.intellih.webservice.services.response.UserResponse;

import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by appaspect on 12/2/15.
 */
public class QuickEntry_Activity extends BaseActivity {
    private ListView listView,innerlist1,innerlist2,innerlist3;
    private TextView txtTitle1,txtTitle2,txtTitle3,labledate;
    UserResponse userDetails;
    ImageButton btnPrev,btnNext;
    private int hourOfTime = -1;
    private int minuteOfTime = -1;
    public ArrayList<GetPatientDataView.AssociatedHFGroup> associateHF;
    public ArrayList<GetPatientDataView.AssociatedHFGroup> SelectedassociateHF;
    public ArrayList<AssociateVitalForSave> saveVitals;
    private SimpleDateFormat simpleDateFormat;
    LinearLayout ll1,ll2,ll3;
    int SelectedPosition;
    Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quickentry);
        RefreshView();

    }
    public void RefreshView(){
        associateHF=new ArrayList<GetPatientDataView.AssociatedHFGroup>();
        SelectedassociateHF=new ArrayList<GetPatientDataView.AssociatedHFGroup>();
        saveVitals=new ArrayList<AssociateVitalForSave>();
        ImageButton imgBack=(ImageButton)findViewById(R.id.imgBtnBack_home);
        ll1=(LinearLayout) findViewById(R.id.bgLayout);
        ll2=(LinearLayout)findViewById(R.id.bgLayout1);
        ll3=(LinearLayout)findViewById(R.id.bgLayout2);

        innerlist1 = (ListView)findViewById(R.id.lstinnerview1);
        innerlist2 = (ListView)findViewById(R.id.lstinnerview2);
        innerlist3 = (ListView)findViewById(R.id.lstinnerview3);


        btnPrev = (ImageButton) findViewById(R.id.prev);
        btnNext = (ImageButton)findViewById(R.id.next);
        labledate = (TextView) findViewById(R.id.labledate);
        labledate.setText("Today");
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DATE, -1);
                labledate.setText(DateFormat.getMediumDateFormat(QuickEntry_Activity.this).format(calendar.getTime()));
                CreateViews();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(/*Calendar.getInstance().compareTo*/getComparisionWithCurrentDate(calendar) > 0){

                    calendar.add(Calendar.DATE, 1);
//        AppFunctions.showMessageDialog(mFragment.getActivity() , cal.getTime()+"");

            /*Calendar cal1 ,currentCal;
            cal1 = Calendar.getInstance();
            cal1.setTime(cal.getTime());
            cal1.set(Calendar.HOUR_OF_DAY, 23);
            cal1.set(Calendar.MINUTE , 59);

            currentCal = Calendar.getInstance();
            currentCal.set(Calendar.HOUR_OF_DAY, 23);
            currentCal.set(Calendar.MINUTE , 59);*/

                    if(getComparisionWithCurrentDate(calendar) <= 0){
                        labledate.setText("Today");
                        calendar = Calendar.getInstance();

//                cal.set(Calendar.HOUR_OF_DAY, 23);
//                cal.set(Calendar.MINUTE , 59);
                    }
                    else{
                        labledate.setText(DateFormat.getMediumDateFormat(QuickEntry_Activity.this).format(calendar.getTime()));
                    }
                    CreateViews();
                }
            }
        });

        txtTitle1= (TextView)findViewById(R.id.txtTitle1);
        txtTitle2= (TextView)findViewById(R.id.txtTitle2);
        txtTitle3= (TextView)findViewById(R.id.txtTitle3);
        userDetails = new Gson().fromJson(getIntent().getStringExtra(AppConstants.INTENT_KEY.USER_DETAILS), UserResponse.class);
        final Gson gson = new Gson();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        WebserviceConstants.jsonObjecttoSaveVitals=new JSONObject();
        TextView txtEdit= (TextView)findViewById(R.id.txtEdit);
        txtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuickEntry_Activity.this, QuickEntry_Edit_Activity.class);
                intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, gson.toJson(userDetails));
                startActivity(intent);
                finish();
            }
        });


        CustomButton txtsave= (CustomButton)findViewById(R.id.txtsave);
        txtsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<AssociateVitalForSave> saveArrayList=new ArrayList<AssociateVitalForSave>();
                for (AssociateVitalForSave item : saveVitals){
                    if (!item.getVitalValue().isEmpty()){
                        saveArrayList.add(item);
                    }
                }
                QuickEntrySelectionParams params=new QuickEntrySelectionParams();
                params.userId = UserSharedPreferences.getInstance(QuickEntry_Activity.this).getString(UserSharedPreferences.KEY_USER_ID);
                params.vitalsInputs=new ArrayList<AssociateVitalForSave>();
                for (AssociateVitalForSave item : saveArrayList){
                    Log.e("Vitals ID","-"+item.getFactorid()+"-"+item.getVitalValue()+"-"+item.getMeasurementDateTime());

                    params.vitalsInputs.add(item);
                }
                if (params.vitalsInputs.size()>0) {
                    CustomProgressbar.showProgressBar(QuickEntry_Activity.this, false);
                    RemoteMethods.getQuickEntry().doSaveEntries(params, new Callback<UserResponse>() {
                        @Override
                        public void success(final UserResponse userResponse, retrofit.client.Response response) {
                            AppFunctions.showMessageDialog(QuickEntry_Activity.this,"Record Saved Successfully");
                            RefreshView();
                            CustomProgressbar.hideProgressBar();
//                            Toast toast = Toast.makeText(QuickEntry_Activity.this, userResponse.Message, Toast.LENGTH_SHORT);
//                            toast.show();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            CustomProgressbar.hideProgressBar();
                            Toast toast = Toast.makeText(QuickEntry_Activity.this, error.getMessage(), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }
                else{
                    AppFunctions.showMessageDialog(QuickEntry_Activity.this,"Please enter value");
                }
            }
        });
        for (GetPatientDataView.AssociatedHFGroup item : WebserviceConstants.associateHFs){
            Log.e("Webservice",item.getTenantHFGroupID()+"-");

          /*  for (AssociateQE QEItem: WebserviceConstants.associateQE) {
                Log.e("Webservice",item.getTenantHFGroupID()+"-"+QEItem.getTenantHFGroupID());
                if (item.getTenantHFGroupID()==QEItem.getTenantHFGroupID()) {
                    associateHF.add(item);
                    for (GetPatientDataView.AssociatedVitals item1 : item.getAssociateVitals()) {
                        AssociateVitalForSave obj = new AssociateVitalForSave();
                        obj.setFactorid(item1.getVitalID());
                        obj.setVitalValue("");
                        obj.setMeasurementDateTime("");
                        saveVitals.add(obj);
                    }

                }
            }*/
        }
        CreateViews();
        calendar=Calendar.getInstance();
    }
    public void CreateViews(){

        if (associateHF.size()==1){
            ll2.setVisibility(View.GONE);
            ll3.setVisibility(View.GONE);
        }
        else if (associateHF.size()==2){
            ll3.setVisibility(View.GONE);
        }
        else if (associateHF.size()==3){
            ll2.setVisibility(View.VISIBLE);
            ll3.setVisibility(View.VISIBLE);
        }
        else{
            ll1.setVisibility(View.GONE);
            ll2.setVisibility(View.GONE);
            ll3.setVisibility(View.GONE);
        }
        int i=0;
        for (GetPatientDataView.AssociatedHFGroup item : associateHF){
            ArrayList<GetPatientDataView.AssociatedVitals>tempArray=new ArrayList<GetPatientDataView.AssociatedVitals>(item.getAssociateVitals());
            Log.e("Group Name:-","---"+item.getTenantHFGroupName());
            if(!item.isIsSeperateDate()){
                GetPatientDataView.AssociatedVitals tempVitals= new GetPatientDataView.AssociatedVitals();
                tempVitals.setVitalName("Time");
                tempArray.add(tempVitals);
            }
            if (i==0){
                txtTitle1.setText(item.getTenantHFGroupName());
                ViewGroup.LayoutParams params = innerlist1.getLayoutParams();
                params.height = 50+(tempArray.size()*170);
                innerlist1.setLayoutParams(params);
                innerlist1.requestLayout();
                //holder.bglayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,50+(tempArray.size()*150)));
                QEInnerVitals adapter = new QEInnerVitals(QuickEntry_Activity.this,tempArray,i,item.isIsSeperateDate());
                innerlist1.setAdapter(adapter);
            }
            else if(i==1){
                txtTitle2.setText(item.getTenantHFGroupName());
                ViewGroup.LayoutParams params = innerlist2.getLayoutParams();
                params.height = 50+(tempArray.size()*170);
                innerlist2.setLayoutParams(params);
                innerlist2.requestLayout();
                //holder.bglayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,50+(tempArray.size()*150)));
                QEInnerVitals adapter = new QEInnerVitals(QuickEntry_Activity.this,tempArray,i,item.isIsSeperateDate());
                innerlist2.setAdapter(adapter);
            }
            else{
                txtTitle3.setText(item.getTenantHFGroupName());
                ViewGroup.LayoutParams params = innerlist3.getLayoutParams();
                params.height = 50+(tempArray.size()*170);
                innerlist3.setLayoutParams(params);
                innerlist3.requestLayout();
                //holder.bglayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,50+(tempArray.size()*150)));
                QEInnerVitals adapter = new QEInnerVitals(QuickEntry_Activity.this,tempArray,i,item.isIsSeperateDate());
                innerlist3.setAdapter(adapter);
            }

            i++;
        }


//        listView = (ListView)findViewById(R.id.lstReports);
//
//        QEGroup adapter = new QEGroup(this,associateHF);
//
//        listView.setAdapter(adapter);

    }
    public class QEGroup extends BaseAdapter {
        int number_of_clicks = 0;
        boolean thread_started = false;
        final int DELAY_BETWEEN_CLICKS_IN_MILLISECONDS = 250;
        InputStream is = null;
        String result123 = null;
        String line = null;
        public ArrayList<GetPatientDataView.AssociatedHFGroup> result;
        Context context;
        int[] imageId;
        public ImageLoader imageLoader;

        private LayoutInflater inflater = null;

        public QEGroup(Context mainActivity, ArrayList<GetPatientDataView.AssociatedHFGroup> arraydata) {
            // TODO Auto-generated constructor stub
            result = arraydata;
            context = mainActivity;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            imageLoader = new ImageLoader(context.getApplicationContext());

        }



        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return result.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public class Holder {
            LinearLayout bglayout;
            TextView tv;
            ListView lstInner;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            Holder holder = new Holder();
            View rowView;
            rowView = inflater.inflate(R.layout.quickentry_raw, null);
            holder.bglayout=(LinearLayout)rowView.findViewById(R.id.bgLayout);

            holder.tv = (TextView) rowView.findViewById(R.id.txtTitle);
            holder.lstInner = (ListView) rowView.findViewById(R.id.lstinnerview);
            ArrayList<GetPatientDataView.AssociatedVitals>tempArray=new ArrayList<GetPatientDataView.AssociatedVitals>(result.get(position).getAssociateVitals());
            Log.e("Group Name:-","---"+result.get(position).getTenantHFGroupName());
            holder.tv.setText(result.get(position).getTenantHFGroupName());
            if(!result.get(position).isIsSeperateDate()){
                GetPatientDataView.AssociatedVitals tempVitals= new GetPatientDataView.AssociatedVitals();
                tempVitals.setVitalName("Time");
                tempArray.add(tempVitals);
            }

            ViewGroup.LayoutParams params = holder.lstInner.getLayoutParams();
            params.height = 50+(tempArray.size()*150);
            holder.lstInner.setLayoutParams(params);
            holder.lstInner.requestLayout();
            //holder.bglayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,50+(tempArray.size()*150)));
            QEInnerVitals adapter = new QEInnerVitals(QuickEntry_Activity.this,tempArray,position,result.get(position).isIsSeperateDate());
            holder.lstInner.setAdapter(adapter);
            rowView.setTag(position);
            return rowView;
        }

    }


    public class QEInnerVitals extends BaseAdapter {
        int number_of_clicks = 0;
        boolean thread_started = false;
        final int DELAY_BETWEEN_CLICKS_IN_MILLISECONDS = 250;
        InputStream is = null;
        String result123 = null;
        String line = null;
        public ArrayList<GetPatientDataView.AssociatedVitals> result;
        Context context;
        int[] imageId;
        boolean seperated;
        public ImageLoader imageLoader;
        int grouptag;
        private LayoutInflater inflater = null;

        public QEInnerVitals(Context mainActivity, ArrayList<GetPatientDataView.AssociatedVitals> arraydata,int groupPosition,boolean isSeperated) {
            // TODO Auto-generated constructor stub
            result = arraydata;
            seperated=isSeperated;
            context = mainActivity;
            grouptag=groupPosition;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            imageLoader = new ImageLoader(context.getApplicationContext());

        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return result.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public class Holder {
            TextView tv1;
            EditText edt1;
            TextView tv2;
            TextView tvunit;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            final Holder holder = new Holder();
            View rowView;
            rowView = inflater.inflate(R.layout.qe_inner_raw, null);
            holder.tv1 = (TextView) rowView.findViewById(R.id.txt1);
            holder.edt1 = (EditText) rowView.findViewById(R.id.edt1);
            holder.tv2 = (TextView) rowView.findViewById(R.id.txtTime);
            holder.tvunit = (TextView) rowView.findViewById(R.id.txt_unit);
            holder.tvunit.setText(result.get(position).getVitalUnitName());
            if (!seperated){
                AssociateVitalForSave obj = null;
                int i=0;
                for (AssociateVitalForSave item:saveVitals){
                    if (saveVitals.get(i).getFactorid()==result.get(position).getVitalID()){
                        obj=saveVitals.get(i);
                        holder.edt1.setText(obj.getVitalValue());
                        Calendar cal = Calendar.getInstance();
                        int millisecond = cal.get(Calendar.MILLISECOND);
                        int second = cal.get(Calendar.SECOND);
                        int minute = cal.get(Calendar.MINUTE);
                        //12 hour format
                        int hour = cal.get(Calendar.HOUR);
                        //24 hour format
                        int hourofday = cal.get(Calendar.HOUR_OF_DAY);
                        SelectedPosition=grouptag;
                        setTimeFrom(hourofday,minute,obj,holder.tv2);
                    }
                    i++;
                }
                holder.tv1.setText(result.get(position).getVitalName());
                if (holder.tv1.getText().toString().equalsIgnoreCase("Time")){
                    holder.edt1.setVisibility(View.GONE);
                    Calendar cal = Calendar.getInstance();
                    int millisecond = cal.get(Calendar.MILLISECOND);
                    int second = cal.get(Calendar.SECOND);
                    int minute = cal.get(Calendar.MINUTE);
                    //12 hour format
                    int hour = cal.get(Calendar.HOUR);
                    //24 hour format
                    int hourofday = cal.get(Calendar.HOUR_OF_DAY);
                    SelectedPosition=grouptag;
                    setTimeFrom(hourofday,minute,obj,holder.tv2);
                }
                else {
                    holder.tv2.setVisibility(View.GONE);
                }
                holder.tv2.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      AssociateVitalForSave obj = null;
                                                      int i = 0;
                                                      for (AssociateVitalForSave item : saveVitals) {
                                                          Log.e("Compare",saveVitals.get(i).getFactorid()+"-"+result.get(position).getVitalID());
                                                          if (saveVitals.get(i).getFactorid() == result.get(position).getVitalID()) {
                                                              obj = saveVitals.get(i);
                                                          }
                                                          i++;
                                                      }
                                                      SelectedPosition=grouptag;
                                                      onTimeSelection(obj,holder.tv2);

                                                  }
                                              });

                        holder.edt1.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count,
                                                          int after) {
                            }

                            @Override
                            public void onTextChanged(final CharSequence s, int start, int before,
                                                      int count) {
                                AssociateVitalForSave obj = null;
                                int i = 0;
                                for (AssociateVitalForSave item : saveVitals) {
                                    if (saveVitals.get(i).getFactorid() == result.get(position).getVitalID()) {
                                        obj = saveVitals.get(i);
                                    }
                                    i++;
                                }
                                obj.setVitalValue(s.toString());
                            }
                            @Override
                            public void afterTextChanged(final Editable s) {
                                //avoid triggering event when text is too short
                            }
                        });



            }
            else{
                holder.tv1.setText(result.get(position).getVitalName());
                holder.tv2.setVisibility(View.VISIBLE);
                AssociateVitalForSave obj = null;
                int i=0;
                for (AssociateVitalForSave item:saveVitals){
                    if (saveVitals.get(i).getFactorid()==result.get(position).getVitalID()){
                        obj=saveVitals.get(i);
                        holder.edt1.setText(obj.getVitalValue());
                        Calendar cal = Calendar.getInstance();
                        int millisecond = cal.get(Calendar.MILLISECOND);
                        int second = cal.get(Calendar.SECOND);
                        int minute = cal.get(Calendar.MINUTE);
                        //12 hour format
                        int hour = cal.get(Calendar.HOUR);
                        //24 hour format
                        int hourofday = cal.get(Calendar.HOUR_OF_DAY);
                        SelectedPosition=grouptag;
                        setTimeFrom(hourofday,minute,obj,holder.tv2);
                    }
                    i++;
                }
                holder.tv2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AssociateVitalForSave obj = null;
                        int i = 0;
                        for (AssociateVitalForSave item : saveVitals) {
                            if (saveVitals.get(i).getFactorid() == result.get(position).getVitalID()) {
                                obj = saveVitals.get(i);
                            }
                            i++;
                        }
                        SelectedPosition=grouptag;
                        onTimeSelection(obj,holder.tv2);

                    }
                });


                holder.edt1.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }
                    @Override
                    public void onTextChanged(final CharSequence s, int start, int before,
                                              int count) {
                        AssociateVitalForSave obj = null;
                        int i=0;
                        for (AssociateVitalForSave item:saveVitals){
                            if (saveVitals.get(i).getFactorid()==result.get(position).getVitalID()){
                                obj=saveVitals.get(i);
                            }
                            i++;
                        }
                        obj.setVitalValue(s.toString());
                    }
                    @Override
                    public void afterTextChanged(final Editable s) {
                        //avoid triggering event when text is too short

                    }
                });
            }

            rowView.setTag(position);
            return rowView;
        }

    }
    private void onTimeSelection(final AssociateVitalForSave obj, final TextView tv){
        Log.e("Time",tv.getText().toString());
        if (tv.getText().toString().length() >0){
            String[] separated = tv.getText().toString().split(" ");
            String[] separated1 = separated[0].split(":");
            hourOfTime=Integer.parseInt(separated1[0]);
            minuteOfTime=Integer.parseInt(separated1[1]);

            if (separated[1].equalsIgnoreCase("PM")){
                hourOfTime=hourOfTime+12;
            }
            else{
                if (hourOfTime==12){
                    hourOfTime=0;
                }
            }


        }
        if (hourOfTime < 0) {
            hourOfTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            minuteOfTime = Calendar.getInstance().get(Calendar.MINUTE);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.datepicker_dialog, new TimePickerDialog.OnTimeSetListener() {
            @Override

            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                /*hourOfTime = hourOfDay;
                minuteOfTime = minute;

                String am_pm = "";
                if(hourOfTime >= 12 && hourOfTime != 24){
                    am_pm = "PM";
                }
                else
                {
                    am_pm = "AM";
                }

                String hourToPass = (hourOfTime%12) < 10 ? "0"+(hourOfTime%12) : (hourOfTime%12)+"";
                String minutesToPass = minuteOfTime < 10 ? "0"+(minuteOfTime) : minuteOfTime+"";
                setTime(hourToPass+" : "+minutesToPass +" " + am_pm);*/
                view.clearFocus();
                hourOfDay=view.getCurrentHour();
                minute=view.getCurrentMinute();
                if (view.isShown())
                {
                    setTimeFrom(hourOfDay, minute,obj,tv);
                }

//                view.clearFocus();


            }

        }, hourOfTime, minuteOfTime, false);

        timePickerDialog.show();
    }

    private void setTimeFrom(int hourOfDay, int minute,AssociateVitalForSave obj,TextView tv) {
        hourOfTime = hourOfDay;
        minuteOfTime = minute;

        String am_pm = "";
        if (hourOfTime >= 12 && hourOfTime != 24) {
            am_pm = "PM";
        } else {
            am_pm = "AM";
        }

        String hourToPass = (hourOfTime%12) < 10 ? ((hourOfTime%12) == 0 ? "12" : "0"+(hourOfTime%12)) : (hourOfTime%12)+"";
        String minutesToPass = minuteOfTime < 10 ? "0"+(minuteOfTime) : minuteOfTime+"";
        AppLog.showD("TAG","time:"+hourToPass+" : "+minutesToPass +" " + am_pm);
        tv.setText(hourToPass + ":" + minutesToPass + " " + am_pm);
        this.simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern(WebserviceConstants.DATE_FORMAT_WEBSERVICE);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, hourOfTime);
        calendar.set(Calendar.MINUTE, minuteOfTime);
        if (obj==null){
            int i=0;
            for (GetPatientDataView.AssociatedHFGroup item : associateHF) {
                        if (i==SelectedPosition) {
                            for (GetPatientDataView.AssociatedVitals item1 : item.getAssociateVitals()) {
                                for (AssociateVitalForSave item2 : saveVitals) {
                                    if (item1.getVitalID()==item2.getFactorid()){
                                        AssociateVitalForSave tempobj = item2;
                                        tempobj.setMeasurementDateTime(simpleDateFormat.format(calendar.getTime()));
                                    }
                                }
                            }
                        }

                i++;
                }
        }
        else {
            obj.setMeasurementDateTime(simpleDateFormat.format(calendar.getTime()));
        }
//        setTime(hourToPass + " : " + minutesToPass + " " + am_pm);
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
}
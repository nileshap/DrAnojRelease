package com.endo.intellih.activities;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.endo.intellih.AppConstants;
import com.endo.intellih.AppFunctions;
import com.endo.intellih.Fragments.dietnewfoodentry.ImageLoader;
import com.endo.intellih.R;
import com.endo.intellih.common.BaseActivity;
import com.endo.intellih.customviews.CustomButton;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.customviews.CustomTextView;
import com.endo.intellih.medicine.data.Medicine_Constants;
import com.endo.intellih.models.FeedbackList;
import com.endo.intellih.models.QuestionariesRetrieve;
import com.endo.intellih.models.QuestionnaireInputParams;
import com.endo.intellih.reports.Questionnaire_Chart;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.webservice.RemoteMethods3;
import com.endo.intellih.webservice.services.response.GetPatientDataView;
import com.endo.intellih.webservice.services.response.UserResponse;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by appaspect on 12/2/15.
 */
public class Questionnaire_Activity extends BaseActivity
{
    private ListView listView;
    GetPatientDataView userDetails;
    private Calendar calendar;
    private ArrayList<GetPatientDataView.CdmQuestionnary> cdmQuestionnaries;
    private ArrayList<GetPatientDataView.Questionary> questionnaries;
    private ArrayList<FeedbackList> feedbackLists;
    private ArrayList<FeedbackList> feedbackLists1;
    private ArrayList<FeedbackList> feedbackLists2;
    private ArrayList<FeedbackList> feedbackLists3;
    private ArrayList<com.endo.intellih.webservice.services.response.OutputDatum> dataList;
    private CustomTextView txtSelectedDate;
    private String currentDate = "Today";
    SimpleDateFormat simpleDateFormat;
    RelativeLayout Tab1,Tab2,Tab3;
    ImageView TabIcon1,TabIcon2,TabIcon3;
    CustomTextView lblTab1,lblTab2,lblTab3;
    int selectedTab=0;
    private Gson gson;
    CustomButton btn_view_chart;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);
        btn_view_chart = findViewById(R.id.btn_chart_questionaire);
        calendar = Calendar.getInstance();
        selectedTab=1;
        RefreshView();
    }


    public  void RefreshView(){


        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        //cdmQuestionnaries = (ArrayList<GetPatientDataView.CdmQuestionnary>) getIntent().getSerializableExtra("List");
        gson = new Gson();
        Bundle extra = getIntent().getExtras();
        cdmQuestionnaries = gson.fromJson(extra.getString(AppConstants.INTENT_KEY.Questionnaire_ARRAY), new TypeToken<List<GetPatientDataView.CdmQuestionnary>>() {
        }.getType());
        feedbackLists = new ArrayList<FeedbackList>();
        feedbackLists1 = new ArrayList<FeedbackList>();
        feedbackLists2 = new ArrayList<FeedbackList>();
        feedbackLists3 = new ArrayList<FeedbackList>();
        dataList = new ArrayList<com.endo.intellih.webservice.services.response.OutputDatum>();
        txtSelectedDate = (CustomTextView)findViewById(R.id.txtSelectedDate);
        txtSelectedDate.setText(currentDate);
        txtSelectedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDateClicked();
            }
        });
        ImageButton imgBtn_workout_home_prev = (ImageButton) findViewById(R.id.imgbtnPrev_medicine_home);
        ImageButton imgBtn_workout_home_next = (ImageButton) findViewById(R.id.imgbtn_next_medicine_home);
        questionnaries=new ArrayList<GetPatientDataView.Questionary>();
        ImageButton imgBack=(ImageButton)findViewById(R.id.imgBtnBack_home);
        userDetails = new Gson().fromJson(getIntent().getStringExtra(AppConstants.INTENT_KEY.USER_DETAILS), GetPatientDataView.class);
        final Gson gson = new Gson();

        Tab1=(RelativeLayout)findViewById(R.id.tabBtns1);
        Tab2=(RelativeLayout)findViewById(R.id.tabBtns2);
        Tab3=(RelativeLayout)findViewById(R.id.tabBtns3);

        TabIcon1=(ImageView)findViewById(R.id.tabicon1);
        TabIcon2=(ImageView)findViewById(R.id.tabicon2);
        TabIcon3=(ImageView)findViewById(R.id.tabicon3);

        lblTab1=(CustomTextView)findViewById(R.id.lbltab1);
        lblTab2=(CustomTextView)findViewById(R.id.lbltab2);
        lblTab3=(CustomTextView)findViewById(R.id.lbltab3);

        Tab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTab=1;
                lblTab1.setTextColor(Color.BLACK);
                lblTab2.setTextColor(Color.BLACK);
                lblTab3.setTextColor(Color.BLACK);
                int cdmCount=0;
                feedbackLists1 = new ArrayList<FeedbackList>();
                feedbackLists2 = new ArrayList<FeedbackList>();
                feedbackLists3 = new ArrayList<FeedbackList>();
                questionnaries=new ArrayList<GetPatientDataView.Questionary>();
                Tab1.setBackgroundResource(R.drawable.tab_bg_select);
                Tab2.setBackgroundResource(R.drawable.tab_bg_unselect);
                Tab3.setBackgroundResource(R.drawable.tab_bg_unselect);
                for (GetPatientDataView.CdmQuestionnary item : cdmQuestionnaries)
                {
                    cdmCount++;
                    for (GetPatientDataView.Questionary hfitem : item.getQuestionaries())
                    {
                        if (cdmCount==1) {
                            FeedbackList FBObject = new FeedbackList();
                            FBObject.setCdmQuestionId(hfitem.getCDMQuestionId());
                            FBObject.setQuestionnaireFeedbackId("");
                            FBObject.setFeedbackValue("");
                            FBObject.setFeedbackDateTime("");
                            feedbackLists1.add(FBObject);
                            questionnaries.add(hfitem);

                        }
                    }
                }
                QSelectionAdapter adapter = new QSelectionAdapter(Questionnaire_Activity.this,questionnaries);
                listView.setAdapter(adapter);
                getData();

            }
        });
        Tab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTab=2;
                lblTab2.setTextColor(Color.BLACK);
                lblTab1.setTextColor(Color.BLACK);
                lblTab3.setTextColor(Color.BLACK);
                int cdmCount=0;
                feedbackLists1 = new ArrayList<FeedbackList>();
                feedbackLists2 = new ArrayList<FeedbackList>();
                feedbackLists3 = new ArrayList<FeedbackList>();
                questionnaries=new ArrayList<GetPatientDataView.Questionary>();
                Tab2.setBackgroundResource(R.drawable.tab_bg_select);
                Tab1.setBackgroundResource(R.drawable.tab_bg_unselect);
                Tab3.setBackgroundResource(R.drawable.tab_bg_unselect);
                for (GetPatientDataView.CdmQuestionnary item : cdmQuestionnaries){
                    cdmCount++;
                    for (GetPatientDataView.Questionary hfitem : item.getQuestionaries()) {
                        if (cdmCount==2) {
                            FeedbackList FBObject = new FeedbackList();
                            FBObject.setCdmQuestionId(hfitem.getCDMQuestionId());
                            FBObject.setQuestionnaireFeedbackId("");
                            FBObject.setFeedbackValue("");
                            FBObject.setFeedbackDateTime("");
                            feedbackLists2.add(FBObject);
                            questionnaries.add(hfitem);

                        }
                    }
                }
                QSelectionAdapter adapter = new QSelectionAdapter(Questionnaire_Activity.this,questionnaries);
                listView.setAdapter(adapter);
                getData();
            }
        });
        Tab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTab=3;
                lblTab3.setTextColor(Color.BLACK);
                lblTab2.setTextColor(Color.BLACK);
                lblTab1.setTextColor(Color.BLACK);
                Tab3.setBackgroundResource(R.drawable.tab_bg_select);
                Tab1.setBackgroundResource(R.drawable.tab_bg_unselect);
                Tab2.setBackgroundResource(R.drawable.tab_bg_unselect);
                int cdmCount=0;
                feedbackLists1 = new ArrayList<FeedbackList>();
                feedbackLists2 = new ArrayList<FeedbackList>();
                feedbackLists3 = new ArrayList<FeedbackList>();
                questionnaries=new ArrayList<GetPatientDataView.Questionary>();
                for (GetPatientDataView.CdmQuestionnary  item : cdmQuestionnaries){
                    cdmCount++;
                    for (GetPatientDataView.Questionary hfitem : item.getQuestionaries()) {
                        if (cdmCount==3) {
                            FeedbackList FBObject = new FeedbackList();
                            FBObject.setCdmQuestionId(hfitem.getCDMQuestionId());
                            FBObject.setQuestionnaireFeedbackId("");
                            FBObject.setFeedbackValue("");
                            FBObject.setFeedbackDateTime("");
                            feedbackLists3.add(FBObject);
                            questionnaries.add(hfitem);

                        }
                    }
                }
                QSelectionAdapter adapter = new QSelectionAdapter(Questionnaire_Activity.this,questionnaries);
                listView.setAdapter(adapter);
                getData();

            }
        });


        imgBtn_workout_home_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DATE, -1);

                setCurrentDate(Medicine_Constants.simpleDateFormat.format(calendar.getTime()));

                getData();
            }
        });
        imgBtn_workout_home_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Calendar.getInstance().compareTo(calendar) > 0)
                {
                    calendar.add(Calendar.DATE, 1);
                    if (Calendar.getInstance().compareTo(calendar) <= 0)
                    {
                        setCurrentDate("Today");
                        calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                    }
                    else
                    {
                        setCurrentDate(Medicine_Constants.simpleDateFormat.format(calendar.getTime()));
                    }
                    getData();
                }
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button txtsave= (Button) findViewById(R.id.btn_save);
        txtsave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Questionnaire_Activity.this.simpleDateFormat = new SimpleDateFormat();
                calendar.set(Calendar.HOUR_OF_DAY, 00);
                calendar.set(Calendar.MINUTE, 00);
                calendar.set(Calendar.SECOND, 00);

                Calendar mCalendar = new GregorianCalendar();
                TimeZone mTimeZone = mCalendar.getTimeZone();
                int mGMTOffset = mTimeZone.getRawOffset();
                int minuteToAdd = (int) TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS);
                // Log.e("GMT offset is %s minutes", "" + TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS));
                Date localDate = Calendar.getInstance().getTime();
                SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sd.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date remoteDate = null;
                try {
                    SimpleDateFormat df=new SimpleDateFormat();
                    df.applyPattern("yyyy-MM-dd HH:mm:ss");
                    String d=df.format(Calendar.getInstance().getTime());
                    remoteDate = sd.parse(d); // your utc date
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                long diff = remoteDate.getTime() - localDate.getTime();
                long seconds = diff / 1000;
                int minutes = (int)seconds / 60;
                if (minutes>0)
                    minuteToAdd=minutes+1;
                else
                    minuteToAdd=minutes;
                Calendar calendarGMT = Calendar.getInstance();
                calendarGMT.setTime(calendar.getTime());
                calendarGMT.add(Calendar.MINUTE, (-minuteToAdd));

                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                QuestionnaireInputParams params=new QuestionnaireInputParams();
                params.setUserId(UserSharedPreferences.getInstance(Questionnaire_Activity.this).getString(UserSharedPreferences.KEY_USER_ID));
                params.setSelectedDate(Medicine_Constants.webServiceDateFormat.format(calendarGMT.getTime()));
                params.setMinutesToAdd(minuteToAdd);

                if (selectedTab==1)
                params.setFeedbackList(new ArrayList<FeedbackList>(feedbackLists1));
                if (selectedTab==2)
                    params.setFeedbackList(new ArrayList<FeedbackList>(feedbackLists2));
                if (selectedTab==3)
                    params.setFeedbackList(new ArrayList<FeedbackList>(feedbackLists3));
//                for (int i=0;i<params.getFeedbackList().size();i++){
//                    FeedbackList obj=params.getFeedbackList().get(i);
//                    if (obj.getFeedbackValue().isEmpty()){
//                        params.getFeedbackList().remove(obj);
//                    }
//                }
//
//                for (int i=0;i<params.getFeedbackList().size();i++){
//                    FeedbackList obj=params.getFeedbackList().get(i);
//                    if (obj.getFeedbackValue().isEmpty()){
//                        params.getFeedbackList().remove(obj);
//                    }
//                }
//                for (int i=0;i<params.getFeedbackList().size();i++){
//                    FeedbackList obj=params.getFeedbackList().get(i);
//                    if (obj.getFeedbackValue().isEmpty()){
//                        params.getFeedbackList().remove(obj);
//                    }
//                }
                for (int i=0;i<10;i++){
                    cleanFeedbackList(params);
                }
                Log.e("Feedback List",params.getFeedbackList().toString());
                for (int i=0;i<params.getFeedbackList().size();i++){
                    FeedbackList obj=params.getFeedbackList().get(i);

                    obj.setFeedbackDateTime(Medicine_Constants.webServiceDateFormat.format(calendarGMT.getTime()));

                    Log.e("getFeedbackValue",obj.getFeedbackValue());
                    Log.e("getFeedbackId",obj.getQuestionnaireFeedbackId());
                    Log.e("getCdmQuestionId", String.valueOf(obj.getCdmQuestionId()));
                    Log.e("FeedbackDateTime", String.valueOf(obj.getFeedbackDateTime()));

                }
                if (params.getFeedbackList().size()>0)
                {
                    CustomProgressbar.showProgressBar(Questionnaire_Activity.this, false);
                    RemoteMethods3.getQuestionnaire().saveQuestions(params, new Callback<UserResponse>() {
                        @Override
                        public void success(final UserResponse userResponse, retrofit.client.Response response) {
                            CustomProgressbar.hideProgressBar();
//                        Toast toast = Toast.makeText(Questionnaire_Activity.this,userResponse.Message,Toast.LENGTH_SHORT);
//                        toast.show();
                            AppFunctions.showMessageDialog(Questionnaire_Activity.this, "Record Saved Successfully");
                            RefreshView();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            CustomProgressbar.hideProgressBar();
                            Toast toast = Toast.makeText(Questionnaire_Activity.this, error.getMessage(), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }
                else{
                    AppFunctions.showMessageDialog(Questionnaire_Activity.this, "Please enter value");
                }
            }
        });
        Log.e("Size-1",cdmQuestionnaries.size()+"");
        int cdmCount=0;
        Tab1.setVisibility(View.GONE);
        Tab2.setVisibility(View.GONE);
        Tab3.setVisibility(View.GONE);
        for (GetPatientDataView.CdmQuestionnary  item : cdmQuestionnaries){
            cdmCount++;
            if (cdmCount==1){
                lblTab1.setText(item.getDescription());
                Tab1.setVisibility(View.VISIBLE);
            }
            if (cdmCount==2){
                lblTab2.setText(item.getDescription());
                Tab2.setVisibility(View.VISIBLE);
                Tab1.setVisibility(View.VISIBLE);
            }
            if (cdmCount==3){
                lblTab3.setText(item.getDescription());
                Tab1.setVisibility(View.VISIBLE);
                Tab2.setVisibility(View.VISIBLE);
                Tab3.setVisibility(View.VISIBLE);
            }
            Log.e("Size-1", item.getQuestionaries().size()+"");
            for (GetPatientDataView.Questionary hfitem : item.getQuestionaries()) {
//                questionnaries.add(hfitem);
                if (cdmCount==selectedTab) {
                    FeedbackList FBObject = new FeedbackList();
                    FBObject.setCdmQuestionId(hfitem.getCDMQuestionId());
                    FBObject.setQuestionnaireFeedbackId("");
                    FBObject.setFeedbackValue("");
                    FBObject.setFeedbackDateTime("");
                    if (selectedTab==1)
                    feedbackLists1.add(FBObject);
                    if (selectedTab==2)
                        feedbackLists2.add(FBObject);
                    if (selectedTab==3)
                        feedbackLists3.add(FBObject);
                    questionnaries.add(hfitem);
                }
            }
        }
        for (GetPatientDataView.Questionary hfitem : questionnaries) {
            FeedbackList FBObject=new FeedbackList();
            FBObject.setCdmQuestionId(hfitem.getCDMQuestionId());
            FBObject.setQuestionnaireFeedbackId("");
            FBObject.setFeedbackValue("");
            FBObject.setFeedbackDateTime("");
            feedbackLists.add(FBObject);
        }

        listView = (ListView)findViewById(R.id.lstReports);

        QSelectionAdapter adapter = new QSelectionAdapter(this,questionnaries);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ToggleButton toggleButton=(ToggleButton)view.findViewById(R.id.cdmselected);
                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value


            }

        });
        getData();

        btn_view_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Questionnaire_Activity.this, Questionnaire_Chart.class);
                Bundle bndle = new Bundle();
                bndle.putString("title", "Home");
                intent.putExtras(bndle);
                startActivity(intent);
            }
        });
    }
    public class QSelectionAdapter extends BaseAdapter {
        int number_of_clicks = 0;
        boolean thread_started = false;
        final int DELAY_BETWEEN_CLICKS_IN_MILLISECONDS = 250;
        InputStream is = null;
        String result123 = null;
        String line = null;
        public ArrayList<GetPatientDataView.Questionary> result;
        Context context;
        int[] imageId;
        public ImageLoader imageLoader;

        private LayoutInflater inflater = null;

        public QSelectionAdapter(Context mainActivity, ArrayList<GetPatientDataView.Questionary> arraydata) {
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
            TextView tv;
            TextView tgl;
            ImageButton btn1;
            ImageButton btn2;
            ImageButton btn3;
            ImageButton btn4;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            Holder holder;
            View rowView=convertView;
                rowView = inflater.inflate(R.layout.questionnaire_raw, null);
                holder=new Holder();

                holder.tv = (TextView) rowView.findViewById(R.id.txtRowContentTitle);
                holder.btn1 = (ImageButton) rowView.findViewById(R.id.btn1);
            holder.btn2 = (ImageButton) rowView.findViewById(R.id.btn2);
            holder.btn3 = (ImageButton) rowView.findViewById(R.id.btn3);
            holder.btn4 = (ImageButton) rowView.findViewById(R.id.btn4);
            holder.btn1.setId(position);
            holder.btn2.setId(position);
            holder.btn3.setId(position);
            holder.btn4.setId(position);

            holder.btn1.setTag(R.id.btn1,holder);
            holder.btn2.setTag(R.id.btn2,holder);
            holder.btn3.setTag(R.id.btn3,holder);
            holder.btn4.setTag(R.id.btn4,holder);
                holder.tgl = (TextView) rowView.findViewById(R.id.txtPicker);
                holder.tgl.setTag(position);
                holder.tv.setTag(position);
                holder.tv.setText(result.get(position).getQuestion());
//                holder.tgl.setText(feedbackLists.get(position).getFeedbackValue());
            if (selectedTab==1)
            {
                Log.e("SelectedTab-1" , " "  +feedbackLists1.get(position).getFeedbackValue());
                if (feedbackLists1.get(position).getFeedbackValue().equalsIgnoreCase("0")){
                    holder.btn1.setBackgroundResource(R.drawable.selectednotatall);
                }
                if (feedbackLists1.get(position).getFeedbackValue().equalsIgnoreCase("1")){
                    holder.btn2.setBackgroundResource(R.drawable.selectedalittleslightly);
                }
                if (feedbackLists1.get(position).getFeedbackValue().equalsIgnoreCase("2")){
                    holder.btn3.setBackgroundResource(R.drawable.selectedagreatdeal);
                }
                if (feedbackLists1.get(position).getFeedbackValue().equalsIgnoreCase("3")){
                    holder.btn4.setBackgroundResource(R.drawable.selectedextremelyworse);
                }
            }
            if (selectedTab==2){

                Log.e("SelectedTab-2" , " "  +feedbackLists2.get(position).getFeedbackValue());
                if (feedbackLists2.get(position).getFeedbackValue().equalsIgnoreCase("0")){
                    holder.btn1.setBackgroundResource(R.drawable.selectednotatall);
                }
                if (feedbackLists2.get(position).getFeedbackValue().equalsIgnoreCase("1")){
                    holder.btn2.setBackgroundResource(R.drawable.selectedalittleslightly);
                }
                if (feedbackLists2.get(position).getFeedbackValue().equalsIgnoreCase("2")){
                    holder.btn3.setBackgroundResource(R.drawable.selectedagreatdeal);
                }
                if (feedbackLists2.get(position).getFeedbackValue().equalsIgnoreCase("3")){
                    holder.btn4.setBackgroundResource(R.drawable.selectedextremelyworse);
                }
            }
            if (selectedTab==3){

                Log.e("SelectedTab-3" , " "  +feedbackLists3.get(position).getFeedbackValue());
                if (feedbackLists3.get(position).getFeedbackValue().equalsIgnoreCase("0")){
                    holder.btn1.setBackgroundResource(R.drawable.selectednotatall);
                }
                if (feedbackLists3.get(position).getFeedbackValue().equalsIgnoreCase("1")){
                    holder.btn2.setBackgroundResource(R.drawable.selectedalittleslightly);
                }
                if (feedbackLists3.get(position).getFeedbackValue().equalsIgnoreCase("2")){
                    holder.btn3.setBackgroundResource(R.drawable.selectedagreatdeal);
                }
                if (feedbackLists3.get(position).getFeedbackValue().equalsIgnoreCase("3")){
                    holder.btn4.setBackgroundResource(R.drawable.selectedextremelyworse);
                }
            }
            for (int i=0;i<feedbackLists.size();i++){
                FeedbackList f=feedbackLists.get(i);
                if (f.getCdmQuestionId()==result.get(position).getCDMQuestionId()){
                    if (!f.getFeedbackValue().isEmpty()) {
                        if (f.getFeedbackValue().equalsIgnoreCase("0")) {
                            holder.btn1.setBackgroundResource(R.drawable.selectednotatall);
                        }
                        if (f.getFeedbackValue().equalsIgnoreCase("1")) {
                            holder.btn2.setBackgroundResource(R.drawable.selectedalittleslightly);
                        }
                        if (f.getFeedbackValue().equalsIgnoreCase("2")) {
                            holder.btn3.setBackgroundResource(R.drawable.selectedagreatdeal);
                        }
                        if (f.getFeedbackValue().equalsIgnoreCase("3")) {
                            holder.btn4.setBackgroundResource(R.drawable.selectedextremelyworse);
                        }
                    }
                }
            }

                rowView.setTag(holder);
                Log.e("Size","-"+dataList.size()+"");


            holder.tgl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv=(TextView)v;

                    show(tv,result.get(Integer.parseInt(tv.getTag().toString())).getLowerLimit(),result.get(Integer.parseInt(tv.getTag().toString())).getUpperLimit(),Integer.parseInt(tv.getTag().toString()));
                }
            });
            holder.btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageButton tv=(ImageButton) v;
                    if (tv.getDrawable()==Questionnaire_Activity.this.getResources().getDrawable(R.drawable.selectednotatall)){

                    }
                    else{

                        tv.setBackgroundResource(R.drawable.selectednotatall);
                        Holder h1=(Holder)v.getTag(R.id.btn1);
                        h1.btn2.setBackgroundResource(R.drawable.alittleslichtly);
                        h1.btn3.setBackgroundResource(R.drawable.agreatdeal);
                        h1.btn4.setBackgroundResource(R.drawable.extremelyworse);
                        feedbackLists.get(tv.getId()).setFeedbackValue("0");
                        feedbackLists.get(tv.getId()).setFeedbackDateTime("");
                        if (selectedTab==1)
                            feedbackLists1.get(tv.getId()).setFeedbackValue("0");
                        if (selectedTab==2)
                            feedbackLists2.get(tv.getId()).setFeedbackValue("0");
                        if (selectedTab==3)
                            feedbackLists3.get(tv.getId()).setFeedbackValue("0");


                    }
                }
            });
            holder.btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageButton tv=(ImageButton) v;
                    if (tv.getDrawable()==Questionnaire_Activity.this.getResources().getDrawable(R.drawable.selectednotatall)){

                    }
                    else{
                        Holder h1=(Holder)v.getTag(R.id.btn2);
                        h1.btn1.setBackgroundResource(R.drawable.notatall);
                        h1.btn3.setBackgroundResource(R.drawable.agreatdeal);
                        h1.btn4.setBackgroundResource(R.drawable.extremelyworse);
                        tv.setBackgroundResource(R.drawable.selectedalittleslightly);
                        feedbackLists.get(tv.getId()).setFeedbackValue("1");
                        if (selectedTab==1)
                            feedbackLists1.get(tv.getId()).setFeedbackValue("1");
                        if (selectedTab==2)
                            feedbackLists2.get(tv.getId()).setFeedbackValue("1");
                        if (selectedTab==3)
                            feedbackLists3.get(tv.getId()).setFeedbackValue("1");
                    }
                }
            });
            holder.btn3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageButton tv=(ImageButton) v;
                    if (tv.getDrawable()==Questionnaire_Activity.this.getResources().getDrawable(R.drawable.selectednotatall)){

                    }
                    else{
                        Holder h1=(Holder)v.getTag(R.id.btn3);
                        h1.btn2.setBackgroundResource(R.drawable.alittleslichtly);
                        h1.btn1.setBackgroundResource(R.drawable.notatall);
                        h1.btn4.setBackgroundResource(R.drawable.extremelyworse);
                        tv.setBackgroundResource(R.drawable.selectedagreatdeal);
                        feedbackLists.get(tv.getId()).setFeedbackValue("2");
                        if (selectedTab==1)
                            feedbackLists1.get(tv.getId()).setFeedbackValue("2");
                        if (selectedTab==2)
                            feedbackLists2.get(tv.getId()).setFeedbackValue("2");
                        if (selectedTab==3)
                            feedbackLists3.get(tv.getId()).setFeedbackValue("2");
                    }
                }
            });
            holder.btn4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageButton tv=(ImageButton) v;
                    if (tv.getDrawable()==Questionnaire_Activity.this.getResources().getDrawable(R.drawable.selectednotatall)){

                    }
                    else{
                        Holder h1=(Holder)v.getTag(R.id.btn4);
                        h1.btn2.setBackgroundResource(R.drawable.alittleslichtly);
                        h1.btn3.setBackgroundResource(R.drawable.agreatdeal);
                        h1.btn1.setBackgroundResource(R.drawable.notatall);
                        tv.setBackgroundResource(R.drawable.selectedextremelyworse);
                        feedbackLists.get(tv.getId()).setFeedbackValue("3");
                        if (selectedTab==1)
                            feedbackLists1.get(tv.getId()).setFeedbackValue("3");
                        if (selectedTab==2)
                            feedbackLists2.get(tv.getId()).setFeedbackValue("3");
                        if (selectedTab==3)
                            feedbackLists3.get(tv.getId()).setFeedbackValue("3");
                    }
                }
            });
            return rowView;
        }

    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void show(final TextView tv, Integer lower, Integer upper, final int position)
    {

        final Dialog d = new Dialog(Questionnaire_Activity.this);
        d.setTitle("Select Answer");
        d.setContentView(R.layout.dialog);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(upper);
        np.setMinValue(lower);

        np.setDisplayedValues( new String[] { "0 – Not at all", "1 – A little slightly", "2 – A great deal, quite a bit","3 – Extremely Worse" } );
        np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        if (!tv.getText().toString().isEmpty())
        np.setValue(Integer.parseInt(tv.getText().toString()));
        np.setWrapSelectorWheel(false);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                tv.setText(String.valueOf(np.getValue()));
                feedbackLists.get(position).setFeedbackValue(tv.getText().toString());
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();


    }

    public void getData() {

        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 00);

        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        int minuteToAdd = (int) TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS);
        // Log.e("GMT offset is %s minutes", "" + TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS));
        Date localDate = Calendar.getInstance().getTime();
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sd.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date remoteDate = null;
        try {
            SimpleDateFormat df=new SimpleDateFormat();
            df.applyPattern("yyyy-MM-dd HH:mm:ss");
            String d=df.format(Calendar.getInstance().getTime());
            remoteDate = sd.parse(d); // your utc date
        } catch (ParseException e) {
            e.printStackTrace();
        }


        long diff = remoteDate.getTime() - localDate.getTime();
        long seconds = diff / 1000;
        int minutes = (int)seconds / 60;
        if (minutes>0)
            minuteToAdd=minutes+1;
        else
            minuteToAdd=minutes;
        Calendar calendarGMT = Calendar.getInstance();
        calendarGMT.setTime(calendar.getTime());
        calendarGMT.add(Calendar.MINUTE, (-minuteToAdd));

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        CustomProgressbar.showProgressBar(Questionnaire_Activity.this, false);
        RemoteMethods3.getQuestionnaire().getData(
                UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID),
                Medicine_Constants.webServiceDateFormat.format(calendarGMT.getTime()), Medicine_Constants.webServiceDateFormat.format(calendarGMT.getTime()),""+minuteToAdd,
                parseCallback);
    }


    Callback parseCallback = new Callback<QuestionariesRetrieve>() {
        @Override
        public void success(QuestionariesRetrieve QueResponse, Response response) {

            //gson.fromJson(response.bo);
            Log.e("Response",QueResponse.toString());
            dataList=(ArrayList<com.endo.intellih.webservice.services.response.OutputDatum>)QueResponse.getOutputData();
            CustomProgressbar.hideProgressBar();
            feedbackLists = new ArrayList<FeedbackList>();
            feedbackLists1 = new ArrayList<FeedbackList>();
            feedbackLists2 = new ArrayList<FeedbackList>();
            feedbackLists3 = new ArrayList<FeedbackList>();
            for (GetPatientDataView.Questionary hfitem : questionnaries)
            {
                FeedbackList FBObject=new FeedbackList();
                FBObject.setCdmQuestionId(hfitem.getCDMQuestionId());
                FBObject.setQuestionnaireFeedbackId("");
                FBObject.setFeedbackValue("");
                FBObject.setFeedbackDateTime("");
                feedbackLists.add(FBObject);
                if (selectedTab==1)
                    feedbackLists1.add(FBObject);
                if (selectedTab==2)
                    feedbackLists2.add(FBObject);
                if (selectedTab==3)
                    feedbackLists3.add(FBObject);
            }
            for (int i=0;i<dataList.size();i++)
            {
                for (int j=0;j<feedbackLists.size();j++)
                {
                    Log.e("Match Found1",dataList.get(i).getCdmQuestionId()+"-"+feedbackLists.get(j).getCdmQuestionId());
                    if (String.valueOf(dataList.get(i).getCdmQuestionId()).equalsIgnoreCase(String.valueOf(feedbackLists.get(j).getCdmQuestionId())))
                    {
                        Log.e("Match Found1",dataList.get(i).getFeedbackValue()+"");
                        feedbackLists.get(j).setQuestionnaireFeedbackId(dataList.get(i).getQuestionnaireFeedbackId());
                        feedbackLists.get(j).setFeedbackValue(dataList.get(i).getFeedbackValue()+"");
                    }
                }

            }
            for (int i=0;i<dataList.size();i++){
                for (int j=0;j<feedbackLists1.size();j++){
                    Log.e("Match Found1",dataList.get(i).getCdmQuestionId()+"-"+feedbackLists1.get(j).getCdmQuestionId());
                    if (String.valueOf(dataList.get(i).getCdmQuestionId()).equalsIgnoreCase(String.valueOf(feedbackLists1.get(j).getCdmQuestionId()))){
                        Log.e("Match Found1",dataList.get(i).getFeedbackValue()+"");
                        feedbackLists1.get(j).setQuestionnaireFeedbackId(dataList.get(i).getQuestionnaireFeedbackId());
                        feedbackLists1.get(j).setFeedbackValue(dataList.get(i).getFeedbackValue()+"");
                    }
                }

            }
            for (int i=0;i<dataList.size();i++){
                for (int j=0;j<feedbackLists2.size();j++){
                    Log.e("Match Found1",dataList.get(i).getCdmQuestionId()+"-"+feedbackLists2.get(j).getCdmQuestionId());
                    if (String.valueOf(dataList.get(i).getCdmQuestionId()).equalsIgnoreCase(String.valueOf(feedbackLists2.get(j).getCdmQuestionId()))){
                        Log.e("Match Found1",dataList.get(i).getFeedbackValue()+"");
                        feedbackLists2.get(j).setQuestionnaireFeedbackId(dataList.get(i).getQuestionnaireFeedbackId());
                        feedbackLists2.get(j).setFeedbackValue(dataList.get(i).getFeedbackValue()+"");
                    }
                }
            }
            for (int i=0;i<dataList.size();i++){
                for (int j=0;j<feedbackLists3.size();j++){
                    Log.e("Match Found1",dataList.get(i).getCdmQuestionId()+"-"+feedbackLists3.get(j).getCdmQuestionId());
                    if (String.valueOf(dataList.get(i).getCdmQuestionId()).equalsIgnoreCase(String.valueOf(feedbackLists3.get(j).getCdmQuestionId()))){
                        Log.e("Match Found1",dataList.get(i).getFeedbackValue()+"");
                        feedbackLists3.get(j).setQuestionnaireFeedbackId(dataList.get(i).getQuestionnaireFeedbackId());
                        feedbackLists3.get(j).setFeedbackValue(dataList.get(i).getFeedbackValue()+"");
                    }
                }

            }
            int cdmCount=0;
            questionnaries=new ArrayList<GetPatientDataView.Questionary>();
            for (GetPatientDataView.CdmQuestionnary item : cdmQuestionnaries){
                cdmCount++;
                for (GetPatientDataView.Questionary hfitem : item.getQuestionaries()) {
                    if (cdmCount==selectedTab) {
                        questionnaries.add(hfitem);
                    }
                }
            }
                QSelectionAdapter adapter = new QSelectionAdapter(Questionnaire_Activity.this,questionnaries);
                listView.setAdapter(adapter);

        }

        @Override
        public void failure(RetrofitError error) {
            CustomProgressbar.hideProgressBar();
            Log.e("Error",error.getMessage());
        }

    };
    public void setCurrentDate(String currentDate)
    {
        Questionnaire_Activity.this.currentDate = currentDate;
        txtSelectedDate.setText(currentDate);
    }
    public void onDateClicked() {
        String tempDate = txtSelectedDate.getText().toString();
        SimpleDateFormat dayDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        tempDate=dayDateFormat.format(calendar.getTime());
        String[] separated = tempDate.split("/");
        //cal.set(Integer.parseInt(separated[2]), Integer.parseInt(separated[0])-1, Integer.parseInt(separated[1]));
        openDateDialog(Integer.parseInt(separated[2]), Integer.parseInt(separated[0]) - 1, Integer.parseInt(separated[1]));

    }
    public void openDateDialog(int year, int month, int day)
    {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(year, month, day);
        DatePickerDialog dobDialog = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
        {
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
                            AppFunctions.showMessageDialog(Questionnaire_Activity.this, getString(R.string.msg_selecteddate_greate_then_to));
                        } else {
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

                            calendar.setTime(newDate.getTime());
                            if (getComparisionWithCurrentDate(calendar) <= 0) {
                                txtSelectedDate.setText("Today");
                                calendar = Calendar.getInstance();
//                cal.set(Calendar.HOUR_OF_DAY, 23);
//                cal.set(Calendar.MINUTE, 59);
                            } else {
                                txtSelectedDate.setText(DateFormat.getMediumDateFormat(Questionnaire_Activity.this).format(calendar.getTime()));
                            }
                            questionnaries=new ArrayList<GetPatientDataView.Questionary>();
                            feedbackLists = new ArrayList<FeedbackList>();
                            dataList = new ArrayList<com.endo.intellih.webservice.services.response.OutputDatum>();
                            loadDetails();
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
                            AppFunctions.showMessageDialog(Questionnaire_Activity.this, getString(R.string.msg_selecteddate_greate_then_to));
                        } else {
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

                            calendar.setTime(newDate.getTime());
                            if (getComparisionWithCurrentDate(calendar) <= 0) {
                                txtSelectedDate.setText("Today");
                                calendar = Calendar.getInstance();
//                cal.set(Calendar.HOUR_OF_DAY, 23);
//                cal.set(Calendar.MINUTE, 59);
                            } else {
                                txtSelectedDate.setText(DateFormat.getMediumDateFormat(Questionnaire_Activity.this).format(calendar.getTime()));
                            }
                            questionnaries=new ArrayList<GetPatientDataView.Questionary>();
                            feedbackLists = new ArrayList<FeedbackList>();
                            dataList = new ArrayList<com.endo.intellih.webservice.services.response.OutputDatum>();
                            loadDetails();
                        }
                    }
                }

            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        }
        dobDialog.show();

    }
    public void loadDetails(){
        for (GetPatientDataView.CdmQuestionnary item : cdmQuestionnaries){
            for (GetPatientDataView.Questionary hfitem : item.getQuestionaries()) {
                questionnaries.add(hfitem);
            }
        }
        for (GetPatientDataView.Questionary hfitem : questionnaries) {
            FeedbackList FBObject=new FeedbackList();
            FBObject.setCdmQuestionId(hfitem.getCDMQuestionId());
            FBObject.setQuestionnaireFeedbackId("");
            FBObject.setFeedbackValue("");
            FBObject.setFeedbackDateTime("");
            feedbackLists.add(FBObject);
        }
        listView = (ListView)findViewById(R.id.lstReports);

        QSelectionAdapter adapter = new QSelectionAdapter(this,questionnaries);
        listView.setAdapter(adapter);
        getData();
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
    public void cleanFeedbackList(QuestionnaireInputParams params){
        for (int i=0;i<params.getFeedbackList().size();i++){
            FeedbackList obj=params.getFeedbackList().get(i);
            if (obj.getFeedbackValue().isEmpty()){
                params.getFeedbackList().remove(obj);
            }
        }
    }
}
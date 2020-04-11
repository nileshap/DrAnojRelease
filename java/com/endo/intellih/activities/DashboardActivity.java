package com.endo.intellih.activities;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.constraint.Group;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.endo.intellih.Settings.Setting_Personal_Activity;
import com.endo.intellih.activities.questionnaire.New_Questionnaire_Activity;
import com.endo.intellih.database.DatabaseHelper;
import com.endo.intellih.database.UserProfileDB;
import com.endo.intellih.familymember.CareTeam_FM;
import com.endo.intellih.familymember.DashboardActivity_FM;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.endo.intellih.AppConstants;
import com.endo.intellih.AppFunctions;
import com.endo.intellih.DietHomeActivity;
import com.endo.intellih.Fragments.adapter.AdapterAppConfig;
import com.endo.intellih.GoalHomeActivity;
import com.endo.intellih.LoginActivity;
import com.endo.intellih.NotificationDetails;
import com.endo.intellih.R;
import com.endo.intellih.SelfMeasurementActivity;
import com.endo.intellih.Settings.Setting_Home_Activity;
import com.endo.intellih.application.AppController;
import com.endo.intellih.careplan.CarePlanActivity;
import com.endo.intellih.careteam.CareTeamActivity;
import com.endo.intellih.common.CommonMethod;

import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.customviews.CustomTextView;
import com.endo.intellih.customviews.PullToRefreshListView;
import com.endo.intellih.medicine.MedicineHomeActivity;
import com.endo.intellih.medicine.data.Medicine_Constants;
import com.endo.intellih.models.DashboardItem;
import com.endo.intellih.opentalk.eventCalendar.BasicActivityDecorated;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.vitalwirless.VitalActivityWirless;
import com.endo.intellih.webservice.Constant;
import com.endo.intellih.webservice.RemoteMethods;
import com.endo.intellih.webservice.RemoteMethods3;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.request.AppConfigParams;
import com.endo.intellih.webservice.services.request.IPCountParams;
import com.endo.intellih.webservice.services.response.AppConfigResponse;
import com.endo.intellih.webservice.services.response.GetLatestMessages;
import com.endo.intellih.webservice.services.response.GetPatientDataView;
import com.endo.intellih.webservice.services.response.IPCountResponse;
import com.endo.intellih.webservice.services.response.NewAppConfigResponse;
import com.endo.intellih.webservice.services.response.NewLoginResponse;
import com.endo.intellih.webservice.services.response.UserResponse;
import com.mikhaellopez.circularimageview.CircularImageView;


import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import sdk.mainscreen.MainDeviceList;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CALENDAR;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_CALENDAR;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class DashboardActivity extends AppCompatActivity
{
    public static final String TAG = DashboardActivity.class.getSimpleName();
    private static final String PACKAGE_NAME = "com.google.android.apps.fitness";
    private static final int PERMISSION_REQUEST_CODE_VIDEO_CALL = 200;
    private static final int PERMISSION_REQUEST_CODE_DIET = 201;
    private static final int PERMISSION_REQUEST_CODE_VITAL = 202;
    private static final int PERMISSION_REQUEST_CODE_PERSONAL_PROFILE = 203;
    public final int REQ_LOGOUT = 100;
    Button btnbreakfast, btnLunch, btnDinner, btnSnack;
    TextView txt_workout_activity;
    CommonMethod common;
    DataAdapterRibbon adapter;
    ArrayList<String> factorArray = new ArrayList<String>();
    boolean permission = false;
    CircularImageView img_user_profile;
    DatabaseHelper db;
    Boolean user_pic = false;
    Toolbar mTopToolbar;
    ArrayList<UserProfileDB> user_profile_data = new ArrayList<UserProfileDB>();
    ImageView img_vital, img_activity, img_diet;
    ImageView img_medicine, img_goal, img_report;
    ImageView img_personal_assessment, img_questionaire, img_health_tweet;
    ImageView layout_videocall, layout_mycareteam;
    private Button ivNotificationCount;
    private Button btn_caretemacount;
    private GoogleApiClient mClient = null;
    private Dialog dialog;
    private GridLayoutManager lLayout;
    // private UserResponse userDetails;
    private UserResponse userR;
    private GetPatientDataView userDetails;
    private Calendar cal;
    private SimpleDateFormat simpleDateFormat;
    private ArrayList<AppConfigResponse> appConfig;
    //private ArrayList<IPCountResponse.> ipCount;
    private ArrayList<IPCountResponse.MedicationColorIndicationList> medicineColorCode;
    private RecyclerView recyclerView_ribbon;
    private ArrayList<DashboardItem> rowListItem;
    private ArrayList<GetPatientDataView.Module> modulesName;
    private ArrayList<GetPatientDataView.AssociatedHFGroup> vitalArray;
    private ArrayList<GetPatientDataView.Reports> tenantReportList;
    private ArrayList<GetPatientDataView.CdmQuestionnary> cdmQuestionnaries;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean logout_check = false;
    boolean checkMedicine = false;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_new);

        mTopToolbar = (Toolbar) findViewById(R.id.toolbar_common);
        setSupportActionBar(mTopToolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar_layout);
        View view = getSupportActionBar().getCustomView();

        actionbarInit(view);


        db = new DatabaseHelper(this);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        common = new CommonMethod(DashboardActivity.this);


        getUserProfileImage();
        if (new Gson().fromJson(getIntent().getStringExtra(AppConstants.INTENT_KEY.USER_DETAILS), NewLoginResponse.class) == null) {
            userDetails = WebserviceConstants.patientViewData;
            Log.e("Settings", " " + "***");
        } else {
            userDetails = new Gson().fromJson(getIntent().getStringExtra(AppConstants.INTENT_KEY.USER_DETAILS), GetPatientDataView.class);
            WebserviceConstants.patientViewData = userDetails;
            Log.e("Settings", " " + "&&&&&&&");
        }

        modulesName = userDetails.getModules();


        Log.e("Settings@@@@", " " + modulesName.size());

        vitalArray = userDetails.getAssociatedHFGroups();


        txt_workout_activity = (TextView) findViewById(R.id.textView_activity_dashboard_activity);
        btnbreakfast = (Button) findViewById(R.id.button_med_breakfast_dashboard_activity);
        btnLunch = (Button) findViewById(R.id.button_med_lunch_dashboard_activity);
        btnDinner = (Button) findViewById(R.id.button_med_dinner_dashboard_activity);
        btnSnack = (Button) findViewById(R.id.button_med_snack_dashboard_activity);
        tenantReportList = userDetails.getReports();
        cdmQuestionnaries = userDetails.getCdmQuestionnaries();


        img_vital = (ImageView) findViewById(R.id.imageView_vital_dashboard_activity);
        img_activity = (ImageView) findViewById(R.id.ImaveView_activity_dashboard_activity);
        img_diet = (ImageView) findViewById(R.id.imageView_diet_dashboard_activity);

        img_goal = (ImageView) findViewById(R.id.ImaveView_goal_dashboard_activity);

        img_report = (ImageView) findViewById(R.id.imageView_report_dashboard_activity);

        img_personal_assessment = (ImageView) findViewById(R.id.imageView_personal_assessment_dashboard_activity);

        img_medicine = (ImageView) findViewById(R.id.imageView_medicine_dashboard_activity);
        img_questionaire = (ImageView) findViewById(R.id.imageView_questionnaire_dashboard_activity);
        img_health_tweet = (ImageView) findViewById(R.id.imageView_healttweet_dashboard_activity);

        layout_videocall = (ImageView) findViewById(R.id.imageView_opentalk_dashboard_activity);
        layout_mycareteam = (ImageView) findViewById(R.id.imageView_mycareteam_dashboard_activity);

        img_vital.setVisibility(View.GONE);
        img_goal.setVisibility(View.GONE);
        img_report.setVisibility(View.GONE);
        img_diet.setVisibility(View.GONE);
        img_activity.setVisibility(View.GONE);
        txt_workout_activity.setVisibility(View.GONE);
        img_medicine.setVisibility(View.GONE);
        img_questionaire.setVisibility(View.GONE);
        img_health_tweet.setVisibility(View.GONE);

        img_personal_assessment.setVisibility(View.GONE);


        rowListItem = new ArrayList<>();
        for (GetPatientDataView.Module item : modulesName) {

            Log.e("modulename", " " + item.getModuleName());
            DashboardItem obj = new DashboardItem();
            obj.setTitle(item.getModuleName());
            switch (item.getModuleName()) {
                case "Vitals":

                    img_vital.setVisibility(View.VISIBLE);
                    break;
                case "Goals":

                    img_goal.setVisibility(View.VISIBLE);
                    break;
                case "Diet":

                    img_diet.setVisibility(View.VISIBLE);
                    break;
                case "Workout":


                    img_activity.setVisibility(View.VISIBLE);
                    break;
                case "Medicine":

                    img_medicine.setVisibility(View.VISIBLE);
                    break;
                case "Reports":

                    img_report.setVisibility(View.VISIBLE);
                    break;

                case "Questionnaire":

                    img_questionaire.setVisibility(View.VISIBLE);
                    break;
                case "Health Tweet":

                    img_health_tweet.setVisibility(View.VISIBLE);
                    break;
                case "Activity":
                    txt_workout_activity.setVisibility(View.VISIBLE);
                    img_activity.setVisibility(View.VISIBLE);
                    break;
                case "Personal Assessment":

                    img_personal_assessment.setVisibility(View.VISIBLE);
                    break;
            }

            rowListItem.add(obj);
        }



            img_vital.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    onSelfMeasurementClick();
                }
            });



            img_diet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    onDietClick();
                }
            });


            img_health_tweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    onHealthTweetClick();
                }
            });



            img_activity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    onWorkoutClick();
                }
            });



            img_medicine.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Log.e("ClickOnMedicine", " " + "Click" + ":" + Constant.CLICK_ON_MEDICINE);
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    onMedicineClick();

                }
            });



            img_report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    onReportsClick();
                }
            });



            img_goal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    onGoalsClick();
                }
            });




            img_questionaire.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    onQuestionsClick();
                }
            });



            img_personal_assessment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    onLifeStyle();
                }
            });



            layout_videocall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    onOpenTalk();
                }
            });




            layout_mycareteam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    careTeam();
                }
            });
                WebserviceConstants.isDirt1 = false;
        init();
        setTextWorkOut();
    }


    public void actionbarInit(View view) {
        TextView txt_title = (TextView) view.findViewById(R.id.textView_title_custome_actionbar);

        RelativeLayout layout_notification = (RelativeLayout) view.findViewById(R.id.layout_custome_actionbar);

        ivNotificationCount = (Button) view.findViewById(R.id.home_ib_notificationcount);
        img_user_profile = (CircularImageView) view.findViewById(R.id.imageView1_custome_actionbar);

        img_user_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user_pic = true;
                if (checkPermissionForDiet()) {
                    permission = false;
                    Intent intent = new Intent(DashboardActivity.this, Setting_Personal_Activity.class);
                    startActivity(intent);
                } else {
                    Log.e("Request Pemission", " " + "Request Permission");
                    requestPermissionPersonalProfile();
                }
            }
        });


        final ImageView img_careplan = (ImageView) view.findViewById(R.id.imageView2_custome_actionbar);

        img_careplan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(DashboardActivity.this, CarePlanActivity.class);
                startActivity(intent);


            }
        });

        final ImageView img_notification = (ImageView) view.findViewById(R.id.imageView4_custome_actionbar);

        img_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(DashboardActivity.this, NotificationDetails.class);
                startActivity(intent);
            }
        });

        final ImageView img_settings = (ImageView) view.findViewById(R.id.imageView5_custome_actionbar);

        img_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                user_pic = true;
                Intent intent = new Intent(DashboardActivity.this, Setting_Home_Activity.class);
                intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, new Gson().toJson(userDetails));
                startActivity(intent);
            }
        });


        final ImageView img_logout = (ImageView) view.findViewById(R.id.imageView6_custome_actionbar);

        img_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout_check = true;

                if (!WebserviceConstants.isDirt1) {
                    WebserviceConstants.isDirt1 = true;
                    UserSharedPreferences.getInstance(AppController.getContext()).putBoolean("logout", true);
                    AppFunctions.showMessageDialog(DashboardActivity.this, getString(R.string.alert_logout), getString(R.string.dialog_yes),
                            getString(R.string.dialog_no), REQ_LOGOUT);
                } else {
                    WebserviceConstants.isDirt1 = false;
                }

            }
        });

        img_user_profile.setVisibility(View.VISIBLE);
        img_careplan.setVisibility(View.VISIBLE);
        txt_title.setVisibility(View.VISIBLE);
        layout_notification.setVisibility(View.VISIBLE);
        img_settings.setVisibility(View.VISIBLE);
        img_logout.setVisibility(View.VISIBLE);


    }

    private void init() {
        recyclerView_ribbon = (RecyclerView) findViewById(R.id.recyclerView_ribbon_recyclerview_dashboard_activity);
        btn_caretemacount = (Button) findViewById(R.id.button_careteam_badge_notificationcount);
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                viewGroup.setFocusableInTouchMode(true);
                viewGroup.requestFocus();
                viewGroup.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {


                            WebserviceConstants.isDirt1 = false;
                            UserSharedPreferences.getInstance(AppController.getContext()).putBoolean("logout", true);
                            AppFunctions.showMessageDialog(DashboardActivity.this, getString(R.string.alert_logout), getString(R.string.dialog_yes),
                                    getString(R.string.dialog_no), REQ_LOGOUT);
                            WebserviceConstants.isDirt1 = false;

                            return true;
                        }
                        return false;
                    }
                });

            }
        }, 1000);


        callWebServiceForRibbonData();

    }

    public void setAdapterRibbon() {
        recyclerView_ribbon.setHasFixedSize(true);

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(DashboardActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView_ribbon.setLayoutManager(horizontalLayoutManager);
        adapter = new DataAdapterRibbon(factorArray);
        recyclerView_ribbon.setAdapter(adapter);

        updateIpCount();

    }

    public void onClickModule(int position) {
        Log.e("ClickonModule", "position=" + position);
        Log.e("ClickonModule", "module name=" + modulesName.get(position).getModuleName());

        switch (modulesName.get(position).getModuleName()) {
            case "Vitals":
                onSelfMeasurementClick();
                break;
            case "Goals":
                onGoalsClick();
                break;
            case "Diet":
                // onDietClick();
                break;
            case "Workout":
                onWorkoutClick();
                break;
            case "Medicine":
                onMedicineClick();
                break;
            case "Reports":
                onReportsClick();
                break;
            case "Quick Entry":
                //  onQuickClick();
                break;
            case "Questionnaire":
                onQuestionsClick();
                break;
            case "Health Tweet":
                onHealthTweetClick();
                break;
            case "Activity":
                onActivityClick();
                break;
        }

    }

    public void onActivityClick() {
        Log.e("GetCDMNnAME", " " + "Click");
        ArrayList<GetPatientDataView.AssociatedCDM> associateCDM = userDetails.getAssociatedCDMs();
        boolean isPostOp = false;
        for (GetPatientDataView.AssociatedCDM cdm : associateCDM) {

            Log.e("GetCDMNnAME", " " + cdm.getCDMName());
            if (cdm.getCDMName().equalsIgnoreCase("Post Operative") || cdm.getCDMName().equalsIgnoreCase("CHF")) {
                isPostOp = true;
            }
        }

        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);

        /* if (isPostOp) {
            Intent intent = new Intent(this, UserActivity.class);
            startActivity(intent);

        } else {
            Intent intent = new Intent(this, WorkoutHomeActivity.class);
            startActivity(intent);
        }*/

    }

    public void onHealthTweetClick() {

       /* Intent intent = new Intent(this, ConnectApp.class);
        startActivity(intent);*/

        Intent intent = new Intent(this, HealthTweetActivity.class);
        startActivity(intent);
    }

    public void onQuestionsClick() {
        //Intent intent = new Intent(this, Questionnaire_Activity.class);
        Intent intent = new Intent(this, New_Questionnaire_Activity.class);
        intent.putExtra(AppConstants.INTENT_KEY.Questionnaire_ARRAY, new Gson().toJson(cdmQuestionnaries));
        startActivity(intent);
    }

    public void onQuickClick() {
        Intent intent = new Intent(this, QuickEntry_Activity.class);
        startActivity(intent);
    }

    public void onGoalsClick() {

        if (userDetails.getGoalSettings().size() != 0) {
            Intent intent = new Intent(this, GoalHomeActivity.class);
            intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, new Gson().toJson(userDetails));
            //intent.putExtra(AppConstants.INTENT_KEY.VITAL_VALIDATION, new Gson().toJson(vitalValidation));
            startActivity(intent);

        } else {
            AppFunctions.showMessageDialog(this, "No goals association with userid. contact with your administrator!");

        }
    }

    public void onDietClick() {


        if (checkPermissionForDiet()) {

            permission = false;
            Intent intent = new Intent(this, DietHomeActivity.class);
            startActivity(intent);


        } else {
            Log.e("Request Pemission", " " + "Request Permission");
            requestPermissionDiet();
        }
    }

    public void onWorkoutClick() {

//        Intent intent = new Intent(this, WorkoutHomeActivity.class);
//        startActivity(intent);
        ArrayList<GetPatientDataView.AssociatedCDM> associateCDM = userDetails.getAssociatedCDMs();
        boolean isPostOp = false;
        for (GetPatientDataView.AssociatedCDM cdm : associateCDM) {
            if (cdm.getCDMName().equalsIgnoreCase("Post Operative") || cdm.getCDMName().equalsIgnoreCase("CHF")) {
                isPostOp = true;
            }
        }

        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);

      /*  if (isPostOp) {
            Intent intent = new Intent(this, UserActivity.class);
            startActivity(intent);

        } else {
            Intent intent = new Intent(this, WorkoutHomeActivity.class);
            startActivity(intent);
        }
*/
      /*  Intent intent = new Intent(this, WorkoutHomeActivity.class);
        startActivity(intent);*/
    }

    public void onMedicineClick() {

        Intent intent = new Intent(this, MedicineHomeActivity.class);
        startActivity(intent);
    }

    public void onOpenTalk() {


        if (checkPermissionForVideoCall()) {

            permission = false;
            Intent intent = new Intent(this, BasicActivityDecorated.class);
            startActivity(intent);


        } else {
            Log.e("Request Pemission", " " + "Request Permission");
            requestPermissionForVideoCall();
        }

    }

    public void careTeam() {

        Intent intent = new Intent(this, CareTeamActivity.class);
        startActivity(intent);



    }

    public void onSelfMeasurementClick() {

        if (vitalArray.size() != 0) {


            if (checkPermissionForVital()) {

                permission = false;
                mBluetoothAdapter.enable();

                Intent intent = new Intent(this, VitalActivityWirless.class);
                intent.putExtra(AppConstants.INTENT_KEY.VITAL_ARRAY, new Gson().toJson(vitalArray));
                // intent.putExtra(AppConstants.INTENT_KEY.VITAL_VALIDATION, new Gson().toJson(vitalValidation));
                startActivity(intent);

            } else {

                Log.e("Request Pemission", " " + "Request Permission");
                requestPermissionVital();
            }

        } else {
            AppFunctions.showMessageDialog(this, "No Vitals association with userid. contact with your administrator!");

        }
    }

    public void onReportsClick() {

        Log.e("Reports", " " + tenantReportList.size());

        Intent intent = new Intent(this, Reports_Activity.class);
        //intent.putExtra("List", tenantReportList);
        intent.putExtra(AppConstants.INTENT_KEY.REPORT_ARRAY, new Gson().toJson(tenantReportList));
        startActivity(intent);
    }

    public void onLifeStyle() {

        Intent intent = new Intent(this, SelfMeasurementActivity.class);
        //intent.putExtra("List", tenantReportList);
        startActivity(intent);
    }

    public void onIpClick(View view) {

      /*  AppLog.showD("TAG", "on ip click");
        Intent intent = new Intent(this, ScoreCardActivity.class);
        startActivity(intent);*/


        Intent intent = new Intent(this, CarePlanActivity.class);
        startActivity(intent);

        /*Intent intent = new Intent(this, NewCarePlan.class);
        startActivity(intent);*/


       /* Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);*/

    }

    public void onWirelessClick(View view) {


        if (!mBluetoothAdapter.isEnabled()) {
            Log.e("Bluetooth_OFF", "" + "Bluetoothoff");
            mBluetoothAdapter.enable();
        } else {
            Log.e("Bluetooth_On", "" + "BluetoothOn");
            if (common.checkLocationOnOff()) {
                //Intent intent = new Intent(this, MainActivity.class);
                Intent intent = new Intent(this, MainDeviceList.class);
                startActivity(intent);
                Log.e("Lcation_On", "" + "Location_on");
            } else {
                showDialog(getResources().getString(R.string.pleaseopenlocation), 1);
            }
        }


    }

    public void showDialog(String message, final int no) {
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
                if (dialog != null) {
                    dialog.dismiss();
                }

                if (no == 1) {

                }
            }
        });

        dialog.show();
    }

    /**
     * on tap notificatin button
     */
    public void onNotificationClick(View view) {
        //setBackgroundImage(R.drawable.bg_normal);
        Intent intent = new Intent(this, NotificationDetails.class);
        startActivity(intent);
    }

    public void onSettingsClick(View view) {

        Intent intent = new Intent(this, Setting_Home_Activity.class);
        intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, new Gson().toJson(userDetails));
        startActivity(intent);
    }

    public void onLogout(View view) {
        Log.e("I M Here", "Here");

        logout_check = true;

        if (!WebserviceConstants.isDirt1) {
            WebserviceConstants.isDirt1 = true;
            UserSharedPreferences.getInstance(AppController.getContext()).putBoolean("logout", true);
            AppFunctions.showMessageDialog(this, getString(R.string.alert_logout), getString(R.string.dialog_yes),
                    getString(R.string.dialog_no), REQ_LOGOUT);
        } else {
            WebserviceConstants.isDirt1 = false;
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Token_ForeGround", WebserviceConstants.AUTHORIZATION_TOKEN);
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        //WebserviceConstants.tempVariable="NO";
        mLastClickTime = 0;
        if (!permission) {
            if (!logout_check) {
                SharedPreferences settins = PreferenceManager
                        .getDefaultSharedPreferences(this);
                String N = settins.getString("TimeOver", "");
                if (N.equals("YES")) {

                    SharedPreferences.Editor edit = settins.edit();
                    edit.putString("TimeOver", "NO");
                    edit.commit();
                    if (!WebserviceConstants.logout) {
                        Intent intent = new Intent(AppController.getContext(), LoginActivity.class);
                        //FLAG_ACTIVITY_NEW_TASK
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        AppController.getContext().startActivity(intent);
                    }
                } else {
                    Log.e("AuthToken", WebserviceConstants.AUTHORIZATION_TOKEN);
                    Log.e("UserId", UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID));
                    // if(WebserviceConstants.UPDATE_APP_CONFIG)
                    callWebServiceForRibbonData();
                }
            }


        }

        if (user_pic) {
            user_pic = false;

            getUserProfileImage();
        }

    }

    public void setAppConfigList(ArrayList<NewAppConfigResponse> appConfigResponses) {
        factorArray.clear();
        WebserviceConstants.appConfing.clear();
        WebserviceConstants.appConfing = appConfigResponses;
        for (NewAppConfigResponse item : appConfigResponses) {
            Log.e("Here", item.getFactorName() + ":" + item.getFactorId());
            if (item.getFactorValue() == null || item.getFactorValue().equalsIgnoreCase("null")) {
                factorArray.add(item.getFactorName() + ":");
            } else {

                if (item.getFactorId() == 16) {
                    factorArray.add(item.getFactorName() + ":" + item.getFactorValue() + " " + item.getUnitName());
                } else {
                    BigDecimal number = new BigDecimal(item.getFactorValue());
                    System.out.println(number.stripTrailingZeros().toPlainString());

                    factorArray.add(item.getFactorName() + ":" + number.stripTrailingZeros().toPlainString() + " " + item.getUnitName());
                }
            }
        }

        setAdapterRibbon();


    }

    public void updateIpCount() {

        Constant.array_medication_badge.clear();

        final IPCountParams ipCountParams = new IPCountParams();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(WebserviceConstants.Cal.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 00);

        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        int minuteToAdd = (int) TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS);
        Log.e("GMT offset is %s minutes", "" + TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS));

        Calendar calendarGMT = Calendar.getInstance();
        calendarGMT.setTime(calendar.getTime());
        calendarGMT.add(Calendar.MINUTE, (-minuteToAdd));

       Log.e("SelectedDate-DDD", " " + Medicine_Constants.webServiceDateFormat.format(calendarGMT.getTime()));
        // CustomProgressbar.showProgressBar(mFragment.getActivity(), false);

        String d1 = common.getCurrentDate("MM-dd-yyyy");

        RemoteMethods3.getHomeService().getIPCount(UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID), d1, "0", new Callback<IPCountResponse>() {
            @Override
            public void success(IPCountResponse ipCountResponse, Response response) {
                System.out.println("IPCount Success");

                Constant.array_medication_badge = ipCountResponse.getMedicationColorIndicationList();

                // Medication Color Codes
                medicineColorCode = ipCountResponse.getMedicationColorIndicationList();
                DisplayColors();

                CustomProgressbar.hideProgressBar();

                WebserviceConstants.UPDATE_APP_CONFIG = false;
                AppController.updatedHomeScreen();
                //updateNficationCount();
                Log.d("TAG", "NotificationCount" + ipCountResponse.getNotificationCount());
                if (ipCountResponse.getNotificationCount() == 0) {
                    ivNotificationCount.setVisibility(View.GONE);
                } else {
                    ivNotificationCount.setVisibility(View.VISIBLE);
                    ivNotificationCount.setText("" + ipCountResponse.getNotificationCount());
                }
                checkForGoogleFit();
            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("IPCount Fail" + error.getMessage());
                CustomProgressbar.hideProgressBar();
            }
        });
        GetLatestMessages();

    }


    public void GetLatestMessages() {
        Log.e("GetLatestMessages", " " + "@@@2");
        RemoteMethods3.getHomeService().getCareTeamConversationsSummary(UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID), new Callback<ArrayList<GetLatestMessages>>() {
            @Override
            public void success(ArrayList<GetLatestMessages> getlatestmesseges, Response response) {
                Log.e("GetLatestMessagesResponse", " " + response);
                WebserviceConstants.latest_messages_arraylist.clear();
                Constant.care_team_count = 0;
                WebserviceConstants.latest_messages_arraylist = getlatestmesseges;

                try {

                    for (int i = 0; i < getlatestmesseges.size(); i++) {
                        if (getlatestmesseges.get(i).getHasUnviewedMessages() && !getlatestmesseges.get(i).getCareTeamMemberUserId().equalsIgnoreCase("")) {
                            Constant.care_team_count = Constant.care_team_count + 1;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("SizeOfAttendee", " " + Constant.care_team_count);

                if (Constant.care_team_count == 0) {
                    btn_caretemacount.setVisibility(View.GONE);
                    Log.e("SizeOfAttendeeGone", " " + Constant.care_team_count);
                } else {
                    btn_caretemacount.setVisibility(View.VISIBLE);
                    btn_caretemacount.setText("" + Constant.care_team_count);
                    Log.e("SizeOfAttendeeVisible", " " + Constant.care_team_count);
                }

                CustomProgressbar.hideProgressBar();

            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("GetLatestMessages", " " + error);
                CustomProgressbar.hideProgressBar();
            }
        });
    }

    //Display Color Code for medicine
    public void DisplayColors() {

        for (int i = 0; i < medicineColorCode.size(); i++)
        {
            if (medicineColorCode.get(i).getMealType().equalsIgnoreCase("breakfast")) {
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("gray"))
                    btnbreakfast.setBackgroundResource(R.drawable.my_button_gray);
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("red"))
                    btnbreakfast.setBackgroundResource(R.drawable.my_button_red);
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("green"))
                    btnbreakfast.setBackgroundResource(R.drawable.my_button_green);
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("orange"))
                    btnbreakfast.setBackgroundResource(R.drawable.my_button_orange);
            } else if (medicineColorCode.get(i).getMealType().equalsIgnoreCase("lunch")) {
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("gray"))
                    btnLunch.setBackgroundResource(R.drawable.my_button_gray);
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("red"))
                    btnLunch.setBackgroundResource(R.drawable.my_button_red);
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("green"))
                    btnLunch.setBackgroundResource(R.drawable.my_button_green);
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("orange"))
                    btnLunch.setBackgroundResource(R.drawable.my_button_orange);
            } else if (medicineColorCode.get(i).getMealType().equalsIgnoreCase("dinner")) {
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("gray"))
                    btnDinner.setBackgroundResource(R.drawable.my_button_gray);
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("red"))
                    btnDinner.setBackgroundResource(R.drawable.my_button_red);
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("green"))
                    btnDinner.setBackgroundResource(R.drawable.my_button_green);
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("orange"))
                    btnDinner.setBackgroundResource(R.drawable.my_button_orange);
            } else if (medicineColorCode.get(i).getMealType().equalsIgnoreCase("snacks")) {
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("gray"))
                    btnSnack.setBackgroundResource(R.drawable.my_button_gray);
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("red"))
                    btnSnack.setBackgroundResource(R.drawable.my_button_red);
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("green"))
                    btnSnack.setBackgroundResource(R.drawable.my_button_green);
                if (medicineColorCode.get(i).getColorIndicator().equalsIgnoreCase("orange"))
                    btnSnack.setBackgroundResource(R.drawable.my_button_orange);
            }
        }
    }

//    public String searchFromArray(String key, ArrayList<AppConfigResponse> appConfigResponses) {
//
//        for (AppConfigResponse item : appConfigResponses) {
//            if (item.getFactorName().equals(key)) {
//                return item.getFactorValue();
//            }
//        }
//        return "";
//    }
//
//    public String searchFromArrayUnit(String key, ArrayList<AppConfigResponse> appConfigResponses) {
//
//        for (AppConfigResponse item : appConfigResponses) {
//            if (item.getFactorName().equals(key)) {
//                return item.getFactorValue();
//            }
//        }
//        return "";
//    }

    public String getRating(String value) {
        String result = "";
        switch (value) {
            case "1":
                result = "1-Very Low";
                break;
            case "2":
                result = "2-Low";
                break;
            case "3":
                result = "3-Fair";
                break;
            case "4":
                result = "4-High";
                break;
            case "5":
                result = "5-Very High";
                break;
        }
        return result;
    }


    public String formatFactors(float value) {
        if (value != 0) {
            double myNum = value;
            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(myNum);
        } else {
            return "";
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_LOGOUT && resultCode == FragmentActivity.RESULT_OK) {
            WebserviceConstants.logout = true;
            finish();
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("mainscreen", "login");
            startActivity(intent);
        }
    }


    public void checkForGoogleFit() {
        AppLog.showD(TAG, "checkForGoogleFit");

        if (isPackageInstalled(PACKAGE_NAME)) {
            AppLog.showD(TAG, "installed");
        } else {
            AppLog.showD(TAG, "not installed");
            if (dialog == null) {
                googleFitDialog();
            }
        }
    }


    private void googleFitDialog() {

        try {
            AppLog.showD(TAG, "googleFitDialog");
            dialog = new Dialog(this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            final View view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_googlefit, null);
            dialog.setContentView(view);

            TextView txtMessage = (TextView) dialog.findViewById(R.id.txt_message);
            txtMessage.setText(getString(R.string.txt_install_google_fit));

            Button btnOk = (Button) dialog.findViewById(R.id.btn_ok);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse("market://details?id=" + PACKAGE_NAME));
                    startActivity(goToMarket);
                    dialog.dismiss();
                }
            });
            Button btnCancle = (Button) dialog.findViewById(R.id.btn_cancle);
            btnCancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isPackageInstalled(String packagename) {
        try {
            getPackageManager().getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void setTextWorkOut() {
        ArrayList<GetPatientDataView.AssociatedCDM> associateCDM = userDetails.getAssociatedCDMs();
        boolean isPostOp = false;
        for (GetPatientDataView.AssociatedCDM cdm : associateCDM) {
            if (cdm.getCDMName().equalsIgnoreCase("Post Operative") || cdm.getCDMName().equalsIgnoreCase("CHF")) {
                isPostOp = true;
            }
        }
        txt_workout_activity.setText(getResources().getString(R.string.activity3));

        /*if (isPostOp) {
            txt_workout_activity.setText(getResources().getString(R.string.activity3));
        } else {
            txt_workout_activity.setText(getResources().getString(R.string.workout));
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {

            common.deleteFolder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Permission for vidio call

    private boolean checkPermissionForVideoCall() {


        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CALENDAR);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_CALENDAR);

        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermissionForVideoCall() {
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, READ_CALENDAR, WRITE_CALENDAR}, PERMISSION_REQUEST_CODE_VIDEO_CALL);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_VIDEO_CALL:
                if (grantResults.length > 0) {

                    boolean microphone = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean readCal = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean writeCal = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (microphone && readCal && writeCal) {
                        permission = false;
                        Intent intent = new Intent(this, BasicActivityDecorated.class);
                        startActivity(intent);
                    } else {

                        showDialogOKDialog(getResources().getString(R.string.somefeatures), 1);
                    }
                }
                break;
            case PERMISSION_REQUEST_CODE_DIET:
                if (grantResults.length > 0) {

                    boolean camera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean readCal = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean writeCal = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (camera && readCal && writeCal) {
                        permission = false;
                        Intent intent = new Intent(this, DietHomeActivity.class);
                        startActivity(intent);
                    } else {

                        showDialogOKDialog(getResources().getString(R.string.somefeatures), 2);
                    }
                }
                break;

            case PERMISSION_REQUEST_CODE_PERSONAL_PROFILE:
                if (grantResults.length > 0) {

                    boolean camera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean readCal = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean writeCal = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (camera && readCal && writeCal) {
                        permission = false;
                        Intent intent = new Intent(this, Setting_Personal_Activity.class);
                        startActivity(intent);
                    } else {

                        showDialogOKDialog(getResources().getString(R.string.somefeatures), 4);
                    }
                }
                break;

            case PERMISSION_REQUEST_CODE_VITAL:
                if (grantResults.length > 0) {

                    boolean recordAudio = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (recordAudio && writeStorage) {
                        permission = false;
                        mBluetoothAdapter.enable();

                        Intent intent = new Intent(this, VitalActivityWirless.class);
                        intent.putExtra(AppConstants.INTENT_KEY.VITAL_ARRAY, new Gson().toJson(vitalArray));
                        // intent.putExtra(AppConstants.INTENT_KEY.VITAL_VALIDATION, new Gson().toJson(vitalValidation));
                        startActivity(intent);

                    } else {

                        showDialogOKDialog(getResources().getString(R.string.somefeatures), 3);
                    }
                }
                break;
        }
    }

    private boolean checkPermissionForVital() {

        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionVital() {
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_VITAL);
    }

    private boolean checkPermissionForDiet() {

        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);


        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermissionDiet() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_DIET);
    }

    private void requestPermissionPersonalProfile() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_PERSONAL_PROFILE);
    }


    public void showDialogOKDialog(String message, final int no) {

        final Dialog dialog = new Dialog(DashboardActivity.this);
        LayoutInflater layoutInflater = LayoutInflater.from(DashboardActivity.this);
        View dialogView = layoutInflater.inflate(R.layout.custome_two_button_dialog, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        ((CustomTextView) dialogView.findViewById(R.id.textView_message_custome_two_dialog)).setText(message);
        Button btnOK = (Button) dialogView.findViewById(R.id.button_ok_custome_two_dialog);
        Button btnCancel = (Button) dialogView.findViewById(R.id.button_cancel_custome_two_dialog);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        permission = true;

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Yes

                if (dialog != null) {
                    dialog.dismiss();

                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Yes

                if (dialog != null) {
                    dialog.dismiss();
                    if (no == 1) {
                        requestPermissionForVideoCall();
                    } else if (no == 2) {
                        requestPermissionDiet();
                    } else if (no == 3) {
                        requestPermissionVital();
                    } else if (no == 4) {
                        requestPermissionPersonalProfile();
                    }
                }
            }
        });

        dialog.show();
    }

    public void getUserProfileImage()
    {
        /*String selectQuery = "SELECT  * FROM " + UserProfileDB.TABLE_NAME + " where " + UserProfileDB.COLUMN_USER_ID + "= ?";
        user_profile_data.clear();
        user_profile_data = db.getEmpData(selectQuery, UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID));
        Log.e("Size is**", " " + user_profile_data.size());
        if(user_profile_data.size() > 0)
        {
            byte[] blob = user_profile_data.get(0).getUser_photo();
            Log.e("bytephoto", " " + blob + ":" + common.getPhoto(blob));

           Glide.with(DashboardActivity.this).load(blob).asBitmap()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                   .fitCenter()
                    .into(img_user_profile);

           *//* img_user_profile.setImageBitmap(null);
            img_user_profile.setImageResource(0);
            img_user_profile.setImageBitmap(common.getPhoto(blob));*//*
        }
        else
        {*/

        String image_path = common.getUrl(DashboardActivity.this) + WebserviceConstants.GET_IMAGE_PATH + Constant.PROFILE_PIC;
        Log.e("UserProfilePic", " " + user_profile_data.size() + ":" + Constant.PROFILE_PIC + ":" + image_path);
        //avatar.png
       /* if(!Constant.PROFILE_PIC.equals("avatar.png"))
        {*/
        if (!Constant.PROFILE_PIC.equals(""))
        {
            Glide.with(getApplicationContext())
                    .load(image_path)
                    .asBitmap()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .placeholder(R.drawable.user)
                    .into(new BitmapImageViewTarget(img_user_profile) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            //Play with bitmap
                            super.setResource(resource);
                            long dele = db.deleteUserProfileData();
                            long id = db.insertUserProfile(UserSharedPreferences.getInstance(DashboardActivity.this).getString(UserSharedPreferences.KEY_USER_ID), resource);
                            Log.e("InsertData", "" + id + ":" + dele);

                        }
                    });


        } else {
            img_user_profile.setImageResource(R.drawable.avatar);
        }


       /* }
        else
        {
            img_user_profile.setImageResource(R.drawable.avatar);
        }*/
    }
    //}


    /* call web Service For ribbon*/

    public void callWebServiceForRibbonData()
    {
        AppConfigParams params = new AppConfigParams();
        params.N = "1";
        params.userId = UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID);
        cal = Calendar.getInstance();

        this.simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern(WebserviceConstants.DATE_FORMAT_WEBSERVICE);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        cal.set(Calendar.HOUR_OF_DAY, 00);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);

        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        int minuteToAdd = (int) TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS);

        AppLog.showE("GMT %s minutes", "" + TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS));

        Calendar calendarGMT = Calendar.getInstance();
        calendarGMT.setTime(cal.getTime());
        calendarGMT.add(Calendar.MINUTE, (-minuteToAdd));

        params.selectedDate = simpleDateFormat.format(cal.getTime());
        CustomProgressbar.showProgressBar(this, false);
        RemoteMethods3.getHomeService().getAppConfig(UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID), new Callback<ArrayList<NewAppConfigResponse>>() {
            @Override
            public void success(ArrayList<NewAppConfigResponse> appConfigResponses, Response response) {
                Log.d("TAG", "appConfigResponses=" + appConfigResponses);
                System.out.println("Success Response");
                setAppConfigList(appConfigResponses);


            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("Failure Response" + error.getMessage());
                CustomProgressbar.hideProgressBar();
            }
        });


    }



    /* ribbon data adapter*/

    //Class Data Adapter
    public class DataAdapterRibbon extends RecyclerView.Adapter<DataAdapterRibbon.ViewHolder> {
        private ArrayList<String> mArrayList;


        public DataAdapterRibbon(ArrayList<String> arrayList) {
            mArrayList = arrayList;

            Log.e("Size$4", " " + mArrayList.size());
        }

        @Override
        public DataAdapterRibbon.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_textview, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DataAdapterRibbon.ViewHolder holder, final int position) {

            if (!mArrayList.get(position).equals("")) {
                String factorname = mArrayList.get(position).split(":")[0];
                String unit = mArrayList.get(position).split(":")[1];

                holder.textview_vital_name.setText(factorname);
                holder.textview_vital_unit_name.setText(unit);
                Log.e("Unit", " " + factorname + " : " + unit);
            }


        }


        @Override
        public int getItemCount() {
            return mArrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textview_vital_name;
            TextView textview_vital_unit_name;


            public ViewHolder(View view) {
                super(view);


                textview_vital_name = (TextView) view
                        .findViewById(R.id.textView_vital_name_ribbon_row);

                textview_vital_unit_name = (TextView) view
                        .findViewById(R.id.textView_vital_unit_ribbon_row);


            }
        }

    }
}

package com.endo.intellih.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.endo.intellih.AppConstants;
import com.endo.intellih.AppFunctions;
import com.endo.intellih.R;
import com.endo.intellih.adapter.VitalListAdapter;
import com.endo.intellih.common.CommonMethod;
import com.endo.intellih.common.DecimalDigitsInputFilter;
import com.endo.intellih.customviews.CustomButton;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.customviews.CustomTextView;
import com.endo.intellih.listener.OnItemClickListener;
import com.endo.intellih.models.FactorIdList;
import com.endo.intellih.models.VitalsMeasurementList;
import com.endo.intellih.models.VitalsParam;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.validation.AllValidation;
import com.endo.intellih.webservice.RemoteMethods3;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.request.VitalDeleteRequest;
import com.endo.intellih.webservice.services.request.VitalListRequest;
import com.endo.intellih.webservice.services.request.VitalUpdateRequest;
import com.endo.intellih.webservice.services.response.BaseResponse;
import com.endo.intellih.webservice.services.response.GetPatientDataView;
import com.endo.intellih.webservice.services.response.VitalRes;
import com.endo.intellih.webservice.services.response.VitalUpdateResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by admin on 10/21/2016.upd
 */
public class VitalListActivity extends FragmentActivity {

    public static final String TAG = VitalListActivity.class.getSimpleName();
    private GetPatientDataView.AssociatedHFGroup moduleData;
    private Gson gson;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private Button btnBack;
    private TextView txtTitle, txtNodata;
    private ArrayList<VitalsMeasurementList> mData;
    private VitalListAdapter mAdapter;
    private SimpleDateFormat sdf;
    private String inputPattern = "yyyy-MM-dd'T'HH:mm:ss";
    private String outputDatePattern = "MMM dd,yyyy";
    private String outputTimePattern = "EEEEEEEEEEE '|' h:mm a";
    private String outputTimePatternOnly = "h:mm a";
    private String outputDialogDate = "MM/dd/yyyy";
    private TAB selectedTab = TAB.TODAY;
    FragmentManager fm;
    EditText txtBMI;
    private String factorCompare = "";
    private VitalsMeasurementList extraItem;
    private VitalRes malGrpVital;
    String hieghtUnit;
    boolean extra_field = false;
    int extra_factorid = 0;
    String extra_factorValue = "";
    String extra_measurmentId = "";
    EditText txtFactorValueextra;
    EditText txtFactorValue;
    TextView txtFactorUnitExtra;
    TextView txtFactorUnit;
    CommonMethod common;
    String selected_date = "";


    private enum TAB {
        TODAY(1), WEEKLY(2), MONTHLY(3);

        final int code;

        TAB(int id) {
            code = id;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vital_list);
        common = new CommonMethod(VitalListActivity.this);
        fm = getSupportFragmentManager();
        sdf = new SimpleDateFormat();
        gson = new Gson();
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            moduleData = gson.fromJson(extra.getString(AppConstants.INTENT_KEY.VITAL_MAIN), GetPatientDataView.AssociatedHFGroup.class);
            selected_date = extra.getString(AppConstants.INTENT_KEY.VITAL_LISTDATE);
            Log.e("SelectedDate", " " + selected_date);
        }
        hieghtUnit = UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_Unit_Height);

        btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        txtNodata = (TextView) findViewById(R.id.txt_nodata);
        txtTitle = (TextView) findViewById(R.id.txt_title);
        txtTitle.setText(moduleData.getTenantHFGroupName());
        Log.e("TitleList", moduleData.getTenantHFGroupName());
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        // mRecyclerView.addItemDecoration(new ItemOffsetDecoration(this, R.dimen.dashboad_item_spacing));
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
//        Calendar calendarToDate = Calendar.getInstance();
        Calendar calendarToDate = WebserviceConstants.Cal;

        calendarToDate.set(Calendar.HOUR_OF_DAY, 00);
        calendarToDate.set(Calendar.MINUTE, 00);
        calendarToDate.set(Calendar.SECOND, 00);

        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        int minuteToAdd = (int) TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS);
        Log.e("minutes", "" + TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS));

        Calendar calendarGMT = Calendar.getInstance();
        calendarGMT.setTime(calendarToDate.getTime());
        calendarGMT.add(Calendar.MINUTE, (-minuteToAdd));

        calendarToDate.set(Calendar.HOUR_OF_DAY, 23);
        calendarToDate.set(Calendar.MINUTE, 59);


       // String date1 = common.getUtcTime2(common.convertDate_14(common.getCurrentDate("yyyy-MM-dd")+" "+"12:00 AM"));
        //String date1 = common.getCurrentDate("MM-dd-yyyy");

        VitalListRequest request = new VitalListRequest();
        request.setUserId(UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID));
        request.setSelectedDate(selected_date);
        //request.setSelectedDate(date);
        request.setMinutesToAdd(0);
        request.setNoOfDays(days);
        ArrayList<FactorIdList> factorList = new ArrayList<>();

        for (GetPatientDataView.AssociatedVitals item : moduleData.getAssociateVitals()) {
            FactorIdList factor = new FactorIdList();
            factor.setFactorId(item.getVitalID());
            factorList.add(factor);

        }
        request.setFactorIdList(factorList);


        RemoteMethods3.getVitalService().vitalList(request, new Callback<VitalRes>() {
            @Override
            public void success(VitalRes response, Response response2) {
                AppLog.showD(TAG, "success");

                CustomProgressbar.hideProgressBar();
                mData = new ArrayList<>();
                // mData = (ArrayList<VitalsMeasurementList>) response.getVitalsMeasurementGroupList().get(1).getVitalsMeasurementList();

                if (response.getVitalsMeasurementList().size() == 0) {
                    txtNodata.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    return;
                }
                txtNodata.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);

              /*
                Log.e("SizeOfdata-1", " "  + malGrpVital.size()  + ":" + mData.size());
                for (VitalRes item : malGrpVital)
                {
                    mData.addAll(item.getVitalsMeasurementList());
                }*/

                mData = response.getVitalsMeasurementList();

                for(int i=0; i < mData.size();i++)
                {
                    Log.e("VitalListData##@@", " " + mData.get(i).getFactorName()  +":" + mData.get(i).getMeasurementDateTimeUTC());
                }


                Collections.sort(mData, new Comparator<VitalsMeasurementList>() {
                    @Override
                    public int compare(VitalsMeasurementList lhs, VitalsMeasurementList rhs) {

                        sdf.applyPattern("yyyy-MM-dd hh:mm a");
                        try {

                            /*Date d1 = sdf.parse(common.convertDate_common("yyyy-MM-dd'T'HH:mm:ss","yyyy-MM-dd hh:mm a" , common.convertInLocalTime(lhs.getMeasurementDateTimeUTC())));
                            Date d2 = sdf.parse(common.convertDate_common("yyyy-MM-dd'T'HH:mm:ss","yyyy-MM-dd hh:mm a" , common.convertInLocalTime(rhs.getMeasurementDateTimeUTC())));
*/
                            Date d1 = sdf.parse(common.convertDate_common("yyyy-MM-dd'T'HH:mm:ss","yyyy-MM-dd hh:mm a", lhs.getMeasurementDateTime()));
                            Date d2 = sdf.parse(common.convertDate_common("yyyy-MM-dd'T'HH:mm:ss","yyyy-MM-dd hh:mm a" , rhs.getMeasurementDateTime()));

                            return d2.compareTo(d1);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        return 0;
                    }
                });

                for(int i=0; i < mData.size();i++)
                {
                    Log.e("VitalListData##", " " + mData.get(i).getFactorName()  +":" + mData.get(i).getMeasurementDateTimeUTC());
                }

                Log.e("SizeOfdata", " " + ":" + mData.size());
                mAdapter = new VitalListAdapter(mData, VitalListActivity.this);
                mAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        AppLog.showD(TAG, "ontap=" + position);
                        showDeleteUpdateDialog(position);
                    }
                });
                mAdapter.setHasStableIds(true);
                mRecyclerView.setAdapter(mAdapter);

            }

            @Override
            public void failure(RetrofitError error) {
                AppLog.showD(TAG, "failure=" + error.getMessage());
                CustomProgressbar.hideProgressBar();
            }
        });

    }


    public void showDeleteUpdateDialog(final int position)
    {
        final Dialog dialog = new Dialog(VitalListActivity.this);
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

        radio_one.setVisibility(View.GONE);

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



       /* AppLog.showD(TAG, "showDeleteUpdateDialog");
        final ArrayAdapter<String> arrayAdapter;
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                this);
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

    private void showDeleteConfirmationDialog(final int position) {

        final Dialog dialog = new Dialog(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View dialogView = layoutInflater.inflate(R.layout.fragment_dialog_delete, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
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


    public void deleteRecord(final int position) {
        AppLog.showD(TAG, "deleteRecord=" + position);
        CustomProgressbar.showProgressBar(this, false);
        VitalDeleteRequest request = new VitalDeleteRequest();
        request.setUserId(UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID));
        request.setMeasurementId(mData.get(position).getMeasurementGroupId());
        RemoteMethods3.getVitalService().vitaldelete(request, new Callback<BaseResponse>() {
            @Override
            public void success(BaseResponse response, Response response2) {
                CustomProgressbar.hideProgressBar();
                WebserviceConstants.appConfing = response.latestVitalValues;
                AppLog.showD(TAG, "deleted succuss");
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
                CustomProgressbar.hideProgressBar();
                AppLog.showD(TAG, "failure");
            }
        });

    }

    DateTimeFormatter webFrt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    DateTimeFormatter txtDateFrt = DateTimeFormat.forPattern("MM/dd/yyyy");
    DateTimeFormatter txtTimeFrt = DateTimeFormat.forPattern("h:mm a");

    public void openDialogForUpdate(final int position)
    {

        Log.e("UpdateDialogOpen", " " + position);
        Log.e(TAG, "openDialogForUpdate");
        final VitalsMeasurementList item = mData.get(position);
        Log.e(TAG, "vital id =" + item.getFactorId() + ":" + item.getFactorDesc());

        /*for (GetPatientDataView.AssociatedVitals assoVital : moduleData.getAssociateVitals())
        {
            if (assoVital.getVitalID() == item.getFactorId()) {
                item.setVitalValidation(assoVital.getVitalValidation());
            }
        }*/

        //checkForFactorCompare(item);

        // checkForFactorCompare(item.getFactorId());

        //final DateTime temp = webFrt.withOffsetParsed().parseDateTime(item.getMeasurementDateTime());

        String mesaurementdatetime = common.getCurrentTimeToUtc_3(item.getMeasurementDateTimeUTC());

        final DateTime temp = webFrt.withOffsetParsed().parseDateTime(mesaurementdatetime);


        String dateShow = txtDateFrt.print(temp);
        String timeShow = txtTimeFrt.print(temp).toUpperCase();
        final int[] hours = {temp.getHourOfDay()};
        final int[] mins = {temp.getMinuteOfHour()};
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View dialogView = layoutInflater.inflate(R.layout.dialog_update_vital, null);
        dialog.setContentView(dialogView);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;



        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));




        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        LinearLayout llExtra = (LinearLayout) dialogView.findViewById(R.id.ll_extra);

        LinearLayout layout_bg = (LinearLayout) dialogView.findViewById(R.id. layout_bg_dialog_update_vital);


        //ViewGroup.LayoutParams params = layout_bg.getLayoutParams();
        // Changes the height and width to the specified *pixels*
        //params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        if (item.getFactorId() == 5 || item.getFactorId() == 6 || item.getFactorId() == 2 || item.getFactorId() == 4) {
            llExtra.setVisibility(View.VISIBLE);
            //int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 455, getResources().getDisplayMetrics());
           // params.height = height;
        } else {
            llExtra.setVisibility(View.GONE);
            //int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 390, getResources().getDisplayMetrics());
           // params.height = height;
        }

        //layout_bg.setLayoutParams(params);


        /*Log.e("ExtraItem", " "+ extraItem);
        if (extraItem != null)
        {
            if (item.getFactorId()==6){
                final VitalsMeasurementList tempitem= item;
                item= extraItem;
                extraItem= tempitem;
                Log.e("Swipe-extraItem",extraItem.getFactorId()+"->"+extraItem.getFactorValue());
                Log.e("Swipe-item",item.getFactorId()+"->"+item.getFactorValue());
               // checkForFactorCompare(item);

            }
            if (item.getFactorId()==2){
                final VitalsMeasurementList tempitem= item;
                item= extraItem;
                extraItem= tempitem;
                Log.e("Swipe-extraItem",extraItem.getFactorId()+"->"+extraItem.getFactorValue());
                Log.e("Swipe-item",item.getFactorId()+"->"+item.getFactorValue());
               // checkForFactorCompare(item);

            }

            AppLog.showE(TAG, "extra item is there");
            llExtra.setVisibility(View.VISIBLE);

            TextView txtFactorLableExtra = (TextView) dialogView.findViewById(R.id.txt_lable1);
            txtFactorLableExtra.setText(extraItem.getFactorDesc());

            final EditText txtFactorValueExtra = (EditText) dialogView.findViewById(R.id.edt_value1);

            Log.e("FactorValue",  " " + extraItem.getFactorValue());

            if(extraItem.getFactorId()==16) {

                txtFactorValueExtra.setText(extraItem.getFactorValue());
            }
            else
            {
                BigDecimal number = new BigDecimal(extraItem.getFactorValue());
                Log.e("FactorValue***",  " " + number.stripTrailingZeros().toPlainString());
                // System.out.println(number.stripTrailingZeros().toPlainString());
                txtFactorValueExtra.setText(number.stripTrailingZeros().toPlainString());
            }



            txtBMI=txtFactorValueExtra;
            if (extraItem.getFactorId()==2){

                txtFactorValueExtra.setAlpha((float) 0.5);
                txtFactorValueExtra.setEnabled(false);
            }
           *//* if (extraItem.getVitalValidation() != null) {
                if (extraItem.getVitalValidation().getDataType().size() > 0) {
                    DataType dataType = extraItem.getVitalValidation().getDataType().get(0);
                    if (dataType.getDataType().equals("Integer")) {
                        txtFactorValueExtra.setRawInputType(Configuration.KEYBOARD_12KEY);
                    } else if (dataType.getDataType().equals("Decimal")) {
                        txtFactorValueExtra.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    }

                    txtFactorValueExtra.setFilters(new InputFilter[]{new InputFilter.LengthFilter(item.getVitalValidation().getDataType().get(0).getValueLength())});
                }
            }*//*

            setValidation(txtFactorValueExtra,extraItem.getFactorId());

            txtFactorValueExtra.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    if (s.length() > 0) {
                        factorCompare = s.toString();
                        extraItem.setFactorValue(s.toString());
                    }
                    else{
                        factorCompare = "";
                        extraItem.setFactorValue("");
                    }


                }
            });

            TextView txtFactorUnitExtra = (TextView) dialogView.findViewById(R.id.txt_unit1);
            txtFactorUnitExtra.setText("" + extraItem.getUnitName());

        } else {
            llExtra.setVisibility(View.GONE);
        }*/

        Log.e("FactorName*****", " " + item.getFactorDesc() + ":" + item.getFactorValue());

        TextView txtTitle = (TextView) dialogView.findViewById(R.id.txt_title);
        txtTitle.setText(item.getFactorDesc());

        TextView txtFactorLable = (TextView) dialogView.findViewById(R.id.txt_lable);
        TextView txtFactorLableExtra = (TextView) dialogView.findViewById(R.id.txt_lable1);
        txtFactorUnitExtra = (TextView) dialogView.findViewById(R.id.txt_unit1);
        txtFactorLable.setText(item.getFactorDesc());

        txtFactorValue = (EditText) dialogView.findViewById(R.id.edt_value);

        txtFactorValueextra = (EditText) dialogView.findViewById(R.id.edt_value1);


        if (item.getFactorId() == 5) {
            AppLog.showE(TAG, "SPB");
            String MeasurementGroupId = item.getMeasurementGroupId();
            for (VitalsMeasurementList vitalList : mData) {

                if (MeasurementGroupId.equals(vitalList.getMeasurementGroupId())) {
                    AppLog.showE(TAG, "got measurement date");
                    if (vitalList.getFactorId() == 6) {
                        extra_field = true;
                        extra_factorid = vitalList.getFactorId();
                        extra_factorValue = vitalList.getFactorValue();
                        extra_measurmentId = vitalList.getMeasurementId();


                        txtFactorLableExtra.setText(vitalList.getFactorDesc());
                        txtFactorValueextra.setText(vitalList.getFactorValue());
                        txtFactorUnitExtra.setText(vitalList.getUnitName());
                        Log.e("ExtraSet", " " + vitalList.getFactorDesc() + ":" + vitalList.getFactorValue() + ":" + vitalList.getUnitName());
                        break;
                    }
                }
            }

            setValidation(txtFactorValueextra, extra_factorid);
        }

        if (item.getFactorId() == 6) {
            AppLog.showE(TAG, "SPB");
            String MeasurementGroupId = item.getMeasurementGroupId();
            for (VitalsMeasurementList vitalList : mData) {

                if (MeasurementGroupId.equals(vitalList.getMeasurementGroupId())) {
                    AppLog.showE(TAG, "got measurement date");
                    if (vitalList.getFactorId() == 5) {
                        extra_field = true;
                        extra_factorid = vitalList.getFactorId();
                        extra_factorValue = vitalList.getFactorValue();
                        extra_measurmentId = vitalList.getMeasurementId();

                        txtFactorLableExtra.setText(vitalList.getFactorDesc());
                        txtFactorValueextra.setText(vitalList.getFactorValue());
                        txtFactorUnitExtra.setText(vitalList.getUnitName());
                        Log.e("ExtraSet", " " + vitalList.getFactorDesc() + ":" + vitalList.getFactorValue() + ":" + vitalList.getUnitName());
                        break;
                    }
                }
            }

            setValidation(txtFactorValueextra, extra_factorid);
        }

        if (item.getFactorId() == 4) {
            AppLog.showE(TAG, "SPB");
            String MeasurementGroupId = item.getMeasurementGroupId();
            for (VitalsMeasurementList vitalList : mData) {

                if (MeasurementGroupId.equals(vitalList.getMeasurementGroupId())) {
                    AppLog.showE(TAG, "got measurement date");
                    if (vitalList.getFactorId() == 2) {
                        extra_field = true;
                        extra_factorid = vitalList.getFactorId();
                        extra_factorValue = vitalList.getFactorValue();
                        extra_measurmentId = vitalList.getMeasurementId();

                        txtFactorLableExtra.setText(vitalList.getFactorDesc());
                        txtFactorValueextra.setText(vitalList.getFactorValue());
                        txtFactorUnitExtra.setText(vitalList.getUnitName());
                        Log.e("ExtraSetBmi", " " + vitalList.getFactorDesc() + ":" + vitalList.getFactorValue() + ":" + vitalList.getUnitName());
                        break;
                    }
                }
            }

            setValidation(txtFactorValueextra, extra_factorid);
        }

        if (item.getFactorId() == 2) {
            AppLog.showE(TAG, "SPB");
            String MeasurementGroupId = item.getMeasurementGroupId();
            for (VitalsMeasurementList vitalList : mData) {

                if (MeasurementGroupId.equals(vitalList.getMeasurementGroupId())) {
                    AppLog.showE(TAG, "got measurement date");
                    if (vitalList.getFactorId() == 4) {
                        extra_field = true;
                        extra_factorid = vitalList.getFactorId();
                        extra_factorValue = vitalList.getFactorValue();
                        extra_measurmentId = vitalList.getMeasurementId();

                        txtFactorLableExtra.setText(vitalList.getFactorDesc());
                        txtFactorValueextra.setText(vitalList.getFactorValue());
                        txtFactorUnitExtra.setText(vitalList.getUnitName());
                        Log.e("ExtraSetBmi", " " + vitalList.getFactorDesc() + ":" + vitalList.getFactorValue() + ":" + vitalList.getUnitName());
                        break;
                    }
                }
            }

            setValidation(txtFactorValueextra, extra_factorid);
        }


        if (item.getFactorId() == 16) {
            txtFactorValue.setText("" + item.getFactorValue());
        } else {
            BigDecimal number = new BigDecimal(item.getFactorValue());
            Log.e("FactorValue***", " " + number.stripTrailingZeros().toPlainString());
            // System.out.println(number.stripTrailingZeros().toPlainString());
            txtFactorValue.setText(number.stripTrailingZeros().toPlainString());
        }

        setValidation(txtFactorValue, item.getFactorId());

        txtFactorUnit = (TextView) dialogView.findViewById(R.id.txt_unit);
        txtFactorUnit.setText("" + item.getUnitName());

        if (extra_factorid != 4 || extra_factorid != 2) {
            txtFactorValue.setEnabled(true);
        }


        if (extra_factorid == 4) {
            txtFactorValue.setEnabled(false);

            txtFactorValueextra.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    if (extra_field) {
                        if (item.getFactorId() == 2 || item.getFactorId() == 4)
                        {
                            if (s.length() > 0)
                            {
                                if (!s.toString().isEmpty() && containsDigit(s.toString()) && !moretanoneDot(s.toString())) {
                                    Log.e("Hieght", WebserviceConstants.Height_for_BMI);
                                    if (txtFactorUnit.getText().toString().equalsIgnoreCase("kg"))
                                    {
                                        double calweight = Float.parseFloat(s.toString());
                                        double calheight = 0;
                                        if (hieghtUnit.equals("cm")) {
                                            calheight = (Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) / 100);
                                            calheight = (Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) * 0.0254) * 0.393;
                                        } else {
                                            calheight = Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) / 39.370;
//                                  calheight = Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) * 0.0254;
                                        }
                                        if (calheight != 0 && calweight != 0) {
                                            double BMI = (calweight / (calheight * calheight));
                                            txtFactorValue.setText(String.format("%.1f", BMI));
                                        }

                                    } else {
                                        double calweight = Float.parseFloat(s.toString()) / 2.2046;
                                        double calheight = 0;
                                        if (hieghtUnit.equals("cm")) {
                                            calheight = (Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) / 100);
                                            //calheight = (Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) * 0.0254) * 0.393;
                                        } else {
                                            //calheight = Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI))/39.370;
                                            calheight = Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) * 0.0254;
                                        }
                                        if (calheight != 0 && calweight != 0) {
                                            double BMI = (calweight / (calheight * calheight));
                                            txtFactorValue.setText(String.format("%.1f", BMI));
                                        }

                                    }
                                    // extraItem.setFactorValue(txtBMI.getText().toString());


                                } else {
                                    txtFactorValue.setText("");
                                    // extraItem.setFactorValue("");

                                }


                            } else if (s.length() == 0) {
                                txtFactorValue.setText("");
                                //  extraItem.setFactorValue("");
                            }
                        }
                    }

                }
            });

        }

        if (extra_factorid == 2) {
            txtFactorValueextra.setEnabled(false);

            txtFactorValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    if (extra_field) {
                        if (item.getFactorId() == 2 || item.getFactorId() == 4) {
                            if (s.length() > 0) {
                                if (!s.toString().isEmpty() && containsDigit(s.toString()) && !moretanoneDot(s.toString())) {
                                    Log.e("Hieght", WebserviceConstants.Height_for_BMI);
                                    if (txtFactorUnitExtra.getText().toString().equalsIgnoreCase("kg")) {
                                        double calweight = Float.parseFloat(s.toString());
                                        double calheight = 0;
                                        if (hieghtUnit.equals("cm")) {
                                            calheight = (Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) / 100);
                                            calheight = (Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) * 0.0254) * 0.393;
                                        } else {
                                            calheight = Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) / 39.370;
//                                  calheight = Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) * 0.0254;
                                        }
                                        if (calheight != 0 && calweight != 0) {
                                            double BMI = (calweight / (calheight * calheight));
                                            txtFactorValueextra.setText(String.format("%.1f", BMI));
                                        }

                                    } else {
                                        double calweight = Float.parseFloat(s.toString()) / 2.2046;
                                        double calheight = 0;
                                        if (hieghtUnit.equals("cm")) {
                                            calheight = (Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) / 100);
                                            //calheight = (Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) * 0.0254) * 0.393;
                                        } else {
                                            //calheight = Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI))/39.370;
                                            calheight = Float.parseFloat(String.valueOf(WebserviceConstants.Height_for_BMI)) * 0.0254;
                                        }
                                        if (calheight != 0 && calweight != 0) {
                                            double BMI = (calweight / (calheight * calheight));
                                            txtFactorValueextra.setText(String.format("%.1f", BMI));
                                        }

                                    }
                                    // extraItem.setFactorValue(txtBMI.getText().toString());


                                } else {
                                    txtFactorValueextra.setText("");
                                    // extraItem.setFactorValue("");

                                }


                            } else if (s.length() == 0) {
                                txtFactorValueextra.setText("");
                                //  extraItem.setFactorValue("");
                            }
                        }
                    }

                }
            });

        }


        final TextView txtFactorDate = (TextView) dialogView.findViewById(R.id.txt_date);
        txtFactorDate.setText(dateShow);

        final TextView txtFactorTime = (TextView) dialogView.findViewById(R.id.txt_time);
        txtFactorTime.setText(timeShow);
        sdf.applyPattern(inputPattern);

        txtFactorDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AppLog.showD(TAG, "onDateClick");
                DatePickerDialog dateDailog = new DatePickerDialog(VitalListActivity.this, R.style.datepicker_dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        view.clearFocus();

                        ((TextView) v).setText(monthOfYear + 1 + "/" + dayOfMonth + "/" + year);

                    }
                }, temp.getYear(), temp.getMonthOfYear() - 1, temp.getDayOfMonth());
                dateDailog.getDatePicker().setMaxDate(System.currentTimeMillis());
                dateDailog.show();
            }
        });

        txtFactorTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                TimePickerDialog timePickerDialog = new TimePickerDialog(VitalListActivity.this, R.style.datepicker_dialog, new TimePickerDialog.OnTimeSetListener() {
                    @Override

                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        view.clearFocus();
                        Calendar mCaleder = Calendar.getInstance();
                        hourOfDay = view.getCurrentHour();
                        minute = view.getCurrentMinute();
                        hours[0] = hourOfDay;
                        mins[0] = minute;
                        mCaleder.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        mCaleder.set(Calendar.MINUTE, minute);
                        int ampmInt = mCaleder.get(Calendar.AM_PM);
                        String ampm = (ampmInt == 0) ? "AM" : "PM";
                        String strHrsToShow = (mCaleder.get(Calendar.HOUR) == 0) ? "12" : Integer.toString(mCaleder.get(Calendar.HOUR));


                        if (view.isShown()) {
                            String S = "";
                            if (minute >= 0 && minute <= 9) {
                                S = "0" + minute;
                            } else {
                                S = minute + "";
                            }
                            ((TextView) v).setText(strHrsToShow + ":" + S + " " + ampm);
                        }

                    }
                }, hours[0], mins[0], false);

                timePickerDialog.show();
            }
        });

        CustomButton btnClose = (CustomButton) dialogView.findViewById(R.id.btn_cancel);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppLog.showD(TAG, "close");
                dialog.dismiss();
            }
        });


        Button btnUpdate = (Button) dialogView.findViewById(R.id.btn_update);
        final VitalsMeasurementList finalItem = item;
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                callUpdateAPI(finalItem,
                        txtFactorValue.getText().toString(),
                        txtFactorDate.getText().toString(),
                        txtFactorTime.getText().toString(), dialog);

            }
        });

        btnUpdate.setImeOptions(EditorInfo.IME_ACTION_DONE);

        dialog.show();

    }


    public void setValidation(EditText edtValue, int factorid) {
        AppLog.showE(TAG, "setValidation");



        /*if(validation!=null)
        {
            if (validation.getDataType().size() > 0)
            {
                DataType dataType = validation.getDataType().get(0);
                if (dataType.getDataType().equals("Integer")) {
                    edtValue.setRawInputType(Configuration.KEYBOARD_12KEY);
                } else if (dataType.getDataType().equals("Decimal")) {
                    edtValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                }
                //setting length

                edtValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(validation.getDataType().get(0).getValueLength())});
            }
        }*/


        for (int v = 0; v < WebserviceConstants.allvalidation.size(); v++) {
            boolean check = false;
            if (factorid == WebserviceConstants.allvalidation.get(v).getFactorId()) {

                for (int r = 0; r < WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().size(); r++) {


                    String dataType = WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getDataType();
                    Log.e("SetDataType", "  " + factorid + ":" + dataType + ":" + WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength() + ":" + WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getDecimalPlaceLength());
                    if (dataType.equals("Integer")) {
                        Log.e("SetDataType-iNTEGER", "  " + factorid + ":" + dataType);
                        edtValue.setRawInputType(Configuration.KEYBOARD_12KEY);
                        edtValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength())});

                        //edtValue.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength(),WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getDecimalPlaceLength())});

                    } else if (dataType.equals("Decimal")) {
                        Log.e("SetDataType-dECIMAL", "  " + factorid + ":" + dataType);
                        edtValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        //edtValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength())});

                        edtValue.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength(), WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getDecimalPlaceLength())});


                    }
                    //setting length
                    check = true;


                }
            }

            Log.e("Check", " " + check);
            if (check) {
                break;
            }

            edtValue.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }


    }


    public final boolean containsDigit(String s) {
        boolean containsDigit = false;

        if (s != null && !s.isEmpty()) {
            for (char c : s.toCharArray()) {
                if (containsDigit = Character.isDigit(c)) {
                    break;
                }
            }
        }

        return containsDigit;
    }

    public final boolean moretanoneDot(String s) {
        boolean containsDigit = false;
        if (s != null && !s.isEmpty()) {
            int count = s.length() - s.replace(".", "").length();
            if (count > 1) {
                containsDigit = true;
            }
        }

        return containsDigit;
    }

    public void checkForFactorCompare(VitalsMeasurementList item) {
        factorCompare = "";
        extraItem = null;
        if (item.getFactorId() == 5) {
            AppLog.showE(TAG, "SPB");
            String MeasurementGroupId = item.getMeasurementGroupId();

            for (VitalsMeasurementList vitalList : mData) {

                if (MeasurementGroupId.equals(vitalList.getMeasurementGroupId())) {
                    AppLog.showE(TAG, "got measurement date");
                    if (vitalList.getFactorId() == 6) {
                        extraItem = vitalList;
                        factorCompare = vitalList.getFactorValue();

                        for (GetPatientDataView.AssociatedVitals assoVital : moduleData.getAssociateVitals()) {

                            if (assoVital.getVitalID() == extraItem.getFactorId()) {
                                // extraItem.setVitalValidation(assoVital.getVitalValidation());
                            }
                        }

                    }
                }

            }
        }

        if (item.getFactorId() == 6) {
            AppLog.showE(TAG, "DBP");
            String MeasurementGroupId = item.getMeasurementGroupId();

            for (VitalsMeasurementList vitalList : mData) {

                if (MeasurementGroupId.equals(vitalList.getMeasurementGroupId())) {
                    AppLog.showE(TAG, "got measurement date");
                    if (vitalList.getFactorId() == 5) {
                        extraItem = vitalList;
                        factorCompare = vitalList.getFactorValue();
                        for (GetPatientDataView.AssociatedVitals assoVital : moduleData.getAssociateVitals()) {

                            if (assoVital.getVitalID() == extraItem.getFactorId()) {
                                // extraItem.setVitalValidation(assoVital.getVitalValidation());
                            }
                        }
                    }
                }
            }
        }

        if (item.getFactorId() == 4) {
            AppLog.showE(TAG, "weight");
            String MeasurementGroupId = item.getMeasurementGroupId();

            for (VitalsMeasurementList vitalList : mData) {

                if (MeasurementGroupId.equals(vitalList.getMeasurementGroupId())) {
                    AppLog.showE(TAG, "got measurement date");
                    if (vitalList.getFactorId() == 2) {
                        extraItem = vitalList;
                        factorCompare = vitalList.getFactorValue();

                        for (GetPatientDataView.AssociatedVitals assoVital : moduleData.getAssociateVitals()) {

                            if (assoVital.getVitalID() == extraItem.getFactorId()) {
                                //extraItem.setVitalValidation(assoVital.getVitalValidation());
                            }
                        }

                    }
                }

            }
        }

        if (item.getFactorId() == 2) {
            AppLog.showE(TAG, "DBP");
            String MeasurementGroupId = item.getMeasurementGroupId();

            for (VitalsMeasurementList vitalList : mData) {

                if (MeasurementGroupId.equals(vitalList.getMeasurementGroupId())) {
                    AppLog.showE(TAG, "got measurement date");
                    if (vitalList.getFactorId() == 4) {
                        extraItem = vitalList;
                        factorCompare = vitalList.getFactorValue();
                        for (GetPatientDataView.AssociatedVitals assoVital : moduleData.getAssociateVitals()) {

                            if (assoVital.getVitalID() == extraItem.getFactorId()) {
                                // extraItem.setVitalValidation(assoVital.getVitalValidation());
                            }
                        }
                    }
                }
            }
        }

    }


    public void callUpdateAPI(VitalsMeasurementList item, String measurementValue, String date, String time, Dialog updateDialog) {
        if (measurementValue != null) {

            if (measurementValue.trim().length() == 0) {
                showDialog("Please enter readings");
                return;
            }


            if (extra_field) {
                if (extra_factorValue.equalsIgnoreCase("")) {
                    showDialog("Please enter readings");
                    return;
                }
            }
            if (measurementValue.equals(".") || measurementValue.equals("-")) {
                AppLog.showD(TAG, "valid values");
                showDialog("Please enter valid reading");
                return;
            } else if (measurementValue.contains("-.")) {
                AppLog.showD(TAG, "valid values");
                showDialog("Please enter valid reading");
                return;
            } else if (measurementValue.contains("-.")) {
                AppLog.showD(TAG, "valid values");
                showDialog("Please enter valid reading");
                return;
            }


            //if factor values is there

            if (item.getFactorId() == 5 || item.getFactorId() == 6) {


                boolean factorStatus = AllValidation.factorToCompareValidationInList(item.getMeasurementGroupId(), item.getFactorId(), measurementValue, txtFactorValueextra.getText().toString(), mData, WebserviceConstants.allvalidation);
                if (factorStatus == false) {
                    // AppFunctions.showMessageDialog(getActivity(), item.getVitalValidation().getFactorToCompare().get(0).getCustomMessage());
                    showDialog(WebserviceConstants.allvalidation.get(Integer.parseInt(AllValidation.MPOSITION)).getValidationFactorToCompareList().get(0).getCustomMessage());
                    return;
                }
            }


            HashMap<String, Integer> returnMap = AllValidation.rangeValidation(item.getFactorId(), measurementValue, WebserviceConstants.allvalidation);
            AppLog.showD(TAG, "returnMap status=" + returnMap.get(AllValidation.STATUS) + ":" + returnMap.get(AllValidation.MPOSITION) + ":" + returnMap.get(AllValidation.POSITION));
            if (returnMap.get(AllValidation.STATUS) == 0) {
                AppLog.showD(TAG, "POSITION=" + returnMap.get(AllValidation.POSITION));
                if (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isIsWarning() &&
                        (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isShown() == false)) {
                    //   WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).setShown(true);
                    showDialogOKCancel("R", returnMap.get(AllValidation.MPOSITION), returnMap.get(AllValidation.POSITION), item.getVitalValidation().getRange().get(returnMap.get(AllValidation.POSITION)).getCustomMessage(),
                            item, measurementValue, date, time, updateDialog);
                    return;
                } else if (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isIsWarning() == false) {
                    showDialog(WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).getCustomMessage());
                    return;
                } else {
                    AppLog.showD(TAG, "warning removed range");
                }
            }

            HashMap<String, Integer> returnValueToComp = AllValidation.valueToCompareValidation(item.getFactorId(), measurementValue, WebserviceConstants.allvalidation);
            if (returnValueToComp.get(AllValidation.STATUS) == 0) {

                if (WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isIsWarning() &&
                        (WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isShown() == false)) {
                    // WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).setShown(true);
                    showDialogOKCancel("V", returnValueToComp.get(AllValidation.MPOSITION), returnValueToComp.get(AllValidation.POSITION), WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).getCustomMessage(),
                            item, measurementValue, date, time, updateDialog);
                    return;
                } else if (WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isIsWarning() == false) {
                    showDialog(WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).getCustomMessage());
                    return;
                } else {
                    AppLog.showD(TAG, "warning removed");
                }
            }


            callUpdateApi(item, item.getFactorId(), measurementValue, date, time, updateDialog);
            // updateDialog.dismiss();
            AppLog.showD(TAG, "first validation-done");
        }


    }


    public void callUpdateApi(VitalsMeasurementList item, int factorid, String measurementValue, String date, String time, Dialog updateDialog) {
        updateDialog.dismiss();
        CustomProgressbar.showProgressBar(this, false);
        VitalUpdateRequest request = new VitalUpdateRequest();
        request.setUserId(UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID));
        VitalsParam param = new VitalsParam();
        param.setMeasurementId(item.getMeasurementId());
        param.setValue(Double.parseDouble(measurementValue));
        param.setFactorid(String.valueOf(factorid));

        SimpleDateFormat formate = new SimpleDateFormat("MM/dd/yyyy h:mm a");

        Date dateTime = new Date();

        String currentValue = date + " " + time;
        String dates[] = currentValue.split(" ");
        String dates1[] = dates[0].split("/");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dates1[1]));
        cal.set(Calendar.YEAR, Integer.parseInt(dates1[2]));
        cal.set(Calendar.MONTH, Integer.parseInt(dates1[0]) - 1);
        String hours[] = dates[1].split(":");
        String hours1[] = hours[1].split(" ");
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hours[0]));
        int Hoursint;
        Hoursint = Integer.parseInt(hours[0]);
        if ((dates[2].contains("PM") || dates[2].contains("P.M.") || dates[2].contains("pm") || dates[2].contains("p.m.")) && Integer.parseInt(hours[0]) != 12) {
            Hoursint = Integer.parseInt(hours[0]) + 12;
        } else if ((dates[2].contains("AM") || dates[2].contains("A.M.") || dates[2].contains("am") || dates[2].contains("a.m.")) && hours[0].equalsIgnoreCase("12")) {
            Hoursint = 0;
        }


//                Hoursint++;
        cal.set(Calendar.HOUR_OF_DAY, Hoursint);
        if (Hoursint == 0)
            cal.set(Calendar.HOUR_OF_DAY, 00);
        cal.set(Calendar.MINUTE, Integer.parseInt(hours1[0]));
        cal.set(Calendar.SECOND, 00);
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        int minuteToAdd = (int) TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS);
        Log.e("GMT offset is %s hours", "" + TimeUnit.MINUTES.convert(mGMTOffset, TimeUnit.MILLISECONDS));
        Calendar calendarGMT = Calendar.getInstance();
        calendarGMT.setTime(cal.getTime());
//                calendarGMT.add(Calendar.MINUTE, (-minuteToAdd));
        String dateAsString = WebserviceConstants.dateFormatter.format(calendarGMT.getTime());
        //selDate = Medicine_Constants.webServiceDateFormat.format(calendarGMT.getTime());
        Log.e("selDate", dateAsString);
        param.setMeasurementTime(dateAsString);


        ArrayList<VitalsParam> arrayList = new ArrayList<>();
        arrayList.add(param);
        if (extra_field) {
            VitalsParam paramextra = new VitalsParam();
            paramextra.setMeasurementId(extra_measurmentId);
            paramextra.setValue(Double.parseDouble(txtFactorValueextra.getText().toString()));
            paramextra.setMeasurementTime(dateAsString);
            paramextra.setFactorid(String.valueOf(extra_factorid));
            arrayList.add(paramextra);
        }
        request.setVitalsParam(arrayList);


        RemoteMethods3.getVitalService().vitalUpdate(request, new Callback<VitalUpdateResponse>() {
            @Override
            public void success(VitalUpdateResponse response, Response response2) {
                CustomProgressbar.hideProgressBar();
                extra_field = false;

                try {

                    if (response.getLatestVitalValues().size() > 0) {

                        for (int i = 0; i < response.getLatestVitalValues().size(); i++) {
                            WebserviceConstants.appConfing.get(i).setFactorId(response.getLatestVitalValues().get(i).getFactorId());
                            WebserviceConstants.appConfing.get(i).setFactorName(response.getLatestVitalValues().get(i).getFactorName());
                            WebserviceConstants.appConfing.get(i).setFactorValue(response.getLatestVitalValues().get(i).getFactorValue());
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                /*for (GetPatientDataView.AssociatedVitals  item : moduleData.getAssociateVitals())
                {
                     *//*  // VitalValidation vitals = item.getVitalValidation();
                        for (ValidationRange range : vitals.getRange()) {
                            range.setShown(false);
                        }
                        for (ValueToCompare valueToCompare : vitals.getValueToCompare()) {
                            valueToCompare.setShown(false);
                        }
                        for (FactorToCompare factorToCompare : vitals.getFactorToCompare()) {
                            factorToCompare.setShown(false);
                        }*//*
                }
*/

                Log.e("Save***", " " + response.getStatus() + ":" + response.getMessage());


                AppFunctions.showMessageDialog(VitalListActivity.this, response.getMessage());


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

                Log.e("Error", " " + error);


                CustomProgressbar.hideProgressBar();
            }
        });


    }


    /****
     * for only ok button
     **/
    public void showDialog(String message) {

        Context mContext = VitalListActivity.this;
        final Dialog dialog1 = new Dialog(mContext);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View dialogView = layoutInflater.inflate(R.layout.fragment_dialog_rss, null);
        dialog1.setContentView(dialogView);
        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);
        ((CustomTextView) dialogView.findViewById(R.id.txtMessage)).setText(message);
        Button btnOK = (Button) dialogView.findViewById(R.id.btnok);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Yes
                dialog1.dismiss();
            }
        });

        dialog1.show();
    }


    public void showDialogOKCancel(final String type, final int mpos, final int pos, String message, final VitalsMeasurementList item, final String measurementValue,
                                   final String date, final String time, final Dialog updateDialog) {

        final Dialog dialog = new Dialog(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
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

                if (type.equalsIgnoreCase("R")) {
                    WebserviceConstants.allvalidation.get(mpos).getValidationRangeList().get(pos).setShown(true);
                } else if (type.equalsIgnoreCase("V")) {
                    WebserviceConstants.allvalidation.get(mpos).getValidationValueToCompareList().get(pos).setShown(true);
                }

                //Yes
                AppLog.showD(TAG, "ok clicked");
                // ((VitalsActivity) this.VitalLis).callApi();
                callUpdateAPI(item, measurementValue, date, time, updateDialog);

                if (type.equalsIgnoreCase("R")) {
                    WebserviceConstants.allvalidation.get(mpos).getValidationRangeList().get(pos).setShown(false);
                } else if (type.equalsIgnoreCase("V")) {
                    WebserviceConstants.allvalidation.get(mpos).getValidationValueToCompareList().get(pos).setShown(false);
                }

                dialog.dismiss();
            }
        });
        Button btnCabcel = (Button) dialogView.findViewById(R.id.dialog_cancel);
        btnCabcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Yes
                for (GetPatientDataView.AssociatedVitals item : moduleData.getAssociateVitals()) {
                    /*VitalValidation vitals = item.getVitalValidation();
                    for (ValidationRange range : vitals.getRange()) {
                        range.setShown(false);
                    }
                    for (ValueToCompare valueToCompare : vitals.getValueToCompare()) {
                        valueToCompare.setShown(false);
                    }
                    for (FactorToCompare factorToCompare : vitals.getFactorToCompare()) {
                        factorToCompare.setShown(false);
                    }*/
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }


}

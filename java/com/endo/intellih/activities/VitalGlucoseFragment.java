package com.endo.intellih.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.endo.intellih.AppConstants;
import com.endo.intellih.AppFunctions;
import com.endo.intellih.R;
import com.endo.intellih.adapter.VitalGlucoseAdapter;
import com.endo.intellih.common.CommonMethod;
import com.endo.intellih.customviews.CustomTextView;
import com.endo.intellih.decor.ItemOffsetDecoration;
import com.endo.intellih.listener.VitalSave;
import com.endo.intellih.models.VitalsInput;
import com.endo.intellih.reports.SM_Factor_Graph_Trend_List_Activity;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.validation.AllValidation;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.response.GetPatientDataView;

import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class VitalGlucoseFragment extends Fragment implements VitalSave {

    public static final String TAG = VitalGlucoseFragment.class.getSimpleName();
    private GetPatientDataView.AssociatedHFGroup moduleData;
    private Gson gson;
    private boolean factorTiming = false;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private VitalGlucoseAdapter mAdapter;
    private ArrayList<GetPatientDataView.AssociatedVitals> mData;
    private TextView txtTimeLable, txtTime;
    private MutableDateTime dateTime;
    private Button btnList, btnChart;
    private DateTimeFormatter localTimeFormate = DateTimeFormat.forPattern("hh:mm a");
    private DateTimeFormatter Serverdtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat ServerdtfDate = new SimpleDateFormat();
    int dialghour, dialogminute;
    boolean checkvalue = true;
    boolean checkone = false;
    boolean checktwo = false;
    boolean checkthree = false;
    ArrayList<Boolean> validation_position = new ArrayList<Boolean>();
    CommonMethod common;


    int v = 0;
    int r = 0;

    public static Fragment newInstance(Context context) {
        VitalGlucoseFragment f = new VitalGlucoseFragment();

        return f;
    }

    @Override
    public void onResume() {
        super.onResume();
        // resetValues();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        gson = new Gson();
        common = new CommonMethod(getActivity());
        ServerdtfDate.applyPattern("yyyy-MM-dd");
        Bundle bundle = getArguments();
        if (bundle != null) {
            moduleData = gson.fromJson(bundle.getString(AppConstants.INTENT_KEY.VITAL_FACTORS), GetPatientDataView.AssociatedHFGroup.class);
        }

        factorTiming = moduleData.isIsSeperateDate();
        AppLog.showD(TAG, "showTiming=" + factorTiming);
        AppLog.showD(TAG, moduleData.getTenantHFGroupName());
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_vital_glucose, null);
        txtTimeLable = (TextView) root.findViewById(R.id.txt_time_lable);
        btnList = (Button) root.findViewById(R.id.btn_list);
        btnChart = (Button) root.findViewById(R.id.btn_chart);
        txtTime = (TextView) root.findViewById(R.id.txt_time);
        if (factorTiming) {
            txtTimeLable.setVisibility(View.GONE);
            txtTime.setVisibility(View.GONE);
        } else {
            txtTimeLable.setVisibility(View.VISIBLE);
            txtTime.setVisibility(View.VISIBLE);
        }

        mRecyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new ItemOffsetDecoration(getActivity(), R.dimen.dashboad_item_spacing));
        mData = (ArrayList<GetPatientDataView.AssociatedVitals>) moduleData.getAssociateVitals();


        mAdapter = new VitalGlucoseAdapter(mData, factorTiming, this);


        mRecyclerView.setAdapter(mAdapter);
        dateTime = ((VitalsActivity) getActivity()).getDateTime();
        dialghour = dateTime.getHourOfDay();
        dialogminute = dateTime.getMinuteOfHour();
        txtTime.setText((localTimeFormate.print(dateTime).toUpperCase()));

        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gotoListScreen();
            }
        });
        btnChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gotoChart();
            }
        });

        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),R.style.datepicker_dialog, new TimePickerDialog.OnTimeSetListener() {
                    @Override

                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        view.clearFocus();
                        Calendar mCaleder = Calendar.getInstance();

                        hourOfDay = view.getCurrentHour();
                        minute = view.getCurrentMinute();
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
                            dateTime.setHourOfDay(hourOfDay);
                            dateTime.setMinuteOfHour(minute);
                            dialghour = dateTime.getHourOfDay();
                            dialogminute = dateTime.getMinuteOfHour();
                        }

                    }
                }, dialghour, dialogminute, false);

                timePickerDialog.show();
            }
        });


        return root;
    }


    public void gotoListScreen() {

        Intent intent = new Intent(getActivity(), VitalListActivity.class);
        intent.putExtra(AppConstants.INTENT_KEY.VITAL_MAIN, gson.toJson(moduleData));
        startActivity(intent);

    }

    public void gotoChart() {

        Intent intent = new Intent(getActivity(), SM_Factor_Graph_Trend_List_Activity.class);
        intent.putExtra(AppConstants.INTENT_KEY.VITAL_MAIN, gson.toJson(moduleData));
        WebserviceConstants.associateHFsListChart = moduleData;
        startActivity(intent);

    }

    @Override
    public ArrayList<VitalsInput> saveVital()
    {
        AppLog.showI(TAG, "saveVital= fragment");
        ArrayList<VitalsInput> returnArray = new ArrayList<>();


        ArrayList<GetPatientDataView.AssociatedVitals> adapterData = mAdapter.getData();
        AppLog.showD(TAG, "" + checkSbpDbp(adapterData));
        if (checkSbpDbp(adapterData)) {
            showDialog("Please enter SBP and DBP reading");
            return null;
        }


        int count = 0;
        for (GetPatientDataView.AssociatedVitals item : adapterData)
        {

            Log.e("vital values=" , " " + adapterData.size() + ":" + item.getVitalName() + ":" + item.getVitalID() + ":" + item.getVitalValue());

            if (item.getVitalValue() != null)
            {

                String values = item.getVitalValue();
                if (values.equals(".") || values.equals("-"))
                {
                    AppLog.showD(TAG, "valid values");
                    showDialog("Please enter valid reading");
                    return null;
                } else if (values.contains("-."))
                {
                    AppLog.showD(TAG, "valid values");
                    showDialog("Please enter valid reading");
                    return null;
                } else if (values.contains("-.")) {
                    AppLog.showD(TAG, "valid values");
                    showDialog("Please enter valid reading");
                    return null;
                }




                Log.e("SizeOfValidation", " " + WebserviceConstants.allvalidation.size());


                if (item.getVitalID() == 5 || item.getVitalID() == 6)
                {
                    boolean factorStatus = AllValidation.factorToCompareValidation(item.getVitalID(), item.getVitalValue(), adapterData, WebserviceConstants.allvalidation);
                    if (factorStatus == false) {
                        // AppFunctions.showMessageDialog(getActivity(), item.getVitalValidation().getFactorToCompare().get(0).getCustomMessage());
                        showDialog(WebserviceConstants.allvalidation.get(Integer.parseInt(AllValidation.MPOSITION)).getValidationFactorToCompareList().get(0).getCustomMessage());
                        return null;
                    }
                }

                HashMap<String, Integer> returnMap = AllValidation.rangeValidation(item.getVitalID(), item.getVitalValue(), WebserviceConstants.allvalidation);
                AppLog.showD(TAG, "returnMap status=" + returnMap.get(AllValidation.STATUS));
                if (returnMap.get(AllValidation.STATUS) == 0)
                {
                    // (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isShown() == false
                    Log.e("RangePosiiton", "POSITION=" + returnMap.get(AllValidation.POSITION));
                    Log.e("RangePosiiton", "mPOSITION=" + returnMap.get(AllValidation.MPOSITION));
                    if (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isIsWarning()
                            && (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isShown() == false)) {
                        //item.getVitalValidation().getRange().get(returnMap.get(AllValidation.POSITION)).setShown(true);
                        // WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).setShown(true);
                        showDialogOKCancel("R", returnMap.get(AllValidation.MPOSITION), returnMap.get(AllValidation.POSITION), WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).getCustomMessage());
                        return null;
                    } else if (WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).isIsWarning() == false) {
                        showDialog(WebserviceConstants.allvalidation.get(returnMap.get(AllValidation.MPOSITION)).getValidationRangeList().get(returnMap.get(AllValidation.POSITION)).getCustomMessage());
                        return null;
                    } else {
                        AppLog.showD(TAG, "warning removed range");
                    }
                }

                HashMap<String, Integer> returnValueToComp = AllValidation.valueToCompareValidation(item.getVitalID(),item.getVitalValue(), WebserviceConstants.allvalidation);

                Log.e("ValueCompare-*",  " " + item.getVitalID() + ":" + returnValueToComp.get(AllValidation.STATUS) + ":" + returnValueToComp.get(AllValidation.MPOSITION) + ":" + returnValueToComp.get(AllValidation.POSITION));

                if (returnValueToComp.get(AllValidation.STATUS) == 0)
                {
                    Log.e("ValueCompare-1",  " " + WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isIsWarning() +":" + WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isShown() + ":" + WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isIsWarning());
                    // (WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isShown() == false
                    if (WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isIsWarning() &&
                            (WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isShown() == false))
                    {
                        Log.e("CallDilaog", " " + "CallDialog@@@");
                        // WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).setShown(true);
                        showDialogOKCancel("V",returnValueToComp.get(AllValidation.MPOSITION),returnValueToComp.get(AllValidation.POSITION),WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).getCustomMessage());
                        Log.e("CallDilaog-1", " " + "CallDialog@@@");
                        return null;
                    }
                    else if (WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).isIsWarning() == false) {
                        showDialog(WebserviceConstants.allvalidation.get(returnValueToComp.get(AllValidation.MPOSITION)).getValidationValueToCompareList().get(returnValueToComp.get(AllValidation.POSITION)).getCustomMessage());
                        return null;
                    } else {
                        AppLog.showD(TAG, "warning removed");
                    }

                }




                VitalsInput obj = new VitalsInput();
                obj.setFactorid(item.getVitalID());
                if (factorTiming) {
                    Log.e("SaveVital-Time", " " + item.getMeasurementDateTime());
                    Date myDate = WebserviceConstants.Cal.getTime();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        myDate = format.parse(ServerdtfDate.format(WebserviceConstants.Cal.getTime()) + " " + item.getMeasurementDateTime());
                        //Log.e("Date2", myDate.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                    calendar.setTime(myDate);
                    Date time = calendar.getTime();
                    SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String dateAsString = outputFmt.format(time);
                    Log.e("Date", dateAsString);
                    dateAsString = dateAsString + ":00";
                    obj.setMeasurementDateTime(dateAsString);
                } else {

                    Log.e("SaveVital-Time-2", " " + ServerdtfDate.format(WebserviceConstants.Cal.getTime()) + " " + dialghour + ":" + dialogminute + ":00");

                    Date myDate = WebserviceConstants.Cal.getTime();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        //  Log.e("Date", Serverdtf.print(dateTime));
                        myDate = format.parse(ServerdtfDate.format(WebserviceConstants.Cal.getTime()) + " " + dialghour + ":" + dialogminute + ":00");
//                        myDate = format.parse(format.format(WebserviceConstants.Cal.getTime()));
                        // Log.e("Date2", myDate.toString());
                        Log.e("date1", dialghour + "-" + dialogminute);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                    calendar.setTime(myDate);
                    Date time = calendar.getTime();
                    SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String dateAsString = outputFmt.format(time);
                    Log.e("Date1", dateAsString);
                    dateAsString = dateAsString + ":00";
                    obj.setMeasurementDateTime(dateAsString);

                }
                obj.setVitalValue(item.getVitalValue());
                obj.setUtcOffsetMinutes(common.getUtcOffsetMinutes());
                returnArray.add(obj);
                count++;
            } else {
            }

            Log.e("SaveData", " " + item.getVitalID() + ":" + item.getVitalName());


        }


        if (count == 0) {
            showDialog("Please enter readings");
            return null;
        }

        AppLog.showI(TAG, "saveVital=" + returnArray.size());
        return returnArray;
    }


    public void onDoneButton(int position) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(
                        getActivity().INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                getActivity().getCurrentFocus().getWindowToken(), 0);
        AppLog.showE(TAG, "onDoneButton=" + position);
        GetPatientDataView.AssociatedVitals item = mAdapter.getData().get(position);
        String valueFrom = item.getVitalValue();
        if (valueFrom == null || valueFrom.trim().length() == 0) {
            return;
        }
        /*HashMap<String, Integer> returnMap = AllValidation.rangeValidation(valueFrom, item.getVitalValidation());
        AppLog.showD(TAG, "returnMap status=" + returnMap.get(AllValidation.STATUS));
        if (returnMap.get(AllValidation.STATUS) == 0) {
            AppLog.showD(TAG, "POSITION=" + returnMap.get(AllValidation.POSITION));
            if (item.getVitalValidation().getRange().get(returnMap.get(AllValidation.POSITION)).getIsWarning() &&
                    (item.getVitalValidation().getRange().get(returnMap.get(AllValidation.POSITION)).isShown() == false)) {
                item.getVitalValidation().getRange().get(returnMap.get(AllValidation.POSITION)).setShown(true);
                showDialogOKCancel(item.getVitalValidation().getRange().get(returnMap.get(AllValidation.POSITION)).getCustomMessage());

            } else if (item.getVitalValidation().getRange().get(returnMap.get(AllValidation.POSITION)).getIsWarning() == false) {
                showDialog(item.getVitalValidation().getRange().get(returnMap.get(AllValidation.POSITION)).getCustomMessage());
            } else {
                AppLog.showD(TAG, "warning removed range");
            }
        }

        HashMap<String, Integer> returnValueToComp = AllValidation.valueToCompareValidation(valueFrom, item.getVitalValidation());
        if (returnValueToComp.get(AllValidation.STATUS) == 0) {

            if (item.getVitalValidation().getValueToCompare().get(returnValueToComp.get(AllValidation.POSITION)).getIsWarning() &&
                    (item.getVitalValidation().getValueToCompare().get(returnValueToComp.get(AllValidation.POSITION)).isShown() == false)) {
                item.getVitalValidation().getValueToCompare().get(returnValueToComp.get(AllValidation.POSITION)).setShown(true);
                showDialogOKCancel(item.getVitalValidation().getValueToCompare().get(returnValueToComp.get(AllValidation.POSITION)).getCustomMessage());

            } else if (item.getVitalValidation().getValueToCompare().get(returnValueToComp.get(AllValidation.POSITION)).getIsWarning() == false) {
                showDialog(item.getVitalValidation().getValueToCompare().get(returnValueToComp.get(AllValidation.POSITION)).getCustomMessage());

            } else {
                AppLog.showD(TAG, "warning removed");
            }
        }*/

    }

    /****
     * for only ok button
     **/
    public void showDialog(String message) {

        final Dialog dialog = new Dialog(getActivity());
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
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
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void showDialogOKCancel(final String type, final int mpos, final int pos, String message) {

        final Dialog dialog = new Dialog(getActivity());
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View dialogView = layoutInflater.inflate(R.layout.dialog_error, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        ((CustomTextView) dialogView.findViewById(R.id.message)).setText(message);
        Button btnOK = (Button) dialogView.findViewById(R.id.dialog_ok);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Log.e("CallDilaog-3", " " + "CallDialog@@@");
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


                AppLog.showD(TAG, "ok clicked");
                ((VitalsActivity) getActivity()).callApi();

               /* if (type.equalsIgnoreCase("R")) {
                    WebserviceConstants.allvalidation.get(mpos).getValidationRangeList().get(pos).setShown(false);
                } else if (type.equalsIgnoreCase("V")) {
                    WebserviceConstants.allvalidation.get(mpos).getValidationValueToCompareList().get(pos).setShown(false);
                }*/

                Log.e("CallDilaog-4", " " + "CallDialog@@@");

            }
        });
        Button btnCabcel = (Button) dialogView.findViewById(R.id.dialog_cancel);
        btnCabcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Yes

               /* if(type.equalsIgnoreCase("R"))
                {
                    WebserviceConstants.allvalidation.get(mpos).getValidationRangeList().get(pos).setShown(false);
                }
                else if(type.equalsIgnoreCase("V"))
                {
                    WebserviceConstants.allvalidation.get(mpos).getValidationValueToCompareList().get(pos).setShown(false);
                }*/

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void resetValues(String message) {

        for (GetPatientDataView.AssociatedVitals item : mAdapter.getData()) {
            item.setVitalValue(null);
          /* VitalValidation vitals = item.getVitalValidation();
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
        mAdapter = new VitalGlucoseAdapter(mData, factorTiming, this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        AppFunctions.showMessageDialog(getActivity(), message);

    }

    public boolean checkSbpDbp(ArrayList<GetPatientDataView.AssociatedVitals> adapterData) {

        GetPatientDataView.AssociatedVitals sbp = null;
        GetPatientDataView.AssociatedVitals dbp = null;

        for (GetPatientDataView.AssociatedVitals vital : adapterData) {

            if (vital.getVitalID() == 6) {
                dbp = vital;
            }

            if (vital.getVitalID() == 5) {
                sbp = vital;
            }
        }


        if ((sbp != null) && (dbp != null)) {
            AppLog.showD(TAG, "dbp=" + dbp.getVitalValue());
            AppLog.showD(TAG, "sbp=" + sbp.getVitalValue());
            if ((dbp.getVitalValue() == null) && (sbp.getVitalValue() == null)) {
                return false;
            } else if ((dbp.getVitalValue() != null) && (sbp.getVitalValue() == null)) {
                return true;
            } else return (sbp.getVitalValue() != null) && (dbp.getVitalValue() == null);
        } else {

            return false;
        }


    }

    public boolean checkValueToCompateLessthanEqulTo(final int factorid, String value)
    {

        checkvalue = false;

        for (v = 0; v < WebserviceConstants.allvalidation.size(); v++)
        {

            Log.e("range-1", " " + "range-1" + ":" + factorid + ":" + WebserviceConstants.allvalidation.get(v).getFactorId());
            if (factorid == WebserviceConstants.allvalidation.get(v).getFactorId())
            {
                double valueDouble = Double.parseDouble(value);

                if(WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList() != null)
                {
                    for (r = 0; r < WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().size(); r++)
                    {
                        Log.e("ShowDialog_V_11", " " + valueDouble + ":" + WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValueToCompare() + ":" + WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValidationOperator());

                        Log.e("ShowDialog_V_111", " " + WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValidationOperator().equals("<") + ";" + WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValidationOperator().equals(">") + ":" + WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValidationOperator().equals("<="));

                        if (WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValidationOperator().equals("<"))
                        {

                            Log.e("ShowDialog_V_1", " " + valueDouble + ":" + WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValueToCompare());

                            if (valueDouble < WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValueToCompare())
                            {

                                if (WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).isIsWarning())
                                {
                                    Log.e("ShowDialog>", " " + "ShowDilaog");


                                    final Dialog dialog = new Dialog(getActivity());
                                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                                    View dialogView = layoutInflater.inflate(R.layout.dialog_error, null);
                                    dialog.setContentView(dialogView);
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    ((CustomTextView) dialogView.findViewById(R.id.message)).setText(WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getCustomMessage());
                                    Button btnOK = (Button) dialogView.findViewById(R.id.dialog_ok);

                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                                    btnOK.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //Yes
                                            Log.e("ShowDialog>-1", " " + "ShowDilaog");

                                            checkvalue = true;
                                            dialog.dismiss();
                                        }
                                    });
                                    Button btnCabcel = (Button) dialogView.findViewById(R.id.dialog_cancel);
                                    btnCabcel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //Yes
                                            Log.e("ShowDialog>-2", " " + "ShowDilaog");
                                            checkvalue = false;

                                            dialog.dismiss();
                                        }
                                    });

                                    dialog.show();
                                } else {
                                    final Dialog dialog = new Dialog(getActivity());
                                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                                    View dialogView = layoutInflater.inflate(R.layout.fragment_dialog_rss, null);
                                    dialog.setContentView(dialogView);
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    ((CustomTextView) dialogView.findViewById(R.id.txtMessage)).setText(WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getCustomMessage());
                                    Button btnOK = (Button) dialogView.findViewById(R.id.btnok);

                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                                    btnOK.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //Yes
                                            checkvalue = false;

                                            dialog.dismiss();
                                        }
                                    });

                                    dialog.show();
                                }

                                break;

                            }

                        }
                       /* else  if (WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValidationOperator().equals("<=") && checkone)
                        {
                                   *//* if (checkrange)
                                    {*//*

                            Log.e("ShowDialog_V_2", " " + valueDouble + ":" + WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValueToCompare());

                            if (valueDouble >= WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValueToCompare())
                            {
                                if (WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).isIsWarning()) {
                                    Log.e("ShowDialog<", " " + "ShowDilaog");


                                    final Dialog dialog = new Dialog(getActivity());
                                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                                    View dialogView = layoutInflater.inflate(R.layout.dialog_error, null);
                                    dialog.setContentView(dialogView);
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    ((CustomTextView) dialogView.findViewById(R.id.message)).setText(WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getCustomMessage());
                                    Button btnOK = (Button) dialogView.findViewById(R.id.dialog_ok);

                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                                    btnOK.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //Yes

                                            Log.e("ShowDialog<-1", " " + "ShowDilaog");

                                            checktwo = true;
                                            checkvalue = true;


                                            dialog.dismiss();
                                        }
                                    });
                                    Button btnCabcel = (Button) dialogView.findViewById(R.id.dialog_cancel);
                                    btnCabcel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //Yes

                                            Log.e("ShowDialog<-2", " " + "ShowDilaog");
                                            checkvalue = false;

                                            dialog.dismiss();
                                        }
                                    });

                                    dialog.show();
                                } else {
                                    final Dialog dialog = new Dialog(getActivity());
                                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                                    View dialogView = layoutInflater.inflate(R.layout.fragment_dialog_rss, null);
                                    dialog.setContentView(dialogView);
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    ((CustomTextView) dialogView.findViewById(R.id.txtMessage)).setText(WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getCustomMessage());
                                    Button btnOK = (Button) dialogView.findViewById(R.id.btnok);

                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                                    btnOK.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //Yes
                                            checkvalue = false;
                                            dialog.dismiss();
                                        }
                                    });

                                    dialog.show();
                                }



                            }
                            // }
                        }
                        else if (WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValidationOperator().equalsIgnoreCase(">") && checktwo) {
                                   *//* if (checkrange)
                                    {*//*

                            Log.e("ShowDialog_V_3", " " + valueDouble + ":" + WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValueToCompare());
                            if (valueDouble > WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getValueToCompare()) {

                                if (WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).isIsWarning()) {

                                    Log.e("ShowDialog<=", " " + "ShowDilaog");


                                    final Dialog dialog = new Dialog(getActivity());
                                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                                    View dialogView = layoutInflater.inflate(R.layout.dialog_error, null);
                                    dialog.setContentView(dialogView);
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    ((CustomTextView) dialogView.findViewById(R.id.message)).setText(WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getCustomMessage());
                                    Button btnOK = (Button) dialogView.findViewById(R.id.dialog_ok);

                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                                    btnOK.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //Yes

                                            checkthree = true;
                                            Log.e("ShowDialog<=1", " " + "ShowDilaog");
                                            checkvalue = true;


                                            dialog.dismiss();
                                        }
                                    });
                                    Button btnCabcel = (Button) dialogView.findViewById(R.id.dialog_cancel);
                                    btnCabcel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //Yes
                                            Log.e("ShowDialog<=2", " " + "ShowDilaog");
                                            checkvalue = false;

                                            dialog.dismiss();
                                        }
                                    });

                                    dialog.show();
                                } else {
                                    final Dialog dialog = new Dialog(getActivity());
                                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                                    View dialogView = layoutInflater.inflate(R.layout.fragment_dialog_rss, null);
                                    dialog.setContentView(dialogView);
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    ((CustomTextView) dialogView.findViewById(R.id.txtMessage)).setText(WebserviceConstants.allvalidation.get(v).getValidationValueToCompareList().get(r).getCustomMessage());
                                    Button btnOK = (Button) dialogView.findViewById(R.id.btnok);

                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                                    btnOK.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //Yes
                                            checkvalue = false;
                                            dialog.dismiss();
                                        }
                                    });

                                    dialog.show();
                                }


                            }
                            //}
                        }*/
                    }
                } else {
                    checkvalue = false;
                    Log.e("ShowDialog_Null", " " + "ShowDilaog");
                }
                break;
            }
        }//for end
        Log.e("CheckValue", " "+ checkvalue);
        return checkvalue;
    }

}
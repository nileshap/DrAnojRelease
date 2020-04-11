package com.endo.intellih.activities.questionnaire;


import android.app.DatePickerDialog;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.endo.intellih.AppFunctions;
import com.endo.intellih.R;
import com.endo.intellih.common.CommonMethod;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.customviews.CustomTextView;
import com.endo.intellih.graphview.series.DataPoint;
import com.endo.intellih.graphview.series.PointsGraphSeries;
import com.endo.intellih.reports.data.Charts_Constants;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.webservice.Constant;
import com.endo.intellih.webservice.MyCallback;
import com.endo.intellih.webservice.RemoteMethods3;
import com.endo.intellih.webservice.services.response.Questionniare.GetPatientQuestionsRes;
import com.endo.intellih.webservice.services.response.Questionniare.GetQuestionReponseReq;
import com.endo.intellih.webservice.services.response.Questionniare.GetQuestionRes;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TrendQuestionResponse extends AppCompatActivity {

    TextView txt_startDate;
    TextView txt_endDate;
    Calendar fromdate;
    Calendar todate;


    String dateTag = "";
    String startDateTimeUtc = "";
    String endDateTimeUtc = "";
    ImageView img_back;
    CommonMethod common;
    RecyclerView recyclerView;
    RecyclerView recyclerView_subjective;
    TextView txt_questionname;
    ArrayList<GetPatientQuestionsRes> arraylist_question = new ArrayList<GetPatientQuestionsRes>();
    ArrayList<GetQuestionRes> arraylist_getquestion = new ArrayList<GetQuestionRes>();
    ArrayList<String> array_date = new ArrayList<String>();
    ArrayList<String> array_questoindata = new ArrayList<String>();

    String questionId = "";
    DataAdapter adapter;
    DataAdapterSujective adapter_subjective;
    float[][] randomNumbersTab;
    LinearLayout layout_chart;
    LinearLayout layout_subjective_type;
    LinearLayout layout_legend_one;
    LinearLayout layout_legend_two;
    TextView txt_question_narration;
    int num_yaxis = 4;
    LineChartData linedata_line;
    //private ComboLineColumnChartView chart;
    PointsGraphSeries<DataPoint> Xyseries;
    private DatePickerDialog date_picker;
    private LineChartView chart_line;
    private int numberOfLines = 1;
    private int maxNumberOfLines = 1;
    private int numberOfPoints = 7;
    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasPoints = true;
    private boolean hasLines = true;
    private boolean isCubic = false;
    private boolean hasLabels = false;
    private int questionTypeCode = -9;
    private boolean hasLabelForSelected = true;
    private ValueShape shape = ValueShape.CIRCLE;
    private boolean isFilled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend_question_response);
        setUP();
    }

    public void setUP() {
        common = new CommonMethod(TrendQuestionResponse.this);
        img_back = (ImageView) findViewById(R.id.imageView_back_trend_question);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_questionname_trend_question);
        recyclerView_subjective = (RecyclerView) findViewById(R.id.recyclerView_sujective_type_trend_question);
        txt_questionname = (TextView) findViewById(R.id.txtView_question_name_trend_question);
        txt_startDate = (TextView) findViewById(R.id.textView_start_date_trend_question);
        txt_endDate = (TextView) findViewById(R.id.textView_enddate_trend_question);
        txt_question_narration = (TextView) findViewById(R.id.txtView_question_narration_trend_question);


        chart_line = (LineChartView) findViewById(R.id.line_chart_view_trend_question);
        layout_chart = (LinearLayout) findViewById(R.id.layout_chart_trend_question);
        layout_subjective_type = (LinearLayout) findViewById(R.id.layout_subjectivetype_trend_question);
        layout_legend_one = (LinearLayout) findViewById(R.id.layout_legend_one_trend_question);
        layout_legend_two = (LinearLayout) findViewById(R.id.layout_legend_two_trend_question);

        fromdate = Calendar.getInstance();

        todate = Calendar.getInstance();


        setCurrentDate();

        onClick();
        callWebserviceForQuestionnair();

    }

    public void onClick() {

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        txt_questionname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (layout_chart.getVisibility() == View.VISIBLE) {
                    layout_chart.setVisibility(View.GONE);
                } else {
                    layout_chart.setVisibility(View.VISIBLE);
                }
                Log.e("Click", " " + "Click");

                if (recyclerView.getVisibility() == View.VISIBLE) {

                    recyclerView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                }
                layout_subjective_type.setVisibility(View.GONE);
                txt_question_narration.setVisibility(View.GONE);

            }
        });

        txt_startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                dateTag = "sDate";
                callStartDateDilaog();

            }
        });

        txt_endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                dateTag = "eDate";
                callEndDateDilaog();

            }
        });

    }


    public void callWebserviceForQuestionnair() {
        // String patientUserId = "f14ee3ee-4e42-4407-91e6-97a844ac9c6a";
        String patientUserId = UserSharedPreferences.getInstance(TrendQuestionResponse.this).getString(UserSharedPreferences.KEY_USER_ID);
        CustomProgressbar.showProgressBar(TrendQuestionResponse.this, false);
        RemoteMethods3.getQuestionnaire().getAllQuestionForDropDown(new MyCallback<ArrayList<GetPatientQuestionsRes>>() {
            @Override
            public void success(ArrayList<GetPatientQuestionsRes> res, Response response) {

                // "Message":"Your account already activated."
                CustomProgressbar.hideProgressBar();
                Log.e("Response", " " + res + ":" + response);
                if (res != null) {
                    arraylist_question.clear();
                    arraylist_question = res;
                    Log.e("Response", " " + response + ":" + arraylist_question.size());
                    setAdapterDataForQuestion();
                } else {

                }
            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("receivedError :" + error.fillInStackTrace());
                AppFunctions.showMessageDialog(TrendQuestionResponse.this, getResources().getString(R.string.unabletoconnect));
                CustomProgressbar.hideProgressBar();
            }
        });
    }

    public void callWebserviceForGetQuestionResponse() {
        String patientUserId = UserSharedPreferences.getInstance(TrendQuestionResponse.this).getString(UserSharedPreferences.KEY_USER_ID);
        GetQuestionReponseReq request = new GetQuestionReponseReq();
        request.setPatientUserId(patientUserId);
        request.setQuestionId(questionId);
        request.setEndTimeUtc(endDateTimeUtc);
        request.setStartTimeUtc(startDateTimeUtc);
        Log.e("Request", " " + request.getQuestionId() + ":" + request.getEndTimeUtc() + ":" + request.getStartTimeUtc());
        CustomProgressbar.showProgressBar(TrendQuestionResponse.this, false);
        RemoteMethods3.getQuestionnaire().getQuestionResponse(request, new MyCallback<ArrayList<GetQuestionRes>>() {
            @Override
            public void success(ArrayList<GetQuestionRes> res, Response response) {

                try {
                    // "Message":"Your account already activated."
                    CustomProgressbar.hideProgressBar();
                    arraylist_getquestion.clear();
                    array_date.clear();
                    array_questoindata.clear();

                    Log.e("Response", " " + res + ":" + questionTypeCode);
                    if (res != null) {


                        arraylist_getquestion = res;

                        if (arraylist_getquestion.size() != 0) {
                            if (questionTypeCode == 1) {
                                layout_legend_one.setVisibility(View.VISIBLE);
                                layout_legend_two.setVisibility(View.GONE);
                                layout_subjective_type.setVisibility(View.GONE);
                                layout_chart.setVisibility(View.VISIBLE);
                                displayChart();
                            } else if (questionTypeCode == 2) {
                                layout_legend_one.setVisibility(View.GONE);
                                layout_legend_two.setVisibility(View.VISIBLE);
                                layout_subjective_type.setVisibility(View.GONE);
                                layout_chart.setVisibility(View.VISIBLE);
                                displayChart();
                            } else if (questionTypeCode == 3) {
                                setAdapterDataForSunjective();
                                layout_subjective_type.setVisibility(View.VISIBLE);
                                layout_chart.setVisibility(View.GONE);

                            }
                            String d1 = common.convertDate_common("MM/dd/yyyy", "MMM dd, yyyy", txt_startDate.getText().toString());
                            String d2 = common.convertDate_common("MM/dd/yyyy", "MMM dd, yyyy", txt_endDate.getText().toString());
                            txt_question_narration.setVisibility(View.VISIBLE);
                            txt_question_narration.setText("You responded to this question " + arraylist_getquestion.size() + " times between " + d1 + " and " + d2);
                        } else {
                            txt_question_narration.setVisibility(View.GONE);
                            layout_chart.setVisibility(View.GONE);
                            layout_subjective_type.setVisibility(View.GONE);
                            AppFunctions.showMessageDialog(TrendQuestionResponse.this, getResources().getString(R.string.no_records));

                        }
                    } else {
                        txt_question_narration.setVisibility(View.GONE);
                        layout_chart.setVisibility(View.GONE);
                        layout_subjective_type.setVisibility(View.GONE);
                        AppFunctions.showMessageDialog(TrendQuestionResponse.this, getResources().getString(R.string.no_records));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(RetrofitError error) {

                txt_question_narration.setVisibility(View.GONE);
                layout_chart.setVisibility(View.GONE);
                layout_subjective_type.setVisibility(View.GONE);
                System.out.println("receivedError :" + error.fillInStackTrace());
                AppFunctions.showMessageDialog(TrendQuestionResponse.this, getResources().getString(R.string.unabletoconnect));
                CustomProgressbar.hideProgressBar();
            }
        });
    }

    public void setAdapterDataForQuestion() {

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TrendQuestionResponse.this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DataAdapter();
        recyclerView.setAdapter(adapter);

    }

    public void setCurrentDate() {
        try {


            txt_endDate.setText(common.getCurrentTimeCommon("MM/dd/yyyy"));

            todate.setTime(new Date(common.getCurrentDate("MM/dd/yyyy")));
            todate.setTime(fromdate.getTime());
            todate.set(Calendar.HOUR_OF_DAY, 23);
            todate.set(Calendar.MINUTE, 59);
            todate.set(Calendar.SECOND, 59);

            fromdate.setTime(fromdate.getTime());
            fromdate.set(Calendar.HOUR_OF_DAY, 23);
            fromdate.set(Calendar.MINUTE, 59);
            fromdate.set(Calendar.SECOND, 59);
            fromdate.add(Calendar.MONTH, -6);

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            txt_startDate.setText(sdf.format(fromdate.getTime()));

            String ndate = common.getNextDate(txt_endDate.getText().toString().trim(), "MM/dd/yyyy") + " " + "12:00:00 AM";
            Log.e("NDate", " " + ndate + ":" + ndate);
            String ndate1 = common.convertDate_common("MM/dd/yyyy hh:mm:ss a", "yyyy-MM-dd'T'HH:mm:ss", ndate);
            Log.e("NDate_1", " " + ndate + ":" + ndate1);


            String sdate = txt_startDate.getText().toString().trim() + " " + "12:00:00 PM";
            Log.e("sDate", " " + sdate + ":" + sdate);
            String sdate1 = common.convertDate_common("MM/dd/yyyy hh:mm:ss a", "yyyy-MM-dd'T'HH:mm:ss", sdate);
            Log.e("sDate_1", " " + sdate + ":" + sdate1);

            startDateTimeUtc = common.getUtcTimeCommonFormat("yyyy-MM-dd'T'HH:mm:ss", sdate1);

            endDateTimeUtc = common.getUtcTimeCommonFormat("yyyy-MM-dd'T'HH:mm:ss", ndate1);


            Log.e("StartAndEndDateTime", " " + startDateTimeUtc + " : " + endDateTimeUtc);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void callStartDateDilaog() {


        date_picker = new DatePickerDialog(TrendQuestionResponse.this, R.style.datepicker_dialog, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int month, int day) {
                if (view.isShown()) {
                    String m = "";
                    if (month + 1 <= 9) {
                        m = "0" + String.valueOf(month + 1);
                    } else {
                        m = String.valueOf(month + 1);
                    }

                    String d = "";

                    if (day <= 9) {
                        d = "0" + String.valueOf(day);
                    } else {
                        d = String.valueOf(day);
                    }

                    Log.e("MondhDay", " " + d + ":" + m);

                    String temp = String.valueOf(m) + "/" + String.valueOf(d) + "/" + String.valueOf(year);

                    if (common.compareDate("MM/dd/yyyy", temp, txt_endDate.getText().toString()) > 0)
                    {
                        showDialog(getResources().getString(R.string.fromdateshouldbe), 1);
                    } else {

                        txt_startDate.setText(String.valueOf(m) + "/" + String.valueOf(d) + "/" + String.valueOf(year));
                        fromdate.set(year, month, day);

                      /*  String sdate = common.getPreviousDate(txt_startDate.getText().toString().trim(), "MM/dd/yyyy") + " " + "12:00:00 PM";
                        String sdate1 = common.convertDate_common("MM/dd/yyyy hh:mm:ss a", "yyyy-MM-dd'T'HH:mm:ss", sdate);
                        Log.e("NDate_1", " " + sdate + ":" + sdate1);
                        startDateTimeUtc = common.getUtcTimeCommonFormat("yyyy-MM-dd'T'HH:mm:ss", sdate1).split("T")[0]+"T"+"18:30:00";
*/

                        String sdate = txt_startDate.getText().toString().trim() + " " + "12:00:00 AM";
                        String sdate1 = common.convertDate_common("MM/dd/yyyy hh:mm:ss a", "yyyy-MM-dd'T'HH:mm:ss", sdate);
                        Log.e("SDate_1", " " + sdate + ":" + sdate1);
                        startDateTimeUtc = common.getUtcTimeCommonFormat("yyyy-MM-dd'T'HH:mm:ss", sdate1);


                        Log.e("S-dateUtc", " " + startDateTimeUtc);
                        callWebserviceForGetQuestionResponse();
                    }

                }
            }
        }, fromdate.get(Calendar.YEAR), fromdate.get(Calendar.MONTH), fromdate.get(Calendar.DAY_OF_MONTH));
        date_picker.show();
    }


    public void callEndDateDilaog() {


        date_picker = new DatePickerDialog(TrendQuestionResponse.this, R.style.datepicker_dialog, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int month, int day) {
                if (view.isShown()) {
                    String m = "";
                    if (month + 1 <= 9) {
                        m = "0" + String.valueOf(month + 1);
                    } else {
                        m = String.valueOf(month + 1);
                    }

                    String d = "";
                    if (day <= 9) {
                        d = "0" + String.valueOf(day);
                    } else {
                        d = String.valueOf(day);
                    }

                    String temp = String.valueOf(String.valueOf(m)) + "/" + String.valueOf(d) + "/" + String.valueOf(year);

                    if (common.compareDate("MM/dd/yyyy", txt_startDate.getText().toString(), temp) > 0)
                    {
                        showDialog(getResources().getString(R.string.todateshouldbe), 1);
                    }
                    else {

                        txt_endDate.setText(String.valueOf(String.valueOf(m)) + "/" + String.valueOf(d) + "/" + String.valueOf(year));
                        todate.set(year, month, day);


                        String ndate = common.getNextDate(txt_endDate.getText().toString().trim(), "MM/dd/yyyy") + " " + "12:00:00 AM";
                        String ndate1 = common.convertDate_common("MM/dd/yyyy hh:mm:ss a", "yyyy-MM-dd'T'HH:mm:ss", ndate);
                        Log.e("NDate_1", " " + ndate + ":" + ndate1);
                        endDateTimeUtc = common.getUtcTimeCommonFormat("yyyy-MM-dd'T'HH:mm:ss", ndate1);


                        Log.e("E-dateUtc", " " + endDateTimeUtc);
                        callWebserviceForGetQuestionResponse();
                    }
                }
            }
        }, todate.get(Calendar.YEAR), todate.get(Calendar.MONTH), todate.get(Calendar.DAY_OF_MONTH));
        date_picker.show();
    }

    public void displayChart() {
        try {

            Collections.sort(arraylist_getquestion, new Comparator<GetQuestionRes>() {
                @Override
                public int compare(GetQuestionRes entry1, GetQuestionRes entry2) {
                    // Sort by date
                    return entry1.getRespondedOnUtc().compareTo(entry2.getRespondedOnUtc());
                }
            });

            array_date.clear();
            array_questoindata.clear();
            Constant.array_questionvaue.clear();


            for (int i = 0; i < arraylist_getquestion.size(); i++) {

                if (arraylist_getquestion.get(i).getAnswerValue() != null) {
                    Log.e("DateUTC", " " + arraylist_getquestion.get(i).getAnswerValue() + ":" + arraylist_getquestion.get(i).getRespondedOnUtc() + " ::: " + common.convertInLocalTime(arraylist_getquestion.get(i).getRespondedOnUtc()));

                    array_date.add(common.convertDate_common("yyyy-MM-dd'T'HH:mm:ss", "MM/dd , hh:mm a", common.convertInLocalTime(arraylist_getquestion.get(i).getRespondedOnUtc())));
                    array_questoindata.add(String.valueOf(arraylist_getquestion.get(i).getAnswerValue()));
                }
            }

            Constant.array_questionvaue = array_questoindata;


            Log.e("ArrayDate_2", " " + array_date);

            chart_line.setVisibility(View.VISIBLE);
            if (questionTypeCode == 1) {


                layout_legend_one.setVisibility(View.VISIBLE);
                layout_legend_two.setVisibility(View.GONE);

                generateDataLinesNew();

            } else if (questionTypeCode == 2) {
                layout_legend_two.setVisibility(View.VISIBLE);
                layout_legend_one.setVisibility(View.GONE);
                generateDataLinesNewPointForYesNo();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private ColumnChartData generateColumnData() {

        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < array_questoindata.size(); ++i) {

            values = new ArrayList<SubcolumnValue>();
            values.add(new SubcolumnValue(4, ChartUtils.COLOR_GREEN));
            columns.add(new Column(values));
        }

        ColumnChartData columnChartData = new ColumnChartData(columns);
        return columnChartData;
    }

    private LineChartData generateLineData() {

        List<Line> lines = new ArrayList<Line>();
        for (int i = 0; i < numberOfLines; ++i) {

            List<PointValue> values = new ArrayList<PointValue>();

            for (int j = 0; j < array_questoindata.size(); ++j) {
                values.add(new PointValue(j, randomNumbersTab[i][j]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLORS[i]);
            line.setCubic(isCubic);
            line.setHasLabels(hasLabels);
            line.setHasLines(hasLines);
            line.setHasPoints(hasPoints);
            lines.add(line);
        }

        LineChartData lineChartData = new LineChartData(lines);

        return lineChartData;

    }

    public void setAdapterDataForSunjective() {
        recyclerView_subjective.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(TrendQuestionResponse.this);
        recyclerView_subjective.setLayoutManager(layoutManager);
        adapter_subjective = new DataAdapterSujective();
        recyclerView_subjective.setAdapter(adapter_subjective);
    }

    //data apdater for subjective type

    //New Column Chart
   /* private void generateDataNew() {
        Charts_Constants.str_class = Charts_Constants.STR_NEW_QUESTION;
        // Chart looks the best when line data and column data have similar maximum viewports.
        data = new ComboLineColumnChartData(generateColumnDataNew(), generateLineDataNew());

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(false);
//            Axis axisRightY = new Axis().setHasLines(false);
            if (hasAxesNames) {
                axisX.setName("Axis X");
                axisY.setName("Axis Y");
//                axisRightY.setName("Axis Y");
            }
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);

            List<AxisValue> axisValues = new ArrayList<AxisValue>();

            for (int i = 0; i < array_date.size(); i++) {

                axisValues.add(new AxisValue(i).setLabel(array_date.get(i)));
            }

            data.setAxisXBottom(new Axis(axisValues).setHasLines(false).setName("DateTime").setTextSize(13).setLineColor(ChartUtils.COLOR_GRAY).setTextColor(ChartUtils.COLOR_GRAY));
            List<AxisValue> axisValues1 = new ArrayList<AxisValue>();
            axisValues1.add(new AxisValue(1).setLabel("1"));
            axisValues1.add(new AxisValue(2).setLabel("2"));
            axisValues1.add(new AxisValue(3).setLabel("3"));
            axisValues1.add(new AxisValue(4).setLabel("4"));
            data.setAxisYLeft(new Axis(axisValues1).setHasLines(false).setMaxLabelChars(3).setName("Score").setTextSize(13).setLineColor(ChartUtils.COLOR_GRAY).setTextColor(ChartUtils.COLOR_GRAY));
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);

        }

        chart.setComboLineColumnChartData(data);
        chart.setViewportCalculationEnabled(false);
        chart.setValueSelectionEnabled(true);

        chart.setVisibility(View.VISIBLE);

        if (questionTypeCode == 1) {
            num_yaxis = 4;
        } else if (questionTypeCode == 2) {
            num_yaxis = 2;
        }
        Viewport v = new Viewport(-1, (int) num_yaxis, array_date.size(), 0);
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
        // Reset viewport height range to (0,100)
        Log.e("MaxValue_", " " + chart.getMaximumViewport());
        v = new Viewport(chart.getMaximumViewport());
        v.bottom = 0;
        v.top = (int) num_yaxis;
        v.left = Float.parseFloat("-0.5");
        v.right = array_date.size();
        chart.setMaximumViewport(v);
        v.right = 2;
        chart.setCurrentViewport(v);

        chart.setZoomType(ZoomType.HORIZONTAL);
        chart.setZoomEnabled(true);
    }*/

    private ColumnChartData generateColumnDataNew() {
        int numSubcolumns = array_questoindata.size();
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numSubcolumns; ++i) {

            values = new ArrayList<SubcolumnValue>();

            if (questionTypeCode == 1) {

                values.add(new SubcolumnValue(Float.parseFloat(array_questoindata.get(i)), ChartUtils.COLOR_GRAY));
                Column c = new Column(values);
                c.setHasLabels(false);
                columns.add(c);
            } else if (questionTypeCode == 2) {

               /* if(arraylist_getquestion.get(i).getAnswerValue() != null)
                {*/
                if (Float.parseFloat(array_questoindata.get(i)) == 1) {
                    values.add(new SubcolumnValue(1.5f, ChartUtils.COLOR_GREEN));
                } else if (Float.parseFloat(array_questoindata.get(i)) == 2) {
                    values.add(new SubcolumnValue(1.5f, ChartUtils.COLOR_RED));
                }
                Column c = new Column(values);
                c.setHasLabels(false);
                columns.add(c);
                //}
            }
        }
        ColumnChartData columnChartData = new ColumnChartData(columns);
        return columnChartData;
    }

    private LineChartData generateLineDataNew() {

        List<Line> lines = new ArrayList<Line>();
        for (int i = 0; i < numberOfLines; ++i) {

            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < array_date.size(); ++j) {


            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_GRAY_DARK);
            line.setCubic(isCubic);
            line.setHasLabels(true);
            line.setHasLines(false);
            line.setHasPoints(hasPoints);
            lines.add(line);
        }

        LineChartData lineChartData = new LineChartData(lines);
        return lineChartData;

    }

    //new DataLine Subjective Chart
    private void generateDataLinesNew() {

        Charts_Constants.str_class = Charts_Constants.STR_BLOOD_PRESURE_SBP_DBP;
        chart_line.setOnValueTouchListener(new ValueTouchListener());
        hasLabelForSelected = true;
        Float max_line = 0f;
        List<Line> lines = new ArrayList<Line>();
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        List<PointValue> values = new ArrayList<PointValue>();
        List<PointValue> values1 = new ArrayList<PointValue>();

        Log.e("ArrayDate---", " " + array_date.size());

        for (int j = 0; j < array_date.size(); ++j) {
            Log.e("Objective_Chart_Value", " " + array_questoindata.get(j) + ":::" + array_date.get(j));

            if (max_line <= Float.parseFloat(array_questoindata.get(j).toString())) {
                max_line = Float.parseFloat(array_questoindata.get(j).toString());
            }
            values.add(new PointValue(j, Float.parseFloat(array_questoindata.get(j).toString())));
            axisValues.add(new AxisValue(j).setLabel(array_date.get(j)));
        }


        Line line = new Line(values);
        line.setColor(ChartUtils.COLOR_BLUE);
        line.setShape(shape);
        line.setCubic(true);
        line.setFilled(isFilled);
        line.setHasLabels(true);
        line.setHasLabelsOnlyForSelected(false);
        line.setHasLines(hasLines);
        line.setHasPoints(hasPoints);
        lines.add(line);
        linedata_line = new LineChartData(lines);
        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(false);
            Axis axisRightY = new Axis().setHasLines(false);
            if (hasAxesNames) {
                axisX.setName("Axis X");
                axisY.setName("Axis Y");
            }
            linedata_line.setAxisXBottom(axisX);
            linedata_line.setAxisYLeft(axisY);

            linedata_line.setAxisXBottom(new Axis(axisValues).setHasLines(true).setName("DateTime").setTextSize(13).setLineColor(ChartUtils.COLOR_GRAY).setTextColor(ChartUtils.COLOR_GRAY));

            List<AxisValue> axisValues1 = new ArrayList<AxisValue>();
            axisValues1.add(new AxisValue(1).setLabel("1"));
            axisValues1.add(new AxisValue(2).setLabel("2"));
            axisValues1.add(new AxisValue(3).setLabel("3"));
            axisValues1.add(new AxisValue(4).setLabel("4"));

            linedata_line.setAxisYLeft(new Axis(axisValues1).setHasLines(true).setMaxLabelChars(3).setName("Score").setTextSize(10).setLineColor(ChartUtils.COLOR_GRAY).setTextColor(ChartUtils.COLOR_GRAY));

        } else {
            linedata_line.setAxisXBottom(null);
            linedata_line.setAxisYLeft(null);

        }

        chart_line.setLineChartData(linedata_line);

        // For build-up animation you have to disable viewport recalculation.
        chart_line.setViewportCalculationEnabled(false);
        chart_line.setValueSelectionEnabled(hasLabelForSelected);
        chart_line.setValueSelectionEnabled(true);

        Log.e("MaxValue", " " + max_line);

        Viewport v = new Viewport(-1, (int) 4 + 1, array_date.size(), 0);
        chart_line.setMaximumViewport(v);
        chart_line.setCurrentViewport(v);
        // Reset viewport height range to (0,100)
        Log.e("MaxValue_", " " + chart_line.getMaximumViewport());

        v = new Viewport(chart_line.getMaximumViewport());
        v.bottom = 0;
        v.top = 4 + 1;
        v.left = Float.parseFloat("-0.5");
        v.right = array_date.size();
        chart_line.setMaximumViewport(v);
        v.right = 2;
        chart_line.setCurrentViewport(v);
        chart_line.setZoomType(ZoomType.HORIZONTAL);
        chart_line.setZoomEnabled(true);
        //resetViewport();
    }

    private void generateDataLinesNewPointForYesNo() {

        Charts_Constants.str_class = Charts_Constants.STR_NEW_QUESTION;
        chart_line.setOnValueTouchListener(new ValueTouchListener());
        hasLabelForSelected = true;
        Float max_line = 0f;
        List<Line> lines = new ArrayList<Line>();
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        List<PointValue> values = new ArrayList<PointValue>();
        List<PointValue> values1 = new ArrayList<PointValue>();

        Log.e("ArrayDate---", " " + array_date.size());


        for (int j = 0; j < array_date.size(); ++j) {
            Log.e("Objective_Chart_Value", " " + array_questoindata.get(j) + ":::" + array_date.get(j));
            if (max_line <= Float.parseFloat(array_questoindata.get(j).toString())) {
                max_line = Float.parseFloat(array_questoindata.get(j).toString());
            }

            float v = Float.parseFloat(array_questoindata.get(j).toString());
            values.add(new PointValue(j, 2));
            axisValues.add(new AxisValue(j).setLabel(array_date.get(j)));
        }


        Line line = new Line(values);
        line.setColor(ChartUtils.COLOR_BLUE);
        line.setShape(shape);
        line.setCubic(true);
        line.setFilled(false);
        line.setHasLabels(false);
        line.setHasLabelsOnlyForSelected(false);
        line.setHasLines(false);
        line.setHasPoints(hasPoints);
        lines.add(line);


        linedata_line = new LineChartData(lines);
        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(false);
            Axis axisRightY = new Axis().setHasLines(false);
            if (hasAxesNames) {
                axisX.setName("Axis X");
                axisY.setName("Axis Y");
                axisRightY.setName("No");
            }
            linedata_line.setAxisXBottom(axisX);
            linedata_line.setAxisYLeft(axisY);
            linedata_line.setAxisYRight(axisRightY);

            linedata_line.setAxisXBottom(new Axis(axisValues).setHasLines(true).setName("DateTime").setTextSize(13).setLineColor(ChartUtils.COLOR_GRAY).setTextColor(ChartUtils.COLOR_GRAY));
            List<AxisValue> axisValues1 = new ArrayList<AxisValue>();
            axisValues1.add(new AxisValue(2).setLabel(""));
            axisValues1.add(new AxisValue(2).setLabel(""));
            linedata_line.setAxisYLeft(new Axis(axisValues1).setHasLines(false).setMaxLabelChars(-1).setName("Yes").setTextSize(13).setLineColor(ChartUtils.COLOR_GRAY).setTextColor(ChartUtils.COLOR_GRAY));
            linedata_line.setAxisYRight(new Axis(axisValues1).setHasLines(false).setMaxLabelChars(-1).setName("No").setTextSize(13).setLineColor(ChartUtils.COLOR_GRAY).setTextColor(ChartUtils.COLOR_GRAY));
        } else {
            linedata_line.setAxisXBottom(null);
            linedata_line.setAxisYLeft(null);
            linedata_line.setAxisYRight(null);

        }

        chart_line.setLineChartData(linedata_line);

        // For build-up animation you have to disable viewport recalculation.
        chart_line.setViewportCalculationEnabled(false);
        chart_line.setValueSelectionEnabled(hasLabelForSelected);
        chart_line.setValueSelectionEnabled(true);

        Log.e("MaxValue", " " + max_line);

        Viewport v = new Viewport(-1, (int) 2 + 1, array_date.size(), 0);
        chart_line.setMaximumViewport(v);
        chart_line.setCurrentViewport(v);
        // Reset viewport height range to (0,100)
        Log.e("MaxValue_", " " + chart_line.getMaximumViewport());

        v = new Viewport(chart_line.getMaximumViewport());
        v.bottom = 0;
        v.top = 2 + 1;
        v.left = Float.parseFloat("-0.5");
        v.right = array_date.size();
        chart_line.setMaximumViewport(v);
        v.right = 2;
        chart_line.setCurrentViewport(v);
        chart_line.setZoomType(ZoomType.HORIZONTAL);
        chart_line.setZoomEnabled(true);
        //resetViewport();
    }

    public void showDialog(String message, final int no) {
        final Dialog dialog = new Dialog(TrendQuestionResponse.this);
        LayoutInflater layoutInflater = LayoutInflater.from(TrendQuestionResponse.this);
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
                if (dialog != null) {
                    dialog.dismiss();
                }


            }
        });

        dialog.show();
    }

//new data line chart end

    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
//            Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onValueDeselected() {
            // TODO Auto-generated method stub

        }

    }

    //Data Adapter class
    public class DataAdapter extends RecyclerView.Adapter<DataAdapter.MyViewHolder> {

        boolean check1 = false;
        boolean check2 = false;
        boolean check3 = false;
        private ArrayList<String> data;

        @Override
        public DataAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_questionnaire_drop_down_row, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final DataAdapter.MyViewHolder holder, final int position) {
            //Movie movie = moviesList.get(position);
            Log.e("Qeustion", " " + arraylist_question.get(position).getText());
            holder.textView_question.setText(arraylist_question.get(position).getText());

            holder.textView_question.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerView.setVisibility(View.GONE);
                    questionTypeCode = arraylist_question.get(position).getResponseTypeCode();
                    recyclerView.setVisibility(View.GONE);
                    questionId = arraylist_question.get(position).getId();
                    txt_questionname.setText(arraylist_question.get(position).getText());
                    callWebserviceForGetQuestionResponse();

                }
            });

        }

        @Override
        public int getItemCount() {
            return arraylist_question.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView textView_question;
            private FrameLayout layout;

            public MyViewHolder(View view) {
                super(view);
                textView_question = (TextView) view.findViewById(R.id.txtDesc_Add_Search);

            }
        }
    }

    //Data Adapter class
    public class DataAdapterSujective extends RecyclerView.Adapter<DataAdapterSujective.MyViewHolder> {

        boolean check1 = false;
        boolean check2 = false;
        boolean check3 = false;
        private ArrayList<String> data;

        @Override
        public DataAdapterSujective.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.new_question_chart_subjective_row, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final DataAdapterSujective.MyViewHolder holder, final int position) {
            //Movie movie = moviesList.get(position);

            try {
                Log.e("Qeustion", " " + arraylist_getquestion.get(position).getAnswerText().toString().trim());
                holder.textView_que_details.setText(arraylist_getquestion.get(position).getAnswerText());
                holder.textView_date.setText(common.convertDate_common("yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy, hh:mm a", common.convertInLocalTime(arraylist_getquestion.get(position).getRespondedOnUtc())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return arraylist_getquestion.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView textView_que_details;
            private TextView textView_date;


            public MyViewHolder(View view) {
                super(view);
                textView_que_details = (TextView) view.findViewById(R.id.textView_details_new_que_chart_sujective_row);
                textView_date = (TextView) view.findViewById(R.id.textView_date_new_que_chart_sujective_row);

            }
        }
    }

}

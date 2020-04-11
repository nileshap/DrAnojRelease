package com.endo.intellih.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.endo.intellih.activities.questionnaire.TrendQuestionResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.endo.intellih.AppConstants;
import com.endo.intellih.AppFunctions;
import com.endo.intellih.R;
import com.endo.intellih.common.BaseActivity;
import com.endo.intellih.reports.Report_Glucose_Impact_Activity;
import com.endo.intellih.reports.SM_Factor_Graph_Activity;
import com.endo.intellih.reports.SM_Factor_Graph_CoRelate_Activity;
import com.endo.intellih.reports.SM_Factor_Graph_Trend_Activity;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.response.GetPatientDataView;
import com.endo.intellih.workout.PDF_Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by appaspect on 12/2/15.
 */
public class Reports_Activity extends BaseActivity {
    private ListView listView;
    private ArrayList<GetPatientDataView.Reports> tenantReportList;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_report_list);
        gson = new Gson();
        Bundle extra = getIntent().getExtras();
        tenantReportList = gson.fromJson(extra.getString(AppConstants.INTENT_KEY.REPORT_ARRAY), new TypeToken<List<GetPatientDataView.Reports>>() {
        }.getType());
        //tenantReportList = (ArrayList<TenantReportList>) getIntent().getSerializableExtra("List");

        ImageButton imgBack = (ImageButton) findViewById(R.id.imgBtnBack_home);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listView = (ListView) findViewById(R.id.lstReports);
        String[] values = new String[tenantReportList.size()];
        int i = 0;
        for (GetPatientDataView.Reports item : tenantReportList) {
          /*  if (item.getReportName().equalsIgnoreCase("Weight Correlate Report")||item.getReportName().equalsIgnoreCase("6-MWT Correlate Report")){
                values[i]="Correlate Report";
            }
            else{
                values[i]=item.getReportName();
            }*/

            values[i] = item.getReportName();
            i++;
        }
//        values = new HashSet<String>(Arrays.asList(values)).toArray(new String[0]);
//        String[] values = new String[] { "Glucose Impact",
//                "Factors Report",
//                "Questionnaires Report",
//                "Custom Report"
//        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.custometext, values);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) listView.getItemAtPosition(position);
                Log.e("ItemValue",  itemValue);
                // Show Alert
                if (itemValue.equalsIgnoreCase("Glucose Impact") || itemValue.equalsIgnoreCase("Glucose Impact Report")) {
                    Intent intent = new Intent(Reports_Activity.this, Report_Glucose_Impact_Activity.class);
                    Bundle bndle = new Bundle();
                    bndle.putString("title", "Home");
                    intent.putExtras(bndle);
                    startActivity(intent);
                } else if (itemValue.equalsIgnoreCase("Factors Report"))
                {

                    Log.e("Hello-Factor", " " + " Factor");
                    Intent intent = new Intent(Reports_Activity.this, SM_Factor_Graph_Activity.class);
                    Bundle bndle = new Bundle();
                    bndle.putString("title", "Home");
                    intent.putExtras(bndle);
                    startActivity(intent);
                } else if (itemValue.equalsIgnoreCase("Questionnaires Report") || itemValue.equalsIgnoreCase("Questionnaire Reports")) {

                    if (WebserviceConstants.associateQuestionnaires.size() != 0) {
                        Intent intent = new Intent(Reports_Activity.this, TrendQuestionResponse.class);
                        Bundle bndle = new Bundle();
                        bndle.putString("title", "Home");
                        intent.putExtras(bndle);
                        startActivity(intent);
                    } else {
                        AppFunctions.showMessageDialog(Reports_Activity.this, getString(R.string.nodata_vitalList));
                    }
                } else if (itemValue.equalsIgnoreCase("Vital Trend Chart") || itemValue.equalsIgnoreCase("Vitals Trend Report")) {
                    Intent intent = new Intent(Reports_Activity.this, SM_Factor_Graph_Trend_Activity.class);
                    Bundle bndle = new Bundle();
                    bndle.putString("title", "Home");
                    intent.putExtras(bndle);
                    startActivity(intent);
                } else if (itemValue.equals("Correlations Report") || itemValue.equals("Correlates Report"))
                {
                    Log.e("ItemValue-1", "~" + itemValue + "~");
                    Intent intent = new Intent(Reports_Activity.this, SM_Factor_Graph_CoRelate_Activity.class);
                    Bundle bndle = new Bundle();
                    intent.putExtras(bndle);
                    startActivity(intent);
                } else if (itemValue.contains("Doctor") && itemValue.contains("Report")) {
                    Intent intent = new Intent(Reports_Activity.this, PDF_Activity.class);
                    Bundle bndle = new Bundle();
                    bndle.putString("title", "Home");
                    intent.putExtras(bndle);
                    startActivity(intent);
                } /*else {
                    Intent intent = new Intent(Reports_Activity.this, SM_Factor_Graph_CoRelate_6MWT_Activity.class);
                    Bundle bndle = new Bundle();
                    bndle.putString("title", "Home");
                    intent.putExtras(bndle);
                    startActivity(intent);
                }
*/
            }

        });
    }
}
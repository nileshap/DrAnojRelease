package com.endo.intellih.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.endo.intellih.AppConstants;
import com.endo.intellih.AppFunctions;
import com.endo.intellih.Fragments.dietnewfoodentry.ImageLoader;
import com.endo.intellih.R;
import com.endo.intellih.Settings.Setting_Home_Activity;
import com.endo.intellih.common.BaseActivity;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.customviews.CustomTextView;
import com.endo.intellih.models.SelectedAssociateCDM;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.webservice.RemoteMethods;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.request.QuickEntrySelectionParams;
import com.endo.intellih.webservice.services.response.GetPatientDataView;
import com.endo.intellih.webservice.services.response.UserResponse;

import java.io.InputStream;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by appaspect on 12/2/15.
 */
public class CDM_Selection_Activity_Setting extends BaseActivity {
    private ListView listView;
    UserResponse userDetails;
    public ArrayList<GetPatientDataView.AssociatedCDM> associateCDM;
    public ArrayList<SelectedAssociateCDM> SelectedassociateCDM;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userDetails = new Gson().fromJson(getIntent().getStringExtra(AppConstants.INTENT_KEY.USER_DETAILS), UserResponse.class);
        setContentView(R.layout.activity_cdm_selection);
        associateCDM=new ArrayList<GetPatientDataView.AssociatedCDM>();
        SelectedassociateCDM=new ArrayList<SelectedAssociateCDM>();
        ImageButton imgBack=(ImageButton)findViewById(R.id.imgBtnBack_home);
        final Gson gson = new Gson();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CDM_Selection_Activity_Setting.this, Setting_Home_Activity.class);
                intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, gson.toJson(userDetails));
                //intent.putExtra(AppConstants.INTENT_KEY.USER_IsFirstTimeUser, str_IsFirstTimeUser);
                startActivity(intent);
                finish();

            }
        });

        CustomTextView txtsave= (CustomTextView)findViewById(R.id.txtsave);

        txtsave.setText(getString(R.string.save));
        txtsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (GetPatientDataView.AssociatedCDM item : associateCDM){
                    Log.e("selected:",item.getCDMName());

                }
                QuickEntrySelectionParams params = new QuickEntrySelectionParams();
                params.userId = UserSharedPreferences.getInstance(CDM_Selection_Activity_Setting.this).getString(UserSharedPreferences.KEY_USER_ID);
                params.cdmParameter=SelectedassociateCDM;
                boolean isChecked=false;
                for (SelectedAssociateCDM item: params.cdmParameter){
                    if (item.getIsActive()){
                        isChecked=true;
                    }
                }
                if (isChecked) {
                    CustomProgressbar.showProgressBar(CDM_Selection_Activity_Setting.this, false);
                    RemoteMethods.getLoginService().doUpdateCDM(params, new Callback<GetPatientDataView>() {
                        @Override
                        public void success(final GetPatientDataView userResponse, retrofit.client.Response response) {
                            WebserviceConstants.associateCDM = userResponse.getAssociatedCDMs();
                            WebserviceConstants.associateHFs = userResponse.getAssociatedHFGroups();
                           // WebserviceConstants.associateQE = userResponse.quickEntries;
                            Intent intent = new Intent(CDM_Selection_Activity_Setting.this, Setting_Home_Activity.class);
                            intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, gson.toJson(userResponse));
                            startActivity(intent);
                            finish();
                            CustomProgressbar.hideProgressBar();

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            CustomProgressbar.hideProgressBar();
                            Toast toast = Toast.makeText(CDM_Selection_Activity_Setting.this, error.getMessage(), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }
                else{
                    AppFunctions.showMessageDialog(CDM_Selection_Activity_Setting.this, "Please select atleast one CDM");
                }



            }
        });

        for (GetPatientDataView.AssociatedCDM item : WebserviceConstants.associateCDM){
            associateCDM.add(item);
            SelectedAssociateCDM obj=new SelectedAssociateCDM();
            obj.setIsActive(item.isPatientActiveCDM());
            obj.setTenantCDMID(item.getCDMID());
            SelectedassociateCDM.add(obj);
        }

        listView = (ListView)findViewById(R.id.lstReports);

        CDMSelectionAdapter adapter = new CDMSelectionAdapter(this,WebserviceConstants.associateCDM);

        listView.setAdapter(adapter);

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                ToggleButton toggleButton=(ToggleButton)view.findViewById(R.id.cdmselected);
//                // ListView Clicked item index
//                int itemPosition     = position;
//
//                // ListView Clicked item value
//               if (toggleButton.isChecked()){
//                   toggleButton.setChecked(false);
//                   associateCDM.remove(WebserviceConstants.associateCDM.get(position));
//               }
//                else{
//                   toggleButton.setChecked(true);
//                   associateCDM.add(WebserviceConstants.associateCDM.get(position));
//               }
//            }
//        });
    }
    public class CDMSelectionAdapter extends BaseAdapter {
        int number_of_clicks = 0;
        boolean thread_started = false;
        final int DELAY_BETWEEN_CLICKS_IN_MILLISECONDS = 250;
        InputStream is = null;
        String result123 = null;
        String line = null;
        public ArrayList<GetPatientDataView.AssociatedCDM> result;
        Context context;
        int[] imageId;
        public ImageLoader imageLoader;
        private LayoutInflater inflater = null;
        public CDMSelectionAdapter(Context mainActivity, ArrayList<GetPatientDataView.AssociatedCDM> arraydata) {
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
            ToggleButton tgl;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            final SelectedAssociateCDM obj=SelectedassociateCDM.get(position);
            Holder holder = new Holder();
            View rowView;
            rowView = inflater.inflate(R.layout.cdm_selection_raw, null);
            holder.tv = (TextView) rowView.findViewById(R.id.textView1);
            holder.tgl = (ToggleButton) rowView.findViewById(R.id.cdmselected);
            holder.tgl.setTag(position);
            if (obj.getIsActive()){
                holder.tgl.setChecked(true);
            }
            else{
                holder.tgl.setChecked(false);
            }
            holder.tv.setText(result.get(position).getCDMName());
            rowView.setTag(position);
            holder.tgl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                    if (!toggleButton.isChecked()){
                        toggleButton.setChecked(false);
                        associateCDM.remove(WebserviceConstants.associateCDM.get(position));
                        obj.setIsActive(false);
                    }
                    else{
                        toggleButton.setChecked(true);
                        obj.setIsActive(true);
                        associateCDM.add(WebserviceConstants.associateCDM.get(position));
                    }
                }
            });
            return rowView;
        }

    }
}
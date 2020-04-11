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
import com.endo.intellih.Main_Health_Profile_Activity;
import com.endo.intellih.R;
import com.endo.intellih.common.BaseActivity;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.customviews.CustomTextView;
import com.endo.intellih.models.AssociateHFGroup;
import com.endo.intellih.models.AssociateQE;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.webservice.RemoteMethods;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.request.QuickEntrySelectionParams;
import com.endo.intellih.webservice.services.response.UserResponse;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by appaspect on 12/2/15.
 */
public class QuickEntry_Selection_Activity extends BaseActivity {
    private ListView listView;
    UserResponse userDetails;
    public ArrayList<AssociateHFGroup> associateHF;
    public ArrayList<AssociateHFGroup> SelectedassociateHF;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hf_selection);
        associateHF=new ArrayList<AssociateHFGroup>();
        SelectedassociateHF=new ArrayList<AssociateHFGroup>();
        ImageButton imgBack=(ImageButton)findViewById(R.id.imgBtnBack_home);
        userDetails = new Gson().fromJson(getIntent().getStringExtra(AppConstants.INTENT_KEY.USER_DETAILS), UserResponse.class);
        final Gson gson = new Gson();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WebserviceConstants.associateCDM.size()>1) {
                    Intent intent = new Intent(QuickEntry_Selection_Activity.this, CDM_Selection_Activity.class);
                    intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, gson.toJson(userDetails));
                    //intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, gson.toJson(userResponse));
                    //intent.putExtra(AppConstants.INTENT_KEY.USER_IsFirstTimeUser, str_IsFirstTimeUser);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(QuickEntry_Selection_Activity.this, Main_Health_Profile_Activity.class);
                    intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, gson.toJson(userDetails));
                    //intent.putExtra(AppConstants.INTENT_KEY.USER_IsFirstTimeUser, str_IsFirstTimeUser);
                    startActivity(intent);
                    finish();
                }
            }
        });

        CustomTextView txtsave= (CustomTextView)findViewById(R.id.txtsave);

        txtsave.setText(getString(R.string.next));
        txtsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                QuickEntrySelectionParams params=new QuickEntrySelectionParams();
                params.userId = UserSharedPreferences.getInstance(QuickEntry_Selection_Activity.this).getString(UserSharedPreferences.KEY_USER_ID);
                params.tenantHFGroupIDList=new ArrayList<String>();
               // WebserviceConstants.associateQE=new ArrayList<AssociateQE>();
                for (AssociateHFGroup item : SelectedassociateHF){
                    Log.e("HF Selected",item.getTenantHFGroupName());
                    params.tenantHFGroupIDList.add(item.getTenantHFGroupID()+"");

                }

                HashSet<String> hashSet = new HashSet<String>();
                hashSet.addAll(params.tenantHFGroupIDList);
                params.tenantHFGroupIDList.clear();
                params.tenantHFGroupIDList.addAll(hashSet);
                for (String item : params.tenantHFGroupIDList){

                    AssociateQE QE=new AssociateQE();
                    QE.setTenantHFGroupID(Integer.valueOf(item));
                    //WebserviceConstants.associateQE.add(QE);
                }
                if (params.tenantHFGroupIDList.size()>3) {
                    AppFunctions.showMessageDialog(QuickEntry_Selection_Activity.this,"Selection of group should not be > 3");
                }
                else {
                    if (params.tenantHFGroupIDList.size() > 0) {
                        CustomProgressbar.showProgressBar(QuickEntry_Selection_Activity.this, false);
                        RemoteMethods.getQuickEntry().doQuickSelection(params, new Callback<UserResponse>() {
                            @Override
                            public void success(final UserResponse userResponse, retrofit.client.Response response) {
                                Intent intent = new Intent(QuickEntry_Selection_Activity.this, DashboardActivity.class);
                                intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, gson.toJson(userDetails));
                                //intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, gson.toJson(userResponse));
                                //intent.putExtra(AppConstants.INTENT_KEY.USER_IsFirstTimeUser, str_IsFirstTimeUser);
                                startActivity(intent);
                                finish();
                                CustomProgressbar.hideProgressBar();

                            }

                            @Override
                            public void failure(RetrofitError error) {
                                CustomProgressbar.hideProgressBar();
                                Toast toast = Toast.makeText(QuickEntry_Selection_Activity.this, error.getMessage(), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                    } else {
                        AppFunctions.showMessageDialog(QuickEntry_Selection_Activity.this, "Please select atleast one vital group");
                    }
                }

            }
        });
       /* for (AssociateHFGroup item : WebserviceConstants.associateHFs){
                associateHF.add(item);
        }*/

        listView = (ListView)findViewById(R.id.lstReports);

        HFSelectionAdapter adapter = new HFSelectionAdapter(this,associateHF);

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
//                   SelectedassociateHF.remove(associateHF.get(position));
//               }
//                else{
//                   toggleButton.setChecked(true);
//                   SelectedassociateHF.add(associateHF.get(position));
//               }
//            }
//        });
    }

    public class HFSelectionAdapter extends BaseAdapter {
        int number_of_clicks = 0;
        boolean thread_started = false;
        final int DELAY_BETWEEN_CLICKS_IN_MILLISECONDS = 250;
        InputStream is = null;
        String result123 = null;
        String line = null;
        public ArrayList<AssociateHFGroup> result;
        Context context;
        int[] imageId;
        public ImageLoader imageLoader;

        private LayoutInflater inflater = null;

        public HFSelectionAdapter(Context mainActivity, ArrayList<AssociateHFGroup> arraydata) {
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
            Holder holder = new Holder();
            View rowView;
            rowView = inflater.inflate(R.layout.hf_selection_raw, null);
            holder.tv = (TextView) rowView.findViewById(R.id.textView1);
            holder.tgl = (ToggleButton) rowView.findViewById(R.id.cdmselected);
            holder.tgl.setTag(position);
            holder.tv.setText(result.get(position).getTenantHFGroupName());
            rowView.setTag(position);


            holder.tgl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                    if (!toggleButton.isChecked()){
                        SelectedassociateHF.remove(associateHF.get(position));
                    }
                    else{
                        SelectedassociateHF.add(associateHF.get(position));
                    }
                }
            });
            return rowView;
        }

    }

}
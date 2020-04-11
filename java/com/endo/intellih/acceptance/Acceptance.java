package com.endo.intellih.acceptance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.endo.intellih.AppConstants;
import com.endo.intellih.AppFunctions;
import com.endo.intellih.LoginActivity;
import com.endo.intellih.R;
import com.endo.intellih.activities.DashboardActivity;
import com.endo.intellih.application.AppController;
import com.endo.intellih.common.CommonMethod;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.familymember.DashboardActivity_FM;
import com.endo.intellih.organization.OrganizationCode;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.webservice.Constant;
import com.endo.intellih.webservice.RemoteMethods3;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.request.acceptance.AcceptanceRes;
import com.endo.intellih.webservice.services.request.acceptance.AcceptenceReq;
import com.endo.intellih.webservice.services.response.BaseResponse;
import com.endo.intellih.webservice.services.response.GetPatientDataView;
import com.google.gson.Gson;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Acceptance extends AppCompatActivity {


    TextView textview_one;
    TextView textview_two;
    Button btn_accept;
    ImageView img_close;
    CommonMethod common;
    String role;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceptance);
        setUP();
    }

    public void setUP() {
        common = new CommonMethod(Acceptance.this);
        btn_accept = (Button) findViewById(R.id.btn_accept_acceptance);
        img_close = (ImageView) findViewById(R.id.acceptance_close);

        if (getIntent().hasExtra(AppConstants.INTENT_KEY.Role)) {
            role = getIntent().getStringExtra(AppConstants.INTENT_KEY.Role);

        }
        onClick();
    }

    public void onClick() {
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(common.isConnected(Acceptance.this)) {
                    callWebServiceForAcceptance();
                }
                else
                {
                    AppFunctions.showMessageDialog(Acceptance.this, getResources().getString(R.string.alert_no_internet_connection));
                }

            }
        });


        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
                updateUsernamePasswordOnRememberMe();
                Intent intent = new Intent(AppController.getContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                AppController.getContext().startActivity(intent);

            }
        });

    }


    public void updateUsernamePasswordOnRememberMe() {

        UserSharedPreferences.getInstance(Acceptance.this).putString(UserSharedPreferences.KEY_USER_NAME, "");
        UserSharedPreferences.getInstance(Acceptance.this).putString(UserSharedPreferences.KEY_PASSWORD, "");

        UserSharedPreferences.getInstance(Acceptance.this).putBoolean(UserSharedPreferences.KEY_REMEMBERME, false);
    }

    public void callWebServiceForAcceptance() {
        CustomProgressbar.showProgressBar(Acceptance.this, false);

        AcceptenceReq request = new AcceptenceReq();
        request.setType(UserSharedPreferences.getInstance(this).getString(UserSharedPreferences.KEY_USER_ID));
        request.setMandatoryClausesAccepted(true);


        RemoteMethods3.getHomeService().saveUserAcceptance(request, new Callback<AcceptanceRes>() {
                    @Override
                    public void success(final AcceptanceRes res, Response response) {

                        CustomProgressbar.hideProgressBar();

                        //{"MandatoryClausesAccepted":true,"MandatoryClausesAcceptedOn":"2020-03-25T12:26:07","UserId":"156f7bbe-997b-4f18-83c0-ce1245903ecf"}


                            Gson gson = new Gson();
                            GetPatientDataView userDetails = new Gson().fromJson(getIntent().getStringExtra(AppConstants.INTENT_KEY.USER_DETAILS), GetPatientDataView.class);
                            WebserviceConstants.logout = false;
                            finish();

                            Log.e("Role", " " + role);

                            if (role.equals("Patient")) {

                                Intent intent = new Intent(Acceptance.this, DashboardActivity.class);
                                intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, gson.toJson(userDetails));
                                startActivity(intent);

                            } else if (role.equals("Family Member")) {
                                WebserviceConstants.logout = false;
                                Intent intent = new Intent(Acceptance.this, DashboardActivity_FM.class);
                                intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, gson.toJson(userDetails));
                                startActivity(intent);

                            } else {
                                Intent intent = new Intent(Acceptance.this, DashboardActivity.class);
                                intent.putExtra(AppConstants.INTENT_KEY.USER_DETAILS, gson.toJson(userDetails));
                                startActivity(intent);

                            }


                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e("failure", " " + error);
                        CustomProgressbar.hideProgressBar();

                    }
                }
        );

    }



}

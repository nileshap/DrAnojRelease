package com.endo.intellih.activities.questionnaire;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.endo.intellih.AppFunctions;
import com.endo.intellih.R;
import com.endo.intellih.common.CommonMethod;
import com.endo.intellih.customviews.CustomButton;
import com.endo.intellih.customviews.CustomProgressbar;
import com.endo.intellih.customviews.CustomTextView;
import com.endo.intellih.storage.preference.UserSharedPreferences;
import com.endo.intellih.webservice.MyCallback;
import com.endo.intellih.webservice.RemoteMethods3;
import com.endo.intellih.webservice.services.response.Questionniare.AddQuestionnaireRequest;
import com.endo.intellih.webservice.services.response.Questionniare.GetAssignedQuestion;

import java.util.ArrayList;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class New_Questionnaire_Activity extends AppCompatActivity
{

    ImageView img_back;

    RecyclerView recyclerView;
    CustomButton btn_submit;
    DataAdapter adapter;
    ArrayList<GetAssignedQuestion.QuestionnaireQuestion> arraylist_question = new ArrayList<GetAssignedQuestion.QuestionnaireQuestion>();
    ArrayList<AddQuestionnaireRequest.QuestionReponse> arraylist_request = new ArrayList<AddQuestionnaireRequest.QuestionReponse>();
    ArrayList<String> arraylist_add_id = new ArrayList<String>();
    String assignedQuestionnaireId = "";
    ImageView imageView_history_reponse;
    LinearLayout layout_submit;
    LinearLayout layout_main;
    LinearLayout layout_aftersubmit;
    CustomTextView txt_message;
    Button btn_ok;
    CommonMethod common;

    AddQuestionnaireRequest request;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_questionnaire);
        setUp();
    }

    public void setUp()
    {
        common = new CommonMethod(New_Questionnaire_Activity.this);
        img_back = (ImageView) findViewById(R.id.imageView_back_new_questionnaire);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_new_questionnaire);
        btn_submit = (CustomButton) findViewById(R.id.btn_submit_new_questionnaire);
        imageView_history_reponse = (ImageView) findViewById(R.id.imageView_history_response_new_questionnaire);
        btn_ok = (Button) findViewById(R.id.btn_ok_new_questionnaire);
        txt_message = (CustomTextView)findViewById(R.id.textView_message__new_questionnaire);
        layout_submit = (LinearLayout) findViewById(R.id.layout_submit_new_questionnaire);
        layout_aftersubmit = (LinearLayout) findViewById(R.id.layout_aftersubmit_new_questionnaire);
        layout_main = (LinearLayout) findViewById(R.id.layout_question_new_questionnaire);
        onClick();

        if(common.isConnected(New_Questionnaire_Activity.this)) {
            callWebserviceForQuestionnair();
        }
        else
        {
            AppFunctions.showMessageDialog(New_Questionnaire_Activity.this, getResources().getString(R.string.alert_no_internet_connection));
        }

    }

    public void onClick()
    {

        imageView_history_reponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(New_Questionnaire_Activity.this,TrendQuestionResponse.class);
                startActivity(intent);
            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        final ArrayList<String> temp_questionid = new ArrayList<String>();

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                for (int i = 0; i < arraylist_question.size(); i++)
                {
                    if (arraylist_question.get(i).getQuestion().getResponseTypeCode() == 1 || arraylist_question.get(i).getQuestion().getResponseTypeCode() == 2) {
                        if (arraylist_request.get(i).getAnswerValue().equals("null"))
                        {
                            Log.e("QuestionIdR", " " + arraylist_question.get(i).getQuestion().getId());
                            temp_questionid.add(arraylist_question.get(i).getQuestion().getId());
                            //arraylist_request.remove(i);
                        }

                    } else if (arraylist_question.get(i).getQuestion().getResponseTypeCode() == 3)
                    {
                        if (arraylist_request.get(i).getAnswerText().equals(""))
                        {
                            //arraylist_request.remove(i);
                            temp_questionid.add(arraylist_question.get(i).getQuestion().getId());
                            Log.e("QuestionIdRR", " " + arraylist_question.get(i).getQuestion().getId());
                        }

                    }
                }
                for(int i=0; i< request.getQuestionReponses().size();i++)
                {
                    Log.e("Question", " " + request.getQuestionReponses().get(i).getQuestionId());
                    for(int t =0; t<temp_questionid.size();t++)
                    {
                        Log.e("Question_match", " " + temp_questionid.get(t) + ":" + (request.getQuestionReponses().get(i).getQuestionId()));
                        if(request.getQuestionReponses().get(i).getQuestionId().equalsIgnoreCase(temp_questionid.get(t)))
                        {
                            Log.e("Question_remove", " " + request.getQuestionReponses().get(i).getQuestionId());
                           request.getQuestionReponses().remove(i);
                        }
                    }

                    Log.e("Size", " " + request.getQuestionReponses().size());
                }
                callWebserviceForAddQuestion();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                layout_main.setVisibility(View.VISIBLE);
                layout_aftersubmit.setVisibility(View.GONE);

                if(common.isConnected(New_Questionnaire_Activity.this))
                {
                    /*arraylist_question.clear();
                    arraylist_request.clear();
                    callWebserviceForQuestionnair();*/

                    finish();
                    Intent i = new Intent(New_Questionnaire_Activity.this,New_Questionnaire_Activity.class);
                    startActivity(i);
                }
                else
                {
                    AppFunctions.showMessageDialog(New_Questionnaire_Activity.this, getResources().getString(R.string.alert_no_internet_connection));
                }
            }
        });

    }

    public void callWebserviceForQuestionnair() {
        //String patientUserId = "f14ee3ee-4e42-4407-91e6-97a844ac9c6a";
        String patientUserId  = UserSharedPreferences.getInstance(New_Questionnaire_Activity.this).getString(UserSharedPreferences.KEY_USER_ID);
        CustomProgressbar.showProgressBar(New_Questionnaire_Activity.this, false);
        RemoteMethods3.getQuestionnaire().getAssignedQuestionnire(patientUserId, new MyCallback<GetAssignedQuestion>() {
            @Override
            public void success(GetAssignedQuestion res, Response response) {

                // "Message":"Your account already activated."
                CustomProgressbar.hideProgressBar();
                Log.e("Response", " " + res + ":" +response );
                if(res !=null)
                {


                    layout_main.setVisibility(View.VISIBLE);
                    layout_aftersubmit.setVisibility(View.GONE);
                    imageView_history_reponse.setVisibility(View.VISIBLE);
                    arraylist_question.clear();
                    assignedQuestionnaireId = res.getId();
                    arraylist_question = res.getQuestionnaire().getQuestionnaireQuestions();
                    Log.e("Response", " " + response + ":" + arraylist_question.size());
                    setAdapterDataForQuestion();
                }
                else
                {

                    imageView_history_reponse.setVisibility(View.GONE);
                    layout_main.setVisibility(View.GONE);
                    layout_aftersubmit.setVisibility(View.VISIBLE);
                    btn_ok.setVisibility(View.GONE);
                    txt_message.setText(getResources().getString(R.string.noquestionnaire));


                }

            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("receivedError :" + error.fillInStackTrace());
                AppFunctions.showMessageDialog(New_Questionnaire_Activity.this, getResources().getString(R.string.unabletoconnect));
                CustomProgressbar.hideProgressBar();
            }
        });
    }

    public void callWebserviceForAddQuestion() {

        //String patientUserId = = UserSharedPreferences.getInstance(New_Questionnaire_Activity.this).getString(UserSharedPreferences.KEY_USER_ID)
        CustomProgressbar.showProgressBar(New_Questionnaire_Activity.this, false);
        RemoteMethods3.getQuestionnaire().addQuestionnaire(request, new MyCallback<GetAssignedQuestion>() {
            @Override
            public void success(GetAssignedQuestion res, Response response) {
                // "Message":"Your account already activated."
                CustomProgressbar.hideProgressBar();

                txt_message.setText(getResources().getString(R.string.responsesubmiteed));
                btn_ok.setVisibility(View.VISIBLE);
                layout_main.setVisibility(View.GONE);
                layout_aftersubmit.setVisibility(View.VISIBLE);

            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("receivedError :" + error.fillInStackTrace());
                AppFunctions.showMessageDialog(New_Questionnaire_Activity.this, getResources().getString(R.string.unabletoconnect));
                CustomProgressbar.hideProgressBar();
            }
        });
    }


    //Time slot adapter
    public void setAdapterDataForQuestion() {
        request = new AddQuestionnaireRequest();
        request.setAssignedQuestionnaireId(assignedQuestionnaireId);

        for (int i = 0; i < arraylist_question.size(); i++) {
            AddQuestionnaireRequest.QuestionReponse req = new AddQuestionnaireRequest.QuestionReponse();
            req.setAnswerValue("null");
            req.setQuestionId(arraylist_question.get(i).getQuestion().getId());
            req.setAnswerText("");
            arraylist_request.add(req);
        }

        request.setQuestionReponses(arraylist_request);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(New_Questionnaire_Activity.this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DataAdapter();
        recyclerView.setAdapter(adapter);

    }

    public void showDialogOK(String message, final int no) {

        final Dialog dialog = new Dialog(New_Questionnaire_Activity.this);
        LayoutInflater layoutInflater = LayoutInflater.from(New_Questionnaire_Activity.this);
        View dialogView = layoutInflater.inflate(R.layout.custome_dialog_error, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        ((CustomTextView) dialogView.findViewById(R.id.textView_message_custome_dialog)).setText(message);
        Button btnOK = (Button) dialogView.findViewById(R.id.button_custome_dialog_ok);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Yes

                if (dialog != null) {
                    dialog.dismiss();
                }

                if (no == 1) //at least one question select
                {


                }
            }
        });
        dialog.show();
    }

    //Data Adapter class
    public class DataAdapter extends RecyclerView.Adapter<DataAdapter.MyViewHolder> {

        boolean check1 = false;
        boolean check2 = false;
        boolean check3 = false;
        private ArrayList<String> data;

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.new_questionnire_row, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            //Movie movie = moviesList.get(position);

            Log.e("Qeustion", " " + arraylist_question.get(position).getQuestion().getText());

            int typecode = arraylist_question.get(position).getQuestion().getResponseTypeCode();
            holder.textView_question.setText(arraylist_question.get(position).getQuestion().getText());

            if (typecode == 1) //objective
            {
                holder.layout_four_option.setVisibility(View.VISIBLE);
                holder.layout_yes_no.setVisibility(View.GONE);
                holder.layout_text_area.setVisibility(View.GONE);
            } else if ((typecode == 2)) // yes no
            {
                holder.layout_yes_no.setVisibility(View.VISIBLE);
                holder.layout_four_option.setVisibility(View.GONE);
                holder.layout_text_area.setVisibility(View.GONE);
            } else if ((typecode == 3)) // subjective type
            {
                holder.layout_four_option.setVisibility(View.GONE);
                holder.layout_yes_no.setVisibility(View.GONE);
                holder.layout_text_area.setVisibility(View.VISIBLE);
            }

            if (typecode == 1) {
                if (arraylist_request.get(position).getAnswerValue().equals("1")) {

                    holder.rd_first.setChecked(true);
                    holder.rd_second.setChecked(false);
                    holder.rd_third.setChecked(false);
                    holder.rd_fourth.setChecked(false);
                } else if (arraylist_request.get(position).getAnswerValue().equals("2")) {
                    holder.rd_second.setChecked(true);
                    holder.rd_first.setChecked(false);
                    holder.rd_third.setChecked(false);
                    holder.rd_fourth.setChecked(false);
                } else if (arraylist_request.get(position).getAnswerValue().equals("3")) {
                    holder.rd_third.setChecked(true);
                    holder.rd_first.setChecked(false);
                    holder.rd_second.setChecked(false);
                    holder.rd_fourth.setChecked(false);
                } else if (arraylist_request.get(position).getAnswerValue().equals("4")) {
                    holder.rd_fourth.setChecked(true);
                    holder.rd_first.setChecked(false);
                    holder.rd_second.setChecked(false);
                    holder.rd_third.setChecked(false);
                } else {
                    check1 = true;
                    holder.rd_first.setChecked(false);
                    holder.rd_second.setChecked(false);
                    holder.rd_third.setChecked(false);
                    holder.rd_fourth.setChecked(false);
                }
            } else if (typecode == 2)//yes/no
            {
                if (arraylist_request.get(position).getAnswerValue().equals("1")) {
                    holder.rdyesno_first.setChecked(true);
                    holder.rdyesno_second.setChecked(false);
                } else if (arraylist_request.get(position).getAnswerValue().equals("2")) {
                    holder.rdyesno_first.setChecked(false);
                    holder.rdyesno_second.setChecked(true);
                } else {
                    check2 = true;
                    holder.rdyesno_first.setChecked(false);
                    holder.rdyesno_second.setChecked(false);
                }
            } else if (typecode == 3) //text area
            {
                if (arraylist_request.get(position).getAnswerValue().equals("")) {
                    check3 = true;
                }

                holder.ed_question_text.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                        arraylist_request.get(position).setAnswerText(s.toString());

                        if (s.length() > 0) {
                            check2 = true;
                        } else {
                            check2 = false;
                        }

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {


                    }
                });
            }


            holder.rd_first.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    arraylist_request.get(position).setAnswerValue("1");
                    adapter.notifyDataSetChanged();
                }
            });

            holder.rd_second.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    arraylist_request.get(position).setAnswerValue("2");
                    adapter.notifyDataSetChanged();
                }
            });

            holder.rd_third.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    arraylist_request.get(position).setAnswerValue("3");
                    adapter.notifyDataSetChanged();
                }
            });

            holder.rd_fourth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    arraylist_request.get(position).setAnswerValue("4");
                    adapter.notifyDataSetChanged();
                }
            });

            holder.rdyesno_first.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    arraylist_request.get(position).setAnswerValue("1");
                    adapter.notifyDataSetChanged();
                }
            });


            holder.rdyesno_second.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    arraylist_request.get(position).setAnswerValue("2");
                    adapter.notifyDataSetChanged();
                }
            });


            if (check1 && check2 && check3) {

                layout_submit.setVisibility(View.GONE);
            } else {
                layout_submit.setVisibility(View.VISIBLE);
            }


        }

        @Override
        public int getItemCount() {
            return arraylist_question.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            RadioButton rd_first;
            RadioButton rd_second;
            RadioButton rd_third;
            RadioButton rd_fourth;
            RadioButton rdyesno_first;
            RadioButton rdyesno_second;
            EditText ed_question_text;
            private LinearLayout layout_four_option;
            private LinearLayout layout_text_area;
            private LinearLayout layout_yes_no;
            private TextView textView_question;
            private FrameLayout layout;

            public MyViewHolder(View view) {
                super(view);
                layout_four_option = (LinearLayout) view.findViewById(R.id.layout_fouroption_new_questionnire);
                layout_yes_no = (LinearLayout) view.findViewById(R.id.layout_yesno_new_questionnire);
                layout_text_area = (LinearLayout) view.findViewById(R.id.layout_text_new_questionnire);
                ed_question_text = (EditText) view.findViewById(R.id.editext_text_new_questionnire);

                textView_question = (TextView) view.findViewById(R.id.textView_question_new_questionnire);
                rd_first = (RadioButton) view.findViewById(R.id.radioButton_first_new_questionnire);
                rd_second = (RadioButton) view.findViewById(R.id.radioButton_second_new_questionnire);
                rd_third = (RadioButton) view.findViewById(R.id.radioButton_third_new_questionnire);
                rd_fourth = (RadioButton) view.findViewById(R.id.radioButton_fourth_new_questionnire);

                rdyesno_first = (RadioButton) view.findViewById(R.id.radioButton_yesno_first_new_questionnire);
                rdyesno_second = (RadioButton) view.findViewById(R.id.radioButton_yesno_second_new_questionnire);

                // layout_four_option = (LinearLayout)findViewById(R.id.layout_fouroption_new_questionnire) ;
                // layout_four_option = (LinearLayout)findViewById(R.id.layout_fouroption_new_questionnire) ;

            }
        }
    }


}

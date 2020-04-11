package com.endo.intellih.adapter;

import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.endo.intellih.Fragments.GoalSelfMeasureFragment;
import com.endo.intellih.R;
import com.endo.intellih.common.DecimalDigitsInputFilter;
import com.endo.intellih.customviews.CustomTextView;
import com.endo.intellih.utils.AppLog;
import com.endo.intellih.webservice.WebserviceConstants;
import com.endo.intellih.webservice.services.response.GetPatientDataView;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by admin on 10/4/2016.
 */
public class GoalSelfMeasureAdapter extends RecyclerView.Adapter<GoalSelfMeasureAdapter.DataObjectHolder>
{

    private static String TAG = GoalSelfMeasureAdapter.class.getSimpleName();

    private ArrayList<GetPatientDataView.Goal> mDataset;
    private GoalSelfMeasureFragment mFragmet;
    DecimalFormat decimalFormat=new DecimalFormat("#.#");
    public GoalSelfMeasureAdapter(ArrayList<GetPatientDataView.Goal> myDataset, GoalSelfMeasureFragment mFragmet)
    {
        mDataset = myDataset;
        this.mFragmet = mFragmet;
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder {
        TextView txtFactorName, txtFactorUnit,textView_setgoal;
        EditText edtMin, edtMax;
        ToEditTextListener toEditTextListener;
        FromEditTextListener fromEditTextListener;

        public DataObjectHolder(View itemView, ToEditTextListener toEditTextListener, FromEditTextListener fromEditTextListener) {
            super(itemView);



            txtFactorName = (TextView) itemView.findViewById(R.id.txt_factorname);
            textView_setgoal = (CustomTextView) itemView.findViewById(R.id.textView_setGoal);
            txtFactorUnit = (TextView) itemView.findViewById(R.id.txt_factorunit);
            edtMin = (EditText) itemView.findViewById(R.id.txt_min);
            edtMax = (EditText) itemView.findViewById(R.id.txt_max);

            this.toEditTextListener = toEditTextListener;
            this.fromEditTextListener = fromEditTextListener;
            edtMax.addTextChangedListener(toEditTextListener);
            edtMin.addTextChangedListener(fromEditTextListener);
        }
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_goal_self_measure, parent, false);
        return new DataObjectHolder(view, new ToEditTextListener(), new FromEditTextListener());
    }
    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position)
    {
        final GetPatientDataView.Goal obj = mDataset.get(position);

        boolean isPostOp = false;
        for (GetPatientDataView.AssociatedCDM cdm : WebserviceConstants.associateCDM) {
            if(cdm.getCDMName().equalsIgnoreCase("Post Operative") || cdm.getCDMName().equalsIgnoreCase("CHF") ){
                isPostOp=true;
            }
        }

        holder.edtMin.setHint("From");
        holder.edtMax.setHint("To");
        holder.textView_setgoal.setText("No Goal Set");


        if(isPostOp){
           if (obj.getFactorName().equalsIgnoreCase("Aerobics")){
               holder.txtFactorName.setText("Duration");
           }
            else{
               holder.txtFactorName.setText(obj.getFactorName());
           }
        }else{
            holder.txtFactorName.setText(obj.getFactorName());
        }
        final DataObjectHolder myViewHolder = holder;
       // myViewHolder.edtMax.setFilters(new InputFilter[]{new InputFilter.LengthFilter(obj.getVitalValidation().getDataType().get(0).getValueLength())});
       // myViewHolder.edtMin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(obj.getVitalValidation().getDataType().get(0).getValueLength())});
        //AppLog.showE(TAG, obj.getVitalValidation().getVitalName());
        holder.toEditTextListener.updatePosition(position);
        holder.fromEditTextListener.updatePosition(position);
        if (!obj.getUnit().equals(null)) {
            myViewHolder.txtFactorUnit.setVisibility(View.VISIBLE);
            myViewHolder.txtFactorUnit.setText(obj.getUnit());
        } else {
            myViewHolder.txtFactorUnit.setVisibility(View.GONE);
        }



        Log.e("Values************","--"+obj.getGoal1()+"--"  + obj.getGoal2());
        if ( obj.getGoal1() == 0 ||  obj.getGoal1() == 0.0 || String.valueOf(obj.getGoal1()).equals(null) || String.valueOf(obj.getGoal1()).equals("") )
        {
            myViewHolder.edtMin.setText("");
            holder.edtMin.setVisibility(View.GONE);
            holder.textView_setgoal.setVisibility(View.VISIBLE);

        } else
            {
                holder.edtMin.setVisibility(View.VISIBLE);
                holder.textView_setgoal.setVisibility(View.GONE);
            String Temp=obj.getGoal1()+"";
            String[] TempArray=Temp.split("\\.");
            if (Long.parseLong(TempArray[1])==0){

                myViewHolder.edtMin.setText(TempArray[0]);
            }
            else{
                myViewHolder.edtMin.setText(obj.getGoal1()+"");
            }
//            myViewHolder.edtMin.setText("" + decimalFormat.format(obj.getGoal1()));
        }


        if (obj.getGoalType().equals("Range"))
        {

            if ( obj.getGoal2() == 0 ||  obj.getGoal2() == 0.0 ||  String.valueOf(obj.getGoal2()).equals(null) || String.valueOf(obj.getGoal2()).equals(""))
            {
                myViewHolder.edtMax.setText("");
                myViewHolder.edtMax.setVisibility(View.GONE);
                myViewHolder.textView_setgoal.setVisibility(View.VISIBLE);
                Log.e("Max", " "+ "Gone");
            }
            else
                {
                    Log.e("Max", " "+ "Visible");
                    myViewHolder.edtMax.setVisibility(View.VISIBLE);
                   // myViewHolder.textView_setgoal.setVisibility(View.GONE);
                String Temp=obj.getGoal2()+"";
                String[] TempArray=Temp.split("\\.");
                if (Long.parseLong(TempArray[1])==0)
                {
                    myViewHolder.edtMax.setText(TempArray[0]);
                }
                else{
                    myViewHolder.edtMax.setText(obj.getGoal2()+"");
                }
//                myViewHolder.edtMax.setText("" + decimalFormat.format(obj.getGoal2()));
            }


        } else {
            myViewHolder.edtMax.setVisibility(View.GONE);

        }
        setValidation(myViewHolder.edtMax, myViewHolder.edtMin,obj.getFactorId(),mDataset.size(),position);
       // setValidation(myViewHolder.edtMin, obj.getFactorId());
        //myViewHolder.edtMax.setOnEditorActionListener(onKeyListener);
        myViewHolder.edtMax.setTag(position);

//        if (!obj.isoverridable()){
//            myViewHolder.edtMax.setEnabled(false);
//            myViewHolder.edtMin.setEnabled(false);
//        }
    }

   /* TextView.OnEditorActionListener onKeyListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            //  AppLog.showE(TAG, "key change");
            //AppLog.showE(TAG, "key change=" + v.getTag());
            int position = (int) v.getTag();
            if (actionId == EditorInfo.IME_ACTION_DONE)
            {
                mFragmet.onDoneButton(position);
                return true;
            }

            return false;
        }


    };*/


    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public ArrayList<GetPatientDataView.Goal> getData() {
        return mDataset;
    }

    public void setValidation(EditText maxEdt,EditText minEdt, int factorid,int total,int pos)
    {
        AppLog.showE(TAG, "setValidation");

       /* if (validation.getDataType().size() > 0)t
        {
            DataType dataType = validation.getDataType().get(0);
            if (dataType.getDataType().equals("Integer"))
            {
                edtValue.setRawInputType(Configuration.KEYBOARD_12KEY);
            } else if (dataType.getDataType().equals("Decimal"))
            {
                edtValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
            //setting length

//            edtValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(validation.getDataType().get(0).getValueLength())});
        }*/

        for(int v=0 ; v < WebserviceConstants.allvalidation.size(); v++)
        {
            boolean check = false;
            if(factorid == WebserviceConstants.allvalidation.get(v).getFactorId())
            {

                for (int r = 0; r < WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().size(); r++)
                {


                    String dataType = WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getDataType();
                    Log.e("SetDataType", "  " + factorid + ":" + dataType + ":" +WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength());
                    if (dataType.equals("Integer")) {
                        Log.e("SetDataType-iNTEGER", "  " + factorid + ":" + dataType);
                        maxEdt.setRawInputType(Configuration.KEYBOARD_12KEY);
                        minEdt.setRawInputType(Configuration.KEYBOARD_12KEY);

                        maxEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength())});
                        minEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength())});


                    } else if (dataType.equals("Decimal")) {
                        Log.e("SetDataType-dECIMAL", "  " + factorid+ ":" + dataType);
                        maxEdt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        minEdt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                    /*    maxEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength())});
                        minEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength())});
*/
                        maxEdt.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength(),WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getDecimalPlaceLength())});

                        minEdt.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getValueLength(),WebserviceConstants.allvalidation.get(v).getValidationDataTypeList().get(r).getDecimalPlaceLength())});

                    }
                    //setting length
                    check = true;

                }
            }

            Log.e("Check", " " + check);
            if(check)
            {
                break;
            }
        }

        if(total-1 != pos)
        {
            Log.e("Next", " " + total + ":" + pos);
            maxEdt.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            minEdt.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }
        else
        {
            Log.e("Done", " " + total + ":" + pos);
            minEdt.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            maxEdt.setImeOptions(EditorInfo.IME_ACTION_DONE);
           // maxEdt.setOnEditorActionListener(new DoneOnEditorActionListener());

        }


    }


    /*class DoneOnEditorActionListener implements EditText.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            Log.e("Done-1", " " + "Done-1");
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Log.e("Done-2", " " + "Done-2");
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        }
    }
*/
    private class ToEditTextListener implements TextWatcher {
        private int position;

        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // no op
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            //  mDataset[position] = charSequence.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
            GetPatientDataView.Goal obj = mDataset.get(position);
            Log.e("goal2before=","" + obj.getGoal2());
//            if (s.length() >= 0) {
//                if (!s.toString().isEmpty())
//                    obj.setGoal1(Float.parseFloat("" + s));
//                else
//                    obj.setGoal1(0);
//                AppLog.showD(TAG, "goal1=" + obj.getGoal1());
//            }
            if (s.length() > 0) {
                Log.e("String Typed","----"+s+"----");

                if(containsDigit(String.valueOf(s)))
                    obj.setGoal2(Float.parseFloat("" + s));
                else
                    obj.setGoal2(0);
            } else if (s.length() == 0) {
                obj.setGoal2(0);
            }
            AppLog.showD(TAG, "goal1=" + obj.getGoal2());
        }
    }

    private class FromEditTextListener implements TextWatcher {
        private int position;

        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // no op
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            //  mDataset[position] = charSequence.toString();
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            GetPatientDataView.Goal obj = mDataset.get(position);
            AppLog.showD(TAG, "goal2before=" + obj.getGoal1());
//            if (s.length() >= 0) {
//                if (!s.toString().isEmpty())
//                    obj.setGoal2(Float.parseFloat("" + s));
//                else
//                    obj.setGoal2(0);
//                AppLog.showD(TAG, "goal2=" + obj.getGoal2());
//            }
            if (s.length() > 0) {
                Log.e("String Typed","----"+s+"----");
                if(containsDigit(String.valueOf(s)))
                    obj.setGoal1(Float.parseFloat("" + s));
                else
                    obj.setGoal1(0);
            } else if (s.length() == 0) {
                obj.setGoal1(0);
            }
            AppLog.showD(TAG, "goal2=" + obj.getGoal1());
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
            if (count>1){
                containsDigit=true;
            }
        }

        return containsDigit;
    }


}

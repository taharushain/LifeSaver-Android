/*******************************************************************************
 * Copyright (c) 2016. Golemtron.com
 * Designed and Developed by Taha Rushain
 ******************************************************************************/

package com.golemtron.lifesaver.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.golemtron.lifesaver.R;
import com.golemtron.lifesaver.util.PreferenceManager;
import com.golemtron.lifesaver.util.SqliteHandler;

public class InformationActivity extends AppCompatActivity {

    private ProgressDialog pDialog;
    private String TAG = SelectHospitalActivity.class.getName();
    private Context mContext;
    private PreferenceManager preferenceManager;
    private SqliteHandler db;


    private Spinner type_spinner;
    private RadioGroup rg_blood, rg_temp, rg_pulse, rg_breath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        preferenceManager = new PreferenceManager(getApplicationContext());
        db = new SqliteHandler(getApplicationContext());

        if (!preferenceManager.isLoggedIn()) {

            Intent intent = new Intent(InformationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        }
        else if(preferenceManager.getRequestSent()==true){
            Intent intent = new Intent(InformationActivity.this, HospitalSelectedActivity.class);
            startActivity(intent);
            finish();
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        type_spinner = (Spinner) findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.request_type_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type_spinner.setAdapter(adapter);
        rg_blood = (RadioGroup) findViewById(R.id.rg_blood);
        rg_temp = (RadioGroup) findViewById(R.id.rg_temp);
        rg_pulse = (RadioGroup) findViewById(R.id.rg_pulse);
        rg_breath = (RadioGroup) findViewById(R.id.rg_breath);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_hospital, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.miLogoff:
                logOff();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logOff() {
        preferenceManager.setLogin(false);
        db.deleteUsers();
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    public void next(View view){


        String msg = "All fields are required\n";
        Boolean error = false;
        if(rg_blood.getCheckedRadioButtonId() == -1){
            error=true;
            msg+=" Blood,";
        }
        if(rg_temp.getCheckedRadioButtonId() == -1){
            error=true;
            msg+=" Temperature,";
        }
        if(rg_breath.getCheckedRadioButtonId() == -1){
            error=true;
            msg+=" Breathing,";
        }
        if(rg_pulse.getCheckedRadioButtonId() == -1){
            error=true;
            msg+=" Pulse";
        }

        if(error){
            msg+=" missing";
            Snackbar.make(view,msg,Snackbar.LENGTH_SHORT).show();
            return;
        }


        String type = type_spinner.getSelectedItem().toString();
        String blood = ((RadioButton)findViewById(rg_blood.getCheckedRadioButtonId())).getText().toString().trim();
        String pulse = ((RadioButton)findViewById(rg_pulse.getCheckedRadioButtonId())).getText().toString().trim();
        String temp = ((RadioButton)findViewById(rg_temp.getCheckedRadioButtonId())).getText().toString().trim();
        String breath = ((RadioButton)findViewById(rg_breath.getCheckedRadioButtonId())).getText().toString().trim();


        preferenceManager.addPatientType(type);
        preferenceManager.addPatientTemperature(temp);
        preferenceManager.addPatientPulse(pulse);
        preferenceManager.addPatientBreathing(breath);
        preferenceManager.addPatientBlood(blood);

        Intent intent = new Intent(InformationActivity.this, SelectHospitalActivity.class);
        startActivity(intent);

    }
}

/*******************************************************************************
 * Copyright (c) 2016. Golemtron.com
 * Designed and Developed by Taha Rushain
 ******************************************************************************/

package com.golemtron.lifesaver.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.golemtron.lifesaver.R;

import com.golemtron.lifesaver.model.AmbulanceUser;
import com.golemtron.lifesaver.util.AppController;
import com.golemtron.lifesaver.util.Constants;
import com.golemtron.lifesaver.util.PreferenceManager;
import com.golemtron.lifesaver.util.SqliteHandler;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private PreferenceManager prefManager;
    private ProgressDialog pDialog;
    private SqliteHandler db;

    private String jsonResponse;
    private static String TAG = LoginActivity.class.getSimpleName();

    private EditText et_email;
    private EditText et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        et_email = (EditText)findViewById(R.id.et_email);
        et_password = (EditText) findViewById(R.id.et_password);

        prefManager = new PreferenceManager(getApplicationContext());
        db = new SqliteHandler(getApplicationContext());

        if (prefManager.isLoggedIn()) {
            if(prefManager.getRequestSent()==true){
                Intent intent = new Intent(LoginActivity.this, HospitalSelectedActivity.class);
                startActivity(intent);
                finish();
            }else{
                // User is already logged in. Take him to main activity
                Intent intent = new Intent(LoginActivity.this, InformationActivity.class);
//            intent.putExtra("id",prefManager.getId());
//            intent.putExtra("email",prefManager.getEmail());
//            intent.putExtra("name",prefManager.getName());
//            intent.putExtra("belongs_to",prefManager.getBelongsTo());
//            intent.putExtra("authentication_token",prefManager.getAuthorizationToken());
                startActivity(intent);
                finish();
            }
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }

    public void btn_login(View v) throws JSONException {

        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        et_password.setText("");
        et_email.setText("");

//        new RetrieveLoginInfo().execute();
        volleyRequest(email, password);

    }

    private void volleyRequest(final String email, final String password) {

        showpDialog();

        String tag_json_obj = "user_login_obj_req";

        String url = Constants.LOGIN_URL;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //Creating JsonObject from response String
                    JSONObject jsonObject = new JSONObject(response.toString());
                    //extracting json array from response string
                    //JSONArray jsonArray = jsonObject.getJSONArray("arrname");
                    //JSONObject jsonRow = jsonArray.getJSONObject(0);
                    //get value from jsonRow
                    Integer id = jsonObject.getInt("id");
                    String email = jsonObject.getString("email");
                    String name = jsonObject.getString("name");
                    String belongs_to = jsonObject.getString("belongs_to");
                    String authentication_token = jsonObject.getString("authentication_token");

                    hidepDialog();

                    AmbulanceUser user = new AmbulanceUser(id,email,name,belongs_to,authentication_token);

                    if(!user.getAuthentication_token().isEmpty() ||
                            user.getAuthentication_token() !="" ||
                            user.getAuthentication_token() != null){
                        startMain(user);
                    }else{
                        Toast.makeText(LoginActivity.this, "Sorry! Something went wrong", Toast.LENGTH_SHORT).show();
                    }

                    //Toast.makeText(LoginActivity.this, id+"\n"+email+"\n"+name+"\n"+belongs_to+"\n"+authentication_token, Toast.LENGTH_SHORT).show();
                }catch (JSONException e) {
                    e.printStackTrace();
                    hidepDialog();
                    Toast.makeText(LoginActivity.this, "Sorry, an error occured:\n"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidepDialog();
//                Toast.makeText(LoginActivity.this, ""+error.networkResponse, Toast.LENGTH_SHORT).show();
                Toast.makeText(LoginActivity.this, "Login Failed, Please check your credentials.", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parameters = new HashMap<String,String>();
                parameters.put("email",email);
                parameters.put("password",password);

                return parameters;
            }

        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj);
    }

    private void startMain(AmbulanceUser user) {
        Intent intent = new Intent(this, InformationActivity.class);
//        intent.putExtra("id",user.getId());
//        intent.putExtra("email",user.getEmail());
//        intent.putExtra("name",user.getName());
//        intent.putExtra("belongs_to",user.getBelongs_to());
//        intent.putExtra("authentication_token",user.getAuthentication_token());
        Log.d(TAG,"============================================");
        Log.d(TAG,""+user.getId());
        Log.d(TAG,""+user.getEmail());
        Log.d(TAG,""+user.getName());
        Log.d(TAG,""+user.getBelongs_to());
        Log.d(TAG,""+user.getAuthentication_token());
        db.addUser(user.getId(),user.getName(),user.getEmail(),user.getBelongs_to(),user.getAuthentication_token());
        prefManager.setLogin(true);
        prefManager.addId(user.getId());
        prefManager.addAuthorizationToken(user.getAuthentication_token());
        Log.d("LA Token :",""+prefManager.getAuthorizationToken());
        Log.d("LA User ID :",""+prefManager.getId());
        startActivity(intent);
        finish();
    }

    private void displaySnackBar(View view, String str){
        Log.e("LoginActivity", "Displaying snackbar");

        Snackbar snackbar = Snackbar
                .make(view, str, Snackbar.LENGTH_LONG);

        snackbar.show();
    }
    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


    class RetrieveLoginInfo extends AsyncTask<Void, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showpDialog();
        }

        @Override
        protected void onPostExecute(String response) {
            if(response == null) {
                response = "THERE WAS AN ERROR";
            }
            Log.i(TAG, response);
            hidepDialog();
        }

        @Override
        protected String doInBackground(Void... urls) {
            // Tag used to cancel the request


            return null;
        }
    }

}

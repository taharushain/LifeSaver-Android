/*******************************************************************************
 * Copyright (c) 2016. Golemtron.com
 * Designed and Developed by Taha Rushain
 ******************************************************************************/

package com.golemtron.lifesaver.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.golemtron.lifesaver.adapters.HospitalsListAdapter;
import com.golemtron.lifesaver.R;
import com.golemtron.lifesaver.model.AmbulanceUser;
import com.golemtron.lifesaver.model.HospitalItem;
import com.golemtron.lifesaver.model.PatientRequest;
import com.golemtron.lifesaver.util.AppController;
import com.golemtron.lifesaver.util.Constants;
import com.golemtron.lifesaver.util.PreferenceManager;
import com.golemtron.lifesaver.util.SqliteHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectHospitalActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {



    private List<HospitalItem> hospitalsList = new ArrayList<>();
    private HospitalsListAdapter adapter;
    private ListView hospitalListView;
    private ProgressDialog pDialog;
    private String TAG = SelectHospitalActivity.class.getName();
    private Context mContext;

    private double longitude;
    private double latitude;
    private String radius="9999";
    static final int LOCATION_SETTING_REQUEST = 1;  // The request code


    private PreferenceManager preferenceManager;
    private SqliteHandler db;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    // Google client to interact with Google API
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private AmbulanceUser user;
    private boolean first_run = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_hospital);

        mContext = this;

        if(!isLocationEnabled(mContext))
            showSettingsAlert();


        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        preferenceManager = new PreferenceManager(getApplicationContext());
        db = new SqliteHandler(getApplicationContext());


        if (!preferenceManager.isLoggedIn()) {

            Intent intent = new Intent(SelectHospitalActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        }
        else if(preferenceManager.getRequestSent()==true){
            Intent intent = new Intent(SelectHospitalActivity.this, HospitalSelectedActivity.class);
            startActivity(intent);
            finish();
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                .addLocationRequest(mLocationRequest);


        preferenceManager = new PreferenceManager(this);

        HashMap<String, String> user_hash = db.getAmbulanceUserDetails();

        user = new AmbulanceUser(
                Integer.parseInt(user_hash.get(Constants.KEY_ID)),
                user_hash.get(Constants.KEY_EMAIL),
                user_hash.get(Constants.KEY_NAME),
                user_hash.get(Constants.KEY_BELONGS_TO),
                user_hash.get(Constants.KEY_AUTH_TOKEN)
        );

//        Log.d(TAG,""+user.getId());
//        Log.d(TAG,""+user.getEmail());
//        Log.d(TAG,""+user.getName());
//        Log.d(TAG,""+user.getBelongs_to());
//        Log.d(TAG,""+user.getAuthentication_token());

        hospitalListView = (ListView) findViewById(R.id.hospitals_list);
        adapter = new HospitalsListAdapter(this, hospitalsList);
        hospitalListView.setAdapter(adapter);

        hospitalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(SelectHospitalActivity.this,HospitalSelectedActivity.class);
//                Bundle b = new Bundle();
//                intent.putExtra("id", hospitalsList.get(position).getId());
//                intent.putExtra("name", hospitalsList.get(position).getName());
//                intent.putExtra("latitude", hospitalsList.get(position).getLatitude());
//                intent.putExtra("longitude", hospitalsList.get(position).getLongitude());
//                startActivity(intent);
//                patientRequest(user.getAuthentication_token(), hospitalsList.get(position).getId()+"", user.getId()+"");
//                Snackbar.make(view, "Pressed :" + position, Snackbar.LENGTH_SHORT).show();

            }
        });
        hospitalListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });



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

        //Temporary commmands
        db.deleteRequests();
        preferenceManager.clear();
        //Temporary commmands

        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void fetchHospitals() {
        Log.d(""+TAG,"Fetch Hospital");
        showpDialog();

//        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }

//        Location location;
//        try {
//            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        } catch (SecurityException e) {
//            return;
//        }
//        double longitude = location.getLongitude();
//        double latitude = location.getLatitude();



        String tag_json_obj = "hospitals_list_req";
        String url = Constants.HOSPITALS_URL+"?radius="
                +radius+"&latitude="
                +latitude+"&longitude="+
                longitude+"";

        Log.d(TAG,"URL:"+url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response.toString());
                            hospitalsList.clear();
                            for(int i=0; i < jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int id = jsonObject.getInt("id");
                                String name = jsonObject.getString("name");
                                float latitude = (float)jsonObject.getDouble("latitude");
                                float longitude = (float)jsonObject.getDouble("longitude");

                                final HospitalItem hospital = new HospitalItem(id,name,latitude,longitude);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        hospitalsList.add(hospital);
//                                        Log.d(TAG,"HOSPITAL");
                                        Log.d(TAG,""+hospital.getName());
                                        adapter.notifyDataSetChanged();
                                    }
                                });

                            }

                            hidepDialog();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidepDialog();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj);
    }

    public void btnSendReqHospitals(View v){
        //fetchHospitals();
        //
        patientRequest(user.getAuthentication_token(),
                user.getId()+"",
                preferenceManager.getPatientType(),
                preferenceManager.getPatientBlood(),
                preferenceManager.getPatientTemperature(),
                preferenceManager.getPatientBreathing(),
                preferenceManager.getPatientPulse(),
                latitude+"",
                longitude+"",
                radius+""
                );
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    /**
     * Function to show settings alert dialog
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent,LOCATION_SETTING_REQUEST);
            }
        });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
//                if(!isLocationEnabled(mContext))
//                    showSettingsAlert();
            }
        });

//        // on pressing cancel button
//        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == LOCATION_SETTING_REQUEST) {
            if(!isLocationEnabled(mContext))
                showSettingsAlert();
            else {
                fetchHospitals();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();

            // Resuming the periodic location updates
            if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();
//
//        // Resuming the periodic location updates
//        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
//            startLocationUpdates();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    protected void startLocationUpdates() {
        try{
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        }catch (SecurityException e) {
//            showSettingsAlert();
        }

    }

    protected void stopLocationUpdates() {
        try{
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);}catch (SecurityException e) {
        }
    }

    private void displayLocation() {

        try {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }catch (SecurityException e) {
//            showSettingsAlert();
        }

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            if(isLocationEnabled(mContext) && first_run){
                first_run = false;
                fetchHospitals();
            }


        } else {

        }
    }

    protected void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        // Displaying the new location on UI
        displayLocation();
    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {

            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();

            Log.d(TAG, "Periodic location updates started!");

        } else {

            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    private void patientRequest(final String authentication_token,
                                final String ambulance_id,
                                final String requests_type,
                                final String blood_pressure,
                                final String temperature,
                                final String breathing,
                                final String pulse_rate,
                                final String latitude,
                                final String longitude,
                                final String radius
                                ) {

        showpDialog();

        String tag_json_obj = "patient_request_obj_req";

        String url = Constants.PATIENT_REQUEST_URL;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //Creating JsonObject from response String
                    JSONArray jsonArray= new JSONArray(response.toString());
                    for(int i=0; i<jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String req_id = jsonObject.getString("id");
                        PatientRequest pr = new PatientRequest(Integer.parseInt(req_id),
                                Integer.parseInt(jsonObject.getString("hospital_id")),
                                Integer.parseInt(jsonObject.getString("ambulance_user_id")),
                                jsonObject.getString("requests_type"),
                                jsonObject.getString("blood_pressure"),
                                jsonObject.getString("temperature"),
                                jsonObject.getString("breathing"),
                                jsonObject.getString("pulse_rate"));
                        db.addPatientRequest(pr);

                    }
                    preferenceManager.addRequestSent(true);

                    hidepDialog();

//                    Toast.makeText(mContext, "Number of reqs: "+jsonArray.length(), Toast.LENGTH_SHORT).show();


                    preferenceManager.addRequestSent(true);
//                    preferenceManager.addReqID(req_id);
//
                    Intent intent = new Intent(SelectHospitalActivity.this, HospitalSelectedActivity.class);
                    startActivity(intent);
                    finish();


                    //Toast.makeText(LoginActivity.this, id+"\n"+email+"\n"+name+"\n"+belongs_to+"\n"+authentication_token, Toast.LENGTH_SHORT).show();
                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidepDialog();
//                Toast.makeText(LoginActivity.this, ""+error.networkResponse, Toast.LENGTH_SHORT).show();
                Toast.makeText(SelectHospitalActivity.this, "Request Failed, Please try again", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parameters = new HashMap<String,String>();
                parameters.put("authentication_token",authentication_token);
                parameters.put("ambulance_user_id",ambulance_id);
                parameters.put("requests_type",requests_type);
                parameters.put("blood_pressure",blood_pressure);
                parameters.put("temperature",temperature);
                parameters.put("breathing",breathing);
                parameters.put("pulse_rate",pulse_rate);
                parameters.put("latitude",latitude);
                parameters.put("longitude",longitude);
                parameters.put("radius",radius);

                return parameters;
            }

        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj);
    }
}
package com.golemtron.lifesaver.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import android.Manifest;

import com.golemtron.lifesaver.R;
import com.golemtron.lifesaver.helpers.DirectionFinder;
import com.golemtron.lifesaver.helpers.DirectionFinderListener;
import com.golemtron.lifesaver.helpers.Route;
import com.golemtron.lifesaver.model.AmbulanceUser;
import com.golemtron.lifesaver.model.PatientRequest;
import com.golemtron.lifesaver.util.AppController;
import com.golemtron.lifesaver.util.Constants;
import com.golemtron.lifesaver.util.PreferenceManager;
import com.golemtron.lifesaver.util.SqliteHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HospitalSelectedActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DirectionFinderListener, LocationListener {

    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();

//    private RequestStatusService service;
//    private boolean isBound = false;

    private PreferenceManager preferenceManager;
    private SqliteHandler db;
    private ProgressDialog pDialog;
    private String TAG = SelectHospitalActivity.class.getName();
    private Context mContext;
    private ProgressBar mProgress;
    private TextView tv_msg;
    private RelativeLayout rl_action_holder;
    private Button btn_complete;



    private GoogleMap mMap;

    private String destination = "";
    private double mLongitude;
    private double mLatitude;
    static final int LOCATION_SETTING_REQUEST = 1;  // The request code
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private boolean first_run = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_selected);

        mContext = this;


        mContext = this;
        mProgress = (ProgressBar) findViewById(R.id.pb_waiting);
        mProgress.setIndeterminate(true);
        tv_msg = (TextView) findViewById(R.id.tv_waiting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        preferenceManager = new PreferenceManager(getApplicationContext());
        db = new SqliteHandler(getApplicationContext());

        if (!preferenceManager.isLoggedIn()) {

                Intent intent = new Intent(HospitalSelectedActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        if (preferenceManager.getRequestAccepted() == true)
            requestAccepted();
        else
            checkRequestStatus(preferenceManager.getAuthorizationToken(), preferenceManager.getId() + "");

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btn_complete = (Button) findViewById(R.id.btn_complete);
        btn_complete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                completeRequest();
                return false;
            }
        });
    }

    private void completeRequest() {
        completePatientRequest(preferenceManager.getAuthorizationToken(),
                preferenceManager.getId()+"",
                preferenceManager.getRequestId()+"");
    }


    private void requestAccepted() {
        if(!isLocationEnabled(mContext))
            showSettingsAlert();
        else{

                mProgress.setVisibility(View.INVISIBLE);
                tv_msg.setText("Enroute to hospital");
                tv_msg.setVisibility(View.INVISIBLE);

                FrameLayout fl_map = (FrameLayout) findViewById(R.id.fl_map);
                fl_map.setVisibility(View.VISIBLE);
                rl_action_holder = (RelativeLayout) findViewById(R.id.action_holder);
                rl_action_holder.setVisibility(View.VISIBLE);
                btn_complete = (Button) findViewById(R.id.btn_complete);
                btn_complete.setVisibility(View.VISIBLE);

//                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                        .findFragmentById(R.id.map);
//
//                mapFragment.getMapAsync(this);

                destination = preferenceManager.getHospitalLatitude()+","+preferenceManager.getHospitalLongitude();
                Log.d("HSS destination:", ""+destination);

                sendRequest();


        }
    }


    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void completePatientRequest(final String authentication_token,
                                 final String ambulance_user_id,
                                 final String request_id) {

        showpDialog();

        String tag_json_obj = "req_complete_obj_req";

        String url = Constants.REQUEST_COMPLETE_URL;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Creating JsonObject from response String
                preferenceManager.clearPatientRequest();
                Intent intent = new Intent(HospitalSelectedActivity.this, InformationActivity.class);
                hidepDialog();
                startActivity(intent);
                finish();



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidepDialog();
//                Toast.makeText(LoginActivity.this, ""+error.networkResponse, Toast.LENGTH_SHORT).show();
                Toast.makeText(HospitalSelectedActivity.this, "Request Failed, Please try again.", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parameters = new HashMap<String,String>();
                parameters.put("authentication_token",authentication_token);
                parameters.put("ambulance_user_id",ambulance_user_id);
                parameters.put("request_id",request_id);

                return parameters;
            }

        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj);
    }

    public void checkRequestStatus(final String authentication_token, final String ambulance_user_id) {

        String tag_json_obj = "patien_request_status_req";

        preferenceManager = new PreferenceManager(getApplicationContext());
        db = new SqliteHandler(getApplicationContext());

        final String url = Constants.PATIENT_REQUEST_STATUS_URL
                + "?authentication_token=" + authentication_token
                + "&ambulance_user_id=" + ambulance_user_id;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("HSS Token :", "" + authentication_token);
                Log.d("HSS User ID :", "" + ambulance_user_id);
                Log.d("HSS URL ", "" + url);
                Log.d("RequestStatusService", "" + response);

                if (!response.equals("null")) {
                    try {
                        JSONObject jsonObjectMain = new JSONObject(response.toString());
                        JSONObject jsonObjectRequest = jsonObjectMain.getJSONObject("request");
                        JSONObject jsonObjectHospital = jsonObjectMain.getJSONObject("hospital");
                        //JSONArray jsonArray = jsonObject.getJSONArray("arrname");
                        Integer id = jsonObjectRequest.getInt("id");
                        Integer hospital_id = jsonObjectRequest.getInt("hospital_id");
                        Integer user_id = jsonObjectRequest.getInt("ambulance_user_id");
                        String requests_type = jsonObjectRequest.getString("requests_type");
                        String blood_pressure = jsonObjectRequest.getString("blood_pressure");
                        String temperature = jsonObjectRequest.getString("temperature");
                        String breathing = jsonObjectRequest.getString("breathing");
                        String pulse_rate = jsonObjectRequest.getString("pulse_rate");

                        Boolean b_accepted = jsonObjectRequest.getBoolean("accepted");
                        Boolean completed = jsonObjectRequest.getBoolean("completed");
                        Integer bed_id = jsonObjectRequest.getInt("bed_id");
                        PatientRequest pr = new PatientRequest(id, hospital_id, user_id,
                                requests_type, temperature, blood_pressure, breathing, pulse_rate);
                        pr.setAccepted(b_accepted);
                        pr.setCompleted(completed);
                        pr.setBed_id(bed_id);

                        if (pr.getAccepted() == false)
                        {
                            checkRequestStatus(authentication_token, ambulance_user_id);}
                        else {
                            db.setRequestAccepted(pr.getId(), pr.getBed_id(), pr.getHospital_id());
                            preferenceManager.addReqID(pr.getId());
                            preferenceManager.setRequestAccepted(true);
                            double hospital_latitude = jsonObjectHospital.getDouble("latitude");
                            double hospital_longitude = jsonObjectHospital.getDouble("longitude");
                            String hospital_name = jsonObjectHospital.getString("name");
                            Log.d("HSS longitude:", "" + hospital_longitude);
                            Log.d("HSS latitude :", "" + hospital_latitude);
                            preferenceManager.setHospitalLatitude(hospital_latitude);
                            preferenceManager.setHospitalLongitude(hospital_longitude);
                            preferenceManager.setHospitalname(hospital_name);
                            requestAccepted();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    checkRequestStatus(authentication_token, ambulance_user_id);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                checkRequestStatus(authentication_token, ambulance_user_id);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
//                parameters.put("authentication_token",authentication_token);
//                parameters.put("ambulance_user_id",ambulance_user_id);

                return parameters;
            }

        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
//        mMap.setTrafficEnabled(true);

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney, 18));


    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

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
                sendRequest();
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

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        // Displaying the new location on UI
        displayLocation();
    }

//    private void togglePeriodicLocationUpdates(Boolean toggle) {
//        if (toggle) {
//
//            // Starting the location updates
//            startLocationUpdates();
//
//            Log.d(TAG, "Periodic location updates started!");
//
//        } else {
//
//            // Stopping the location updates
//            stopLocationUpdates();
//
//            Log.d(TAG, "Periodic location updates stopped!");
//        }
//    }
    //    private void displayResultsOnMap() {
//        if(mMap != null){
//        LatLng origin = new LatLng(mLatitude, mLongitude);
//        mMap.addMarker(new MarkerOptions().position(origin).title("Origin"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
//        }
//    }



    private void displayLocation() {

        try {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }catch (SecurityException e) {
//            showSettingsAlert();
        }

        if (mLastLocation != null) {
            mLatitude = mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
            if(isLocationEnabled(mContext)){
                sendRequest();
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



    private void sendRequest() {
        Log.d("sendRequest","in");
        String origin = mLatitude+","+mLongitude;
        String destination = preferenceManager.getHospitalLatitude()+","+preferenceManager.getHospitalLongitude();

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
//        progressDialog = ProgressDialog.show(this, "Please wait.",
//                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
//        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_white_48dp))
                    .title(preferenceManager.getName())
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_white_48dp))
                    .title(preferenceManager.getHospitalName())
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.RED).
                    width(9);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

}

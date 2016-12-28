/*******************************************************************************
 * Copyright (c) 2016. Golemtron.com
 * Designed and Developed by Taha Rushain
 ******************************************************************************/

package com.golemtron.lifesaver.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by trushain on 8/13/16.
 */
public class PreferenceManager {
    private String TAG = PreferenceManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "lifesaver_ambulance_driver";

    // All Shared Preferences Keys
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_AUTH_TOKEN = "authorization_token";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_BELONGS_TO = "belongs_to";
    private static final String KEY_EMAIL = "email";

    private static final String KEY_REQ_ID = "req_id";
    private static final String KEY_TYPE = "req_type";
    private static final String KEY_BLOOD = "req_blood";
    private static final String KEY_TEMPERATURE = "req_temperature";
    private static final String KEY_BREATHING = "req_breathing";
    private static final String KEY_PULSE = "req_pulse";

    private static final String KEY_ACCEPTED = "req_accepted";
    private static final String KEY_COMPLETED = "req_completed";
    private static final String KEY_REQUEST_SENT = "req_sent";

    private static final String KEY_HOSPITAL_LONGITUDE = "hospital_longitude";
    private static final String KEY_HOSPITAL_LATITUDE = "hospital_latitude";
    private static final String KEY_HOSPITAL_NAME = "hospital_name";


    // Constructor
    public PreferenceManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void addReqID(Integer id){
        editor.putInt(KEY_REQ_ID, id);
        editor.commit();
    }

    public Boolean ifRequestExists(){
        return pref.contains(KEY_TYPE);
    }

    public void clearPatientRequest(){
        editor.remove(KEY_TYPE);
        editor.remove(KEY_BLOOD);
        editor.remove(KEY_BREATHING);
        editor.remove(KEY_PULSE);
        editor.remove(KEY_TEMPERATURE);
        editor.remove(KEY_REQ_ID);
        editor.remove(KEY_ACCEPTED);
        editor.remove(KEY_COMPLETED);
        editor.remove(KEY_REQUEST_SENT);
        editor.remove(KEY_HOSPITAL_LONGITUDE);
        editor.remove(KEY_HOSPITAL_LATITUDE);
        editor.remove(KEY_HOSPITAL_NAME);

        editor.apply();
    }
    public String getPatientType(){return pref.getString(KEY_TYPE,"");}
    public String getPatientBlood(){return pref.getString(KEY_BLOOD,"");}
    public String getPatientBreathing(){return pref.getString(KEY_BREATHING,"");}
    public String getPatientPulse(){return pref.getString(KEY_PULSE,"");}
    public String getPatientTemperature(){return pref.getString(KEY_TEMPERATURE,"");}
    public Boolean getRequestSent(){return pref.getBoolean(KEY_REQUEST_SENT,false);}

    public void addRequestSent (Boolean req) {

        editor.putBoolean(KEY_REQUEST_SENT, req);
        editor.commit();
    }

    public void setRequestAccepted (Boolean bool) {

        editor.putBoolean(KEY_ACCEPTED, bool);
        editor.commit();
    }
    public void setRequestCompleted (Boolean bool) {

        editor.putBoolean(KEY_COMPLETED, bool);
        editor.commit();
    }

    public void addPatientPulse (String pulse) {

        editor.putString(KEY_PULSE, pulse);
        editor.commit();
    }
    public void addPatientBreathing (String breath) {

        editor.putString(KEY_BREATHING, breath);
        editor.commit();
    }
    public void addPatientTemperature (String temp) {

        editor.putString(KEY_TEMPERATURE, temp);
        editor.commit();
    }
    public void addPatientType (String type) {

        editor.putString(KEY_TYPE, type);
        editor.commit();
    }

    public void addPatientBlood (String blood) {

        editor.putString(KEY_BLOOD, blood);
        editor.commit();
    }

    public void addAuthorizationToken (String auth) {

        editor.putString(KEY_AUTH_TOKEN, auth);
        editor.commit();
    }
    public void addId (int id) {

        editor.putInt(KEY_ID, id);
        editor.commit();
    }
    public void addName (String name) {

        editor.putString(KEY_NAME, name);
        editor.commit();
    }
    public void addBelongsTo (String belongs_to) {

        editor.putString(KEY_BELONGS_TO, belongs_to);
        editor.commit();
    }
    public void addEmail (String email) {

        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
    public int getId(){ return pref.getInt(KEY_ID, -1); }
    public String getName(){
        return pref.getString(KEY_NAME, "");
    }
    public String getEmail(){
        return pref.getString(KEY_EMAIL, "");
    }
    public String getBelongsTo(){
        return pref.getString(KEY_BELONGS_TO, "");
    }
    public String getAuthorizationToken(){return pref.getString(KEY_AUTH_TOKEN,"");}
    public boolean getRequestAccepted(){
        return pref.getBoolean(KEY_ACCEPTED, false);
    }
    public boolean getRequestCompleted(){
        return pref.getBoolean(KEY_COMPLETED, false);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }


    public void setHospitalLongitude(double hospitalLongitude) {
        editor.putFloat(KEY_HOSPITAL_LONGITUDE, (float) hospitalLongitude);
        editor.commit();
    }

    public void setHospitalLatitude(double hospitalLatitude) {
        editor.putFloat(KEY_HOSPITAL_LATITUDE, (float) hospitalLatitude);
        editor.commit();
    }

    public void setHospitalname(String name) {
        editor.putString(KEY_HOSPITAL_NAME, (String) name);
        editor.commit();
    }

    public String getHospitalName() {
        return pref.getString(KEY_HOSPITAL_NAME,"");
    }

    public float getHospitalLongitude() {
        return pref.getFloat(KEY_HOSPITAL_LONGITUDE, 0);
    }

    public float getHospitalLatitude() {
        return pref.getFloat(KEY_HOSPITAL_LATITUDE, 0);
    }

    public int getRequestId(){return pref.getInt(KEY_REQ_ID,-1);}
}

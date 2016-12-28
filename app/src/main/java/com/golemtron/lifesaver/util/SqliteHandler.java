/*******************************************************************************
 * Copyright (c) 2016. Golemtron.com
 * Designed and Developed by Taha Rushain
 ******************************************************************************/

package com.golemtron.lifesaver.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.golemtron.lifesaver.R;
import com.golemtron.lifesaver.model.PatientRequest;

import java.util.HashMap;

/**
 * Created by trushain on 8/13/16.
 */
public class SqliteHandler extends SQLiteOpenHelper {

    private static final String TAG = SqliteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 5;

    // Database Name
    private static final String DATABASE_NAME = AppController.getContext().getString(R.string.app_name);

    // Login table name
    private static final String TABLE_AMB_USER = Constants.TABLE_AMB_USER;

    // Login Table Columns names
    private static final String KEY_ID = Constants.KEY_ID;
    private static final String KEY_NAME = Constants.KEY_NAME;
    private static final String KEY_EMAIL = Constants.KEY_EMAIL;
    private static final String KEY_BELONGS_TO= Constants.KEY_BELONGS_TO;
    private static final String KEY_AUTH_TOKEN = Constants.KEY_AUTH_TOKEN;


    private static final String TABLE_REQ = "requests";
    // Request Table Columns names
    private static final String KEY_REQ_ID = "req_id";
    private static final String KEY_HOSPITAL_ID = "hospital_id";
    private static final String KEY_AMBULANCE_ID = "ambulance_user_id";
    private static final String KEY_ACCEPTED = "accepted";
    private static final String KEY_COMPLETED = "completed";
    private static final String KEY_BED_ID = "bed_id";
    private static final String KEY_TYPE = "req_type";
    private static final String KEY_BLOOD = "req_blood";
    private static final String KEY_TEMPERATURE = "req_temperature";
    private static final String KEY_BREATHING = "req_breathing";
    private static final String KEY_PULSE = "req_pulse";


    public SqliteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_AMB_USER + "("
                + KEY_ID + " INTEGER UNIQUE,"
                + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_BELONGS_TO + " TEXT,"
                + KEY_AUTH_TOKEN + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_PATIENT_REQUEST_TABLE = "CREATE TABLE " + TABLE_REQ + "("
                + KEY_REQ_ID + " INTEGER,"
                + KEY_HOSPITAL_ID + " INTEGER,"
                + KEY_AMBULANCE_ID + " INTEGER,"
                + KEY_ACCEPTED + " BOOLEAN,"
                + KEY_COMPLETED + " BOOLEAN,"
                + KEY_BED_ID + " INTEGER,"
                + KEY_TYPE + " TEXT,"
                + KEY_BLOOD + " TEXT,"
                + KEY_TEMPERATURE + " TEXT,"
                + KEY_BREATHING + " TEXT,"
                + KEY_PULSE + " TEXT"
                + ")";
        db.execSQL(CREATE_PATIENT_REQUEST_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AMB_USER);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(int _id, String name, String email, String belongs_to, String auth_token) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, _id);
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_BELONGS_TO, belongs_to);
        values.put(KEY_AUTH_TOKEN, auth_token);

        // Inserting Row
        long id = db.insert(TABLE_AMB_USER, null, values);
        Log.d("SqliteHandler","=================================");
        Log.d("SqliteHandler",""+_id);
        Log.d("SqliteHandler",""+name);
        Log.d("SqliteHandler",""+email);
        Log.d("SqliteHandler",""+belongs_to);
        Log.d("SqliteHandler",""+auth_token);

        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getAmbulanceUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_AMB_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put(KEY_ID, cursor.getString(0));
            user.put(KEY_NAME, cursor.getString(1));
            user.put(KEY_EMAIL, cursor.getString(2));
            user.put(KEY_BELONGS_TO, cursor.getString(3));
            user.put(KEY_AUTH_TOKEN, cursor.getString(4));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());
//        Log.d(TAG, "Fetching user from Sqlite: " + user.get(KEY_ID));
//        Log.d(TAG, "Fetching user from Sqlite: " + user.get(KEY_NAME));
//        Log.d(TAG, "Fetching user from Sqlite: " + user.get(KEY_EMAIL));
//        Log.d(TAG, "Fetching user from Sqlite: " + user.get(KEY_BELONGS_TO));
//        Log.d(TAG, "Fetching user from Sqlite: " + user.get(KEY_AUTH_TOKEN));

        return user;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_AMB_USER, null, null);
        db.close();

        Log.d(TAG, "Cleared all user info from sqlite");
    }

    public void deleteRequests() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_REQ, null, null);
        db.close();

        Log.d(TAG, "Cleared all request info from sqlite");
    }

    public void addPatientRequest(PatientRequest pr) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_REQ_ID, pr.getId());
        values.put(KEY_HOSPITAL_ID, pr.getHospital_id()); // Name
        values.put(KEY_AMBULANCE_ID, pr.getAmbulance_user_id()); // Email
        values.put(KEY_ACCEPTED, pr.getAccepted());
        values.put(KEY_COMPLETED, pr.getCompleted());
        values.put(KEY_BED_ID, pr.getBed_id());
        values.put(KEY_TYPE, pr.getRequest_type());
        values.put(KEY_BLOOD, pr.getBlood_pressure());
        values.put(KEY_TEMPERATURE, pr.getTemperature());
        values.put(KEY_BREATHING, pr.getBreathing());
        values.put(KEY_PULSE, pr.getPulse_rate());

        // Inserting Row
        long id = db.insert(TABLE_REQ, null, values);

        db.close(); // Closing database connection

    }

    public void setRequestAccepted(int request_id, int bed_id, int hospital_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ACCEPTED, true);
        values.put(KEY_BED_ID, bed_id);
        String[] args = new String[]{hospital_id+""};
        db.update(TABLE_REQ, values, "hospital_id=?", args);
        db.close();
        removeUnacceptedRequests();
    }

    private void removeUnacceptedRequests() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REQ, "accepted = ?", new String[] { false+"" });
        db.close();
    }
}
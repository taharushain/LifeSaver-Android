/*******************************************************************************
 * Copyright (c) 2016. Golemtron.com
 * Designed and Developed by Taha Rushain
 ******************************************************************************/

package com.golemtron.lifesaver.util;

/**
 * Created by trushain on 8/13/16.
 */
public class Constants {

//    private static final String url = "https://lifesaver-taharushain.c9users.io/";
    private static final String url ="http://54.191.38.65/";
    public static final String TABLE_AMB_USER = "ambulance_user";
    public static final String KEY_ID = "amb_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_BELONGS_TO= "belongs_to";
    public static final String KEY_AUTH_TOKEN = "authentication_token";
    public static final String LOGIN_URL = url+"api/v1/sessions";
    public static final String HOSPITALS_URL = url+"api/v1/hospitals";
    public static final String PATIENT_REQUEST_URL = url+"api/v1/requests";
    public static final String PATIENT_REQUEST_STATUS_URL = url+"api/v1/request_status";
    public static final String REQUEST_COMPLETE_URL = url+"api/v1/request_complete";

    public static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    public static final String GOOGLE_API_KEY = "AIzaSyAEWMVUk-yS-gd5GbG50p8CpLsHmrbVHEU";

}

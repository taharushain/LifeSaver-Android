package com.golemtron.lifesaver.model;

/**
 * Created by taharushain on 11/27/16.
 */

public class PatientRequest {
    private int id;
    private int hospital_id;
    private int ambulance_user_id;
    private Boolean accepted = false;
    private Boolean completed = false;
    private int bed_id;
    private String request_type;
    private String blood_pressure;
    private String pulse_rate;
    private String temperature;
    private String breathing;

    public PatientRequest(int id, int hospital_id, int ambulance_user_id, String request_type, String temperature, String blood_pressure, String breathing,String pulse_rate ) {
        this.id = id;
        this.hospital_id = hospital_id;
        this.ambulance_user_id = ambulance_user_id;
        this.request_type = request_type;
        this.blood_pressure = blood_pressure;
        this.pulse_rate = pulse_rate;
        this.temperature = temperature;
        this.breathing = breathing;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHospital_id() {
        return hospital_id;
    }

    public void setHospital_id(int hospital_id) {
        this.hospital_id = hospital_id;
    }

    public int getAmbulance_user_id() {
        return ambulance_user_id;
    }

    public void setAmbulance_user_id(int ambulance_user_id) {
        this.ambulance_user_id = ambulance_user_id;
    }



    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public String getBlood_pressure() {
        return blood_pressure;
    }

    public void setBlood_pressure(String blood_pressure) {
        this.blood_pressure = blood_pressure;
    }

    public String getPulse_rate() {
        return pulse_rate;
    }

    public void setPulse_rate(String pulse_rate) {
        this.pulse_rate = pulse_rate;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getBreathing() {
        return breathing;
    }

    public void setBreathing(String breathing) {
        this.breathing = breathing;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public int getBed_id() {
        return bed_id;
    }

    public void setBed_id(int bed_id) {
        this.bed_id = bed_id;
    }
}

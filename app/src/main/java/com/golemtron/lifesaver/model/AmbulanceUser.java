/*******************************************************************************
 * Copyright (c) 2016. Golemtron.com
 * Designed and Developed by Taha Rushain
 ******************************************************************************/

package com.golemtron.lifesaver.model;


/**
 * Created by trushain on 9/18/16.
 */
public class AmbulanceUser {

    private Integer id;
    private String email;
    private String name;
    private String belongs_to;
    private String authentication_token;

    public AmbulanceUser(Integer id, String email, String name, String belongs_to, String authentication_token) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.belongs_to = belongs_to;
        this.authentication_token = authentication_token;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String isName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBelongs_to() {
        return belongs_to;
    }

    public String getName() {
        return name;
    }

    public void setBelongs_to(String belongs_to) {
        this.belongs_to = belongs_to;
    }

    public String getAuthentication_token() {
        return authentication_token;
    }

    public void setAuthentication_token(String authentication_token) {
        this.authentication_token = authentication_token;
    }
}

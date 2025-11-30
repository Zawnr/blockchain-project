package com.example.tapetrove.Activity;

import com.google.gson.annotations.SerializedName;

public class SnapTokenResponse {
    @SerializedName("token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

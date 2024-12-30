package com.secke.user_service.Model;

public class AuthResponse {
    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }
    // getter

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
package com.simple.crud.demo.model.dto;

public class AuthResponseDto {
    private String accessToken;
    private UserResponseDto user;

    public AuthResponseDto() {}

    public AuthResponseDto(String accessToken, UserResponseDto user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public UserResponseDto getUser() {
        return user;
    }

    public void setUser(UserResponseDto user) {
        this.user = user;
    }
}

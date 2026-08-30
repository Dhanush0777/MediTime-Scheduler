package com.medtime.dto;

import com.medtime.entity.Role;

public class AuthResponse {

    private Long userId;
    private String name;
    private String email;
    private Role role;
    private Long profileId; // doctorId or patientId
    private String token;

    public AuthResponse() {
    }

    public AuthResponse(Long userId, String name, String email, Role role, Long profileId, String token) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.profileId = profileId;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

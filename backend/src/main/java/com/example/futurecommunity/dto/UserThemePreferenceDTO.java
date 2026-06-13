package com.example.futurecommunity.dto;

/**
 * 用户主题偏好DTO
 */
public class UserThemePreferenceDTO {
    private String mode;
    private String color;

    public UserThemePreferenceDTO() {
    }

    public UserThemePreferenceDTO(String mode, String color) {
        this.mode = mode;
        this.color = color;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

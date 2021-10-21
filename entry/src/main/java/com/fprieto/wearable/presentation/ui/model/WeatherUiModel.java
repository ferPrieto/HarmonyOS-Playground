package com.fprieto.wearable.presentation.ui.model;

public class WeatherUiModel {
    private String city, status;
    private WeatherType weatherType;

    public WeatherUiModel(String city, String status, WeatherType weatherType) {
        this.city = city;
        this.status = status;
        this.weatherType = weatherType;
    }

    public WeatherType getWeatherType() {
        return weatherType;
    }

    public void setWeatherType(WeatherType weatherType) {
        this.weatherType = weatherType;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

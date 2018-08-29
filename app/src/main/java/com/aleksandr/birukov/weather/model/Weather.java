package com.aleksandr.birukov.weather.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

// Используется для запросов с помощью Retrofit
// описывает погоду в конкретной точке времени
public class Weather  {

    public Weather(double temp, double humidity, String icon, long time) {
        weatherTemp = new WeatherValue();
        weatherTemp.temp = temp;
        weatherTemp.humidity = humidity;
        weatherIcon = new ArrayList<>();
        weatherIcon.add(new WeatherIcon());
        weatherIcon.get(0).icon = icon;
        this.time = time;
    }

    private class WeatherValue {
        double temp;
        double humidity;
    }

    private class WeatherIcon {
        String icon;
    }

    @SerializedName("main")
    private WeatherValue weatherTemp;

    @SerializedName("weather")
    private List<WeatherIcon> weatherIcon;

    @SerializedName("dt")
    private long time;

    public Calendar getDate() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time * 1000);
        return date;
    }

    public String getTemperatureStr() {
        return String.valueOf(weatherTemp.temp) + "\u00B0";
    }

    public String getHumidityStr() {
        return String.valueOf(weatherTemp.humidity) + "%";
    }

    public String getIconUrl() {
        return "http://openweathermap.org/img/w/" + weatherIcon.get(0).icon + ".png";
    }
    public double getTemperature(){
        return weatherTemp.temp;
    }
    public double getHumidity(){
        return weatherTemp.humidity;
    }
    public String getIcon(){
        return weatherIcon.get(0).icon;
    }
    public long getTime(){
        return time;
    }


}

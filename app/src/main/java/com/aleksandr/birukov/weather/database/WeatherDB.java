package com.aleksandr.birukov.weather.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

// описывает то, каким образом данные о погоде хранятся в БД
@Entity
public class WeatherDB {
    @PrimaryKey(autoGenerate = true)
    int id;
    double temp;
    double humidity;
    String iconUrl;
    long time;

    public WeatherDB(double temp, double humidity, String iconUrl, long time) {
        this.temp = temp;
        this.humidity = humidity;
        this.iconUrl = iconUrl;
        this.time = time;
    }
}

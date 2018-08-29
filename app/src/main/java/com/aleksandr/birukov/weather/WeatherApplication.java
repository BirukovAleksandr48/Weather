package com.aleksandr.birukov.weather;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.aleksandr.birukov.weather.database.AppDatabase;
import com.aleksandr.birukov.weather.database.WeatherDao;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
// Сюда вынесена логика, которая используется в разных частях программы,
// чтобы избежать дублирования кода
public class WeatherApplication extends Application{

    public static WeatherApplication instance;

    private AppDatabase appdb;
    private WeatherDao database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        appdb = Room.databaseBuilder(this, AppDatabase.class, "database")
                .allowMainThreadQueries()
                .build();
        database = appdb.weatherDao();
    }

    public static WeatherApplication getInstance() {
        return instance;
    }

    public WeatherDao getDatabase() {
        return database;
    }

    //Получение названия города по координатам
    public static String getCityName(Context context, double lat, double lon){
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(lat, lon, 1);
            if (addresses.size() > 0) {
                return addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

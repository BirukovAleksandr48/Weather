package com.aleksandr.birukov.weather.database;

import com.aleksandr.birukov.weather.model.Weather;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// конвертирует объекты Weather -> WeatehrDB и обратно
public class Converter {
    public static List<WeatherDB> convertForDB(List<Weather> weatherlist){
        ArrayList<WeatherDB> result = new ArrayList<>();
        for(Weather w : weatherlist){
            double temp = w.getTemperature();
            double humidity = w.getHumidity();
            String icon = w.getIcon();
            long time = w.getTime();
            result.add(new WeatherDB(temp, humidity, icon, time));
        }
        return result;
    }

    public static List<Weather> convertFromDB(List<WeatherDB> weatherdblist){
        ArrayList<Weather> result = new ArrayList<>();
        for(WeatherDB w : weatherdblist){
            result.add(new Weather(w.temp, w.humidity, w.iconUrl, w.time));
        }
        return result;
    }
}

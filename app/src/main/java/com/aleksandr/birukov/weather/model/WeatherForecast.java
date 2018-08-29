package com.aleksandr.birukov.weather.model;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// Используется для запросов с помощью Retrofit
// по сути достает список объектов Weather
public class WeatherForecast {

    @SerializedName("list")
    private List<Weather> forecast;

    public WeatherForecast(List<Weather> weatherList){
        forecast = weatherList;
    }
    public List<Weather> getForecast() {
        return forecast;
    }

    //Возвращает прогноз на 15:00 каждого дня.
    //Если текущий день прошел отметку 15:00, то на этот день возвращает самый ранний прогноз
    public List<Weather> getWeeklyForcast(){
        List<Weather> temp = new ArrayList<>();
        int today_min_h = forecast.get(0).getDate().get(Calendar.HOUR_OF_DAY);
        if(today_min_h > 15){
            temp.add(forecast.get(0));
        }
        for(Weather weather : forecast){
            if(weather.getDate().get(Calendar.HOUR_OF_DAY) == 15){
                temp.add(weather);
            }
        }
        return temp;
    }

    //Возвращает подробный прогноз на остаток дня
    //shift - сдвиг ( 0 - текущий день, 1 - следующий, 2 - через день.. )
    public List<Weather> getDailyForcast(int shift){
        List<Weather> temp = new ArrayList<>();
        int day_num = forecast.get(shift * 8).getDate().get(Calendar.DAY_OF_MONTH);
        for(Weather weather : forecast){
            if(weather.getDate().get(Calendar.DAY_OF_MONTH) == day_num){
                temp.add(weather);
            }
        }
        return temp;
    }
}

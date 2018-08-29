package com.aleksandr.birukov.weather;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.aleksandr.birukov.weather.model.Weather;
import com.aleksandr.birukov.weather.model.WeatherForecast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Сервис, который отвечает за создание уведомлений
public class WeatherService extends IntentService{
    private static final int INTERVAL = 1000 * 60;
    WeatherApi.ApiInterface api;
    double lat, lon;

    public static Intent newIntent(Context context){
        return new Intent(context, WeatherService.class);
    }

    public WeatherService() {
        super("weather");
    }

    public WeatherService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }
        api = WeatherApi.getInstance().create(WeatherApi.ApiInterface.class);
        updateWeather();
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }

    // включает/выключает таймер вызова сервиса
    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = WeatherService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    // проверяет включен ли таймер вызова сервиса
    public static boolean isServiceAlarmOn(Context context) {
        Intent i = WeatherService.newIntent(context);
        PendingIntent pi = PendingIntent
                .getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    // достает координаты и находит по ним текущую погоду
    public void updateWeather(){
        SharedPreferences mSharPref = PreferenceManager.getDefaultSharedPreferences(this);

        lat = Double.longBitsToDouble(mSharPref.getLong(Constants.SP_KEY_CITY_LAT, 0));
        lon = Double.longBitsToDouble(mSharPref.getLong(Constants.SP_KEY_CITY_LON, 0));

        // если координаты равны 0, то пользователь еще не выбирал город и сервис прекращает работу
        if(lat == lon && lon == 0){
            return;
        }

        Call<Weather> callForecast = api.getToday(lat, lon, WeatherApi.UNITS, WeatherApi.KEY);
        callForecast.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                Weather weather = response.body();
                if (response.isSuccessful()) {
                    sendNotification(weather);
                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {

            }
        });
    }
    public void sendNotification(Weather weather){
        String cityName = WeatherApplication.getCityName(this, lat, lon);
        String temp = weather.getTemperatureStr();
        String humid = weather.getHumidityStr();

        Intent i = new Intent(this, NavigationActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(cityName + ": " + temp + ", " + humid)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(cityName)
                .setContentText(temp + ", " + humid)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notification);
    }

    public static void log(String text){
        Log.e("MyLog", text);
    }
}

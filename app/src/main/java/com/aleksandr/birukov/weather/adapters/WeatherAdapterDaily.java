package com.aleksandr.birukov.weather.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aleksandr.birukov.weather.R;
import com.aleksandr.birukov.weather.model.Weather;
import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

// Адаптер для отображения подробного дневого прогноза погоды
public class WeatherAdapterDaily extends BaseAdapter {
    private final Context mContext;
    private List<Weather> mForecast;
    DateFormat format;

    TextView tvDate, tvTemp, tvHum;
    ImageView ivIcon;

    public WeatherAdapterDaily(Context context, List<Weather> forecast) {
        this.mContext = context;
        this.mForecast = forecast;
        format = new SimpleDateFormat("H:mm");
    }

    @Override
    public int getCount() {
        return mForecast.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_weather_hor, parent, false);
        Weather dayWeather = mForecast.get(position);

        tvDate = view.findViewById(R.id.tv_date);
        tvTemp = view.findViewById(R.id.tv_temp);
        tvHum = view.findViewById(R.id.tv_humidity);
        ivIcon = view.findViewById(R.id.ivIcon);

        tvDate.setText(format.format(dayWeather.getDate().getTime()));
        tvTemp.setText(dayWeather.getTemperatureStr());
        tvHum.setText(dayWeather.getHumidityStr());
        Glide.with(mContext).load(dayWeather.getIconUrl()).into(ivIcon);

        if(position % 2 == 1) {
            tvDate.setBackgroundColor(Color.parseColor("#FF97AFF5"));
            tvTemp.setBackgroundColor(Color.parseColor("#FF97AFF5"));
            tvHum.setBackgroundColor(Color.parseColor("#FF97AFF5"));
            ivIcon.setBackgroundColor(Color.parseColor("#FF97AFF5"));
        }
        return view;
    }
}
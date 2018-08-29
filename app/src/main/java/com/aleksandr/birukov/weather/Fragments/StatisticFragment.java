package com.aleksandr.birukov.weather.Fragments;


import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aleksandr.birukov.weather.Constants;
import com.aleksandr.birukov.weather.R;
import com.aleksandr.birukov.weather.WeatherApplication;
import com.aleksandr.birukov.weather.database.Converter;
import com.aleksandr.birukov.weather.database.WeatherDao;
import com.aleksandr.birukov.weather.model.Weather;
import com.aleksandr.birukov.weather.model.WeatherForecast;
import com.anychart.anychart.AnyChart;
import com.anychart.anychart.AnyChartView;
import com.anychart.anychart.Cartesian;
import com.anychart.anychart.CartesianSeriesLine;
import com.anychart.anychart.DataEntry;
import com.anychart.anychart.Mapping;
import com.anychart.anychart.Set;
import com.anychart.anychart.Stroke;
import com.anychart.anychart.ValueDataEntry;
import com.savvi.rangedatepicker.CalendarPickerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;

// отвечает за построение графика изменения температуры
public class StatisticFragment extends Fragment {
    ImageButton btn;
    TextView tv;
    DateFormat formatGraph, formatTextView;
    public static final int CODE_TIME_RANGE = 1;
    WeatherDao db;
    WeatherForecast mWeatherForecast;
    long start, end;
    AnyChartView anyChartView;
    SharedPreferences.Editor spEditor;
    SharedPreferences sharedPreferences;
    String cityName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_statistic, container, false);

        db = WeatherApplication.getInstance().getDatabase();
        mWeatherForecast = new WeatherForecast(Converter.convertFromDB(db.getAll()));
        formatGraph = new SimpleDateFormat("H:mm d.MM");
        formatTextView = new SimpleDateFormat("d.MM");

        btn = v.findViewById(R.id.btn_date);
        tv = v.findViewById(R.id.tv_selected_date);
        anyChartView = v.findViewById(R.id.any_chart_view);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateRangeFragment fragment = new DateRangeFragment();
                fragment.setTargetFragment(StatisticFragment.this, CODE_TIME_RANGE);
                fragment.show(getFragmentManager(), fragment.getClass().getName());
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        spEditor = sharedPreferences.edit();

        start = sharedPreferences.getLong(Constants.SP_KEY_DATE_START, 0);
        end = sharedPreferences.getLong(Constants.SP_KEY_DATE_END, 0);
        cityName = sharedPreferences.getString(Constants.SP_KEY_CITY_NAME, null);

        if(mWeatherForecast.getForecast().size() > 0 && start > 0 && end > 0 && cityName != null){
             updateUI();
        }
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        start = data.getExtras().getLong(Constants.KEY_DIALOG_RESULT_START);
        end = data.getExtras().getLong(Constants.KEY_DIALOG_RESULT_END);
        if(end > 0 && start > 0){
            spEditor.putLong(Constants.SP_KEY_DATE_START, start);
            spEditor.putLong(Constants.SP_KEY_DATE_END, end);
            spEditor.commit();
        }

        updateUI();
    }

    public void updateUI() {
        if (cityName == null) {
            Toasty.error(getActivity(), "Ошибка. Сначала выберите город",
                    Toast.LENGTH_LONG, true).show();
            return;
        }

        tv.setText(cityName + "  " + formatTextView.format(new Date(start)) +
                " - " + formatTextView.format(new Date(end)));

        List<Weather> data = mWeatherForecast.getForecast();
        Cartesian cartesian = AnyChart.line();

        cartesian.setAnimation(true);
        cartesian.getCrosshair().setEnabled(true);
        cartesian.getCrosshair()
                .setYLabel(true)
                .setYStroke((Stroke) null, null, null, null, null);
        cartesian.getXAxis().getLabels().setPadding(5d, 5d, 20d, 5d);

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(end));
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long temp = cal.getTimeInMillis();

        List<DataEntry> seriesData = new ArrayList<>();
        for (Weather w : data){
            if(w.getTime()*1000 >= start && w.getTime()*1000 < temp) {
                seriesData.add(new ValueDataEntry(formatGraph.format(w.getDate().getTime()), w.getTemperature()));
            }
        }
        Set set = new Set(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
        CartesianSeriesLine series1 = cartesian.line(series1Mapping);
        series1.setName("Температура, \u00B0C");
        cartesian.getLegend().setEnabled(true);
        cartesian.getLegend().setFontSize(20d);
        cartesian.getLegend().setPadding(0d, 0d, 10d, 0d);
        anyChartView.setChart(cartesian);
    }

    public void log(String text){
        Log.e("MyLog", text);
    }

}

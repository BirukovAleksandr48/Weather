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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aleksandr.birukov.weather.Constants;
import com.aleksandr.birukov.weather.R;
import com.aleksandr.birukov.weather.WeatherApi;
import com.aleksandr.birukov.weather.WeatherApplication;
import com.aleksandr.birukov.weather.adapters.WeatherAdapterDaily;
import com.aleksandr.birukov.weather.adapters.WeatherAdapterWeekly;
import com.aleksandr.birukov.weather.database.Converter;
import com.aleksandr.birukov.weather.database.WeatherDB;
import com.aleksandr.birukov.weather.database.WeatherDao;
import com.aleksandr.birukov.weather.model.WeatherForecast;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.text.SimpleDateFormat;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// отвечает за подробное отображение прогноза погоды на 120 часов
public class ForecastFragment extends Fragment {
    WeatherApi.ApiInterface api;
    GridView gvForecastWeek, gvForecastDay;
    ImageButton btnFind;
    TextView tvCurDay;

    double lon=0, lat=0;
    String cityName;
    final int PLACE_PICKER_REQUEST = 1;
    String units = "metric";
    String key = WeatherApi.KEY;
    WeatherForecast mWeatherForecast;
    PlaceAutocompleteFragment autocompleteFragment;
    SharedPreferences.Editor spEditor;
    SharedPreferences sharedPreferences;
    WeatherDao db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_forecast, container, false);

        api = WeatherApi.getInstance().create(WeatherApi.ApiInterface.class);
        db = WeatherApplication.getInstance().getDatabase();

        gvForecastWeek = v.findViewById(R.id.gv_forecast_week);
        gvForecastDay = v.findViewById(R.id.gv_forecast_day);
        btnFind = v.findViewById(R.id.btn_find);
        tvCurDay = v.findViewById(R.id.tv_cur_day);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                updatePlace(place);
                updateWeather();
            }

            @Override
            public void onError(Status status) {
                Log.i("MyLog", "An error occurred: " + status);
            }
        });

        btnFind.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        gvForecastWeek.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tvCurDay.setText(new SimpleDateFormat("EEEE, d.MM").format(mWeatherForecast.getDailyForcast(position).get(0).getDate().getTime()));
                gvForecastDay.setAdapter(new WeatherAdapterDaily(
                        getActivity(), mWeatherForecast.getDailyForcast(position)));
            }
        });

        //пытается достать данные о погоде с БД
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        spEditor = sharedPreferences.edit();

        cityName = sharedPreferences.getString(Constants.SP_KEY_CITY_NAME, null);
        lat = Double.longBitsToDouble(sharedPreferences.getLong(Constants.SP_KEY_CITY_LAT, 0));
        lon = Double.longBitsToDouble(sharedPreferences.getLong(Constants.SP_KEY_CITY_LON, 0));

        if(cityName != null)
            autocompleteFragment.setText(cityName);

        mWeatherForecast = new WeatherForecast(Converter.convertFromDB(db.getAll()));
        if (mWeatherForecast.getForecast().size()>0)
            updateUI();

        return v;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLACE_PICKER_REQUEST:
                if (resultCode != getActivity().RESULT_OK)
                    return;
                Place place = PlacePicker.getPlace(getActivity(), data);
                updatePlace(place);
                updateWeather();
                if (cityName != null){
                    autocompleteFragment.setText(cityName);
                }
        }
    }
    public void updateWeather(){
        Call<WeatherForecast> callForecast = api.getForecast(lat, lon, units, key);
        callForecast.enqueue(new Callback<WeatherForecast>() {
            @Override
            public void onResponse(Call<WeatherForecast> call, Response<WeatherForecast> response) {
                mWeatherForecast = response.body();
                if (response.isSuccessful()) {
                    updateUI();
                    saveToDB();
                }
            }

            @Override
            public void onFailure(Call<WeatherForecast> call, Throwable t) {

            }
        });
    }

    public void updateUI(){
        tvCurDay.setText(new SimpleDateFormat("EEEE, d.MM").
                format(mWeatherForecast.getDailyForcast(0).get(0).getDate().getTime()));
        gvForecastWeek.setAdapter(new WeatherAdapterWeekly(getActivity(),
                mWeatherForecast.getWeeklyForcast()));
        gvForecastDay.setAdapter(new WeatherAdapterDaily(
                getActivity(), mWeatherForecast.getDailyForcast(0)));
    }
    public void saveToDB(){
        List<WeatherDB> weatherDBlist = Converter.convertForDB(mWeatherForecast.getForecast());

        db.nukeTable();
        for (WeatherDB w : weatherDBlist){
            db.insert(w);
        }

        log(String.valueOf(lat));
        log(String.valueOf(lon));

        spEditor.putString(Constants.SP_KEY_CITY_NAME, cityName)
                .putLong(Constants.SP_KEY_CITY_LAT, Double.doubleToRawLongBits(lat))
                .putLong(Constants.SP_KEY_CITY_LON, Double.doubleToRawLongBits(lon))
                .commit();
    }

    public void updatePlace(Place place){
        lat = place.getLatLng().latitude;
        lon = place.getLatLng().longitude;
        cityName = WeatherApplication.getCityName(getActivity(), lat, lon);
    }

    public void log(String text){
        Log.e("MyLog", text);
    }
}

package com.fprieto.wearable.presentation.vm;

import com.fprieto.wearable.MyApplication;
import com.fprieto.wearable.presentation.ui.slice.weather.WeatherViewState;
import com.fprieto.wearable.presentation.vm.base.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class WeatherViewModel extends BaseViewModel<WeatherViewState> {

    private static final String API_KEY = "606bc3ea79005f4573fb6da49df63f0a";

    public void getWeatherByLocation(int latitude, int longitude) {
        final Observable<WeatherViewState> obs = MyApplication.getApiService()
                .getWeather(latitude, longitude, API_KEY)
                .map(WeatherViewState.Loaded::new)
                .cast(WeatherViewState.class)
                .onErrorReturn(WeatherViewState.ErrorState::new)
                .subscribeOn(Schedulers.io())
                .startWith(new WeatherViewState.Loading());

        super.subscribe(obs);
    }
}

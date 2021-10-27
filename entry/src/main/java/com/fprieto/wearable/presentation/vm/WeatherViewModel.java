package com.fprieto.wearable.presentation.vm;

import com.fprieto.wearable.presentation.ui.slice.weather.WeatherViewState;
import com.fprieto.wearable.presentation.vm.base.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class WeatherViewModel extends BaseViewModel<WeatherViewState> {
    public static WeatherRepository weatherRepository;

    public WeatherViewModel(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    public void getWeatherByLocation(int latitude, int longitude) {
        Observable<WeatherViewState> observable = weatherRepository
                .getWeatherByLocation(latitude, longitude)
                .doOnSubscribe(disposable1 -> new WeatherViewState.Loading())
                .map(WeatherViewState.Loaded::new)
                .cast(WeatherViewState.class)
                .onErrorReturn(WeatherViewState.ErrorState::new)
                .subscribeOn(Schedulers.io())
                .toObservable()
                .startWith(new WeatherViewState.Loading());
        subscribe(observable);
    }
}

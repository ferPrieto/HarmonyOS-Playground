package com.fprieto.wearable.presentation.ui.slice.weather;

import com.fprieto.wearable.model.WeatherResponse;
import com.fprieto.wearable.presentation.ui.base.BaseViewState;

import java.util.List;

public abstract class WeatherViewState extends BaseViewState {
    public static class Loading extends WeatherViewState {

    }

    public static class ErrorState extends WeatherViewState {
        private final Throwable throwable;

        public ErrorState(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }

    public static class Loaded extends WeatherViewState {
        private final WeatherResponse weatherResponse;

        public Loaded(WeatherResponse weatherResponse) {
            this.weatherResponse = weatherResponse;
        }

        public WeatherResponse getWeatherResponse() {
            return weatherResponse;
        }
    }
}

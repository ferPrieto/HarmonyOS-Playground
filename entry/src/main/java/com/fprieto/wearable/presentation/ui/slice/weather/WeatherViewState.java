package com.fprieto.wearable.presentation.ui.slice.weather;

import com.fprieto.wearable.data.model.WeatherResponse;
import com.fprieto.wearable.presentation.ui.base.BaseViewState;
import com.fprieto.wearable.presentation.ui.model.WeatherUiModel;

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
        private WeatherMapperImpl mapper = new WeatherMapperImpl();

        public Loaded(WeatherResponse weatherResponse) {
            this.weatherResponse = weatherResponse;
        }
        public WeatherUiModel getWeatherUiModel() {
            return mapper.toWeatherUi(weatherResponse);
        }
    }
}

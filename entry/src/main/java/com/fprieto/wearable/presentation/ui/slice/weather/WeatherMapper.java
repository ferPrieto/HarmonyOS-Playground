package com.fprieto.wearable.presentation.ui.slice.weather;

import com.fprieto.wearable.data.model.WeatherResponse;
import com.fprieto.wearable.presentation.ui.model.WeatherUiModel;

public interface WeatherMapper {
    public WeatherUiModel toWeatherUi(WeatherResponse weatherResponse);
}

class WeatherMapperImpl implements WeatherMapper {
    @Override
    public WeatherUiModel toWeatherUi(WeatherResponse weatherResponse) {
        WeatherIconTransformerImpl weatherIconTransformer = new WeatherIconTransformerImpl();
        return new WeatherUiModel(weatherResponse.getName(), weatherResponse.getWeather()[0].getDescription(), weatherIconTransformer.toWeatherType(weatherResponse.getWeather()[0].getIcon()));
    }
}

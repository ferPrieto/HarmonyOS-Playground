package com.fprieto.wearable.presentation.ui.slice.weather;

import com.fprieto.wearable.presentation.ui.model.WeatherType;

import static com.fprieto.wearable.presentation.ui.model.WeatherType.*;

public interface WeatherIconTransformer {
    public WeatherType toWeatherType(String weatherIcon);
}

class WeatherIconTransformerImpl implements WeatherIconTransformer {
    @Override
    public WeatherType toWeatherType(String weatherIcon) {
        WeatherType weatherType = SUNNY;
        switch (weatherIcon) {
            case "01d":
                weatherType = SUNNY;
                break;
            case "02d":
                weatherType = PARTIALLY_SUNNY;
                break;
            case "03d":
                weatherType = CLOUDY_LIGHT;
                break;
            case "04d":
                weatherType = CLOUDY_DARK;
                break;
            case "09d":
                weatherType = RAINY;
                break;
            case "10d":
                weatherType = RAIN_WITH_SUN;
                break;
            case "11d":
                weatherType = STORMY;
                break;
            case "13d":
                weatherType = SNOWY;
                break;
        }
        return weatherType;
    }
}
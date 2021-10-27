package com.fprieto.wearable.presentation.di;

import com.fprieto.wearable.presentation.vm.WeatherRepository;

public final class Injection {

    private static Injection INSTANCE;

    private Injection(){}

    public static Injection getInstance(){
        if(INSTANCE==null){
            INSTANCE = new Injection();
        }
        return INSTANCE;
    }

    public WeatherRepository provideWeatherRepository(){
        return new WeatherRepository();
    }
}

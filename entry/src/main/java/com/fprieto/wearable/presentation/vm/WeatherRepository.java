package com.fprieto.wearable.presentation.vm;

import com.fprieto.wearable.data.ApiService;
import com.fprieto.wearable.data.model.WeatherResponse;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

import java.util.concurrent.TimeUnit;

public class WeatherRepository {
    private static final String API_KEY = "606bc3ea79005f4573fb6da49df63f0a";
    private static ApiService apiService;

    public WeatherRepository() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60L, TimeUnit.SECONDS)
                .build();
        apiService = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create()).client(client)
                .build()
                .create(ApiService.class);
    }

    public Single<WeatherResponse> getWeatherByLocation(int latitude, int longitude) {
        return apiService.getWeather(latitude, longitude, API_KEY);
    }
}

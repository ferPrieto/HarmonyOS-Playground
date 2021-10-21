package com.fprieto.wearable.data;

import com.fprieto.wearable.data.model.WeatherResponse;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("weather/")
    Observable<WeatherResponse> getWeather(@Query("lat") double latitude, @Query("lon") double longitude,
                                           @Query("appid") String apiKey);
}

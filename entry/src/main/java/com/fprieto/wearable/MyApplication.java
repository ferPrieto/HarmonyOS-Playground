package com.fprieto.wearable;

import com.fprieto.wearable.data.ApiService;
import ohos.aafwk.ability.AbilityPackage;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

import java.util.concurrent.TimeUnit;

public class MyApplication extends AbilityPackage {
    private static ApiService apiService = null;

    @Override
    public void onInitialize() {
        super.onInitialize();
    }

    public synchronized static ApiService getApiService() {
        if(apiService == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60L, TimeUnit.SECONDS)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.openweathermap.org/data/2.5/")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(MoshiConverterFactory.create()).client(client)
                    .build();
            apiService = retrofit.create(ApiService.class);
        }

        return apiService;
    }
}

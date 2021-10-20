package com.fprieto.wearable.presentation.ui.slice.weather;

import com.fprieto.wearable.P2PAbilitySlice;
import com.fprieto.wearable.ResourceTable;
import com.fprieto.wearable.model.WeatherResponse;
import com.fprieto.wearable.presentation.ui.UiObserver;
import com.fprieto.wearable.presentation.vm.WeatherViewModel;
import com.fprieto.wearable.util.LogUtils;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.RoundProgressBar;
import ohos.agp.components.Text;
import ohos.location.Location;
import ohos.location.Locator;
import ohos.location.LocatorCallback;
import ohos.location.RequestParam;

public class WeatherAbilitySlice extends P2PAbilitySlice {

    private static final String TAG = "WeatherAbilitySlice";

    private Text weatherStatusTextView;
    private WeatherViewModel weatherViewModel;
    private RoundProgressBar progressBar;

    private String logger = "Logs \n";
    private Locator locator;
    private MyLocatorCallback locatorCallback;

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        setUIContent(ResourceTable.Layout_ability_joke);

        weatherViewModel = new WeatherViewModel();
        locator = new Locator(this);
        locatorCallback = new MyLocatorCallback();

        progressBar = (RoundProgressBar) findComponentById(ResourceTable.Id_round_progress_bar);
        weatherStatusTextView = (Text) findComponentById(ResourceTable.Id_weather_status_text);

        weatherViewModel.getStates().addObserver(new UiObserver<WeatherViewState>(this) {
            @Override
            public void onValueChanged(WeatherViewState weatherViewState) {
                if (weatherViewState instanceof WeatherViewState.Loading) {
                    onLoading((WeatherViewState.Loading) weatherViewState);
                } else if (weatherViewState instanceof WeatherViewState.ErrorState) {
                    onError((WeatherViewState.ErrorState) weatherViewState);
                } else if (weatherViewState instanceof WeatherViewState.Loaded) {
                    onLoaded((WeatherViewState.Loaded) weatherViewState);
                }
            }
        }, false);
    }

    private void onLoading(final WeatherViewState.Loading loadingState) {
        progressBar.setVisibility(Component.VISIBLE);
        weatherStatusTextView.setVisibility(Component.HIDE);
    }

    private void onLoaded(final WeatherViewState.Loaded loadedState) {
        progressBar.setVisibility(Component.HIDE);
        weatherStatusTextView.setVisibility(Component.VISIBLE);

        final WeatherResponse weatherResponse = loadedState.getWeatherResponse();
        if (weatherResponse != null) {
            weatherStatusTextView.setText(weatherResponse.getWeather()[0].getDescription());
        } else {
            weatherStatusTextView.setText("Weather info unavailable...");
        }
    }

    private void onError(final WeatherViewState.ErrorState errorState) {
        progressBar.setVisibility(Component.HIDE);
        weatherStatusTextView.setVisibility(Component.VISIBLE);

        final Throwable throwable = errorState.getThrowable();
        weatherStatusTextView.setText("Failed to load joke... :(");
        LogUtils.e(TAG, throwable.getMessage(), throwable);
    }

    private void startLocating() {
        RequestParam requestParam = new RequestParam(RequestParam.PRIORITY_FAST_FIRST_FIX, 0, 0);
        // PRIORITY_ACCURACY / PRIORITY_FAST_FIRST_FIX / PRIORITY_LOW_POWER
        locator.startLocating(requestParam, locatorCallback);
    }

    @Override
    public void onActive() {
        super.onActive();
        startLocating();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        locator.stopLocating(locatorCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        weatherViewModel.unbind();
    }

    public class MyLocatorCallback implements LocatorCallback {

        @Override
        public void onLocationReport(Location location) {
            logger += "Lat: " + location.getLatitude() + ", Long: " + location.getLongitude() + "\n";
            LogUtils.i(TAG, "Lat: " + location.getLatitude() + ", Long: " + location.getLongitude());
            getUITaskDispatcher().syncDispatch(() -> {
                weatherViewModel.getWeatherByLocation((int)location.getLatitude(), (int)location.getLongitude());
            });
        }

        @Override
        public void onStatusChanged(int type) {
            logger += "status changed: " + type + "\n";
            LogUtils.i(TAG, "status changed: " + type);
        }

        @Override
        public void onErrorReport(int type) {
            logger += "error: " + type + "\n";
            LogUtils.i(TAG, "error: " + type);
        }
    }
}

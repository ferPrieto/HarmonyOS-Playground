package com.fprieto.wearable.presentation.ui.slice.weather;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.value.LottieAnimationViewData;
import com.fprieto.wearable.P2PAbilitySlice;
import com.fprieto.wearable.ResourceTable;
import com.fprieto.wearable.presentation.di.Injection;
import com.fprieto.wearable.presentation.ui.UiObserver;
import com.fprieto.wearable.presentation.ui.model.WeatherType;
import com.fprieto.wearable.presentation.ui.model.WeatherUiModel;
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

    private Text cityTextView;
    private Text weatherStatusTextView;
    private LottieAnimationView animationView;
    private RoundProgressBar progressBar;

    private WeatherViewModel weatherViewModel;

    private Locator locator;
    private MyLocatorCallback locatorCallback;

    private String logger = "Logs \n";
    private int previousLocation[] = null;

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        setUIContent(ResourceTable.Layout_ability_weather);

        initLocator();
        initViewModel();
        initViews();
    }

    private void initLocator() {
        locator = new Locator(this);
        locatorCallback = new MyLocatorCallback();
    }

    private void initViewModel() {
        weatherViewModel = new WeatherViewModel(Injection.getInstance().provideWeatherRepository());
        weatherViewModel.getStates().addObserver(new UiObserver<WeatherViewState>(this) {
            @Override
            public void onValueChanged(WeatherViewState weatherViewState) {
                if (weatherViewState instanceof WeatherViewState.Loading) {
                    onLoading();
                } else if (weatherViewState instanceof WeatherViewState.ErrorState) {
                    onError((WeatherViewState.ErrorState) weatherViewState);
                } else if (weatherViewState instanceof WeatherViewState.Loaded) {
                    onLoaded((WeatherViewState.Loaded) weatherViewState);
                }
            }
        }, false);
    }

    private void initViews() {
        progressBar = (RoundProgressBar) findComponentById(ResourceTable.Id_round_progress_bar);
        cityTextView = (Text) findComponentById(ResourceTable.Id_weather_city_text);
        weatherStatusTextView = (Text) findComponentById(ResourceTable.Id_weather_status_text);
        animationView = (LottieAnimationView) findComponentById(ResourceTable.Id_animationView);
    }

    private void onLoading() {
        progressBar.setVisibility(Component.VISIBLE);
        cityTextView.setVisibility(Component.HIDE);
        weatherStatusTextView.setVisibility(Component.HIDE);
        animationView.setVisibility(Component.HIDE);
    }

    private void onLoaded(final WeatherViewState.Loaded loadedState) {
        progressBar.setVisibility(Component.HIDE);
        cityTextView.setVisibility(Component.VISIBLE);
        weatherStatusTextView.setVisibility(Component.VISIBLE);
        animationView.setVisibility(Component.VISIBLE);
        bindUiValues(loadedState.getWeatherUiModel());
    }

    private void bindUiValues(WeatherUiModel weatherUiModel) {
        if (weatherUiModel != null) {
            cityTextView.setText(weatherUiModel.getCity()+new String(Character.toChars(0x1F4CD)));
            weatherStatusTextView.setText(weatherUiModel.getStatus());
            setUpAnimation(weatherUiModel.getWeatherType());
        } else {
            weatherStatusTextView.setText("Weather info unavailable...");
        }
    }

    private void setUpAnimation(WeatherType weatherType) {
        LottieAnimationViewData data = new LottieAnimationViewData();
        data.setFilename(getLottieFile(weatherType));
        data.autoPlay = true;
        animationView.setAnimationData(data);
    }

    private String getLottieFile(WeatherType weatherType) {
        String lottieFile = "sunny.json";
        switch (weatherType) {
            case RAINY:
                lottieFile = "rainy.json";
                break;
            case SNOWY:
                lottieFile = "snowy.json";
                break;
            case STORMY:
                lottieFile = "stormy.json";
                break;
            case PARTIALLY_SUNNY:
                lottieFile = "partially_sunny.json";
                break;
            case SUNNY:
                lottieFile = "sunny.json";
                break;
            case RAIN_WITH_SUN:
                lottieFile = "rain_with_sun.json";
                break;
            case CLOUDY_LIGHT:
                lottieFile = "cloudy_light.json";
                break;
            case CLOUDY_DARK:
                lottieFile = "cloudy_dark.json";
                break;
        }
        return lottieFile;
    }

    private void onError(final WeatherViewState.ErrorState errorState) {
        progressBar.setVisibility(Component.HIDE);
        animationView.setVisibility(Component.HIDE);
        cityTextView.setVisibility(Component.HIDE);
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
            if (previousLocation == null || previousLocation[0] != (int)location.getLatitude() || previousLocation[1] != (int)location.getLongitude()) {
                previousLocation = new int[]{(int)location.getLatitude(), (int)location.getLongitude()};
                getUITaskDispatcher().syncDispatch(() -> {
                    weatherViewModel.getWeatherByLocation((int) location.getLatitude(), (int) location.getLongitude());
                });
            }
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

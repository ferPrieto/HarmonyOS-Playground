package com.fprieto.wearable.presentation.ui.slice;

import com.fprieto.wearable.ResourceTable;
import com.fprieto.wearable.util.LogUtils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Text;
import ohos.location.*;
import java.io.*;
import java.util.List;

public class LocationAbilitySlice extends AbilitySlice {
    String TAG = "LocationAbilitySlice:";

    private Text text_location_log;
    private Button button_startLocation;
    private Button button_stopLocation;
    private Button button_reverseLocation;
    private Button button_reverseAddress;

    private String myLogs= "Logs \n";

    private Locator locator;
    private MyLocatorCallback locatorCallback;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_location);

        locator = new Locator(this);
        locatorCallback = new MyLocatorCallback();
        initialiseViews();
    }



    public void initialiseViews(){
        text_location_log = (Text) findComponentById(ResourceTable.Id_text_location_log);
        button_startLocation = (Button) findComponentById(ResourceTable.Id_button_startLocation);
        button_stopLocation = (Button) findComponentById(ResourceTable.Id_button_stopLocation);
        button_reverseLocation = (Button) findComponentById(ResourceTable.Id_button_reverseLocation);
        button_reverseAddress =  (Button) findComponentById(ResourceTable.Id_button_reverseAddress);

        button_startLocation.setClickedListener(component -> locationManager());

        button_stopLocation.setClickedListener(component -> stopLocationUpdate());

        button_reverseLocation.setClickedListener(component -> reverseLocationManager());

        button_reverseAddress.setClickedListener(component -> reverseAddress());
    }

    private void locationManager(){
        RequestParam requestParam =  new RequestParam(RequestParam.PRIORITY_FAST_FIRST_FIX,0,0);
        // PRIORITY_ACCURACY / PRIORITY_FAST_FIRST_FIX / PRIORITY_LOW_POWER

        // continuous location update
        locator.startLocating(requestParam, locatorCallback);
    }

    private void reverseLocationManager(){
        try {
            GeoConvert geoConvert = new GeoConvert();
            List<GeoAddress> geoAddresses = geoConvert.getAddressFromLocation(40.0, 116.0, 1);
            if(geoAddresses.size() >=1){
                LogUtils.i(TAG, "Reverse Location: country: "+geoAddresses.get(0).getCountryName());
                myLogs += "Reverse Location: country: "+geoAddresses.get(0).getCountryName()+"\n";
                text_location_log.setText(myLogs);
            }
        } catch (IOException e) {
            LogUtils.i(TAG, "error: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void reverseAddress(){
        try {
            GeoConvert geoConvert = new GeoConvert();
            List<GeoAddress> geoAddresses = geoConvert.getAddressFromLocationName("Malaz Riyadh Saudi Arabia", 1);
            if(geoAddresses.size() >= 1){
                LogUtils.i(TAG, "Reverse Address: Lat: "+geoAddresses.get(0).getLatitude()+", Long: "+geoAddresses.get(0).getLongitude());
                myLogs += "Reverse Address: Lat: "+geoAddresses.get(0).getLatitude()+", Long: "+geoAddresses.get(0).getLongitude()+"\n";
                text_location_log.setText(myLogs);
            }
        } catch (IOException e) {
            LogUtils.i(TAG, "error: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopLocationUpdate(){
        locator.stopLocating(locatorCallback);
    }

    @Override
    public void onActive() {
        super.onActive();
        locationManager();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    public class MyLocatorCallback implements LocatorCallback {

        @Override
        public void onLocationReport(Location location) {
            myLogs += "Lat: "+location.getLatitude()+", Long: "+location.getLongitude()+"\n";
            LogUtils.i(TAG, "Lat: "+location.getLatitude()+", Long: "+location.getLongitude());
            getUITaskDispatcher().syncDispatch(() -> {
                text_location_log.setText(myLogs);
            });
        }

        @Override
        public void onStatusChanged(int type) {
            myLogs += "status changed: "+type+"\n";
            LogUtils.i(TAG, "status changed: "+type);
            getUITaskDispatcher().syncDispatch(() -> {
                text_location_log.setText(myLogs);
            });
        }

        @Override
        public void onErrorReport(int type) {
            myLogs += "error: "+type+"\n";
            LogUtils.i(TAG, "error: "+type);
            getUITaskDispatcher().syncDispatch(() -> {
                text_location_log.setText(myLogs);
            });
        }
    }
}
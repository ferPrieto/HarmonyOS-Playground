package com.fprieto.wearable.presentation.ui.slice;

import com.fprieto.wearable.ResourceTable;
import com.fprieto.wearable.model.DataMessage;
import com.fprieto.wearable.model.PlayerCommand;
import com.fprieto.wearable.presentation.ui.slice.joke.JokeAbilitySlice;
import com.fprieto.wearable.util.LogUtils;
import com.google.gson.Gson;
import com.huawei.watch.kit.hiwear.HiWear;
import com.huawei.watch.kit.hiwear.p2p.HiWearMessage;
import com.huawei.watch.kit.hiwear.p2p.P2pClient;
import com.huawei.watch.kit.hiwear.p2p.Receiver;
import com.huawei.watch.kit.hiwear.p2p.SendCallback;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.agp.components.Component.RotationEventListener;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.multimodalinput.event.RotationEvent;
import ohos.sensor.agent.CategoryBodyAgent;
import ohos.sensor.bean.CategoryBody;
import ohos.sensor.data.CategoryBodyData;
import ohos.sensor.listener.ICategoryBodyDataCallback;
import ohos.utils.zson.ZSONObject;

import java.nio.charset.StandardCharsets;

public class HealthAbilitySlice extends AbilitySlice {

    private final static String TAG = "HealthAbilitySlice";

    private final static String peerPkgName = "com.fprieto.hms.wearable";
    private final static String peerFinger = "CFCC7E8B7AF0C5B2B488190B17B897BB483541B26A7F15065602D716E586FEDA";
    private static final int FACTOR = 3;
    int rotationEventCount = 0;

    private CategoryBodyAgent categoryBodyAgent = new CategoryBodyAgent();
    private CategoryBody heartRateSensor;
    private CategoryBody wearDetectionSensor;
    private ICategoryBodyDataCallback bodyDataCallback;

    private Text caloriesValue;
    private Text heartRateValue;
    private Text stepsValue;
    private Text oxygenValue;
    private ScrollView scrollView;
    private RoundProgressBar progressBarSteps;
    private RoundProgressBar progressBarOxygen;
    private RoundProgressBar progressBarHeartRate;
    private RoundProgressBar progressBarCalories;

    private TaskDispatcher uiDispatcher;
    private P2pClient p2pClient;

    private final Receiver receiver = message -> {
        if (message.getType() == HiWearMessage.MESSAGE_TYPE_DATA) {
            getHealthData(message);
            LogUtils.d(TAG, "Received text: " + new String(message.getData()));
        }
    };

    private void getHealthData(HiWearMessage message) {
        final String messageValue = new String(message.getData());
        final String detailValue = messageValue.substring(messageValue.indexOf("-") + 2, messageValue.length());
        if (messageValue.contains("Calories")) {
            uiDispatcher.syncDispatch(() -> {
                caloriesValue.setText(detailValue);
                progressBarCalories.setVisibility(Component.HIDE);
                caloriesValue.setVisibility(Component.VISIBLE);
            });
        } else if (messageValue.contains("HeartRate")) {
            uiDispatcher.syncDispatch(() -> {
                heartRateValue.setText(detailValue);
                progressBarHeartRate.setVisibility(Component.HIDE);
                heartRateValue.setVisibility(Component.VISIBLE);
            });
        } else if (messageValue.contains("Steps")) {
            uiDispatcher.syncDispatch(() -> {
                stepsValue.setText(detailValue);
                progressBarSteps.setVisibility(Component.HIDE);
                stepsValue.setVisibility(Component.VISIBLE);
            });
        } else if (messageValue.contains("Oxygen")) {
            uiDispatcher.syncDispatch(() -> {
                oxygenValue.setText(detailValue);
                progressBarOxygen.setVisibility(Component.HIDE);
                oxygenValue.setVisibility(Component.VISIBLE);
            });
        }
    }

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_health);

        uiDispatcher = getUITaskDispatcher();

        initP2PClient();
        initViews();
        initMotionCallback();
        initSensors();
        subscribeSensorData(wearDetectionSensor);
        subscribeSensorData(heartRateSensor);
    }

    private void initP2PClient() {
        p2pClient = HiWear.getP2pClient(this);
        p2pClient.setPeerPkgName(peerPkgName);
        p2pClient.setPeerFingerPrint(peerFinger);
        try {
            p2pClient.ping(resultCode -> {
                LogUtils.d(TAG, "ping result = " + resultCode);
            });
        } catch (Exception e) {
            LogUtils.d(TAG, e.getMessage());
        }
    }

    private void subscribeSensorData(CategoryBody categoryBody) {
        LogUtils.d(TAG, "SubscribeSensorData: " + ZSONObject.toZSONString(categoryBody));
        if (categoryBody != null) {
            categoryBodyAgent.setSensorDataCallback(bodyDataCallback, categoryBody, 1000000L);
        }
    }

    private void initSensors() {
        wearDetectionSensor = categoryBodyAgent.getSingleSensor(CategoryBody.SENSOR_TYPE_WEAR_DETECTION);
        heartRateSensor = categoryBodyAgent.getSingleSensor(CategoryBody.SENSOR_TYPE_HEART_RATE);
        LogUtils.d(TAG, "categoryBodyAgent wearDetectionSensor " + ZSONObject.toZSONString(wearDetectionSensor));
        LogUtils.d(TAG, "categoryBodyAgent heartRateSensor " + ZSONObject.toZSONString(heartRateSensor));
    }

    private void initMotionCallback() {
        bodyDataCallback = new ICategoryBodyDataCallback() {
            @Override
            public void onSensorDataModified(CategoryBodyData categoryBodyData) {
                int dim = categoryBodyData.getSensorDataDim(); //Get the dimensional information of the sensor
                float dataValue = categoryBodyData.getValues()[0]; // Obtain the first-dimensional data of directional sensors

                if (categoryBodyData.getSensor().getSensorId() == heartRateSensor.getSensorId()) {
                    // Heart Rate logic. When watch is not on the wrist - sensor returns 255 bpm.
                    LogUtils.d(TAG, "Body：dim= " + dim + ", heartRate= " + dataValue);
                }

                if (categoryBodyData.getSensor().getSensorId() == wearDetectionSensor.getSensorId()) {
                    // Wear Detection logic. Sensor return 0 - when watch is in the air, 1 - when on the any object (table, wrist etc.)
                    LogUtils.d(TAG, "Wear Detection：dim= " + dim + ", detection= " + dataValue);
                }
            }

            @Override
            public void onAccuracyDataModified(CategoryBody categoryBody, int i) {
                LogUtils.d(TAG, "onAccuracyDataModified");

            }

            @Override
            public void onCommandCompleted(CategoryBody categoryBody) {
                LogUtils.d(TAG, "onAccuracyDataModified");

            }
        };
    }

    private void initViews() {
        progressBarCalories = (RoundProgressBar) findComponentById(ResourceTable.Id_round_progress_bar_calories);
        progressBarSteps = (RoundProgressBar) findComponentById(ResourceTable.Id_round_progress_bar_steps);
        progressBarOxygen = (RoundProgressBar) findComponentById(ResourceTable.Id_round_progress_bar_oxygen);
        progressBarHeartRate = (RoundProgressBar) findComponentById(ResourceTable.Id_round_progress_bar_heart_rate);

        caloriesValue = (Text) findComponentById(ResourceTable.Id_text_calories_value);
        heartRateValue = (Text) findComponentById(ResourceTable.Id_text_heart_rate_value);
        stepsValue = (Text) findComponentById(ResourceTable.Id_text_steps_value);
        oxygenValue = (Text) findComponentById(ResourceTable.Id_text_oxygen_value);
        scrollView = (ScrollView) findComponentById(ResourceTable.Id_scrollview_main);

        scrollView.setReboundEffect(true);
        scrollView.setVibrationEffectEnabled(true);
        scrollView.setTouchFocusable(true);
        scrollView.requestFocus();
        scrollView.setRotationEventListener((component, rotationEvent) -> {
            if (rotationEvent != null) {
                float rotationValue = rotationEvent.getRotationValue();
                if (Math.abs(rotationEventCount) == FACTOR) {
                    int y = (int) (scrollView.getScrollValue(1) + rotationEventCount / FACTOR) + (rotationValue > 0 ? 10 : -10);
                    scrollView.scrollTo(0, y);
                    rotationEventCount = 0;
                } else {
                    rotationEventCount += rotationValue > 0 ? -1 : 1;
                }
                return true;
            }
            return false;
        });
        scrollView.setVibrationEffectEnabled(true);
    }

    @Override
    public void onActive() {
        super.onActive();
        p2pClient.registerReceiver(receiver);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        p2pClient.unregisterReceiver(receiver);

        if (heartRateSensor != null) {
            categoryBodyAgent.releaseSensorDataCallback(bodyDataCallback, heartRateSensor);
        }
        if (wearDetectionSensor != null) {
            categoryBodyAgent.releaseSensorDataCallback(bodyDataCallback, wearDetectionSensor);
        }
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    @Override
    protected void onBackground() {
        super.onBackground();
    }
}

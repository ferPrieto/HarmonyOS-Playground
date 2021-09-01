package com.fprieto.wearable.presentation.ui.slice;

import com.fprieto.wearable.ResourceTable;
import com.fprieto.wearable.util.LogUtils;
import com.huawei.watch.kit.hiwear.HiWear;
import com.huawei.watch.kit.hiwear.p2p.HiWearMessage;
import com.huawei.watch.kit.hiwear.p2p.P2pClient;
import com.huawei.watch.kit.hiwear.p2p.Receiver;
import com.huawei.watch.kit.hiwear.p2p.SendCallback;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;

public class AudioAbilitySlice extends AbilitySlice {

    private final static String TAG = "AudioAbilitySlice";

    private final static String peerPkgName = "com.fprieto.hms.wearable";
    private final static String peerFinger = "CFCC7E8B7AF0C5B2B488190B17B897BB483541B26A7F15065602D716E586FEDA";
    private static final int FACTOR = 3;
    int rotationEventCount = 0;

    private Text lastReceivedImageTitle;
    private Image lastReceivedImage;
    private ScrollView scrollView;

    private TaskDispatcher uiDispatcher;
    private P2pClient p2pClient;

    private final Receiver receiver = new Receiver() {
        @Override
        public void onReceiveMessage(HiWearMessage message) {
            final int type = message.getType();

            switch (type) {
                case HiWearMessage.MESSAGE_TYPE_DATA:
                    final String text = new String(message.getData());
                    LogUtils.d(TAG, "Received text: " + text);
                    break;
                case HiWearMessage.MESSAGE_TYPE_FILE:
                    LogUtils.d(TAG, "Received file.");
                    final PixelMap pixelMap = ImageSource.create(message.getFile(),
                            new ImageSource.SourceOptions())
                            .createPixelmap(new ImageSource.DecodingOptions());
                    uiDispatcher.syncDispatch(()-> {
                        lastReceivedImage.setPixelMap(pixelMap);
                        lastReceivedImage.setVisibility(Component.VISIBLE);
                        lastReceivedImageTitle.setVisibility(Component.VISIBLE);
                    });
                    break;
            }
        }
    };

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_audio);

        uiDispatcher = getUITaskDispatcher();

        initP2PClient();
        initViews();
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

    private void initViews() {
        scrollView = (ScrollView) findComponentById(ResourceTable.Id_scrollview_main);
        lastReceivedImage = (Image) findComponentById(ResourceTable.Id_image_received_image);
        lastReceivedImageTitle = (Text) findComponentById(ResourceTable.Id_image_received_title);

        final Button playAudioButton = (Button) findComponentById(ResourceTable.Id_button_play_audio);
        playAudioButton.setClickedListener(component -> present(new AudioPlayerDashboardAbilitySlice(), new Intent()));

        final Button recordButton = (Button) findComponentById(ResourceTable.Id_button_record_audio);
        recordButton.setClickedListener(component -> present(new RecordAudioAbilitySlice(), new Intent()));

        scrollView.setReboundEffect(true);
        scrollView.setVibrationEffectEnabled(true);
        scrollView.setTouchFocusable(true);
        scrollView.requestFocus();
        scrollView.setRotationEventListener((component, rotationEvent) -> {
            if (rotationEvent != null) {
                float rotationValue = rotationEvent.getRotationValue();
                if (Math.abs(rotationEventCount) == FACTOR) {
                    int y = scrollView.getScrollValue(1) + rotationEventCount / FACTOR + (rotationValue > 0 ? 10 : -10);
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

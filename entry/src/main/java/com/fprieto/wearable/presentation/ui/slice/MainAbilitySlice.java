package com.fprieto.wearable.presentation.ui.slice;

import com.fprieto.wearable.ResourceTable;
import com.fprieto.wearable.menu.CircleMenu;
import com.fprieto.wearable.menu.OnMenuStatusChangeListener;
import com.fprieto.wearable.presentation.ui.slice.joke.JokeAbilitySlice;
import com.fprieto.wearable.util.LogUtils;
import com.huawei.watch.kit.hiwear.HiWear;
import com.huawei.watch.kit.hiwear.p2p.HiWearMessage;
import com.huawei.watch.kit.hiwear.p2p.P2pClient;
import com.huawei.watch.kit.hiwear.p2p.Receiver;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.utils.Color;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;

public class MainAbilitySlice extends AbilitySlice {

    private final static String TAG = "MainAbilitySlice";

    private final static String peerPkgName = "com.fprieto.hms.wearable";
    private final static String peerFinger = "CFCC7E8B7AF0C5B2B488190B17B897BB483541B26A7F15065602D716E586FEDA";

    private CircleMenu circleMenu;

    private P2pClient p2pClient;

    private final Receiver receiver = message -> {
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
                //todo: open messaging to show the image
                break;
        }
    };

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

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
        circleMenu = (CircleMenu) findComponentById(ResourceTable.Id_circle_menu);
        circleMenu.addSubMenu(Color.getIntColor("#30E8AE"), ResourceTable.Media_health)
                .addSubMenu(Color.getIntColor("#D7376B"), ResourceTable.Media_weather)
                .addSubMenu(Color.getIntColor("#69DEFF"), ResourceTable.Media_messaging)
                .addSubMenu(Color.getIntColor("#A752FE"), ResourceTable.Media_videoplayer)
                .addSubMenu(Color.getIntColor("#FBD56C"), ResourceTable.Media_audio)
                .setOnMenuSelectedListener(index -> {
                    switch (index) {
                        case 0: {
                            present(new HealthAbilitySlice(), new Intent());
                            break;
                        }
                        case 1: {
                            present(new JokeAbilitySlice(), new Intent());
                            break;
                        }
                        case 2: {
                            present(new MessagingAbilitySlice(), new Intent());
                            break;
                        }
                        case 3: {
                            present(new RemoteVideoPlayerAbilitySlice(), new Intent());
                            break;
                        }
                        case 4: {
                            present(new AudioAbilitySlice(), new Intent());
                            break;
                        }
                    }
                })
                .setOnMenuStatusChangeListener(new OnMenuStatusChangeListener() {

                    @Override
                    public void onMenuOpened() {
                    }

                    @Override
                    public void onMenuClosed() {
                    }

                });
    }

    @Override
    public void onActive() {
        super.onActive();
        p2pClient.registerReceiver(receiver);
        circleMenu.startOpenMenuAnima();
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

package com.fprieto.wearable.presentation.ui.slice;

import com.fprieto.wearable.ResourceTable;
import com.fprieto.wearable.data.model.DataMessage;
import com.fprieto.wearable.data.model.PlayerCommand;
import com.fprieto.wearable.util.LogUtils;
import com.google.gson.Gson;
import com.huawei.watch.kit.hiwear.HiWear;
import com.huawei.watch.kit.hiwear.p2p.HiWearMessage;
import com.huawei.watch.kit.hiwear.p2p.P2pClient;
import com.huawei.watch.kit.hiwear.p2p.Receiver;
import com.huawei.watch.kit.hiwear.p2p.SendCallback;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.Image;

import java.nio.charset.StandardCharsets;

public class RemoteVideoPlayerAbilitySlice extends AbilitySlice {

    private final static String TAG = "RemoteVideoPlayerAbilitySlice";

    private final static String peerPkgName = "com.fprieto.hms.wearable";
    private final static String peerFinger = "CFCC7E8B7AF0C5B2B488190B17B897BB483541B26A7F15065602D716E586FEDA";

    private Image buttonPlay;
    private Image buttonPause;
    private Image buttonRewind;
    private Image buttonFastForward;
    private Image buttonPrevious;
    private Image buttonNext;

    private P2pClient p2pClient;

    private enum Command {
        PLAY,
        PAUSE,
        FASTFORWARD,
        REWIND,
        NEXT,
        PREVIOUS
    }

    private final Receiver receiver = new Receiver() {
        @Override
        public void onReceiveMessage(HiWearMessage message) {
            if (message.getType() == HiWearMessage.MESSAGE_TYPE_DATA) {
                final String text = new String(message.getData());
                LogUtils.d(TAG, "Received text: " + text);
                //TODO: parse player action
            }
        }
    };

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_remotevideoplayer);


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
        buttonPlay = (Image) findComponentById(ResourceTable.Id_buttonPlay);
        buttonPause = (Image) findComponentById(ResourceTable.Id_buttonPause);
        buttonRewind = (Image) findComponentById(ResourceTable.Id_buttonRewind);
        buttonFastForward = (Image) findComponentById(ResourceTable.Id_buttonFastForward);
        buttonPrevious = (Image) findComponentById(ResourceTable.Id_buttonPrevious);
        buttonNext = (Image) findComponentById(ResourceTable.Id_buttonNext);

        buttonPlay.setClickedListener(component -> {
            sendPlayerCommand(Command.PLAY);
            buttonPlay.setVisibility(Component.HIDE);
            buttonPause.setVisibility(Component.VISIBLE);
        });

        buttonPause.setClickedListener(component -> {
            sendPlayerCommand(Command.PAUSE);
            buttonPlay.setVisibility(Component.VISIBLE);
            buttonPause.setVisibility(Component.HIDE);
        });

        buttonFastForward.setClickedListener(component -> {
            sendPlayerCommand(Command.FASTFORWARD);
        });

        buttonRewind.setClickedListener(component -> {
            sendPlayerCommand(Command.REWIND);
        });

        buttonNext.setClickedListener(component -> {
            sendPlayerCommand(Command.NEXT);
        });

        buttonPrevious.setClickedListener(component -> {
            sendPlayerCommand(Command.PREVIOUS);
        });
    }

    private void sendPlayerCommand(Command command) {
        PlayerCommand playerCommand = new PlayerCommand();
        playerCommand.setCommand(commandToString(command));
        DataMessage dataMessage = new DataMessage();
        dataMessage.setMessageType("Player-Command");
        dataMessage.setPlayerCommand(playerCommand);

        Gson gson = new Gson();
        String json = gson.toJson(dataMessage);
        try {
            HiWearMessage.Builder builder = new HiWearMessage.Builder();
            builder.setPayload(json.getBytes(StandardCharsets.UTF_8));
            HiWearMessage msgPayload = builder.build();
            SendCallback sendCallback = i -> {
                LogUtils.d(TAG, "Send Command result - " + i);
            };
            p2pClient.send(msgPayload, sendCallback);
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
        }
    }

    private String commandToString(Command playerCommand) {
        String result = "";
        switch (playerCommand) {
            case PLAY:
                result = "play";
                break;
            case PAUSE:
                result = "pause";
                break;
            case FASTFORWARD:
                result = "fastforward";
                break;
            case REWIND:
                result = "rewind";
                break;
            case NEXT:
                result = "next";
                break;
            case PREVIOUS:
                result = "previous";
                break;
        }
        return result;
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

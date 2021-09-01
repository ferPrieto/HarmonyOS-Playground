package com.fprieto.wearable.presentation.ui.slice;

import com.fprieto.wearable.ResourceTable;
import com.fprieto.wearable.util.LogUtils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.Image;
import ohos.agp.components.Text;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.global.resource.BaseFileDescriptor;
import ohos.media.audio.AudioManager;
import ohos.media.common.Source;
import ohos.media.player.Player;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

public class AudioPlayerAbilitySlice extends AbilitySlice {
    private static final String LOCAL_FILE_PATH = "entry/resources/rawfile/sample-music.mp3";
    private static final String RADIO_STATION_URI = "https://streams.kqed.org/kqedradio";

    private String TAG = "Player";
    Player.IPlayerCallback iPlayerCallback = new Player.IPlayerCallback() {
        @Override
        public void onPrepared() { }

        @Override
        public void onMessage(int i, int i1) {
            LogUtils.w(TAG, "onMessage:" + i + "i1" + i1);
        }

        @Override
        public void onError(int i, int i1) {
            LogUtils.w(TAG, "onError:" + i + "i1" + i1);
        }

        @Override
        public void onResolutionChanged(int i, int i1) { }

        @Override
        public void onPlayBackComplete() { }

        @Override
        public void onRewindToComplete() { }

        @Override
        public void onBufferingChange(int i) { }

        @Override
        public void onNewTimedMetaData(Player.MediaTimedMetaData mediaTimedMetaData) { }

        @Override
        public void onMediaTimeIncontinuity(Player.MediaTimeInfo mediaTimeInfo) { }
    };

    private Image buttonPlay;
    private Image buttonVolumeUp;
    private Image buttonVolumeDown;
    private Text filePlayingName;
    private boolean isPlaying = false;
    private Player player;
    private AudioManager audioManager;
    private BaseFileDescriptor baseFileDescriptor;
    private Source radioSource;
    private Source lastRecordingSource;
    private PlayingType playingType;
    enum PlayingType {
        LOCAL, RADIO, RECORDING
    }
    private RecorderEventHandler recorderHandler;


    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_audioplayer);

        initAudioComponents();
        initViews();
        setIntentParams(intent);
    }

    private void setIntentParams(Intent intent) {
        String playingTypeParam = intent.getStringParam("PLAYING_TYPE");
        if(playingTypeParam.equals("local")){
            playingType = PlayingType.LOCAL;
            filePlayingName.setText("Playing: sample-music.mp3");
            getFileDescriptorFromLocalFile();
        }else if(playingTypeParam.equals("radio")){
            playingType = PlayingType.RADIO;
            filePlayingName.setText("Playing: kqedradio");
            getFileDescriptorFromRadio();
        }else if(playingTypeParam.equals("recording")){
            playingType = PlayingType.RECORDING;
            filePlayingName.setText("Last recording");
            getFileDescriptorFromLastRecording();
        }
    }

    private void initAudioComponents(){
        EventRunner runner = EventRunner.create(true);
        recorderHandler = new RecorderEventHandler(runner);
        audioManager = new AudioManager(this);
    }

    private void initViews() {
        filePlayingName = (Text) findComponentById(ResourceTable.Id_file_name);
        buttonPlay = (Image) findComponentById(ResourceTable.Id_imgPlayer);
        buttonVolumeUp = (Image) findComponentById(ResourceTable.Id_imgVolumeLoud);
        buttonVolumeDown = (Image) findComponentById(ResourceTable.Id_imgVolumeQuiet);

        buttonPlay.setClickedListener(listener -> {
            isPlaying = !isPlaying;
            if (!isPlaying) {
                buttonPlay.setPixelMap(ResourceTable.Media_play_arrow);
                Runnable runnable = () -> pauseMusic();
                recorderHandler.postTask(runnable, 0, EventHandler.Priority.IMMEDIATE);

            } else {
                buttonPlay.setPixelMap(ResourceTable.Media_icon_pause);
                Runnable runnable = () -> playMusic();
                recorderHandler.postTask(runnable, 0, EventHandler.Priority.IMMEDIATE);
            }
        });

        buttonVolumeUp.setClickedListener(listener -> {
            audioManager.changeVolumeBy(AudioManager.AudioVolumeType.STREAM_MUSIC, 1);
        });

        buttonVolumeDown.setClickedListener(listener -> {
            audioManager.changeVolumeBy(AudioManager.AudioVolumeType.STREAM_MUSIC, -1);
        });
    }

    private void getFileDescriptorFromRadio() {
        radioSource = new Source(RADIO_STATION_URI);
    }

    private void getFileDescriptorFromLastRecording() {
        String resourceStr = "/data/user/0/com.fprieto.wearable/files/recording.pcm";
        File file = new File(resourceStr);
        file.getAbsolutePath();
        LogUtils.w(TAG, "file" + file.getAbsolutePath());
        try (FileInputStream in = new FileInputStream(new File(resourceStr))) {
            FileDescriptor fd = in.getFD();
            lastRecordingSource = new Source(fd);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, e.getMessage());
        }
    }

    private void getFileDescriptorFromLocalFile() {
        try {
            baseFileDescriptor = getResourceManager().getRawFileEntry(LOCAL_FILE_PATH).openRawFileDescriptor();
        } catch (IOException e) {
            LogUtils.e(TAG, "Audio resource is unavailable: " + e.toString());
            return;
        }
    }

    private void playMusic() {
        if (player == null || !player.isNowPlaying()) {
            player = new Player(this);
            player.setVolume(100f);
            player.setPlayerCallback(iPlayerCallback);

            setPlayerSource();
            tryToPreparePlayer();
            tryToPlayPlayer();
        } else if (player != null) {
            player.play();
        }
    }

    private void setPlayerSource(){
        if(playingType== PlayingType.LOCAL){
            if (!player.setSource(baseFileDescriptor)) {
                LogUtils.w(TAG, "baseFileDescriptor is invalid");
                return;
            }
        }else if(playingType == PlayingType.RADIO){
            if (!player.setSource(radioSource)) {
                LogUtils.w(TAG, "radio uri is invalid");
                return;
            }
        }else{
            if (!player.setSource(lastRecordingSource)) {
                LogUtils.w(TAG, "last recording source is invalid");
                return;
            }
        }
    }

    private void tryToPreparePlayer(){
        if (!player.prepare()) {
            LogUtils.w(TAG, "prepare failed");
            return;
        }
    }

    private void tryToPlayPlayer(){
        if (player.play()) {
            LogUtils.i(TAG, "Play success");
        } else {
            LogUtils.e(TAG, "Play failed");
        }
    }

    private boolean pauseMusic() {
        if (player == null) {
            return false;
        }
        player.pause();
        return true;
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    private static class RecorderEventHandler extends EventHandler {
        private RecorderEventHandler(EventRunner runner) throws IllegalArgumentException {
            super(runner);
        }
    }
}


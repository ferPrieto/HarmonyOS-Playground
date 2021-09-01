package com.fprieto.wearable.presentation.ui.slice;

import com.fprieto.wearable.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;

public class AudioPlayerDashboardAbilitySlice extends AbilitySlice {
    private String TAG = "AudioDashboardAbilitySlice";

    private Button buttonPlayLocalFile;
    private Button buttonPlayRadioStation;
    private Button buttonPlayLastRecording;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_audio_dashboard);
        initViews();
    }

    private void initViews() {
        buttonPlayLocalFile = (Button) findComponentById(ResourceTable.Id_button_local_file);
        buttonPlayRadioStation = (Button) findComponentById(ResourceTable.Id_button_radio);
        buttonPlayLastRecording = (Button) findComponentById(ResourceTable.Id_button_last_recording);

        buttonPlayLocalFile.setClickedListener(listener -> {
            Intent audioPlayerIntent = new Intent();
            audioPlayerIntent.setParam("PLAYING_TYPE", "local");
            present(new AudioPlayerAbilitySlice(), audioPlayerIntent);
        });
        buttonPlayRadioStation.setClickedListener(listener -> {
            Intent audioPlayerIntent = new Intent();
            audioPlayerIntent.setParam("PLAYING_TYPE", "radio");
            present(new AudioPlayerAbilitySlice(), audioPlayerIntent);
        });
        buttonPlayLastRecording.setClickedListener(listener -> {
            Intent audioPlayerIntent = new Intent();
            audioPlayerIntent.setParam("PLAYING_TYPE", "recording");
            present(new AudioPlayerAbilitySlice(), audioPlayerIntent);
        });
    }
}


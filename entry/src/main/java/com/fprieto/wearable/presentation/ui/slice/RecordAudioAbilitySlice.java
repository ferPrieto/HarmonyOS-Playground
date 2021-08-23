package com.fprieto.wearable.presentation.ui.slice;

import com.fprieto.wearable.ResourceTable;
import com.fprieto.wearable.audio.MyAudioPlayer;
import ohos.media.audio.AudioCapturer;
import com.fprieto.wearable.util.LogUtils;
import com.huawei.watch.kit.hiwear.p2p.HiWearMessage;
import com.huawei.watch.kit.hiwear.p2p.SendCallback;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.media.audio.AudioManager;
import ohos.media.audio.AudioCapturerInfo;
import ohos.media.audio.AudioCapturerConfig;
import ohos.media.audio.AudioCapturerCallback;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import ohos.media.audio.AudioStreamInfo;
import static com.huawei.watch.kit.hiwear.utils.FileUtil.toByteArray;

public class RecordAudioAbilitySlice extends AbilitySlice {

    private static final String TAG = "RecordAudioSlice";

    private static final int SAMPLING_RATE_IN_HZ = 44100;

    private static final int CHANNEL_CONFIG =
            AudioStreamInfo.getChannelCount(AudioStreamInfo.ChannelMask.CHANNEL_IN_STEREO);

    private static final int BUFFER_SIZE = AudioCapturer.getMinBufferSize(SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG,
            AudioStreamInfo.EncodingFormat.ENCODING_PCM_16BIT.getValue()) * 2;

    private AudioManager audioManager = new AudioManager();
    private AudioCapturer capture;

    private Button recordAudioButton;

    private AtomicBoolean isRecording = new AtomicBoolean(false);
    private Runnable recordRunnable = new Runnable() {
        final MyAudioPlayer player = new MyAudioPlayer();

        @Override
        public void run() {
            try {
                player.stopAndResetPlayer();

                String file_path = getApplicationContext().getFilesDir().getPath();
                File file = new File(file_path);
                String file_name = file + "/" + "recording.pcm";
                final File outputFile = new File(file_name);
                outputFile.deleteOnExit();
                outputFile.createNewFile();
                byte[] buffer;
                final FileOutputStream outStream = new FileOutputStream(outputFile);

                capture.start();
                while (isRecording.get()) {
                    buffer = new byte[BUFFER_SIZE];
                    capture.read(buffer, 0, BUFFER_SIZE);
                    outStream.write(buffer);
                }
                outStream.close();
                capture.stop();

                sendAudioSampleToPhone(outputFile);

                final boolean audioStarted = player.startPlayer();
                LogUtils.d(TAG, "Audio Started: " + audioStarted);
                player.writeBuffer(inputStream2ByteArray(outputFile));
            } catch (IOException e) {
                throw new RuntimeException("Writing of recorded audio failed", e);
            }
        }
    };

    private Thread recordingThread;

    private byte[] inputStream2ByteArray(final File file) throws IOException {
        final InputStream in = new FileInputStream(file);
        final byte[] data = toByteArray(in);
        in.close();
        return data;
    }

    private void sendAudioSampleToPhone(final File file) {
        final HiWearMessage.Builder builder = new HiWearMessage.Builder();
        builder.setPayload(file);
        HiWearMessage sendMessage = builder.build();

        SendCallback sendCallback = new SendCallback() {
            @Override
            public void onSendResult(int resultCode) {
                LogUtils.d(TAG, "Send audio sample result: " + resultCode);
            }

            @Override
            public void onSendProgress(long progress) {
                LogUtils.d(TAG, "Send audio sample progress: " + progress);
            }
        };

        //getP2PClient().send(sendMessage, sendCallback);
    }

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_record_audio);
        recordingThread = new Thread(recordRunnable);

        initViews();
        initAudioCapturer();
    }

    private void initViews() {
        recordAudioButton = (Button) findComponentById(ResourceTable.Id_button_record_audio);
        recordAudioButton.setClickedListener(component -> {
            isRecording.set(!isRecording.get());
            renderRecordingUIAccordingToState();

            if (isRecording.get()) {
                recordingThread.start();
            }
        });
    }

    private void initAudioCapturer() {
        final AudioStreamInfo audioStreamInfo = new AudioStreamInfo.Builder().encodingFormat(
                AudioStreamInfo.EncodingFormat.ENCODING_PCM_16BIT)
                .channelMask(AudioStreamInfo.ChannelMask.CHANNEL_IN_STEREO)
                .sampleRate(SAMPLING_RATE_IN_HZ)
                .build();
        final AudioCapturerInfo info = new AudioCapturerInfo.Builder().audioStreamInfo(audioStreamInfo).build();
        capture = new AudioCapturer(info);

        AudioCapturerCallback audioCapturerCallback = new AudioCapturerCallback() {
            @Override
            public void onCapturerConfigChanged(List<AudioCapturerConfig> configs) {
                configs.forEach(config -> {
                    LogUtils.d(TAG, "On new AudioCapturer Config: " + config);
                });
            }
        };
        audioManager.registerAudioCapturerCallback(audioCapturerCallback);
    }

    private void renderRecordingUIAccordingToState() {
        if (isRecording.get()) {
            recordAudioButton.setText("Is Recording...");
        } else {
            recordAudioButton.setText("Record Audio");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        capture.release();
    }

}

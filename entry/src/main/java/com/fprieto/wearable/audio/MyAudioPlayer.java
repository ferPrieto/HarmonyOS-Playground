/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.fprieto.wearable.audio;

import ohos.media.audio.AudioCapturer;
import ohos.media.audio.AudioRenderer;
import ohos.media.audio.AudioRendererInfo;
import ohos.media.audio.AudioStreamInfo;

/**
 * MyAudioPlayer
 *
 * @since 2020-09-08
 */
public class MyAudioPlayer {

    private static final int SAMPLING_RATE_IN_HZ = 44100;

    private static final int CHANNEL_CONFIG =
            AudioStreamInfo.getChannelCount(AudioStreamInfo.ChannelMask.CHANNEL_IN_STEREO);

    private static final int BUFFER_SIZE = AudioCapturer.getMinBufferSize(SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG,
            AudioStreamInfo.EncodingFormat.ENCODING_PCM_16BIT.getValue());

    private AudioRenderer audioRenderer;

    public MyAudioPlayer() {
        AudioStreamInfo audioStreamInfo = new AudioStreamInfo.Builder().sampleRate(SAMPLING_RATE_IN_HZ)
                .audioStreamFlag(AudioStreamInfo.AudioStreamFlag.AUDIO_STREAM_FLAG_MAY_DUCK)
                .encodingFormat(AudioStreamInfo.EncodingFormat.ENCODING_PCM_16BIT)
                .channelMask(AudioStreamInfo.ChannelMask.CHANNEL_OUT_STEREO)
                .streamUsage(AudioStreamInfo.StreamUsage.STREAM_USAGE_MEDIA)
                .build();

        AudioRendererInfo audioRendererInfo = new AudioRendererInfo.Builder().audioStreamInfo(audioStreamInfo)
                .audioStreamOutputFlag(AudioRendererInfo.AudioStreamOutputFlag.AUDIO_STREAM_OUTPUT_FLAG_DIRECT_PCM)
                .bufferSizeInBytes(BUFFER_SIZE)
                .isOffload(false)
                .build();
        audioRenderer = new AudioRenderer(audioRendererInfo, AudioRenderer.PlayMode.MODE_STREAM);
        audioRenderer.setSampleRate(SAMPLING_RATE_IN_HZ);
        final float volume = 10.0f;
        audioRenderer.setVolume(volume);
    }

    public void writeBuffer(byte[] buffer) {
        audioRenderer.write(buffer, 0, buffer.length);
    }

    public boolean startPlayer() {
        return audioRenderer.start();
    }

    public void stopAndResetPlayer() {
        audioRenderer.stop();
        audioRenderer.flush();
    }
}

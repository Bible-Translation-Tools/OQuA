package org.wycliffeassociates.otter.jvm.device.audio.injection

import dagger.Module
import dagger.Provides
import device.IAudioRecorder
import device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioRecorderImpl
import javax.inject.Singleton

@Module
class AudioModule {
    @Provides
    fun providesRecorder(): IAudioRecorder = AudioRecorderImpl()

    @Provides
    @Singleton
    fun providesPlayer(): IAudioPlayer = AudioPlayer()
}
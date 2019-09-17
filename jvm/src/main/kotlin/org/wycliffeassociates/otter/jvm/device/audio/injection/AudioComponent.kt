package org.wycliffeassociates.otter.jvm.device.audio.injection

import dagger.Component
import javax.inject.Singleton
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.device.IAudioPlayer

@Component(modules = [AudioModule::class])
@Singleton
interface AudioComponent {
    fun injectRecorder(): IAudioRecorder
    fun injectPlayer(): IAudioPlayer
}
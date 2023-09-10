package platform.audio

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import utils.EventBus
import java.io.File

@OptIn(DelicateCoroutinesApi::class)
class AudioManager : EventBus() {
    private val job: Job

    init {
        // Note that this is only implemented for Windows, I have no idea how to implement it for other platforms
        val process = Runtime.getRuntime()
            .exec(File(System.getProperty("compose.application.resources.dir")).resolve("GetAudioPeak.exe").absolutePath)
        job = GlobalScope.launch {
            process.inputStream.bufferedReader().lineSequence().asFlow().collect { output ->
                output.trim().toDoubleOrNull()
                    ?.let { peak -> super.publish(AudioPeakUpdateEvent(peak)) }
            }
        }
    }

    protected fun finalize() {
        job.cancel()
    }
}
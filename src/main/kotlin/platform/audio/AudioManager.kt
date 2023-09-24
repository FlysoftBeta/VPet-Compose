package platform.audio

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import utils.EventBus
import utils.resourceDirectory

@OptIn(DelicateCoroutinesApi::class)
class AudioManager : EventBus() {
    private var job: Job? = null
    private var shutdownHook: Thread? = null

    init {
        try {
            // Get the real executable
            val helperExecutable = resourceDirectory.resolve(resourceDirectory.resolve("audio-helper").readText())

            // Note that this is only implemented for Windows/Linux with PipeWire, I have no idea how to implement it for other platforms
            val process = Runtime.getRuntime()
                .exec(helperExecutable.absolutePath)

            shutdownHook = Thread {
                process.destroy()
            }
            Runtime.getRuntime().addShutdownHook(shutdownHook)

            job = GlobalScope.launch {
                process.inputStream.bufferedReader().lineSequence().asFlow().collect { output ->
                    output.trim().toDoubleOrNull()
                        ?.let { peak -> super.publish(AudioPeakUpdateEvent(peak)) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun finalize() {
        job?.cancel()
        shutdownHook?.let { hook -> Runtime.getRuntime().removeShutdownHook(hook) }
    }
}
package resource.move

import resource.RawResource

data class MoveTrigger(val left: Int?, val top: Int?, val right: Int?, val bottom: Int?) {
    companion object {
        fun fromRawResource(rawResource: RawResource): MoveTrigger {
            return MoveTrigger(
                rawResource["TriggerLeft"]?.toInt(),
                rawResource["TriggerTop"]?.toInt(),
                rawResource["TriggerRight"]?.toInt(),
                rawResource["TriggerBottom"]?.toInt()
            )
        }
    }
}

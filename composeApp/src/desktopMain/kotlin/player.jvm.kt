import audio.DenpaPlayer
import audio.DenpaPlayerJVM
import audio.DenpaTrack

actual val createDenpaPlayer: DenpaPlayer<DenpaTrack> get() = DenpaPlayerJVM() as DenpaPlayer<DenpaTrack>
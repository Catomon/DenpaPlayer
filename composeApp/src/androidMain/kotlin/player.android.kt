import android.content.Context
import audio.DenpaPlayer
import audio.DenpaPlayerAndy
import audio.DenpaTrack

lateinit var playerContext: () -> Context

actual val createDenpaPlayer: DenpaPlayer<DenpaTrack> get() =
    DenpaPlayerAndy(playerContext()).apply { create() } as DenpaPlayer<DenpaTrack>
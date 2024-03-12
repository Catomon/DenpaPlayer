package audio

import kotlin.random.Random

interface DenpaTrack {
    val uri: String
    val id: String
    val author: String
    val name: String
    val duration: Long get() = Long.MAX_VALUE
}

val emptyDenpaTrack
    get() = object : DenpaTrack {
        override val uri: String = "uri"
        override val id: String = "id"
        override val author: String = "DenpaAuthor${Random.nextInt(0, 11)}"
        override val name: String = "Denpa Song Name"
    }
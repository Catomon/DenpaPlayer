interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

//actual fun getPlatform(): Platform {
//    return object : Platform {
//        override val name: String = "no idea"
//    }
//}
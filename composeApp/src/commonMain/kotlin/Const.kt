const val appNameJp = "電波プレーヤー"
const val appNameEng = "DenpaPlayer"

var appName = if (loadSettings().japaneseTitle) appNameJp else appNameEng

const val minWindowHeight = 108
const val minWindowWidth = 320

const val maxWindowHeight = 900
const val maxWindowWidth = 600
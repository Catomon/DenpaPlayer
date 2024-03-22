import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object Colors {
    val slightlyTransparentWhite = Color(255, 255, 255, 230)
    val halfTransparentWhite = Color(255, 255, 255, 255 / 2)
    val lightBlue = Color(2, 158, 224)
    val halfTransparentLightBlue = Color(56, 196, 255, 255 / 2)
    val pink = Color(255, 117, 236)
    val deepPink = Color(153, 0, 132)
    val lightPink = Color(255, 158, 242)
    val lightPinkHalfTransparent = Color(255, 158, 242, 255 / 2)
    val greenyBlue = Color(107, 255, 230)
    val yellow = Color(255, 222, 146)
    val violet = Color(87, 64, 125)
    val darkishViolet = Color(62, 46, 89)
    val grey = Color(73, 73, 73)
    val orange = Color(239, 151, 130)
    val oddRed = Color(239, 151, 130)

    ///

//    val backgroundPrimary = Color.White
//    val surfacePrimary = slightlyTransparentWhite
//    val surfaceSecondary = halfTransparentWhite
//    val specialPrimary = pink
//    val specialSecondary = lightBlue
//    val specialPrimaryTransparent = lightPinkHalfTransparent
//    val objectPrimary = Color.Black
//    val objectBackgroundPrimary = Color.White
//    val textPrimary = Color.Black
//    val textSecondary = Color.White
//    val textSpecial = pink
//    val textSpecial2 = lightBlue

    //white
    var backgroundPrimary by mutableStateOf(Color.White)
    var surfacePrimary by mutableStateOf(slightlyTransparentWhite)
    var surfaceSecondary by mutableStateOf(halfTransparentWhite)
    var specialPrimary by mutableStateOf(pink)
    var specialSecondary by mutableStateOf(lightBlue)
    var specialPrimaryTransparent by mutableStateOf(lightPinkHalfTransparent)
    var objectPrimary by mutableStateOf(Color.Black)
    var objectBackgroundPrimary by mutableStateOf(Color.White)
    var textPrimary by mutableStateOf(Color.Black)
    var textSecondary by mutableStateOf(Color.White)
    var textSpecial by mutableStateOf(pink)
    var textSpecial2 by mutableStateOf(lightBlue)


    //dark
//    val backgroundPrimary = violet
//    val surfacePrimary = darkishViolet.copy(alpha = 0.80f)
//    val surfaceSecondary = greenyBlue.copy(alpha = 0.60f)
//    val specialPrimary = yellow
//    val specialPrimaryTransparent = orange
//    val specialSecondary = lightBlue
//    val objectPrimary = Color.Black
//    val objectBackgroundPrimary = darkishViolet
//    val textPrimary = Color.White
//    val textSecondary = Color.White
//    val textSpecial = pink
//    val textSpecial2 = lightBlue

    fun whiteTheme() {
        backgroundPrimary = Color.White
        surfacePrimary = slightlyTransparentWhite
        surfaceSecondary = halfTransparentWhite
        specialPrimary = pink
        specialSecondary = lightBlue
        specialPrimaryTransparent = lightPinkHalfTransparent
        objectPrimary = Color.Black
        objectBackgroundPrimary = Color.White
        textPrimary = Color.Black
        textSecondary = Color.White
        textSpecial = pink
        textSpecial2 = lightBlue
    }

    fun darkTheme() {
        backgroundPrimary = violet
        surfacePrimary = darkishViolet.copy(alpha = 0.80f)
        surfaceSecondary = greenyBlue.copy(alpha = 0.60f)
        specialPrimary = yellow
        specialPrimaryTransparent = orange
        specialSecondary = lightBlue
        objectPrimary = Color.Black
        objectBackgroundPrimary = darkishViolet
        textPrimary = Color.White
        textSecondary = Color.White
        textSpecial = pink
        textSpecial2 = lightBlue
    }
}
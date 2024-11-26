package com.TraSka.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.TraSka.R

object AppFont {
    val RobotoFontFamily = FontFamily(
        Font(R.font.roboto_regular, FontWeight.Normal),
        Font(R.font.roboto_italic, FontWeight.Normal, style = FontStyle.Italic),
        Font(R.font.roboto_bold, FontWeight.Bold),
        Font(R.font.roboto_bolditalic, FontWeight.Bold, style = FontStyle.Italic),
        Font(R.font.roboto_light, FontWeight.Light),
        Font(R.font.roboto_lightitalic, FontWeight.Light, style = FontStyle.Italic),
        Font(R.font.roboto_medium, FontWeight.Medium),
        Font(R.font.roboto_mediumitalic, FontWeight.Medium, style = FontStyle.Italic),
        Font(R.font.roboto_black, FontWeight.Black),
        Font(R.font.roboto_blackitalic, FontWeight.Black, style = FontStyle.Italic),
        Font(R.font.roboto_thin, FontWeight.Thin),
        Font(R.font.roboto_thinitalic, FontWeight.Thin, style = FontStyle.Italic)
    )
}

private val defaultTypography = Typography()
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = AppFont.RobotoFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = AppFont.RobotoFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = AppFont.RobotoFontFamily),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = AppFont.RobotoFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = AppFont.RobotoFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = AppFont.RobotoFontFamily),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = AppFont.RobotoFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = AppFont.RobotoFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = AppFont.RobotoFontFamily),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = AppFont.RobotoFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = AppFont.RobotoFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = AppFont.RobotoFontFamily),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = AppFont.RobotoFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = AppFont.RobotoFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = AppFont.RobotoFontFamily)
)
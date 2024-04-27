package com.sam.imagespeak.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sam.imagespeak.R

val montserrat = FontFamily(
        Font(R.font.montserrat_black, FontWeight.Black, FontStyle.Normal),
        Font(R.font.montserrat_blackitalic, FontWeight.Black, FontStyle.Italic),
        Font(R.font.montserrat_bold, FontWeight.Bold, FontStyle.Normal),
        Font(R.font.montserrat_bolditalic, FontWeight.Bold, FontStyle.Italic),
        Font(R.font.montserrat_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
        Font(R.font.montserrat_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
        Font(R.font.montserrat_extralight, FontWeight.ExtraLight, FontStyle.Normal),
        Font(R.font.montserrat_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(R.font.montserrat_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.montserrat_light, FontWeight.Light, FontStyle.Normal),
        Font(R.font.montserrat_lightitalic, FontWeight.Light, FontStyle.Italic),
        Font(R.font.montserrat_medium, FontWeight.Medium, FontStyle.Normal),
        Font(R.font.montserrat_mediumitalic, FontWeight.Medium, FontStyle.Italic),
        Font(R.font.montserrat_regular, FontWeight.Normal, FontStyle.Normal),
        Font(R.font.montserrat_semibold, FontWeight.SemiBold, FontStyle.Normal),
        Font(R.font.montserrat_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
        Font(R.font.montserrat_thin, FontWeight.Thin, FontStyle.Normal),
        Font(R.font.montserrat_thinitalic, FontWeight.Thin, FontStyle.Italic)
)

// Set of Material typography styles to start with
val Typography = Typography(
        headlineLarge = TextStyle(
                fontFamily = montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 32.sp
        )

        /* Default text styles to override
        bodyLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
        ),

            titleLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
            labelSmall = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            )
            */
)
package com.example.aulatelas.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.aulamobile.R

// Fonte principal do app (Poppins). Os arquivos ficam em res/font.
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

// Fonte dos títulos grandes (Bem-vindo, Entrar, Cadastrar).
// Para trocar a fonte dos títulos em todas as telas, mude só esta linha.
val TitleFont = Poppins

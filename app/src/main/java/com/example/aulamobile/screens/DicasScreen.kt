package com.example.aulatelas.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Green = Color(0xFF198754)
private val LightGreen = Color(0xFFEAF6EF)
private val Ink = Color(0xFF1C2922)

private data class Tip(val icon: ImageVector, val title: String, val text: String)

private val tips = listOf(
    Tip(
        Icons.Default.Schedule,
        "Tome sempre no mesmo horário",
        "Criar uma rotina com horários fixos ajuda a lembrar das doses. Use a aba Minha rotina para acompanhar."
    ),
    Tip(
        Icons.Default.Info,
        "Leia a receita e a bula",
        "Em caso de dúvida sobre dose, horário ou interação com outros remédios, fale com seu médico ou farmacêutico."
    ),
    Tip(
        Icons.Default.Restaurant,
        "Atenção à alimentação",
        "Alguns medicamentos devem ser tomados com ou sem comida. Siga o que está na sua receita."
    ),
    Tip(
        Icons.Default.WaterDrop,
        "Hidrate-se",
        "Manter o corpo hidratado faz parte do cuidado diário. Pergunte ao seu médico qual a quantidade ideal para você."
    ),
    Tip(
        Icons.Default.Bedtime,
        "Cuide do sono",
        "Dormir bem ajuda no bem-estar. Se tiver doses à noite, deixe o horário cadastrado no app."
    ),
    Tip(
        Icons.Default.Home,
        "Guarde com cuidado",
        "Mantenha os medicamentos em local seco e fresco, longe do alcance de crianças e fora do prazo de validade."
    )
)

@Composable
fun DicasScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                Modifier.fillMaxWidth()
                    .background(LightGreen, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Text(
                    "Dicas de cuidado",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Pequenos hábitos que ajudam a manter a rotina em dia.",
                    color = Color(0xFF496054)
                )
            }
        }
        item {
            Text(
                "PARA O SEU DIA A DIA",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
        items(tips) { tip ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        Modifier.size(44.dp).background(LightGreen, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) { Icon(tip.icon, null, tint = Green) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            tip.title,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            tip.text,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        item {
            Text(
                "Estas dicas são gerais e não substituem a orientação de um profissional de saúde.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

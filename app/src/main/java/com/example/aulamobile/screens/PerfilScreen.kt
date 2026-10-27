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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

private val Green = Color(0xFF198754)
private val LightGreen = Color(0xFFEAF6EF)
private val Ink = Color(0xFF1C2922)
private val Red = Color(0xFFB42318)

@Composable
fun PerfilScreen(
    total: Int,
    taken: Int,
    late: Int,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    val email = FirebaseAuth.getInstance().currentUser?.email ?: "Usuário local"

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                Modifier.fillMaxWidth()
                    .background(Green, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(72.dp).background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Person, null, tint = Green, modifier = Modifier.size(40.dp)) }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Minha conta",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(email, color = Color.White.copy(alpha = .9f), textAlign = TextAlign.Center)
            }
        }
        item {
            Text(
                "RESUMO DE HOJE",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("Cadastrados", total.toString(), Ink, Modifier.weight(1f))
                StatCard("Tomadas", taken.toString(), Green, Modifier.weight(1f))
                StatCard("Atrasadas", late.toString(), if (late > 0) Red else Ink, Modifier.weight(1f))
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LightGreen)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        "Sobre o app",
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Os medicamentos ficam salvos neste aparelho, separados por usuário. O app não envia notificações e não calcula dosagem. Siga sempre a orientação de um profissional de saúde.",
                        color = Color(0xFF496054),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        item {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red, contentColor = Color.White)
            ) { Text("Sair da conta", fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                color = valueColor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }
    }
}

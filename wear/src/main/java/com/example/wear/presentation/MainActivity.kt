package com.example.wear.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : ComponentActivity() {

    private val expensesState = mutableStateListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Referencia a Firebase Realtime Database
        val database = Firebase.database.reference.child("gastos")

        // 🔹 Escuchar cambios en Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                expensesState.clear()
                expensesState.addAll(list)
                Log.d("WearApp", "📩 Lista actualizada desde Firebase: ${list.size} elementos")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WearApp", "❌ Error leyendo Firebase: ${error.message}")
            }
        })

        // 🔹 Configurar la UI con Jetpack Compose
        setContent { WearApp(expensesState) }
    }
}

@Composable
fun WearApp(expenses: List<String>) {
    MaterialTheme {
        if (expenses.isEmpty()) {
            Text(
                "Sin datos todavía...",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "📊 Gastos sincronizados",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(expenses, key = { it.hashCode() }) { expense ->
                    Text(expense, modifier = Modifier.padding(6.dp))
                }
            }
        }

        LaunchedEffect(expenses) {
            Log.d("WearApp", "🔄 UI recomposed con ${expenses.size} elementos")
        }
    }
}

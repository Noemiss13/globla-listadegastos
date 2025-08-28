package com.example.wear.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.google.firebase.database.*

class MainActivity : ComponentActivity() {

    private lateinit var database: DatabaseReference
    private val messageState = mutableStateOf("Esperando conexión...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Referencia al nodo "test_connection" en Firebase
        database = FirebaseDatabase.getInstance().reference.child("test_connection")

        // 🔹 Escuchar cambios en Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val msg = snapshot.getValue(String::class.java) ?: "Mensaje vacío"
                Log.d("WearOS", "📩 Mensaje recibido: $msg")
                messageState.value = msg
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WearOS", "❌ Error leyendo Firebase: ${error.message}")
            }
        })

        // 🔹 Configurar UI con Compose
        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = messageState.value,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

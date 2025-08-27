package com.example.weaos_registrodegastos_noemi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.wearable.*
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.coroutines.*

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    private val expensesState = mutableStateListOf<String>()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Inicializar Firebase
        database = FirebaseDatabase.getInstance().reference.child("gastos")

        // Registrar listener de Wear
        Wearable.getMessageClient(this).addListener(this)

        // Escuchar cambios en Firebase (solo para inicialización)
        database.get().addOnSuccessListener { snapshot ->
            val list = snapshot.children.mapNotNull { it.getValue(String::class.java) }
            expensesState.clear()
            expensesState.addAll(list)
        }

        setContent {
            MobileApp(expensesState) { newExpense ->
                addExpense(newExpense)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        Wearable.getMessageClient(this).removeListener(this)
        super.onPause()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/request_expenses") {
            Log.d("MobileApp", "📩 Wear solicitó la lista de gastos")
            sendExpensesToWear()
        }
    }

    private fun addExpense(expense: String) {
        // 🔹 Guardar en Firebase
        val key = database.push().key
        if (key != null) {
            database.child(key).setValue(expense)
        }

        // 🔹 Actualizar lista local
        expensesState.add(expense)

        // 🔹 Enviar automáticamente al Wear OS
        sendExpensesToWear()
    }

    private fun sendExpensesToWear() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nodes = Wearable.getNodeClient(this@MainActivity).connectedNodes.await()
                val json = Gson().toJson(expensesState.toList())

                Log.d("MobileApp", "📤 Enviando lista al Wear: $json")

                for (node in nodes) {
                    Wearable.getMessageClient(this@MainActivity)
                        .sendMessage(node.id, "/sync_expenses", json.toByteArray())
                        .await()
                }
                Log.d("MobileApp", "✅ Lista de gastos enviada al Wear")
            } catch (e: Exception) {
                Log.e("MobileApp", "❌ Error enviando gastos: ${e.message}", e)
            }
        }
    }
}

@Composable
fun MobileApp(expenses: List<String>, onAddExpense: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text("📊 Gastos registrados", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nuevo gasto") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onAddExpense(text)
                        text = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar gasto")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (expenses.isEmpty()) {
                Text("No hay gastos registrados aún")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(expenses) { expense ->
                        Text(expense, modifier = Modifier.padding(6.dp))
                    }
                }
            }
        }
    }
}

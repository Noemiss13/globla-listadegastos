package com.example.wear.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    private val expensesState = mutableStateListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registrar listener una sola vez
        Wearable.getMessageClient(this).addListener(this)

        setContent { WearApp(expensesState) }

        // Pedir la lista al móvil después de setContent
        requestExpensesFromMobile()
    }

    override fun onResume() {
        super.onResume()
        // Re-pedir lista al móvil cada vez que se reanuda la actividad
        requestExpensesFromMobile()
        Log.d("WearApp", "onResume: listener activo, solicitando lista al móvil")
    }

    override fun onPause() {
        super.onPause()
        Log.d("WearApp", "onPause: listener sigue activo")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            "/sync_expenses" -> {
                val json = String(messageEvent.data)
                val type = object : TypeToken<List<String>>() {}.type
                val newList: List<String> = Gson().fromJson(json, type)

                runOnUiThread {
                    expensesState.clear()
                    expensesState.addAll(newList)
                }

                Log.d("WearApp", "📩 Lista recibida: ${newList.size} gastos")
                Log.d("WearApp", "Contenido: $newList")
            }
        }
    }

    private fun requestExpensesFromMobile() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nodes = Wearable.getNodeClient(this@MainActivity).connectedNodes.await()
                if (nodes.isEmpty()) {
                    Log.w("WearApp", "⚠️ No hay nodos conectados")
                    return@launch
                }

                for (node in nodes) {
                    Wearable.getMessageClient(this@MainActivity)
                        .sendMessage(node.id, "/request_expenses", ByteArray(0))
                        .await()
                    Log.d("WearApp", "📤 Petición enviada al nodo: ${node.displayName}")
                }
            } catch (e: Exception) {
                Log.e("WearApp", "❌ Error solicitando gastos: ${e.message}", e)
            }
        }
    }
}

@Composable
fun WearApp(expenses: List<String>) {
    // Observamos la lista y forzamos recomposición si cambia
    val currentExpenses by rememberUpdatedState(newValue = expenses)

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (currentExpenses.isEmpty()) {
                Text(
                    "Sin datos todavía...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "📊 Gastos sincronizados",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(currentExpenses, key = { it.hashCode() }) { expense ->
                            Text(expense, modifier = Modifier.padding(6.dp))
                        }
                    }
                }
            }
        }

        // Log cada vez que la lista cambia
        LaunchedEffect(currentExpenses) {
            Log.d("WearApp", "🔄 UI recomposed con ${currentExpenses.size} elementos")
        }
    }
}

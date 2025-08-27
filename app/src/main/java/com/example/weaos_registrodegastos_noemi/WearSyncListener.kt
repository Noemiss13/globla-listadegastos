package com.example.weaos_registrodegastos_noemi

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearRequestListener : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/request_expenses") {
            Log.d("MobileApp", "📩 Wear solicitó la lista de gastos")

            // Obtenemos la lista de gastos desde tu app móvil
            val expenses = loadExpenses()
            val json = Gson().toJson(expenses)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val nodes = Wearable.getNodeClient(this@WearRequestListener).connectedNodes.await()
                    for (node in nodes) {
                        Wearable.getMessageClient(this@WearRequestListener)
                            .sendMessage(node.id, "/sync_expenses", json.toByteArray())
                            .await()
                    }
                    Log.d("MobileApp", "📤 Lista enviada al Wear (${expenses.size} items)")
                } catch (e: Exception) {
                    Log.e("MobileApp", "❌ Error respondiendo al Wear: ${e.message}", e)
                }
            }
        } else {
            super.onMessageReceived(messageEvent)
        }
    }

    // Función que devuelve la lista de gastos
    private fun loadExpenses(): List<String> {
        // Aquí debes obtener los datos reales de tu app
        // Por ahora, datos simulados:
        return listOf("Gasto 1: $100", "Gasto 2: $50")
    }
}

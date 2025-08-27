package com.example.weaos_registrodegastos_noemi

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object WearSyncHelper {

    private const val PATH_SYNC = "/sync_expenses"

    fun sendExpenses(context: Context, expenses: List<String>) {
        val json = Gson().toJson(expenses)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nodeClient = Wearable.getNodeClient(context)
                val nodes = nodeClient.connectedNodes.await()

                for (node: Node in nodes) {
                    Wearable.getMessageClient(context)
                        .sendMessage(node.id, PATH_SYNC, json.toByteArray())
                        .await()
                    Log.d("WearSyncHelper", "✅ Gastos enviados a ${node.displayName}")
                }
            } catch (e: Exception) {
                Log.e("WearSyncHelper", "❌ Error enviando datos: ${e.message}")
            }
        }
    }
}

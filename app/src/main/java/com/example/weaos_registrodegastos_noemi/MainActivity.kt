package com.example.weaos_registrodegastos_noemi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

class MainActivity : ComponentActivity() {

    private val expensesState = mutableStateListOf<String>()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Inicializar Firebase
        database = FirebaseDatabase.getInstance().reference.child("gastos")

        // 🔹 Escuchar cambios en Firebase (solo para inicialización)
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

    private fun addExpense(expense: String) {
        // 🔹 Guardar en Firebase
        val key = database.push().key
        if (key != null) {
            database.child(key).setValue(expense)
        }

        // 🔹 Actualizar lista local
        expensesState.add(expense)
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

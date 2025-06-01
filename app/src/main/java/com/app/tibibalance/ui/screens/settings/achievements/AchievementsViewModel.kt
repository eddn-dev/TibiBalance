package com.app.tibibalance.ui.screens.settings.achievements

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@HiltViewModel
class AchievementsViewModel @Inject constructor() : ViewModel() {

    private val _logros = MutableStateFlow<Map<String, Logro>>(emptyMap())
    val logros: StateFlow<Map<String, Logro>> = _logros

    init {
        loadLogros()
    }

    private fun loadLogros() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("achievements")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val mapa = snapshot.documents.associate { doc ->
                        val id = doc.id
                        val progress = doc.getLong("progress")?.toInt() ?: 0
                        val unlocked = doc.getBoolean("unlock") ?: false
                        id to Logro(id, progress, unlocked)
                    }
                    _logros.value = mapa
                    verifyAndUpdate(mapa)
                }
            }
    }
}

private fun verifyAndUpdate(logrosMap: Map<String, Logro>) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    for ((id, logro) in logrosMap) {
        if (logro.progress >= 100 && !logro.unlocked) {
            // Actualiza el campo "desbloqueado" en Firestore
            firestore.collection("users")
                .document(userId)
                .collection("achievements")
                .document(id)
                .update("unlock", true)
        }
    }
}


// Notar que ahora usamos nombres en inglés para que coincidan con los usados en la UI
data class Logro(
    val id: String,
    val progress: Int,
    val unlocked: Boolean
)
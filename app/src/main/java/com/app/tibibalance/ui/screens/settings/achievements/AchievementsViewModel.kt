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
            .collection("logros")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val mapa = snapshot.documents.associate { doc ->
                        val id = doc.id
                        val progreso = doc.getLong("progreso")?.toInt() ?: 0
                        val desbloqueado = doc.getBoolean("desbloqueado") ?: false
                        id to Logro(id, progreso, desbloqueado)
                    }
                    _logros.value = mapa
                }
            }
    }
}

data class Logro(
    val id: String,
    val progress: Int,
    val unlocked: Boolean
)

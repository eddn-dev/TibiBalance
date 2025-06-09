package com.app.data.alert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.app.domain.usecase.emotions.SaveEmotion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject


@AndroidEntryPoint
class EmotionChoiceReceiver : BroadcastReceiver() {

    @Inject
    lateinit var saveEmotion: SaveEmotion

    // scope ad-hoc que vive sólo lo que dura la tarea
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(ctx: Context, intent: Intent) {

        val choice  = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence("emotion_choice")?.toString() ?: return

        val emojiId = when (choice) {
            "🙂" -> "HAPPY"
            "😐" -> "CALM"
            "🙁" -> "SAD"
            "😡" -> "ANGRY"
            "🤢" -> "DISGUST"
            else -> "FEAR"
        }

        val dateUtc = intent.getLongExtra("dateUtc", 0L)
        val date    = Instant.fromEpochMilliseconds(dateUtc)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date

        // ─── Lanza la tarea en hilo IO ───
        scope.launch {
            saveEmotion(date, emojiId)
        }
    }
}
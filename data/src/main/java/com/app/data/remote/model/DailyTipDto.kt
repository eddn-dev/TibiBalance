/* :data/remote/model/DailyTipDto.kt */
package com.app.data.remote.model

import androidx.annotation.Keep
import com.app.domain.entities.Challenge
import com.app.domain.entities.DailyTip
import com.app.domain.entities.Link
import com.app.domain.entities.Item
import com.app.domain.entities.Text
import kotlinx.serialization.Serializable

@Keep                       // evita ProGuard/R8 stripping
@Serializable
data class DailyTipDto(
    val id: Int            = 0,
    val title: String      = "",
    val subtitle: String   = "",
    val icon: String       = "",
    val active: Boolean    = true,
    val content: List<ContentDto> = emptyList()
) {
    /** ctor sin-args requerido por Firestore */
    @Suppress("unused") constructor() : this(0, "", "", "", true, emptyList())

    @Keep
    @Serializable
    data class ContentDto(
        val text: String,
        val type: String,
        val icon: String? = null,
        val target: String? = null     // sólo links
    ) {
        @Suppress("unused") constructor() : this("", "text", null)
    }

    /* Mapper a dominio ----------------------------------------- */

    /* :data/remote/model/DailyTipDto.kt */
    fun toDomain() = DailyTip(
        id, title, subtitle, icon,
        content = content.map {
            when (it.type) {
                "item"       -> Item(it.text, it.icon.orEmpty())
                "link"       -> Link(it.text, it.target.orEmpty())
                "challenge"  -> Challenge(it.text)
                else         -> Text(it.text)
            }
        },
        active
    )
}

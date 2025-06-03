/* :domain/entities/DailyTip.kt */
package com.app.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyTip(
    val id       : Int,
    val title    : String,
    val subtitle : String,
    val icon     : String,
    val content  : List<DailyTipItem>,
    val active   : Boolean
)

/* Polimórfico: ahora tiene 4 subtipos */
@Serializable
sealed interface DailyTipItem { val text: String }

@Serializable @SerialName("text")
data class Text(override val text: String) : DailyTipItem

@Serializable @SerialName("item")
data class Item(
    override val text: String,
    val icon: String
) : DailyTipItem

@Serializable @SerialName("link")
data class Link(
    override val text: String,
    val target: String            // URL o deeplink
) : DailyTipItem

@Serializable @SerialName("challenge")
data class Challenge(
    override val text: String
) : DailyTipItem

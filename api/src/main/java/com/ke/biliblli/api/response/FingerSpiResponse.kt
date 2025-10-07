package com.ke.biliblli.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FingerSpiResponse(
    @SerialName("b_3")
    val b3: String? = null
)

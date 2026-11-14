package com.example.budgetpulse.data.remote

import com.google.gson.annotations.SerializedName

data class AdviceResponse(
    @SerializedName("slip")
    val slip: AdviceSlip
)

data class AdviceSlip(
    @SerializedName("id")
    val id: Int,
    @SerializedName("advice")
    val advice: String
)

data class FinancialInsight(
    val id: Int,
    val title: String,
    val tipText: String,
    val bannerImageUrl: String
)

package com.example.budgetpulse.data.remote

import retrofit2.http.GET

interface FinancialApiService {
    @GET("advice")
    suspend fun getRandomFinancialAdvice(): AdviceResponse
}

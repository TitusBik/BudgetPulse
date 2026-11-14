package com.example.budgetpulse.di

import com.example.budgetpulse.data.remote.FinancialApiService
import com.example.budgetpulse.data.repository.FinancialInsightsRepository
import com.example.budgetpulse.data.repository.NetworkFinancialInsightsRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val financialInsightsRepository: FinancialInsightsRepository
}

class DefaultAppContainer : AppContainer {

    private val baseUrl = "https://api.adviceslip.com/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofitService: FinancialApiService by lazy {
        retrofit.create(FinancialApiService::class.java)
    }

    override val financialInsightsRepository: FinancialInsightsRepository by lazy {
        NetworkFinancialInsightsRepository(retrofitService)
    }
}

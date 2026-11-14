package com.example.budgetpulse.data.repository

import com.example.budgetpulse.data.remote.FinancialApiService
import com.example.budgetpulse.data.remote.FinancialInsight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface FinancialInsightsRepository {
    suspend fun fetchDailyFinancialInsight(): Result<FinancialInsight>
}

class NetworkFinancialInsightsRepository(
    private val apiService: FinancialApiService
) : FinancialInsightsRepository {

    // Curated high-resolution financial banner images loaded remotely via Coil
    private val bannerUrls = listOf(
        "https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1554224155-6726b3ff858f?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1565514020179-026b92b84bb6?auto=format&fit=crop&w=800&q=80"
    )

    override suspend fun fetchDailyFinancialInsight(): Result<FinancialInsight> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRandomFinancialAdvice()
                val banner = bannerUrls[response.slip.id % bannerUrls.size]
                Result.success(
                    FinancialInsight(
                        id = response.slip.id,
                        title = "Daily Smart Money Tip #${response.slip.id}",
                        tipText = response.slip.advice,
                        bannerImageUrl = banner
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

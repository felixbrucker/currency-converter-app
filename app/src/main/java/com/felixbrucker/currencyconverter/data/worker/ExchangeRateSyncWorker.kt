package com.felixbrucker.currencyconverter.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.felixbrucker.currencyconverter.data.repository.CurrencyRepository
import java.util.concurrent.TimeUnit

class ExchangeRateSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "ExchangeRateSyncWorker executing background rate sync...")
        val repository = CurrencyRepository.getInstance(applicationContext)
        val result = repository.refreshRates()
        return if (result.isSuccess) {
            Log.d(TAG, "Background sync successful: updated ${result.getOrNull()} currencies")
            Result.success()
        } else {
            Log.w(TAG, "Background sync failed: ${result.exceptionOrNull()?.message}")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "ExchangeRateSyncWorker"
        private const val WORK_NAME = "periodic_exchange_rate_sync"

        fun schedule(context: Context, intervalHours: Long, enabled: Boolean) {
            val workManager = WorkManager.getInstance(context)
            if (!enabled) {
                workManager.cancelUniqueWork(WORK_NAME)
                Log.d(TAG, "Periodic sync cancelled by user settings")
                return
            }

            val safeInterval = intervalHours.coerceAtLeast(1)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<ExchangeRateSyncWorker>(
                safeInterval,
                TimeUnit.HOURS,
                15, // Flex interval
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
            Log.d(TAG, "Scheduled periodic sync every $safeInterval hours")
        }
    }
}

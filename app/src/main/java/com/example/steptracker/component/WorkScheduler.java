package com.example.steptracker.component;

import android.content.Context;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.steptracker.worker.FitnessWorker;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class WorkScheduler {
    private static final String FITNESS_WORK_NAME = "FitnessDataWork";

    private WorkScheduler() {
    }

    public static void startPeriodicFitnessWork(Context context) {
        Timber.d("Scheduling periodic FitnessWorker...");

        PeriodicWorkRequest fitnessWorkRequest =
            new PeriodicWorkRequest.Builder(
                FitnessWorker.class,
                15, TimeUnit.MINUTES
            ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            FITNESS_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            fitnessWorkRequest
        );
    }

    public static void stopPeriodicFitnessWork(Context context) {
        Timber.d("Cancelling periodic FitnessWorker...");
        WorkManager.getInstance(context).cancelUniqueWork(FITNESS_WORK_NAME);
    }
}

package com.example.steptracker.worker;

import android.content.Context;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.steptracker.data.repository.FitnessRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.FitnessOptions;
import lombok.NonNull;
import timber.log.Timber;

public class FitnessWorker extends Worker {

    public FitnessWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Timber.d("FitnessWorker started!");

        FitnessOptions fitnessOptions = FitnessOptions.builder()
            .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(com.google.android.gms.fitness.data.DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
            .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
            .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(getApplicationContext(), fitnessOptions);

        if (account == null) {
            Timber.e("GoogleSignInAccount is null! Cannot send fitness data.");
            return Result.failure();
        }

        FitnessRepository repository = new FitnessRepository(getApplicationContext(), account);
        repository.collectAndSendDailyFitnessData();
        return Result.success();
    }
}

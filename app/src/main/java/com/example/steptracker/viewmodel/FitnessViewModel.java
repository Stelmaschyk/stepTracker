package com.example.steptracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.steptracker.data.repository.FitnessRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.FitnessOptions;
import lombok.Getter;

public class FitnessViewModel extends AndroidViewModel {
    private final MutableLiveData<Integer> stepCount = new MutableLiveData<>();
    private final MutableLiveData<Float> heartRate = new MutableLiveData<>();
    private final MutableLiveData<Float> calories = new MutableLiveData<>();
    private final MutableLiveData<Float> distance = new MutableLiveData<>();
    private final MutableLiveData<Integer> moveMinutes = new MutableLiveData<>();
    private final MutableLiveData<Integer> sleep = new MutableLiveData<>();

    @Getter
    private final FitnessOptions fitnessOptions;
    private FitnessRepository repository;

    public FitnessViewModel(@NonNull Application application) {
        super(application);
        fitnessOptions = FitnessOptions.builder()
            .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(com.google.android.gms.fitness.data.DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
            .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
            .build();
    }

    public LiveData<Integer> getStepCount() {
        return stepCount;
    }

    public LiveData<Float> getHeartRate() {
        return heartRate;
    }

    public LiveData<Float> getCalories() {
        return calories;
    }

    public LiveData<Float> getDistance() {
        return distance;
    }

    public LiveData<Integer> getMoveMinutes() {
        return moveMinutes;
    }

    public LiveData<Integer> getSleep() {
        return sleep;
    }

    public void initializeRepository(GoogleSignInAccount account) {
        if (repository == null) {
            repository = new FitnessRepository(getApplication(), account);
        }
    }

    public void loadFitnessData() {
        if (repository == null) return;

        repository.getStepsCount(stepCount::postValue);
        repository.getHeartRate(heartRate::postValue);
        repository.getCalories(calories::postValue);
        repository.getDistance(distance::postValue);
        repository.getMoveMinutes(moveMinutes::postValue);
        repository.getSleep(sleep::postValue);
    }

    public void sendFitnessData() {
        if (repository != null) {
            repository.collectAndSendDailyFitnessData();
        }
    }
}

package com.example.steptracker.data.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.steptracker.data.controller.ApiService;
import com.example.steptracker.data.dto.GoogleFitRequestDto;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class FitnessRepository {
    private static final String BASE_URL = "http://10.0.2.2:8080/fit/";
    private final Context context;
    private final GoogleSignInAccount account;
    private final ApiService apiService;


    public FitnessRepository(Context context, GoogleSignInAccount account) {
        this.context = context;
        this.account = account;

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        this.apiService = retrofit.create(ApiService.class);
    }

    public interface DataCallback<T> {
        void onData(T data);
    }

    public void getStepsCount(@NonNull DataCallback<Integer> callback) {
        Fitness.getHistoryClient(context, account)
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener(dataSet -> {
                int totalSteps = dataSet.isEmpty() ? 0 :
                    dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                callback.onData(totalSteps);
            });
    }

    public void getHeartRate(@NonNull DataCallback<Float> callback) {
        Fitness.getHistoryClient(context, account)
            .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
            .addOnSuccessListener(dataSet -> {
                float bpm = dataSet.isEmpty() ? 0f :
                    dataSet.getDataPoints().get(0).getValue(Field.FIELD_BPM).asFloat();
                callback.onData(bpm);
            });
    }

    public void getCalories(@NonNull DataCallback<Float> callback) {
        Fitness.getHistoryClient(context, account)
            .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener(dataSet -> {
                float kcal = dataSet.isEmpty() ? 0f :
                    dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();
                callback.onData(kcal);
            });
    }

    public void getDistance(@NonNull DataCallback<Float> callback) {
        Fitness.getHistoryClient(context, account)
            .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
            .addOnSuccessListener(dataSet -> {
                float dist = dataSet.isEmpty() ? 0f :
                    dataSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE).asFloat();
                callback.onData(dist);
            });
    }

    public void getMoveMinutes(@NonNull DataCallback<Integer> callback) {
        Fitness.getHistoryClient(context, account)
            .readDailyTotal(DataType.TYPE_MOVE_MINUTES)
            .addOnSuccessListener(dataSet -> {
                int minutes = dataSet.isEmpty() ? 0 :
                    dataSet.getDataPoints().get(0).getValue(Field.FIELD_DURATION).asInt();
                callback.onData(minutes);
            });
    }

    public void getSleep(@NonNull DataCallback<Integer> callback) {
        Fitness.getHistoryClient(context, account)
            .readDailyTotal(DataType.TYPE_MOVE_MINUTES)
            .addOnSuccessListener(dataSet -> {
                int sleepMinutes = dataSet.isEmpty() ? 0 :
                    dataSet.getDataPoints().get(0).getValue(Field.FIELD_DURATION).asInt();
                callback.onData(sleepMinutes);
            });
    }

    // ✅ 2️⃣ Новий метод: збирає все разом і відправляє!
    public void collectAndSendDailyFitnessData() {
        AtomicInteger steps = new AtomicInteger();
        AtomicReference<Float> heartRate = new AtomicReference<>();
        AtomicReference<Float> calories = new AtomicReference<>();
        AtomicReference<Float> distance = new AtomicReference<>();
        AtomicInteger moveMinutes = new AtomicInteger();
        AtomicInteger sleepMinutes = new AtomicInteger();


        getStepsCount(data -> {
            steps.set(data);
            checkAndSend(steps, heartRate, calories, distance, moveMinutes, sleepMinutes);
        });
        getHeartRate(data -> {
            heartRate.set(data);
            checkAndSend(steps, heartRate, calories, distance, moveMinutes, sleepMinutes);
        });
        getCalories(data -> {
            calories.set(data);
            checkAndSend(steps, heartRate, calories, distance, moveMinutes, sleepMinutes);
        });
        getDistance(data -> {
            distance.set(data);
            checkAndSend(steps, heartRate, calories, distance, moveMinutes, sleepMinutes);
        });
        getMoveMinutes(data -> {
            sleepMinutes.set(data);
            checkAndSend(steps, heartRate, calories, distance, moveMinutes, sleepMinutes);
        });
    }

    private void checkAndSend(
        AtomicInteger steps,
        AtomicReference<Float> heartRate,
        AtomicReference<Float> calories,
        AtomicReference<Float> distance,
        AtomicInteger moveMinutes,
         AtomicInteger sleepMinutes
    ) {
        if (heartRate.get() != null && calories.get() != null && distance.get() != null &&
            steps.get() != 0 && moveMinutes.get() != 0 && sleepMinutes.get() != 0) {

            GoogleFitRequestDto dto = new GoogleFitRequestDto(
                steps.get(),
                heartRate.get(),
                calories.get(),
                distance.get(),
                moveMinutes.get(),
                sleepMinutes.get()
            );

            apiService.sendFitnessData(dto).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Timber.d("Fitness data successfully sent to server.");
                    } else {
                        Timber.e("Failed to send fitness data. Server responded with code: %d and message: %s",
                            response.code(), response.message());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Timber.e(t, "Error sending fitness data to server");
                }
            });

        }
    }
}

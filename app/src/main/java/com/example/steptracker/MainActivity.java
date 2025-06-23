package com.example.steptracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION_CODE = 2;

    private TextView stepCountTextView;
    private TextView heartRateValue;
    private TextView caloriesValue;
    private TextView distanceValue;
    private TextView moveValue;
    private TextView sleepValue;

    private FitnessOptions fitnessOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stepCountTextView = findViewById(R.id.stepsValue);
        heartRateValue = findViewById(R.id.heartRateValue);
        caloriesValue = findViewById(R.id.caloriesValue);
        distanceValue = findViewById(R.id.distanceValue);
        moveValue = findViewById(R.id.moveValue);
        sleepValue = findViewById(R.id.sleepValue);

        Button refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(view -> readAllFitnessData());

        fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
            .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                REQUEST_OAUTH_REQUEST_CODE,
                account,
                fitnessOptions
            );
        } else {
            checkActivityRecognitionPermission();
        }
    }

    private void checkActivityRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                REQUEST_ACTIVITY_RECOGNITION_PERMISSION_CODE
            );
        } else {
            readAllFitnessData();
        }
    }

    private void readAllFitnessData() {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener(dataSet -> {
                int totalSteps = dataSet.isEmpty() ? 0 :
                    dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                stepCountTextView.setText(String.valueOf(totalSteps));
            });

        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
            .addOnSuccessListener(dataSet -> {
                String bpm = dataSet.isEmpty() ? "-- bpm" :
                    dataSet.getDataPoints().get(0).getValue(Field.FIELD_BPM).asFloat() + " bpm";
                heartRateValue.setText(bpm);
            });

        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener(dataSet -> {
                String kcal = dataSet.isEmpty() ? "-- kcal" :
                    String.format(Locale.getDefault(), "%.2f kcal",
                        dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat());
                caloriesValue.setText(kcal);
            });

        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
            .addOnSuccessListener(dataSet -> {
                String dist = dataSet.isEmpty() ? "-- m" :
                    String.format(Locale.getDefault(), "%.2f m",
                        dataSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE).asFloat());
                distanceValue.setText(dist);
            });

        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_MOVE_MINUTES)
            .addOnSuccessListener(dataSet -> {
                String minutes = dataSet.isEmpty() ? "-- min" :
                    dataSet.getDataPoints().get(0).getValue(Field.FIELD_DURATION).asInt() + " min";
                moveValue.setText(minutes);
            });

        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_SLEEP_SEGMENT)
            .addOnSuccessListener(dataSet -> {
                String sleep = "-- h";
                if (!dataSet.isEmpty()) {
                    long duration = dataSet.getDataPoints().get(0).getEndTime(TimeUnit.MILLISECONDS)
                        - dataSet.getDataPoints().get(0).getStartTime(TimeUnit.MILLISECONDS);
                    int hours = (int) (duration / (1000 * 60 * 60));
                    sleep = hours + " h";
                }
                sleepValue.setText(sleep);
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                checkActivityRecognitionPermission();
            } else {
                stepCountTextView.setText("Permission denied for Google Fit");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readAllFitnessData();
            } else {
                stepCountTextView.setText("Permission denied");
            }
        }
    }
}
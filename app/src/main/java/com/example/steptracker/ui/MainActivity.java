package com.example.steptracker.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;
import com.example.steptracker.R;
import com.example.steptracker.component.WorkScheduler;
import com.example.steptracker.util.PermissionHelper;
import com.example.steptracker.viewmodel.FitnessViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.FitnessOptions;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION_CODE = 2;

    private TextView stepCountTextView;
    private TextView heartRateValue;
    private TextView caloriesValue;
    private TextView distanceValue;
    private TextView moveValue;
    private TextView sleepValue;

    private FitnessViewModel fitnessViewModel;

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

        fitnessViewModel = new ViewModelProvider(this).get(FitnessViewModel.class);

        Button refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> fitnessViewModel.loadFitnessData());

        refreshButton.setOnClickListener(v -> {
            fitnessViewModel.loadFitnessData();  // оновлюємо LiveData
            fitnessViewModel.sendFitnessData();  // відправляємо на backend
        });

        observeViewModel();

        FitnessOptions fitnessOptions = fitnessViewModel.getFitnessOptions();
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

    private void observeViewModel() {
        fitnessViewModel.getStepCount().observe(this, steps ->
            stepCountTextView.setText(String.valueOf(steps)));

        fitnessViewModel.getHeartRate().observe(this, hr ->
            heartRateValue.setText(String.valueOf(hr)));

        fitnessViewModel.getCalories().observe(this, cal ->
            caloriesValue.setText(String.valueOf(cal)));

        fitnessViewModel.getDistance().observe(this, dist ->
            distanceValue.setText(String.valueOf(dist)));

        fitnessViewModel.getMoveMinutes().observe(this, move ->
            moveValue.setText(String.valueOf(move)));

        fitnessViewModel.getSleep().observe(this, sleep ->
            sleepValue.setText(String.valueOf(sleep)));
    }

    private void checkActivityRecognitionPermission() {
        if (!PermissionHelper.hasActivityRecognitionPermission(this)) {
            PermissionHelper.requestActivityRecognitionPermission(this, REQUEST_ACTIVITY_RECOGNITION_PERMISSION_CODE);
        } else {
            GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessViewModel.getFitnessOptions());
            fitnessViewModel.initializeRepository(account);
            fitnessViewModel.loadFitnessData();
            WorkScheduler.startPeriodicFitnessWork(this);
        }
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessViewModel.getFitnessOptions());
                fitnessViewModel.initializeRepository(account);
                fitnessViewModel.loadFitnessData();
                WorkScheduler.startPeriodicFitnessWork(this);
            } else {
                stepCountTextView.setText("Permission denied");
            }
        }
    }
}
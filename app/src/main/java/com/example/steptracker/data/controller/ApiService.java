package com.example.steptracker.data.controller;

import com.example.steptracker.data.dto.GoogleFitRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface ApiService {
    @POST("/fitness")
    Call<Void> sendFitnessData(@Body GoogleFitRequestDto data);
}


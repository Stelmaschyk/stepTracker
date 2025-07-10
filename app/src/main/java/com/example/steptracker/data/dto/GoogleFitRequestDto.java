package com.example.steptracker.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GoogleFitRequestDto {
    int steps;
    float heartRate;
    float calories;
    float distance;
    long moveMinutes;
    int sleep;
}

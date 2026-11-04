package com.example.employees.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record Employee(
        @Positive int id,
        @NotBlank String name,
        @NotBlank String department,
        @DecimalMin("0.0") double salary,
        @Min(0) int age) {
}

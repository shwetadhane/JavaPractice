package com.example.employees.service;

public record ServiceResult<T>(T value, boolean fallback) {
}

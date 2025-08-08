package ru.otus.hw.controllers.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseDto<T> {
    private String message;

    private T data;

    private boolean success;
}

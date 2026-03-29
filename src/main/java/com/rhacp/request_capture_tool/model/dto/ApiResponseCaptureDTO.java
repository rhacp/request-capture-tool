package com.rhacp.request_capture_tool.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ApiResponseCaptureDTO(

        @NotBlank(message = "Original method is required")
        String originalMethod,

        @NotBlank(message = "Original path is required")
        String originalPath,

        @NotNull(message = "Status code is required")
        Integer statusCode,

        String contentType,

        Map<String, String> headers,

        String responseBody
) {
}
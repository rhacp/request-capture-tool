package com.rhacp.request_capture_tool.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RestResponseDTO(
        Long id,
        String groupName,
        String method,
        String path,
        Integer statusCode
) {
}
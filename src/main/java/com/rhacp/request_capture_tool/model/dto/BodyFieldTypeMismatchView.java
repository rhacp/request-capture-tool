package com.rhacp.request_capture_tool.model.dto;

public record BodyFieldTypeMismatchView(
        String fieldPath,
        String leftType,
        String rightType
) {
}
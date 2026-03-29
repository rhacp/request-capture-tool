package com.rhacp.request_capture_tool.model.dto.compare;

public record BodyFieldTypeMismatchView(
        String fieldPath,
        String leftType,
        String rightType
) {
}
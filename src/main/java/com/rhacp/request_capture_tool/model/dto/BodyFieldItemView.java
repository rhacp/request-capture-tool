package com.rhacp.request_capture_tool.model.dto;

import com.rhacp.request_capture_tool.util.enumeration.BodyValueType;

public record BodyFieldItemView(
        String fieldPath,
        String fieldValue,
        BodyValueType valueType
) {
}
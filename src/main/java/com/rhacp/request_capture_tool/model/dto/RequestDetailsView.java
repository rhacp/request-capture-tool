package com.rhacp.request_capture_tool.model.dto;

import com.rhacp.request_capture_tool.util.enumeration.ContentTypeCategory;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;

import java.time.LocalDateTime;
import java.util.List;

public record RequestDetailsView(
        Long id,
        LocalDateTime receivedAt,
        SourceType sourceType,
        String groupName,
        String method,
        String path,
        String contentType,
        ContentTypeCategory contentTypeCategory,
        String bodyRaw,
        String normalizedStructureJson,
        List<HeaderItemView> headers,
        List<QueryParamItemView> queryParams,
        List<BodyFieldItemView> bodyFields
) {
}
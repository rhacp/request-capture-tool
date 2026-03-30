package com.rhacp.request_capture_tool.service.view;

import com.rhacp.request_capture_tool.model.dto.request.RequestDetailsView;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestViewService {

    List<RequestDetailsView> getFilteredRequests(
            String groupName,
            SourceType sourceType,
            LocalDateTime receivedAtFrom,
            LocalDateTime receivedAtTo
    );

    RequestDetailsView getRequestDetails(Long id);
}
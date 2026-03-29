package com.rhacp.request_capture_tool.service.view;

import com.rhacp.request_capture_tool.model.dto.request.RequestDetailsView;

import java.util.List;

public interface RequestViewService {

    List<RequestDetailsView> getAllRequests();

    List<RequestDetailsView> getRequestsByGroup(String groupName);

    RequestDetailsView getRequestDetails(Long id);
}
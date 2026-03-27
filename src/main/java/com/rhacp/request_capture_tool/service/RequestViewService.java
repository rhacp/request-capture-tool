package com.rhacp.request_capture_tool.service;

import com.rhacp.request_capture_tool.model.dto.RequestDetailsView;

import java.util.List;

public interface RequestViewService {

    List<RequestDetailsView> getAllRequests();

    List<RequestDetailsView> getRequestsByGroup(String groupName);

    RequestDetailsView getRequestDetails(Long id);
}
package com.rhacp.request_capture_tool.service;

import com.rhacp.request_capture_tool.model.dto.CompareResultView;

public interface CompareService {

    CompareResultView compareRequests(Long leftId, Long rightId);
}
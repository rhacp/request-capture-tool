package com.rhacp.request_capture_tool.service.compare;

import com.rhacp.request_capture_tool.model.dto.compare.CompareResultView;

public interface CompareService {

    CompareResultView compareRequests(Long leftId, Long rightId);
}
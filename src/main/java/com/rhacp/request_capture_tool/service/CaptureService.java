package com.rhacp.request_capture_tool.service;

import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import jakarta.servlet.http.HttpServletRequest;

public interface CaptureService {

    CapturedRequest captureRequest(SourceType sourceType, String group, HttpServletRequest request);
}

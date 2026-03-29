package com.rhacp.request_capture_tool.service.capture;

import com.rhacp.request_capture_tool.model.dto.ApiResponseCaptureDTO;
import com.rhacp.request_capture_tool.model.dto.RestResponseDTO;
import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import jakarta.servlet.http.HttpServletRequest;

public interface CaptureService {

    CapturedRequest captureRequest(SourceType sourceType, String group, HttpServletRequest request);

    RestResponseDTO captureRestRequest(SourceType sourceType, String group, HttpServletRequest request);

    RestResponseDTO captureApiResponse(String group, ApiResponseCaptureDTO dto);
}
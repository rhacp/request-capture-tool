package com.rhacp.request_capture_tool.service.capture;

import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface NormalizationService {

    void normalizeJsonBody(HttpServletRequest request, CapturedRequest capturedRequest);

    void normalizeFormUrlEncodedBody(HttpServletRequest request, CapturedRequest capturedRequest);

    void normalizeMultipartBody(HttpServletRequest request, CapturedRequest capturedRequest);
}
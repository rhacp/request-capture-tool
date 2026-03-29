package com.rhacp.request_capture_tool.controller.capture;

import com.rhacp.request_capture_tool.model.dto.ApiResponseCaptureDTO;
import com.rhacp.request_capture_tool.model.dto.RestResponseDTO;
import com.rhacp.request_capture_tool.service.capture.CaptureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/capture/response")
public class ResponseCaptureController {

    private final CaptureService captureService;

    public ResponseCaptureController(CaptureService captureService) {
        this.captureService = captureService;
    }

    @PostMapping("/{group}")
    public ResponseEntity<RestResponseDTO> captureResponse(@PathVariable String group,
                                                           @Valid @RequestBody ApiResponseCaptureDTO dto) {
        return ResponseEntity.ok(captureService.captureApiResponse(group, dto));
    }
}
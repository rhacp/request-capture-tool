package com.rhacp.request_capture_tool.controller.capture;

import com.rhacp.request_capture_tool.model.dto.RestResponseDTO;
import com.rhacp.request_capture_tool.service.capture.CaptureService;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/capture/webhook")
public class WebhookCaptureController {

    private final CaptureService captureService;

    public WebhookCaptureController(CaptureService captureService) {
        this.captureService = captureService;
    }

    @PostMapping(value = "/{group}")
    public ResponseEntity<RestResponseDTO> captureWebhookPost(@PathVariable String group, HttpServletRequest request) {
        return ResponseEntity.ok(captureService.captureRestRequest(SourceType.WEBHOOK, group, request));
    }

    @GetMapping(value = "/{group}")
    public ResponseEntity<RestResponseDTO> captureWebhookGet(@PathVariable String group, HttpServletRequest request) {
        return ResponseEntity.ok(captureService.captureRestRequest(SourceType.WEBHOOK, group, request));
    }
}

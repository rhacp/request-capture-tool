package com.rhacp.request_capture_tool.controller.capture;

import com.rhacp.request_capture_tool.model.dto.CallbackResponseDTO;
import com.rhacp.request_capture_tool.service.capture.CaptureService;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/capture/confirmCallback")
public class ConfirmCallbackController {

  private final CaptureService captureService;

  public ConfirmCallbackController(CaptureService captureService) {
    this.captureService = captureService;
  }

  @PostMapping(value = "/{group}/{status}")
  public ResponseEntity<CallbackResponseDTO> captureCallbackPost(@PathVariable String group, @PathVariable String status, HttpServletRequest request) {
    return ResponseEntity.ok(captureService.captureCallbackRestRequest(SourceType.CALLBLACK, group, status, request));
  }
}

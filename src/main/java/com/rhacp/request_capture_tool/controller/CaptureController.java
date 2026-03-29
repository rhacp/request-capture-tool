package com.rhacp.request_capture_tool.controller;

import com.rhacp.request_capture_tool.service.capture.CaptureService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/capture")
public class CaptureController {

    private final CaptureService captureService;

    public CaptureController(CaptureService captureService) {
        this.captureService = captureService;
    }


}
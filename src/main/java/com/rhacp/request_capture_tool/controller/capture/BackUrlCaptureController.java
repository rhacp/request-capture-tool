package com.rhacp.request_capture_tool.controller.capture;

import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import com.rhacp.request_capture_tool.service.capture.CaptureService;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/capture/backurl")
public class BackUrlCaptureController {

    private final CaptureService captureService;

    public BackUrlCaptureController(CaptureService captureService) {
        this.captureService = captureService;
    }

    @GetMapping("/{group}")
    public String captureBackUrlGet(@PathVariable String group,
                                    HttpServletRequest request,
                                    Model model) {
        CapturedRequest saved = captureService.captureRequest(SourceType.BACKURL, group, request);
        populateSuccessModel(model, saved, "BackUrl GET captured successfully");
        return "capture-success";
    }

    @PostMapping("/{group}")
    public String captureBackUrlPost(@PathVariable String group,
                                     HttpServletRequest request,
                                     Model model) {
        CapturedRequest saved = captureService.captureRequest(SourceType.BACKURL, group, request);
        populateSuccessModel(model, saved, "BackUrl POST captured successfully");
        return "capture-success";
    }

    private void populateSuccessModel(Model model, CapturedRequest saved, String title) {
        model.addAttribute("title", title);
        model.addAttribute("captureId", saved.getId());
        model.addAttribute("groupName", safe(saved.getGroupName()));
        model.addAttribute("sourceType", saved.getSourceType() != null ? saved.getSourceType().name() : "-");
        model.addAttribute("method", safe(saved.getMethod()));
        model.addAttribute("path", safe(saved.getPath()));
        model.addAttribute("contentType", safe(saved.getContentType()));
        model.addAttribute("contentTypeCategory",
                saved.getContentTypeCategory() != null ? saved.getContentTypeCategory().name() : "-");
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
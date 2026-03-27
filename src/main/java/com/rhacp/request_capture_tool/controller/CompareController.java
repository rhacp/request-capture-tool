package com.rhacp.request_capture_tool.controller;

import com.rhacp.request_capture_tool.model.dto.CompareResultView;
import com.rhacp.request_capture_tool.model.dto.RequestDetailsView;
import com.rhacp.request_capture_tool.service.CompareService;
import com.rhacp.request_capture_tool.service.RequestViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/ui")
public class CompareController {

    private final CompareService compareService;
    private final RequestViewService requestViewService;
    private final ObjectMapper objectMapper;

    public CompareController(
            CompareService compareService,
            RequestViewService requestViewService,
            ObjectMapper objectMapper
    ) {
        this.compareService = compareService;
        this.requestViewService = requestViewService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/compare")
    public String compareRequests(
            @RequestParam Long leftId,
            @RequestParam Long rightId,
            Model model
    ) {
        CompareResultView compareResult = compareService.compareRequests(leftId, rightId);

        RequestDetailsView leftRequest = requestViewService.getRequestDetails(leftId);
        RequestDetailsView rightRequest = requestViewService.getRequestDetails(rightId);

        model.addAttribute("compareResult", compareResult);
        model.addAttribute("leftRequest", leftRequest);
        model.addAttribute("rightRequest", rightRequest);

        model.addAttribute("leftRawBodyFormatted", formatJsonIfPossible(leftRequest.bodyRaw(), "No body"));
        model.addAttribute("rightRawBodyFormatted", formatJsonIfPossible(rightRequest.bodyRaw(), "No body"));

        model.addAttribute(
                "leftNormalizedStructureFormatted",
                formatJsonIfPossible(leftRequest.normalizedStructureJson(), "{}")
        );
        model.addAttribute(
                "rightNormalizedStructureFormatted",
                formatJsonIfPossible(rightRequest.normalizedStructureJson(), "{}")
        );

        return "compare-result";
    }

    private String formatJsonIfPossible(String value, String fallbackIfNullOrBlank) {
        if (value == null || value.isBlank()) {
            return fallbackIfNullOrBlank;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(value);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (Exception e) {
            return value;
        }
    }
}
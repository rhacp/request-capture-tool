package com.rhacp.request_capture_tool.controller;

import com.rhacp.request_capture_tool.model.dto.CompareResultView;
import com.rhacp.request_capture_tool.model.dto.RequestDetailsView;
import com.rhacp.request_capture_tool.service.compare.CompareService;
import com.rhacp.request_capture_tool.service.view.RequestFormattingService;
import com.rhacp.request_capture_tool.service.view.RequestViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CompareViewController {

    private final CompareService compareService;

    private final RequestViewService requestViewService;

    private final RequestFormattingService requestFormattingService;

    public CompareViewController(
            CompareService compareService,
            RequestViewService requestViewService,
            RequestFormattingService requestFormattingService
    ) {
        this.compareService = compareService;
        this.requestViewService = requestViewService;
        this.requestFormattingService = requestFormattingService;
    }

    @GetMapping("/ui/compare")
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
        model.addAttribute("leftBodyPreview", buildBodyPreview(leftRequest));
        model.addAttribute("rightBodyPreview", buildBodyPreview(rightRequest));

        return "compare-result";
    }

    private String buildBodyPreview(RequestDetailsView request) {
        if (request == null || request.contentTypeCategory() == null) {
            return requestFormattingService.formatRawBodyForDisplay(request);
        }

        return switch (request.contentTypeCategory()) {
            case JSON, FORM_URLENCODED -> requestFormattingService.formatDecodedBody(request);
            case MULTIPART, UNKNOWN -> requestFormattingService.formatRawBodyForDisplay(request);
        };
    }
}
package com.rhacp.request_capture_tool.controller.view;

import com.rhacp.request_capture_tool.model.dto.request.RequestDetailsView;
import com.rhacp.request_capture_tool.service.view.RequestFormattingService;
import com.rhacp.request_capture_tool.service.view.RequestViewService;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/ui/requests")
public class RequestViewController {

    private static final DateTimeFormatter DATETIME_LOCAL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final RequestViewService requestViewService;

    private final RequestFormattingService formattingService;

    public RequestViewController(
            RequestViewService requestViewService,
            RequestFormattingService formattingService
    ) {
        this.requestViewService = requestViewService;
        this.formattingService = formattingService;
    }

    @GetMapping
    public String getRequests(
            @RequestParam(required = false) String group,
            @RequestParam(required = false) SourceType sourceType,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
            LocalDateTime receivedAtFrom,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
            LocalDateTime receivedAtTo,
            Model model
    ) {
        List<RequestDetailsView> requests = requestViewService.getFilteredRequests(
                group,
                sourceType,
                receivedAtFrom,
                receivedAtTo
        );

        model.addAttribute("requests", requests);
        model.addAttribute("selectedGroup", group != null ? group : "");
        model.addAttribute("selectedSourceType", sourceType);
        model.addAttribute(
                "selectedReceivedAtFrom",
                receivedAtFrom != null ? receivedAtFrom.format(DATETIME_LOCAL_FORMATTER) : ""
        );
        model.addAttribute(
                "selectedReceivedAtTo",
                receivedAtTo != null ? receivedAtTo.format(DATETIME_LOCAL_FORMATTER) : ""
        );

        return "requests-list";
    }

    @GetMapping("/{id}")
    public String requestDetails(@PathVariable Long id, Model model) {
        RequestDetailsView requestItem = requestViewService.getRequestDetails(id);

        model.addAttribute("requestItem", requestItem);
        model.addAttribute("formattedRawBody", formattingService.formatRawBodyForDisplay(requestItem));
        model.addAttribute("formattedDecodedBody", formattingService.formatDecodedBody(requestItem));

        return "request-details";
    }
}
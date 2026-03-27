package com.rhacp.request_capture_tool.controller;

import com.rhacp.request_capture_tool.model.dto.RequestDetailsView;
import com.rhacp.request_capture_tool.service.view.RequestFormattingService;
import com.rhacp.request_capture_tool.service.view.RequestViewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/ui/requests")
public class RequestViewController {

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
            HttpServletRequest request,
            Model model
    ) {
        String baseUrl = getBaseUrl(request);
        model.addAttribute("baseUrl", baseUrl);

        List<RequestDetailsView> requests = (group != null && !group.isBlank())
                ? requestViewService.getRequestsByGroup(group)
                : requestViewService.getAllRequests();

        model.addAttribute("requests", requests);
        model.addAttribute("selectedGroup", group);

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

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isBlank()) {
            scheme = request.getScheme();
        }

        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = request.getServerName();
        }

        String portHeader = request.getHeader("X-Forwarded-Port");
        int port = portHeader != null ? Integer.parseInt(portHeader) : request.getServerPort();

        boolean isDefaultPort =
                ("http".equalsIgnoreCase(scheme) && port == 80) ||
                        ("https".equalsIgnoreCase(scheme) && port == 443);

        return scheme + "://" + host + (isDefaultPort ? "" : ":" + port);
    }
}
package com.rhacp.request_capture_tool.controller.view;

import com.rhacp.request_capture_tool.model.dto.request.RequestDetailsView;
import com.rhacp.request_capture_tool.service.view.RequestFormattingService;
import com.rhacp.request_capture_tool.service.view.RequestViewService;
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
            Model model
    ) {
        List<RequestDetailsView> requests = (group != null && !group.isBlank())
                ? requestViewService.getRequestsByGroup(group)
                : requestViewService.getAllRequests();

        model.addAttribute("requests", requests);
        model.addAttribute("selectedGroup", group != null && !group.isBlank() ? group : "");

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
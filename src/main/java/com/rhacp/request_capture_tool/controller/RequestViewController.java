package com.rhacp.request_capture_tool.controller;

import com.rhacp.request_capture_tool.model.dto.RequestDetailsView;
import com.rhacp.request_capture_tool.service.RequestViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/ui/requests")
public class RequestViewController {

    private final RequestViewService requestViewService;

    public RequestViewController(RequestViewService requestViewService) {
        this.requestViewService = requestViewService;
    }

    @GetMapping
    public String listRequests(
            @RequestParam(required = false) String group,
            Model model
    ) {
        List<RequestDetailsView> requests;

        if (group != null && !group.isBlank()) {
            requests = requestViewService.getRequestsByGroup(group.trim());
        } else {
            requests = requestViewService.getAllRequests();
        }

        model.addAttribute("requests", requests);
        model.addAttribute("selectedGroup", group);

        return "requests-list";
    }

    @GetMapping("/{id}")
    public String requestDetails(@PathVariable Long id, Model model) {
        model.addAttribute("requestItem", requestViewService.getRequestDetails(id));
        return "request-details";
    }
}
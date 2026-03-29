package com.rhacp.request_capture_tool.controller.view;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui/help")
public class HelpViewController {

    @GetMapping
    public String showHelp(HttpServletRequest request, Model model) {
        model.addAttribute("baseUrl", getBaseUrl(request));
        return "help";
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
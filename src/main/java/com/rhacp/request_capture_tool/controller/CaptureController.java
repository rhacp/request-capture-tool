package com.rhacp.request_capture_tool.controller;

import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import com.rhacp.request_capture_tool.service.capture.CaptureService;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/capture")
public class CaptureController {

    private final CaptureService captureService;

    public CaptureController(CaptureService captureService) {
        this.captureService = captureService;
    }

    @GetMapping(value = "/backurl/{group}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> captureBackUrlGet(@PathVariable String group, HttpServletRequest request) {
        CapturedRequest saved = captureService.captureRequest(SourceType.BACKURL, group, request);
        return ResponseEntity.ok(buildSuccessHtml(saved, "BackUrl GET captured successfully"));
    }

    @PostMapping(value = "/backurl/{group}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> captureBackUrlPost(@PathVariable String group, HttpServletRequest request) {
        CapturedRequest saved = captureService.captureRequest(SourceType.BACKURL, group, request);
        return ResponseEntity.ok(buildSuccessHtml(saved, "BackUrl POST captured successfully"));
    }

    @PostMapping(value = "/webhook/{group}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> captureWebhookPost(@PathVariable String group, HttpServletRequest request) {
        CapturedRequest saved = captureService.captureRequest(SourceType.WEBHOOK, group, request);

        return ResponseEntity.ok("""
                Webhook captured successfully.
                Capture ID: %d
                Group: %s
                Method: %s
                Path: %s
                """.formatted(
                saved.getId(),
                saved.getGroupName(),
                saved.getMethod(),
                saved.getPath()
        ));
    }

    @GetMapping(value = "/webhook/{group}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> captureWebhookGet(@PathVariable String group, HttpServletRequest request) {
        CapturedRequest saved = captureService.captureRequest(SourceType.WEBHOOK, group, request);

        return ResponseEntity.ok("""
                Webhook GET received successfully.
                Capture ID: %d
                Group: %s
                Method: %s
                Path: %s
                """.formatted(
                saved.getId(),
                saved.getGroupName(),
                saved.getMethod(),
                saved.getPath()
        ));
    }

    private String buildSuccessHtml(CapturedRequest saved, String title) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Request Captured</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            margin: 24px;
                            background: #121212;
                            color: #e6e6e6;
                        }

                        .card {
                            max-width: 900px;
                            background: #1b1b1b;
                            border: 1px solid #2d2d2d;
                            border-left: 6px solid #2e7d32;
                            padding: 20px;
                            border-radius: 10px;
                        }

                        h1 {
                            margin-top: 0;
                            color: #ffffff;
                        }

                        p {
                            margin: 8px 0;
                        }

                        .muted {
                            color: #aaaaaa;
                        }

                        .links {
                            margin-top: 20px;
                            display: flex;
                            gap: 12px;
                            flex-wrap: wrap;
                        }

                        a {
                            color: #64b5f6;
                            text-decoration: none;
                            border: 1px solid #333;
                            background: #242424;
                            padding: 10px 14px;
                            border-radius: 6px;
                            display: inline-block;
                        }

                        a:hover {
                            background: #2e2e2e;
                            text-decoration: none;
                        }

                        .label {
                            color: #aaaaaa;
                        }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h1>%s</h1>
                        <p class="muted">The incoming request was captured and stored successfully.</p>

                        <p><span class="label">Capture ID:</span> <strong>%d</strong></p>
                        <p><span class="label">Group:</span> <strong>%s</strong></p>
                        <p><span class="label">Source Type:</span> <strong>%s</strong></p>
                        <p><span class="label">Method:</span> <strong>%s</strong></p>
                        <p><span class="label">Path:</span> <strong>%s</strong></p>
                        <p><span class="label">Content Type:</span> <strong>%s</strong></p>
                        <p><span class="label">Content Type Category:</span> <strong>%s</strong></p>

                        <div class="links">
                            <a href="/ui/requests/%d">Open captured request</a>
                            <a href="/ui/requests">Open dashboard</a>
                            <a href="/ui/requests?group=%s">Open this group</a>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                title,
                saved.getId(),
                safe(saved.getGroupName()),
                saved.getSourceType() != null ? saved.getSourceType().name() : "-",
                safe(saved.getMethod()),
                safe(saved.getPath()),
                safe(saved.getContentType()),
                saved.getContentTypeCategory() != null ? saved.getContentTypeCategory().name() : "-",
                saved.getId(),
                safe(saved.getGroupName())
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
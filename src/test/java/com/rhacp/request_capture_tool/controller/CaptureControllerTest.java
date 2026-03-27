package com.rhacp.request_capture_tool.controller;

import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import com.rhacp.request_capture_tool.service.capture.CaptureService;
import com.rhacp.request_capture_tool.util.enumeration.ContentTypeCategory;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CaptureController.class)
class CaptureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CaptureService captureService;

    @Test
    @DisplayName("GET /capture/backurl/{group} should return html confirmation")
    void shouldCaptureBackUrlGet() throws Exception {
        CapturedRequest saved = CapturedRequest.builder()
                .id(1L)
                .receivedAt(LocalDateTime.now())
                .sourceType(SourceType.BACKURL)
                .groupName("test-group")
                .method("GET")
                .path("/capture/backurl/test-group")
                .contentTypeCategory(ContentTypeCategory.UNKNOWN)
                .build();

        when(captureService.captureRequest(eq(SourceType.BACKURL), eq("test-group"), any(HttpServletRequest.class)))
                .thenReturn(saved);

        mockMvc.perform(get("/capture/backurl/test-group")
                        .queryParam("status", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("BackUrl GET captured successfully")))
                .andExpect(content().string(containsString("Capture ID:")))
                .andExpect(content().string(containsString("test-group")))
                .andExpect(content().string(containsString("/ui/requests/1")))
                .andExpect(content().string(containsString("/ui/requests")));
    }

    @Test
    @DisplayName("POST /capture/webhook/{group} should return plain text confirmation")
    void shouldCaptureWebhookPost() throws Exception {
        CapturedRequest saved = CapturedRequest.builder()
                .id(2L)
                .receivedAt(LocalDateTime.now())
                .sourceType(SourceType.WEBHOOK)
                .groupName("webhook-group")
                .method("POST")
                .path("/capture/webhook/webhook-group")
                .contentTypeCategory(ContentTypeCategory.JSON)
                .build();

        when(captureService.captureRequest(eq(SourceType.WEBHOOK), eq("webhook-group"), any(HttpServletRequest.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/capture/webhook/webhook-group")
                        .contentType("application/json")
                        .content("{\"status\":\"SUCCESS\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(content().string(containsString("Webhook captured successfully")))
                .andExpect(content().string(containsString("Capture ID: 2")))
                .andExpect(content().string(containsString("Group: webhook-group")));
    }

    @Test
    @DisplayName("GET /capture/webhook/{group} should always return 200")
    void shouldReturn200ForWebhookGet() throws Exception {
        CapturedRequest saved = CapturedRequest.builder()
                .id(3L)
                .receivedAt(LocalDateTime.now())
                .sourceType(SourceType.WEBHOOK)
                .groupName("health-check")
                .method("GET")
                .path("/capture/webhook/health-check")
                .contentTypeCategory(ContentTypeCategory.UNKNOWN)
                .build();

        when(captureService.captureRequest(eq(SourceType.WEBHOOK), eq("health-check"), any(HttpServletRequest.class)))
                .thenReturn(saved);

        mockMvc.perform(get("/capture/webhook/health-check"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(content().string(containsString("Webhook GET received successfully")))
                .andExpect(content().string(containsString("Capture ID: 3")));
    }
}
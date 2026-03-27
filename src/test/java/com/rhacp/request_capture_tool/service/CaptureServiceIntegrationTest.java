package com.rhacp.request_capture_tool.service;

import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import com.rhacp.request_capture_tool.repository.CapturedRequestRepository;
import com.rhacp.request_capture_tool.service.capture.CaptureService;
import com.rhacp.request_capture_tool.util.enumeration.ContentTypeCategory;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CaptureServiceIntegrationTest {

    @Autowired
    private CaptureService captureService;

    @Autowired
    private CapturedRequestRepository repository;

    @Test
    @DisplayName("Should capture JSON body and extract body fields")
    void shouldCaptureJsonBodyAndExtractFields() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/capture/webhook/test-group");
        request.setContentType("application/json");
        request.setContent("""
                {
                  "status": "SUCCESS",
                  "amount": 100,
                  "paid": true
                }
                """.getBytes());

        CapturedRequest saved = captureService.captureRequest(SourceType.WEBHOOK, "test-group", request);

        assertThat(saved.getId()).isNotNull();

        CapturedRequest loaded = repository.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getSourceType()).isEqualTo(SourceType.WEBHOOK);
        assertThat(loaded.getContentTypeCategory()).isEqualTo(ContentTypeCategory.JSON);
        assertThat(loaded.getBodyRaw()).contains("SUCCESS");
        assertThat(loaded.getBodyFields()).hasSize(3);
        assertThat(loaded.getNormalizedStructureJson()).contains("status");
        assertThat(loaded.getNormalizedStructureJson()).contains("amount");
        assertThat(loaded.getNormalizedStructureJson()).contains("paid");
    }

    @Test
    @DisplayName("Should capture query parameters from GET request")
    void shouldCaptureQueryParametersFromGetRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/capture/backurl/group-a");
        request.setQueryString("status=SUCCESS&orderId=123");

        CapturedRequest saved = captureService.captureRequest(SourceType.BACKURL, "group-a", request);

        assertThat(saved.getId()).isNotNull();

        CapturedRequest loaded = repository.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getMethod()).isEqualTo("GET");
        assertThat(loaded.getQueryParams()).hasSize(2);
        assertThat(loaded.getBodyRaw()).isNull();
    }

    @Test
    @DisplayName("Should capture x-www-form-urlencoded body fields")
    void shouldCaptureFormUrlEncodedBodyFields() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/capture/webhook/group-form");
        request.setContentType("application/x-www-form-urlencoded");
        request.setContent("status=SUCCESS&orderId=123".getBytes());

        CapturedRequest saved = captureService.captureRequest(SourceType.WEBHOOK, "group-form", request);

        CapturedRequest loaded = repository.findById(saved.getId()).orElseThrow();

        assertThat(loaded.getContentTypeCategory()).isEqualTo(ContentTypeCategory.FORM_URLENCODED);
        assertThat(loaded.getBodyFields()).hasSize(2);
        assertThat(loaded.getBodyRaw()).contains("status=SUCCESS");
    }
}
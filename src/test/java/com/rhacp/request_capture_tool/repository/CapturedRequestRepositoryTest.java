package com.rhacp.request_capture_tool.repository;

import com.rhacp.request_capture_tool.model.entity.CapturedBodyField;
import com.rhacp.request_capture_tool.model.entity.CapturedHeader;
import com.rhacp.request_capture_tool.model.entity.CapturedQueryParam;
import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import com.rhacp.request_capture_tool.util.enumeration.BodyValueType;
import com.rhacp.request_capture_tool.util.enumeration.ContentTypeCategory;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
class CapturedRequestRepositoryTest {

    @Autowired
    private CapturedRequestRepository repository;

    @Test
    @DisplayName("Should save and load a captured request with children")
    void shouldSaveAndLoadCapturedRequest() {
        CapturedRequest request = CapturedRequest.builder()
                .receivedAt(LocalDateTime.now())
                .sourceType(SourceType.WEBHOOK)
                .groupName("group-1")
                .method("POST")
                .path("/capture/webhook/group-1")
                .contentType("application/json")
                .contentTypeCategory(ContentTypeCategory.JSON)
                .bodyRaw("{\"orderId\":\"123\"}")
                .normalizedStructureJson("{\"orderId\":\"STRING\"}")
                .build();

        request.addHeader(CapturedHeader.builder()
                .headerName("content-type")
                .headerValue("application/json")
                .build());

        request.addQueryParam(CapturedQueryParam.builder()
                .paramName("status")
                .paramValue("SUCCESS")
                .build());

        request.addBodyField(CapturedBodyField.builder()
                .fieldPath("orderId")
                .fieldValue("123")
                .valueType(BodyValueType.STRING)
                .build());

        CapturedRequest saved = repository.save(request);

        assertThat(saved.getId()).isNotNull();

        List<CapturedRequest> found = repository.findAllByOrderByReceivedAtDesc();

        assertThat(found).hasSize(1);

        CapturedRequest loaded = found.get(0);
        assertThat(loaded.getSourceType()).isEqualTo(SourceType.WEBHOOK);
        assertThat(loaded.getContentTypeCategory()).isEqualTo(ContentTypeCategory.JSON);
        assertThat(loaded.getHeaders()).hasSize(1);
        assertThat(loaded.getQueryParams()).hasSize(1);
        assertThat(loaded.getBodyFields()).hasSize(1);
    }

    @Test
    @DisplayName("Should filter by group name")
    void shouldFilterByGroupName() {
        repository.save(CapturedRequest.builder()
                .receivedAt(LocalDateTime.now())
                .sourceType(SourceType.BACKURL)
                .groupName("group-a")
                .method("GET")
                .path("/capture/backurl/group-a")
                .contentType("application/json")
                .contentTypeCategory(ContentTypeCategory.JSON)
                .build());

        repository.save(CapturedRequest.builder()
                .receivedAt(LocalDateTime.now())
                .sourceType(SourceType.WEBHOOK)
                .groupName("group-b")
                .method("POST")
                .path("/capture/webhook/group-b")
                .contentType("application/x-www-form-urlencoded")
                .contentTypeCategory(ContentTypeCategory.FORM_URLENCODED)
                .build());

        List<CapturedRequest> results = repository.findByGroupNameOrderByReceivedAtDesc("group-a");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getGroupName()).isEqualTo("group-a");
    }

    @Test
    @DisplayName("Should filter by content type category")
    void shouldFilterByContentTypeCategory() {
        repository.save(CapturedRequest.builder()
                .receivedAt(LocalDateTime.now())
                .sourceType(SourceType.BACKURL)
                .groupName("json-group")
                .method("POST")
                .path("/capture/backurl/json-group")
                .contentType("application/json")
                .contentTypeCategory(ContentTypeCategory.JSON)
                .build());

        repository.save(CapturedRequest.builder()
                .receivedAt(LocalDateTime.now())
                .sourceType(SourceType.BACKURL)
                .groupName("form-group")
                .method("POST")
                .path("/capture/backurl/form-group")
                .contentType("multipart/form-data")
                .contentTypeCategory(ContentTypeCategory.MULTIPART)
                .build());

        List<CapturedRequest> jsonResults =
                repository.findByContentTypeCategoryOrderByReceivedAtDesc(ContentTypeCategory.JSON);

        assertThat(jsonResults).hasSize(1);
        assertThat(jsonResults.get(0).getContentTypeCategory()).isEqualTo(ContentTypeCategory.JSON);
    }
}
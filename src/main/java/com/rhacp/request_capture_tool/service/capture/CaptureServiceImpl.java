package com.rhacp.request_capture_tool.service.capture;

import com.rhacp.request_capture_tool.model.entity.CapturedHeader;
import com.rhacp.request_capture_tool.model.entity.CapturedQueryParam;
import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import com.rhacp.request_capture_tool.repository.CapturedRequestRepository;
import com.rhacp.request_capture_tool.util.enumeration.ContentTypeCategory;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.Locale;

@Service
@Slf4j
public class CaptureServiceImpl implements CaptureService {

    private final CapturedRequestRepository capturedRequestRepository;

    private final NormalizationService normalizationService;

    public CaptureServiceImpl(
            CapturedRequestRepository capturedRequestRepository,
            NormalizationService normalizationService
    ) {
        this.capturedRequestRepository = capturedRequestRepository;
        this.normalizationService = normalizationService;
    }

    @Override
    @Transactional
    public CapturedRequest captureRequest(SourceType sourceType, String group, HttpServletRequest request) {
        String rawContentType = request.getContentType();
        ContentTypeCategory contentTypeCategory = resolveContentTypeCategory(rawContentType);

        log.info(
                "Capturing request: sourceType={}, group={}, method={}, path={}, contentType={}, contentTypeCategory={}",
                sourceType,
                group,
                request.getMethod(),
                request.getRequestURI(),
                rawContentType,
                contentTypeCategory
        );

        CapturedRequest capturedRequest = CapturedRequest.builder()
                .receivedAt(LocalDateTime.now())
                .sourceType(sourceType)
                .groupName(group)
                .method(request.getMethod())
                .path(request.getRequestURI())
                .contentType(rawContentType)
                .contentTypeCategory(contentTypeCategory)
                .build();

        extractHeaders(request, capturedRequest);
        extractQueryParams(request, capturedRequest);

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            log.debug("GET request detected. Skipping body normalization. group={}, path={}", group, request.getRequestURI());
            capturedRequest.setBodyRaw(null);
            capturedRequest.setNormalizedStructureJson("{}");
        } else {
            switch (contentTypeCategory) {
                case JSON -> {
                    log.debug("Delegating JSON normalization. group={}, path={}", group, request.getRequestURI());
                    normalizationService.normalizeJsonBody(request, capturedRequest);
                }
                case FORM_URLENCODED -> {
                    log.debug("Delegating FORM_URLENCODED normalization. group={}, path={}", group, request.getRequestURI());
                    normalizationService.normalizeFormUrlEncodedBody(request, capturedRequest);
                }
                case MULTIPART -> {
                    log.debug("Delegating MULTIPART normalization. group={}, path={}", group, request.getRequestURI());
                    normalizationService.normalizeMultipartBody(request, capturedRequest);
                }
                case UNKNOWN -> {
                    log.warn(
                            "Unknown content type category detected. group={}, path={}, rawContentType={}",
                            group,
                            request.getRequestURI(),
                            rawContentType
                    );
                    capturedRequest.setBodyRaw(null);
                    capturedRequest.setNormalizedStructureJson("{}");
                }
            }
        }

        CapturedRequest saved = capturedRequestRepository.save(capturedRequest);

        log.info(
                "Request captured successfully: captureId={}, sourceType={}, group={}, headersCount={}, queryParamsCount={}, bodyFieldsCount={}",
                saved.getId(),
                saved.getSourceType(),
                saved.getGroupName(),
                saved.getHeaders().size(),
                saved.getQueryParams().size(),
                saved.getBodyFields().size()
        );

        return saved;
    }

    private void extractHeaders(HttpServletRequest request, CapturedRequest capturedRequest) {
        Enumeration<String> headerNames = request.getHeaderNames();
        int extractedCount = 0;

        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);

            if (headerValues == null) {
                continue;
            }

            while (headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();

                capturedRequest.addHeader(
                        CapturedHeader.builder()
                                .headerName(headerName.toLowerCase(Locale.ROOT))
                                .headerValue(headerValue)
                                .build()
                );

                extractedCount++;
            }
        }

        log.debug("Extracted {} headers for path={}", extractedCount, request.getRequestURI());
    }

    private void extractQueryParams(HttpServletRequest request, CapturedRequest capturedRequest) {
        String queryString = request.getQueryString();

        if (queryString == null || queryString.isBlank()) {
            log.debug("No query params found for path={}", request.getRequestURI());
            return;
        }

        String[] pairs = queryString.split("&");
        int extractedCount = 0;

        for (String pair : pairs) {
            if (pair.isBlank()) {
                continue;
            }

            String[] keyValue = pair.split("=", 2);
            String key = decode(keyValue[0]);
            String value = keyValue.length > 1 ? decode(keyValue[1]) : "";

            capturedRequest.addQueryParam(
                    CapturedQueryParam.builder()
                            .paramName(key)
                            .paramValue(value)
                            .build()
            );

            extractedCount++;
        }

        log.debug("Extracted {} query params for path={}", extractedCount, request.getRequestURI());
    }

    private String decode(String value) {
        return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private ContentTypeCategory resolveContentTypeCategory(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return ContentTypeCategory.UNKNOWN;
        }

        String normalized = contentType.toLowerCase(Locale.ROOT);

        if (normalized.contains("application/json")) {
            return ContentTypeCategory.JSON;
        }
        if (normalized.contains("application/x-www-form-urlencoded")) {
            return ContentTypeCategory.FORM_URLENCODED;
        }
        if (normalized.contains("multipart/form-data")) {
            return ContentTypeCategory.MULTIPART;
        }

        return ContentTypeCategory.UNKNOWN;
    }
}
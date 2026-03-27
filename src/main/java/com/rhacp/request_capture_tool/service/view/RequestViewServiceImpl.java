package com.rhacp.request_capture_tool.service.view;

import com.rhacp.request_capture_tool.model.dto.BodyFieldItemView;
import com.rhacp.request_capture_tool.model.dto.HeaderItemView;
import com.rhacp.request_capture_tool.model.dto.QueryParamItemView;
import com.rhacp.request_capture_tool.model.dto.RequestDetailsView;
import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import com.rhacp.request_capture_tool.repository.CapturedBodyFieldRepository;
import com.rhacp.request_capture_tool.repository.CapturedHeaderRepository;
import com.rhacp.request_capture_tool.repository.CapturedQueryParamRepository;
import com.rhacp.request_capture_tool.repository.CapturedRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class RequestViewServiceImpl implements RequestViewService {

    private final CapturedRequestRepository capturedRequestRepository;

    private final CapturedHeaderRepository capturedHeaderRepository;

    private final CapturedQueryParamRepository capturedQueryParamRepository;

    private final CapturedBodyFieldRepository capturedBodyFieldRepository;

    public RequestViewServiceImpl(
            CapturedRequestRepository capturedRequestRepository,
            CapturedHeaderRepository capturedHeaderRepository,
            CapturedQueryParamRepository capturedQueryParamRepository,
            CapturedBodyFieldRepository capturedBodyFieldRepository
    ) {
        this.capturedRequestRepository = capturedRequestRepository;
        this.capturedHeaderRepository = capturedHeaderRepository;
        this.capturedQueryParamRepository = capturedQueryParamRepository;
        this.capturedBodyFieldRepository = capturedBodyFieldRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDetailsView> getAllRequests() {
        List<RequestDetailsView> requests = capturedRequestRepository.findAllByOrderByReceivedAtDesc()
                .stream()
                .map(this::toSummaryView)
                .toList();

        log.debug("Loaded {} request summaries for UI list page", requests.size());

        return requests;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDetailsView> getRequestsByGroup(String groupName) {
        List<RequestDetailsView> requests = capturedRequestRepository.findByGroupNameOrderByReceivedAtDesc(groupName)
                .stream()
                .map(this::toSummaryView)
                .toList();

        log.debug("Loaded {} request summaries for group='{}'", requests.size(), groupName);

        return requests;
    }

    @Override
    @Transactional(readOnly = true)
    public RequestDetailsView getRequestDetails(Long id) {
        log.debug("Loading request details for captureId={}", id);

        CapturedRequest request = capturedRequestRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Request details not found for captureId={}", id);
                    return new IllegalArgumentException("Request not found: " + id);
                });

        List<HeaderItemView> headers = capturedHeaderRepository.findByCapturedRequestIdOrderByIdAsc(id)
                .stream()
                .map(h -> new HeaderItemView(h.getHeaderName(), h.getHeaderValue()))
                .toList();

        List<QueryParamItemView> queryParams = capturedQueryParamRepository.findByCapturedRequestIdOrderByIdAsc(id)
                .stream()
                .map(p -> new QueryParamItemView(p.getParamName(), p.getParamValue()))
                .toList();

        List<BodyFieldItemView> bodyFields = capturedBodyFieldRepository.findByCapturedRequestIdOrderByIdAsc(id)
                .stream()
                .map(f -> new BodyFieldItemView(f.getFieldPath(), f.getFieldValue(), f.getValueType()))
                .toList();

        log.debug(
                "Loaded request details for captureId={}. headersCount={}, queryParamsCount={}, bodyFieldsCount={}",
                id,
                headers.size(),
                queryParams.size(),
                bodyFields.size()
        );

        return new RequestDetailsView(
                request.getId(),
                request.getReceivedAt(),
                request.getSourceType(),
                request.getGroupName(),
                request.getMethod(),
                request.getPath(),
                request.getContentType(),
                request.getContentTypeCategory(),
                request.getBodyRaw(),
                request.getNormalizedStructureJson(),
                headers,
                queryParams,
                bodyFields
        );
    }

    private RequestDetailsView toSummaryView(CapturedRequest request) {
        return new RequestDetailsView(
                request.getId(),
                request.getReceivedAt(),
                request.getSourceType(),
                request.getGroupName(),
                request.getMethod(),
                request.getPath(),
                request.getContentType(),
                request.getContentTypeCategory(),
                request.getBodyRaw(),
                request.getNormalizedStructureJson(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
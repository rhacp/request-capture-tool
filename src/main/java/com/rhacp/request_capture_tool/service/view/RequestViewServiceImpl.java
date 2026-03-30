package com.rhacp.request_capture_tool.service.view;

import com.rhacp.request_capture_tool.model.dto.compare.BodyFieldItemView;
import com.rhacp.request_capture_tool.model.dto.request.HeaderItemView;
import com.rhacp.request_capture_tool.model.dto.request.QueryParamItemView;
import com.rhacp.request_capture_tool.model.dto.request.RequestDetailsView;
import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import com.rhacp.request_capture_tool.repository.CapturedBodyFieldRepository;
import com.rhacp.request_capture_tool.repository.CapturedHeaderRepository;
import com.rhacp.request_capture_tool.repository.CapturedQueryParamRepository;
import com.rhacp.request_capture_tool.repository.CapturedRequestRepository;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public List<RequestDetailsView> getFilteredRequests(
            String groupName,
            SourceType sourceType,
            LocalDateTime receivedAtFrom,
            LocalDateTime receivedAtTo
    ) {
        Specification<CapturedRequest> specification = buildSpecification(
                groupName,
                sourceType,
                receivedAtFrom,
                receivedAtTo
        );

        List<RequestDetailsView> requests = capturedRequestRepository.findAll(
                        specification,
                        Sort.by(Sort.Direction.DESC, "receivedAt")
                )
                .stream()
                .map(this::toSummaryView)
                .toList();

        log.debug(
                "Loaded {} request summaries with filters: groupName='{}', sourceType='{}', receivedAtFrom='{}', receivedAtTo='{}'",
                requests.size(),
                groupName,
                sourceType,
                receivedAtFrom,
                receivedAtTo
        );

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
                request.getStatusCode(),
                request.getContentType(),
                request.getContentTypeCategory(),
                headers,
                queryParams,
                bodyFields,
                request.getBodyRaw(),
                request.getNormalizedStructureJson()
        );
    }

    private Specification<CapturedRequest> buildSpecification(
            String groupName,
            SourceType sourceType,
            LocalDateTime receivedAtFrom,
            LocalDateTime receivedAtTo
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(groupName)) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("groupName")),
                                "%" + groupName.trim().toLowerCase() + "%"
                        )
                );
            }

            if (sourceType != null) {
                predicates.add(cb.equal(root.get("sourceType"), sourceType));
            }

            if (receivedAtFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("receivedAt"), receivedAtFrom));
            }

            if (receivedAtTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("receivedAt"), receivedAtTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private RequestDetailsView toSummaryView(CapturedRequest request) {
        return new RequestDetailsView(
                request.getId(),
                request.getReceivedAt(),
                request.getSourceType(),
                request.getGroupName(),
                request.getMethod(),
                request.getPath(),
                request.getStatusCode(),
                request.getContentType(),
                request.getContentTypeCategory(),
                List.of(),
                List.of(),
                List.of(),
                request.getBodyRaw(),
                request.getNormalizedStructureJson()
        );
    }
}